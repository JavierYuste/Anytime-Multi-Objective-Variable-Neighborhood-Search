import argparse
import itertools
import logging
import multiprocessing as mp
import os
import re
from math import inf
from typing import Any

import imageio.v2 as imageio
import matplotlib
import moocore
import numpy as np
import pandas as pd
import seaborn as sns
from matplotlib import pyplot as plt
from numpy import ndarray, dtype
from pandas import DataFrame
from pdf2image import convert_from_path
from scipy.stats import t
import transform_QIs

# Customize axis labels if needed
objective_names_MOKP = ["Objective 1", "Objective 2"]
objective_names_MCA = ["(1) Coupling", "(2) Cohesion", "(3) MQ", "(4) Modules", "(5) IsolatedModules"]
markers = ['s', 'x', '*', '^', 'v', '<', '>', 'p', 'P', 'D', 'h', 'H', '+', 'x', 'X', 'd', '|', '_']
METHOD_ORDER = ["MO-VND", "Anytime MO-VND", "Anytime MO-VND-s", "Anytime MO-VND-g",
                "MO-GVNS", "Anytime MO-GVNS", "Anytime MO-GVNS-s", "Anytime MO-GVNS-g"]

worst_value = {'PFS': 0.0, 'IGD+': 1.0, 'HV': 0.0, 'Relative HV': 1.0}


def obtain_reference(root: str, instance_name: str):
    """
    Given a folder with obtained results, obtain the reference front for the specified instance by
    aggregating all non-dominated solutions from all methods and studies.
    :param root: folder with obtained results
    :param instance_name: name of the instance to obtain the reference front for
    :return: reference front as a DataFrame or ndarray
    """
    reference_point = None
    nadir_point = None
    subfolders = [f for f in os.listdir(root) if os.path.isdir(os.path.join(root, f))]
    subfolders.sort()  # Ensure consistent ordering for unique_id
    reference_front = pd.DataFrame()
    for subfolder in subfolders:
        subfolder_path = os.path.join(root, subfolder)
        study_data_path = os.path.join(subfolder_path, "Study", "data")
        if os.path.isdir(study_data_path):
            for method in os.listdir(study_data_path):
                method_path = os.path.join(study_data_path, method)
                if os.path.isdir(method_path):
                    for folder in os.listdir(method_path):
                        instance = folder.split("-")[-1]
                        if instance == instance_name:
                            pareto_front = os.path.join(method_path, folder, 'FUN0.csv')
                            if os.path.isfile(pareto_front):
                                try:
                                    df = pd.read_csv(pareto_front, header=None)
                                    reference_front = pd.concat([reference_front, df], ignore_index=True)
                                    # Update reference point with the maximum values found for each column
                                    if reference_point is None:
                                        reference_point = [float('-inf') for col in df.columns]
                                    for col in df.columns:
                                        max_value = df[col].max()
                                        reference_point[col] = max(reference_point[col], max_value)
                                    # Update nadir point with the minimum values found for each column
                                    if nadir_point is None:
                                        nadir_point = [float('inf') for col in df.columns]
                                    for col in df.columns:
                                        min_value = df[col].min()
                                        nadir_point[col] = min(nadir_point[col], min_value)
                                except Exception as e:
                                    logging.info(f"Could not read {pareto_front}: {e}")
    if not reference_front.empty:
        # Remove dominated solutions
        reference_front = moocore.filter_dominated(reference_front, maximise=False)

    subfolders = [f for f in os.listdir(root) if os.path.isdir(os.path.join(root, f))]
    subfolders.sort()  # Ensure consistent ordering for unique_id
    results = list()
    for subfolder in subfolders:
        subfolder_path = os.path.join(root, subfolder)
        study_data_path = os.path.join(subfolder_path, "progressOverTime")
        if os.path.isdir(study_data_path):
            for method in os.listdir(study_data_path):
                method_path = os.path.join(study_data_path, method)
                if os.path.isdir(method_path):
                    for front_file in os.listdir(method_path):
                        method_name = front_file.split("-")[1]
                        instance = front_file.split("-")[2]
                        cpu_time = front_file.split("-")[3].removesuffix(".csv")
                        if instance == instance_name:
                            try:
                                df = pd.read_csv(os.path.join(method_path, front_file), header=None)
                                for col in df.columns:
                                    max_value = df[col].max()
                                    reference_point[col] = max(reference_point[col], max_value)
                                for col in df.columns:
                                    min_value = df[col].min()
                                    nadir_point[col] = min(nadir_point[col], min_value)
                            except Exception as e:
                                logging.info(f"Could not read {front_file}: {e}")

    return reference_front, reference_point, nadir_point


