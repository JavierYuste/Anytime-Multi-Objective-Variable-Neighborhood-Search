import pandas as pd
import numpy as np
import os
import argparse
import matplotlib.pyplot as plt
import seaborn as sns

# Build default path relative to this script's location
base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
default_csv_path = os.path.join(base_dir, "instances", "aggregated_QIs.csv")

# MCA
TIME_POINTS_MCA = [10.27, 117.89, 348.81, 600.0]
# MOKP
TIME_POINTS_MOKP = [10, 60, 150, 300]


def transform_at_time(df_relative_hv: pd.DataFrame, cpu_time: float) -> tuple[pd.DataFrame, float]:
    """Return the transformed DataFrame filtered to the closest available CPUt (s) value,
    along with the actual time point used."""
    available_times = df_relative_hv["CPUt (s)"].unique()
    actual_time = min(available_times, key=lambda t: abs(t - cpu_time))

    if actual_time != cpu_time:
        print(f"[t={cpu_time}] No exact match found. Using closest time point: {actual_time}")

    df_filtered = df_relative_hv[df_relative_hv["CPUt (s)"] == actual_time].copy()

    keys = (df_filtered["Instance"].astype(str) + "_"
            + df_filtered["Run"].astype(str))
    df_filtered["Repetition"] = pd.factorize(keys)[0]

    return df_filtered[["Method", "Value", "Repetition"]].rename(
        columns={"Method": "Algorithm", "Value": "BestCost"}
    ), actual_time


def compute_auc(df_relative_hv: pd.DataFrame, linear_scale=True) -> pd.DataFrame:
    """Compute the area under the curve (trapezoid) of Relative HV over CPUt (s)
    for every (Method, Instance) pair. Returns a DataFrame with Algorithm, BestCost,
    and Repetition columns, where Repetition is a unique integer per Instance."""
    records = []
    for (method, instance, run), group in df_relative_hv.groupby(["Method", "Instance", "Run"]):
        group_sorted = group.sort_values("CPUt (s)")
        if linear_scale:
            auc = np.trapezoid(y=group_sorted["Value"], x=group_sorted["CPUt (s)"])
        else:
            auc = np.trapezoid(y=group_sorted["Value"])
        records.append({"Algorithm": method, "BestCost": auc, "Instance": instance, "Run": run})

    df_auc = pd.DataFrame(records)
    df_auc["Repetition"] = pd.factorize(
        df_auc["Instance"].astype(str) + "_"
        + df_auc["Run"].astype(str)
    )[0]
    return df_auc[["Algorithm", "BestCost", "Repetition"]]


METHOD_ORDER = [
    "MO-VND",
    "Anytime MO-VND",
    "Anytime MO-VND-s",
    "Anytime MO-VND-g",
    "MO-GVNS",
    "Anytime MO-GVNS",
    "Anytime MO-GVNS-s",
    "Anytime MO-GVNS-g"
]


def plot_auc(df_auc: pd.DataFrame, output_dir: str, output_suffix="") -> None:
    """Generate AUC boxplot."""
    present_methods = df_auc["Algorithm"].unique()
    order = [m for m in METHOD_ORDER if m in present_methods] + \
            [m for m in present_methods if m not in METHOD_ORDER]

    # Seaborn's line plots use the "deep" palette by default; matching its
    # hue order keeps each algorithm's color consistent across plot types.
    palette = dict(zip(order, sns.color_palette("deep", n_colors=len(order))))

    fig, ax = plt.subplots(figsize=(7, 4))
    sns.boxplot(data=df_auc, x="Algorithm", y="BestCost", order=order,
                palette=palette, ax=ax, showfliers=False)
    for box, method in zip(ax.patches, order):
        box.set_facecolor((*palette[method], 0.55))
        box.set_edgecolor(palette[method])
        box.set_linewidth(1.4)
    sns.stripplot(data=df_auc, x="Algorithm", y="BestCost", order=order, ax=ax,
                  palette=palette, jitter=0.18, alpha=0.6, size=4, zorder=2)

    fontsize = 12
    ax.set_xlabel("")
    ax.set_ylabel("AUC (RHV)", fontsize=fontsize)
    ax.tick_params(axis="x", rotation=10, labelsize=fontsize)
    ax.tick_params(axis="y", labelsize=fontsize)
    plt.tight_layout()

    path = os.path.join(output_dir, f"AUC_boxplot{output_suffix}.pdf")
    fig.savefig(path)
    plt.close(fig)
    print(f"[Plot] AUC boxplot saved to: {path}")


def main(csv_path, output, problem):
    df = pd.read_csv(csv_path)
    df_relative_hv = df[df["QI"] == "Relative HV"].copy()
    output_dir = output if output else csv_path

    # Generate one output file per time point
    if problem == "MCA":
        time_points = TIME_POINTS_MCA
    else:
        time_points = TIME_POINTS_MOKP

    for t in time_points:
        df_transformed, actual_t = transform_at_time(df_relative_hv, t)
        filename = f"transformed_QIs_{str(actual_t).replace('.', '_')}.csv"
        output_path = os.path.join(output_dir, filename)
        df_transformed.to_csv(output_path, index=False)
        print(f"[t={t}] {len(df_transformed)} rows written to: {output_path}")

    # Generate AUC output file
    df_auc = compute_auc(df_relative_hv)
    auc_path = os.path.join(output_dir, "transformed_QIs_AUC.csv")
    df_auc.to_csv(auc_path, index=False)
    print(f"[AUC] {len(df_auc)} rows written to: {auc_path}")

    # Generate box plot
    plot_auc(df_auc, output_dir)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Read aggregated QIs CSV into a DataFrame.")
    parser.add_argument("csv_path", nargs="?", default=default_csv_path,
                        help="Path to the CSV file (default: instances_test/aggregated_QIs.csv)")
    parser.add_argument("--output", "-o", default=None,
                        help="Path for the output CSV file (default: same directory as input, named transformed_QIs.csv)")
    parser.add_argument("--problem", "-p", default="MOKP",
                        help="Problem type (MCA or MOKP) to determine time points (default: MOKP)")
    args = parser.parse_args()
    main(args.csv_path, args.output, args.problem)