def get_QIs(root: str, instance_name: str, ref_front: DataFrame | ndarray, ref_point, nadir_point):
    """
    Given a folder with obtained results, obtain the set of QIs available for the specified instance.
    :param root: folder with obtained results
    :param instance_name: name of the instance to obtain the QIs for
    :return: set of QIs
    """
    reference_front = moocore.normalise(ref_front, to_range=[0, 1], lower=nadir_point, upper=ref_point, maximise=False)
    normalized_ref_point = list()
    normalized_ref_point.append(ref_point)
    normalized_ref_point = moocore.normalise(normalized_ref_point, to_range=[0, 1], lower=nadir_point, upper=ref_point,
                                             maximise=False)
    RHV = moocore.RelativeHypervolume(ref_set=reference_front, ref=normalized_ref_point, maximise=False)
    subfolders = [f for f in os.listdir(root) if os.path.isdir(os.path.join(root, f))]
    subfolders.sort()  # Ensure consistent ordering for unique_id
    results = list()
    for subfolder in subfolders:
        subfolder_path = os.path.join(root, subfolder)
        study_data_path = os.path.join(subfolder_path, "progressOverTime")
        if os.path.isdir(study_data_path):
            for method in os.listdir(study_data_path):
                method_path = os.path.join(study_data_path, method)
                if os.path.isdir(method_path):
                    for front_file in os.listdir(method_path):
                        method_name = front_file.split("-")[1]
                        instance = front_file.split("-")[2]
                        cpu_time = front_file.split("-")[3].removesuffix(".csv")
                        if instance == instance_name:
                            try:
                                df = pd.read_csv(os.path.join(method_path, front_file), header=None)

                                # Normalize
                                df = moocore.normalise(df, to_range=[0, 1], lower=nadir_point, upper=ref_point,
                                                       maximise=False)

                                # PFS: number of non-dominated solutions
                                pfs = len(df)
                                logging.debug(f"    PFS: {pfs}")
                                results.append(
                                    {'Method': method_name, 'Instance': instance, 'CPUt (s)': cpu_time, 'QI': 'PFS',
                                     'Value': pfs, 'Run': subfolder})

                                # IGD+
                                try:
                                    igd_plus = moocore.igd_plus(df, reference_front, maximise=False)
                                    logging.debug(f"    IGD+: {igd_plus}")
                                    results.append(
                                        {'Method': method_name, 'Instance': instance, 'CPUt (s)': cpu_time,
                                         'QI': 'IGD+', 'Value': igd_plus, 'Run': subfolder})
                                except Exception as e:
                                    logging.info(f"    Could not calculate IGD+: {e}")
                                    results.append(
                                        {'Method': method_name, 'Instance': instance, 'CPUt (s)': cpu_time,
                                         'QI': 'IGD+', 'Value': 'Error', 'Run': subfolder})

                                try:
                                    hv = moocore.hypervolume(df, normalized_ref_point, maximise=False)
                                    logging.debug(f"    HV: {hv}")
                                    results.append(
                                        {'Method': method_name, 'Instance': instance, 'CPUt (s)': cpu_time,
                                         'QI': 'HV', 'Value': hv, 'Run': subfolder})
                                except Exception as e:
                                    logging.info(f"    Could not calculate HV: {e}")
                                    results.append(
                                        {'Method': method_name, 'Instance': instance, 'CPUt (s)': cpu_time,
                                         'QI': 'HV', 'Value': 'Error', 'Run': subfolder})
                                try:
                                    relative_hv = RHV(df)
                                    logging.debug(f"    Relative HV: {relative_hv}")
                                    results.append(
                                        {'Method': method_name, 'Instance': instance, 'CPUt (s)': cpu_time,
                                         'QI': 'Relative HV', 'Value': relative_hv, 'Run': subfolder})
                                except Exception as e:
                                    logging.info(f"    Could not calculate Relative HV: {e}")
                                    results.append(
                                        {'Method': method_name, 'Instance': instance, 'CPUt (s)': cpu_time,
                                         'QI': 'Relative HV', 'Value': 'Error', 'Run': subfolder})
                            except Exception as e:
                                logging.info(f"Could not read {front_file}: {e}")
    return results


def fill_missing_points(QIs: list) -> list:
    """
    Given a list of QIs, fill in missing time points for each method and QI by carrying forward the last known value.
    :param QIs: list of QIs
    :return: list of QIs with missing points filled
    """
    all_cpu_times = set()
    for instance_list in QIs:
        for qi in instance_list:
            all_cpu_times.add(float(qi['CPUt (s)']))
    all_cpu_times = sorted(list(all_cpu_times))

    filled_results = list()
    for results_list in QIs:
        df = pd.DataFrame(results_list)
        for (method, qi, instance, run), group in df.groupby(['Method', 'QI', 'Instance', 'Run']):
            group = group.sort_values(by='CPUt (s)').reset_index(drop=True)
            last_value = None
            last_time = 0.0
            for time_point in all_cpu_times:
                # time_point is a float, while 'CPUt (s)' in group is a string, so we need to compare as floats
                if time_point in group['CPUt (s)'].astype(float).values:
                    row = group[group['CPUt (s)'].astype(float) == time_point].iloc[0]
                    filled_results.append(row.to_dict())
                    last_value = row['Value']
                else:
                    if last_value is not None:
                        # If no value was recorded at this point, we can carry forward the last known value
                        logging.debug(
                            f"Adding missing point for Method: {method}, QI: {qi}, Instance: {instance}, Run: {run} at time {time_point}s with value {last_value}")
                        filled_results.append(
                            {'Method': method, 'Instance': group.iloc[0]['Instance'], 'CPUt (s)': time_point, 'QI': qi,
                             'Value': last_value, 'Run': group.iloc[0]['Run']})
                    if last_value is None:
                        # If no value has been recorded yet, we can use the worst possible value for this QI
                        logging.debug(
                            f"Adding missing point for Method: {method}, QI: {qi}, Instance: {instance}, Run: {run} at time {time_point}s with value {worst_value[qi]}")
                        filled_results.append(
                            {'Method': method, 'Instance': group.iloc[0]['Instance'], 'CPUt (s)': time_point, 'QI': qi,
                             'Value': worst_value[qi], 'Run': group.iloc[0]['Run']})
    return filled_results


def plot_progress_over_time(data: pd.DataFrame, output_dir: str = 'output'):
    # Create output directory
    os.makedirs(output_dir, exist_ok=True)

    # Convert CPUt (s) to numeric
    data['CPUt (s)'] = pd.to_numeric(data['CPUt (s)'], errors='raise')

    # Define markers and line styles for different methods
    markers = ['o', 's', '^', 'D', 'v', '<', '>', 'p', '*', 'h', 'H', '+', 'x', '|', '_']
    linestyles = ['-', '--', '-.', ':', (0, (3, 1, 1, 1)), (0, (5, 1)), (0, (3, 1, 1, 1, 1, 1))]

    # Get unique methods to assign consistent styles
    unique_methods = data['Method'].unique()
    method_styles = {}

    # Assign a unique marker and line style to each method
    for i, method in enumerate(unique_methods):
        marker_idx = i % len(markers)
        linestyle_idx = i % len(linestyles)
        method_styles[method] = {'marker': markers[marker_idx], 'linestyle': linestyles[linestyle_idx]}

    for qi, qi_data in data.groupby('QI'):
        plot_data = qi_data.copy()
        plot_data['Value'] = pd.to_numeric(plot_data['Value'], errors='coerce')
        plot_data = plot_data[
            (plot_data['CPUt (s)'] > 0) & (plot_data['Value'] > 0)
        ].dropna(subset=['CPUt (s)', 'Value'])
        if plot_data.empty:
            logging.info(f"Skipping log-log seaborn plot for {qi}: no positive observations")
            continue

        method_order = sorted(
            plot_data['Method'].unique(),
            key=lambda m: (METHOD_ORDER.index(m) if m in METHOD_ORDER else len(METHOD_ORDER), m),
        )
        seaborn_markers = ['o', 's', '^', 'D', 'v', '<', '>', 'p', '*', 'h', 'H', '+', 'x', '|', '_']
        method_markers = {method: seaborn_markers[i % len(seaborn_markers)]
                          for i, method in enumerate(method_order)}
        fig, ax = plt.subplots(figsize=(7, 4))
        sns.lineplot(
            data=plot_data,
            x='CPUt (s)',
            y='Value',
            hue='Method',
            hue_order=method_order,
            estimator='mean',
            errorbar=('ci', 95),
            n_boot=1000,
            seed=1234,
            style='Method',
            style_order=method_order,
            markers=method_markers,
            dashes=False,
            ax=ax,
        )
        for line in ax.lines:
            if line.get_marker() not in (None, '', 'None', ' '):
                color = matplotlib.colors.to_rgb(line.get_color())
                darker_color = tuple(channel * 0.75 for channel in color)
                line.set_markeredgecolor(darker_color)
        ax.set_xscale('log')
        ax.set_yscale('log')
        ax.set_xlabel('CPU Time (s)', fontsize=14)
        ax.set_ylabel(qi, fontsize=14)
        ax.tick_params(axis='both', labelsize=14)
        ax.grid(True, linestyle='--', alpha=0.7)
        ax.legend(fontsize=14)
        fig.tight_layout()
        fig.savefig(os.path.join(output_dir, f'progress_{qi}.pdf'), format='pdf')
        plt.close(fig)


def plot_pareto_fronts(plots_folder: str, instance_to_process, method_names, QIs: DataFrame):
    # Search recursively if there exists any folder in plots_folder that contains the name "MCAProblem"
    for root, dirs, files in os.walk(plots_folder):
        for dir in dirs:
            if "MCAProblem" in dir:
                return
            if "MOKPProblem" in dir:
                plot_pareto_fronts_2d(plots_folder, objective_names_MOKP, markers, instance_to_process, method_names,
                                      QIs)
                return
    return


def get_last_available_front(plots_folder, times_sorted, method_name, instance, cpu_time):
    # Find latest available front for this method, instance, and cpu_time
    last_available_front = None
    last_available_time = None
    # For each study and method
    subfolders = [f for f in os.listdir(plots_folder) if os.path.isdir(os.path.join(plots_folder, f))]
    subfolders.sort()  # Ensure consistent ordering for unique_id
    for subfolder in subfolders:
        subfolder_path = os.path.join(plots_folder, subfolder)
        study_data_path = os.path.join(subfolder_path, "progressOverTime")
        if os.path.isdir(study_data_path):
            os.makedirs(os.path.join(plots_folder, 'pareto_fronts', instance, subfolder), exist_ok=True)
            method_path = os.path.join(study_data_path, method_name)
            if os.path.isdir(method_path):
                for front_file in os.listdir(method_path):
                    method_name_of_file = front_file.split("-")[1]
                    instance_name_of_file = front_file.split("-")[2]
                    cpu_time_of_file = front_file.split("-")[3].removesuffix(".csv")
                    if (method_name == method_name_of_file and instance_name_of_file == instance and float(
                            cpu_time_of_file) <= cpu_time and (
                            last_available_time is None or last_available_time < float(cpu_time_of_file))):
                        last_available_front = os.path.join(method_path, front_file)
                        last_available_time = float(cpu_time_of_file)
    return last_available_front


def plot_pareto_fronts_2d(plots_folder: str, objective_names: list[str], markers: list[str], instance, method_names,
                          QIs: DataFrame) -> None:
    print(f"Starting plots of Pareto fronts for instance: {instance}")

    # Collect all times from all methods for this instance
    times_sorted = set()
    # Get all values of CPUt (s) for this instance from QIs DataFrame
    instance_qis = QIs[QIs['Instance'] == instance]
    for cpu_time in instance_qis['CPUt (s)'].unique():
        times_sorted.add(float(cpu_time))
    times_sorted = sorted(list(times_sorted))

    ref_front, ref_point, nadir_point = obtain_reference(plots_folder, instance)

    # Compute limits so that all pareto fronts fit in the same axes, regardless of cpu time and study
    x_limits, y_limits = get_limits_for_instance_pareto_fronts(instance, plots_folder, ref_point, nadir_point)

    # For each study and method
    subfolders = [f for f in os.listdir(plots_folder) if os.path.isdir(os.path.join(plots_folder, f))]
    subfolders.sort()  # Ensure consistent ordering for unique_id
    for subfolder in subfolders:
        subfolder_path = os.path.join(plots_folder, subfolder)
        study_data_path = os.path.join(subfolder_path, "progressOverTime")
        if os.path.isdir(study_data_path):
            os.makedirs(os.path.join(plots_folder, 'pareto_fronts', instance, subfolder), exist_ok=True)
            # Sort method folders by METHOD_ORDER (using display names)
            raw_methods = [m for m in os.listdir(study_data_path) if
                           os.path.isdir(os.path.join(study_data_path, m)) and (
                                       method_names is None or m in method_names)]
            raw_methods = sorted(raw_methods, key=lambda m: (
                METHOD_ORDER.index(method_names[m]) if m in method_names and method_names[m] in METHOD_ORDER else len(
                    METHOD_ORDER), method_names.get(m, m)))
            # Plot all methods for this study
            for cpu_time in times_sorted:
                plt.figure(figsize=(7, 4))
                for idx, method in enumerate(raw_methods):
                    method_path = os.path.join(study_data_path, method)
                    if os.path.isdir(method_path):
                        # Find latest available front for this method, instance, and cpu_time
                        last_available_front = None
                        last_available_time = None
                        for front_file in os.listdir(method_path):
                            method_name_of_file = front_file.split("-")[1]
                            instance_name_of_file = front_file.split("-")[2]
                            cpu_time_of_file = front_file.split("-")[3].removesuffix(".csv")
                            if (method == method_name_of_file and instance_name_of_file == instance and float(
                                    cpu_time_of_file) <= cpu_time and (
                                    last_available_time is None or last_available_time < float(cpu_time_of_file))):
                                last_available_front = front_file
                                last_available_time = float(cpu_time_of_file)
                        # Plot the last available front
                        try:
                            logging.debug(f"Adding front from file: {last_available_front}")
                            df = pd.read_csv(os.path.join(method_path, last_available_front), header=None)
                            df = moocore.normalise(df, to_range=[0, 1], lower=nadir_point, upper=ref_point,
                                                   maximise=False)
                            # points = df.to_numpy()
                            plt.scatter(df[:, 0], df[:, 1],
                                        label=method_names[method] if method in method_names else method,
                                        marker=markers[idx % len(markers)])
                        except Exception:
                            continue

                fontsize = 12
                plt.xlabel(objective_names[0], fontsize=fontsize)
                plt.ylabel(objective_names[1], fontsize=fontsize)
                plt.xticks(fontsize=fontsize)
                plt.yticks(fontsize=fontsize)
                plt.legend(fontsize=fontsize)
                plt.title(f"Pareto fronts - {instance} at t={cpu_time}", fontsize=fontsize)
                plt.grid(True)
                plt.legend(fontsize=fontsize)

                # Compute axis limits for all points in this instance
                # plt.xlim(*x_limits)
                # plt.ylim(*y_limits)
                plt.xlim(0, 0.5)
                plt.ylim(0, 0.5)

                out_path = os.path.join(plots_folder, "pareto_fronts", instance, subfolder,
                                        f"PF_{instance}_t{str(cpu_time).replace('.', '_')}.pdf")
                logging.debug(f"Saving Pareto front plot for instance: {instance} at time {cpu_time} to {out_path}")
                plt.savefig(out_path)
                plt.close()

    print(f"Finished plots of Pareto fronts for instance: {instance}")
    create_evolution_video(plots_folder, instance)


def create_evolution_video(plots_folder: str, instance: str):
    # Create video showing evolution over time for each instance and study
    instance_plot_folder = os.path.join(plots_folder, "pareto_fronts", instance)
    print(f"Creating video for instance: {instance}")
    for subfolder in os.listdir(instance_plot_folder):
        instance_plot_folder_sub = os.path.join(instance_plot_folder, subfolder)
        if os.path.isdir(instance_plot_folder_sub):
            # Find all plot files and sort by time
            plot_files = []
            time_pattern = re.compile(r'PF_' + re.escape(instance) + r'_t([0-9_]+)\.pdf')
            for fname in os.listdir(instance_plot_folder_sub):
                match = time_pattern.match(fname)
                if match:
                    # Convert time string back to float for sorting
                    t_str = match.group(1).replace('_', '.')
                    try:
                        t_val = float(t_str)
                    except Exception:
                        t_val = 0
                    plot_files.append((t_val, os.path.join(instance_plot_folder_sub, fname)))
            plot_files.sort()
            images = []
            error_raised = False
            for _, pdf_path in plot_files:
                # Convert PDF to image (first page only)
                try:
                    pil_images = convert_from_path(pdf_path, dpi=150, fmt='png')
                    if pil_images:
                        images.append(np.array(pil_images[0]))
                except Exception as e:
                    logging.info(f"[!] - Error generating video: {e}")
                    error_raised = True
            if error_raised:
                print(
                    f"[!] - Error generating video. Is poppler installed? (https://github.com/Belval/pdf2image#how-to-install)")
                continue
            if images:
                video_path = os.path.join(instance_plot_folder_sub, f"PF_evolution_{instance}.mp4")
                imageio.mimsave(video_path, images, fps=1)
                logging.info(f"[Process] Video saved for instance {instance} and study {subfolder} at {video_path}")
            print(f"Finished video for instance: {instance}")


def get_limits_for_instance_pareto_fronts(instance, plots_folder: str, ref_point, nadir_point) -> tuple[
    tuple[float | int | Any, float | int | Any], tuple[float | int | Any, float | int | Any]]:
    x_min = inf
    y_min = inf
    x_max = -inf
    y_max = -inf
    # For each study and method
    subfolders = [f for f in os.listdir(plots_folder) if os.path.isdir(os.path.join(plots_folder, f))]
    subfolders.sort()  # Ensure consistent ordering for unique_id
    results = list()
    for subfolder in subfolders:
        subfolder_path = os.path.join(plots_folder, subfolder)
        study_data_path = os.path.join(subfolder_path, "progressOverTime")
        if os.path.isdir(study_data_path):
            for method in os.listdir(study_data_path):
                method_path = os.path.join(study_data_path, method)
                if os.path.isdir(method_path):
                    for front_file in os.listdir(method_path):
                        method_name = front_file.split("-")[1]
                        instance_name = front_file.split("-")[2]
                        cpu_time = front_file.split("-")[3].removesuffix(".csv")
                        if instance == instance_name:
                            try:
                                df = pd.read_csv(os.path.join(method_path, front_file), header=None)
                                df = moocore.normalise(df, to_range=[0, 1], lower=nadir_point, upper=ref_point,
                                                       maximise=False)

                                x_min = min(x_min, df[:, 0].min())
                                x_max = max(x_max, df[:, 0].max())
                                y_min = min(y_min, df[:, 1].min())
                                y_max = max(y_max, df[:, 1].max())
                            except Exception as e:
                                logging.info(f"Could not read {front_file}: {e}")
                                continue

    # Compute axis limits for all points in this instance
    x_margin = 0.05 * (x_max - x_min) if x_max > x_min else 1
    y_margin = 0.05 * (y_max - y_min) if y_max > y_min else 1
    x_limits = (max(0, x_min - x_margin), min(x_max + x_margin, 1))
    y_limits = (max(0, y_min - y_margin), min(y_max + y_margin, 1))
    return x_limits, y_limits


def _process_instance(args):
    root, instance = args
    ref_front, ref_point, nadir_point = obtain_reference(root, instance)
    print(f"Reference front for instance {instance} has {len(ref_front)} non-dominated solutions.")
    QIs = get_QIs(root, instance, ref_front, ref_point, nadir_point)
    logging.info(f"Found {len(QIs)} QIs for instance {instance}.")
    for qi in QIs:
        logging.debug(f"  QI: {qi}")
    return QIs


def main(root, max_time, n_processes=None, method_names=None, args=None):
    pool_size = n_processes if n_processes is not None else 1
    subfolders = [f for f in os.listdir(root) if os.path.isdir(os.path.join(root, f))]
    subfolders.sort()  # Ensure consistent ordering for unique_id

    # Obtain methods, instances, and number of studies performed
    # Search for Study/data folders in each subfolder
    methods = set()
    instances = set()
    num_studies = 0
    for subfolder in subfolders:
        subfolder_path = os.path.join(root, subfolder)
        study_data_path = os.path.join(subfolder_path, "Study", "data")
        if os.path.isdir(study_data_path) and os.path.exists(study_data_path):
            num_studies += 1
            for method in os.listdir(study_data_path):
                methods.add(method)
                method_path = os.path.join(study_data_path, method)
                if os.path.isdir(method_path):
                    for folder in os.listdir(method_path):
                        instance = folder.split("-")[-1]
                        instances.add(instance)

    logging.info(f"Found {num_studies} studies in {root}.")
    logging.info(f"Found {len(methods)} unique methods across studies.")
    logging.info(f"Found {len(instances)} unique instances across studies.")

    if pool_size and pool_size > 1:
        with mp.Pool(processes=pool_size) as pool:
            instance_results = pool.map(_process_instance,
                                        [(root, instance) for instance in instances])
    else:
        instance_results = [_process_instance((root, instance)) for instance in instances]

    instance_results = fill_missing_points(instance_results)
    print(f"Finished processing QIs for instance {instance}.")
    results_df = pd.DataFrame(instance_results)

    # Convert CPUt (s) to numeric
    results_df['CPUt (s)'] = pd.to_numeric(results_df['CPUt (s)'], errors='raise')
    # Filter by max_time
    results_df = results_df[results_df['CPUt (s)'] <= max_time]
    logging.info(f"After filtering, {len(results_df)} QI entries remain within max time of {max_time} seconds.")
    # Map method names if provided
    if method_names:
        results_df['Method'] = results_df['Method'].map(method_names).fillna(results_df['Method'])
    # Save results to CSV
    results_csv_path = os.path.join(root, "aggregated_QIs.csv")
    results_df.to_csv(results_csv_path, index=False)
    logging.info(f"Aggregated QIs saved to {results_csv_path}.")

    # Map method names if provided
    if method_names:
        results_df['Method'] = results_df['Method'].map(method_names).fillna(results_df['Method'])
    if args.plot_progress:
        plot_progress_over_time(results_df, output_dir=os.path.join(root, "progress"))

    if args.plot_fronts:
        instances = results_df['Instance'].unique()
        if pool_size and pool_size > 1:
            with mp.Pool(processes=pool_size) as pool:
                pool.starmap(plot_pareto_fronts,
                             [(root, instance, method_names, results_df) for instance in instances], )
        else:
            for instance in instances:
                plot_pareto_fronts(root, instance, method_names, results_df)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("root_folder", type=str, help="Root folder containing subfolders to process.")
    parser.add_argument("--n_processes", type=int, default=round(os.cpu_count() / 2),
                        help="Number of parallel processes to use (default: half of CPU cores).")
    parser.add_argument("--max_time", type=int, default=300, help="Max time to report results (default: 300 seconds).")
    parser.add_argument("--problem", "-p", default="MOKP",
                        help="Problem type (MCA or MOKP). This setting overrides others.")
    args = parser.parse_args()

    if args.problem == "MCA":
        args.plot_fronts = False
        args.plot_progress = True
    elif args.problem == "MOKP":
        args.plot_fronts = True
        args.plot_progress = True
    else:
        raise ValueError(f"Unknown problem type: {args.problem}. Supported types are 'MCA' and 'MOKP'.")

    method_names = {
        "MOVND": "MO-VND",
        "AnytimeMOVND": "Anytime MO-VND",
        "AnytimeMOVND_s": "Anytime MO-VND-s",
        "AnytimeMOVND_g": "Anytime MO-VND-g",
        "MOGVNS": "MO-GVNS",
        "AnytimeMOGVNS": "Anytime MO-GVNS",
        "AnytimeMOGVNS_s": "Anytime MO-GVNS-s",
        "AnytimeMOGVNS_g": "Anytime MO-GVNS-g",
    }

    # Configure logging to show info messages
    logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

    main(args.root_folder, args.max_time, args.n_processes, method_names, args)

    transform_QIs.main(os.path.join(args.root_folder, "aggregated_QIs.csv"), args.root_folder, args.problem)
