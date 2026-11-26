# Anytime Multi-Objective Variable Neighborhood Search

[![Code](https://img.shields.io/badge/Code-Java-orange.svg)]()
[![Code](https://img.shields.io/badge/Code-Python-blue.svg)]()
[![Code](https://img.shields.io/badge/Code-R-blue.svg)]()

## Table of contents

- [Repository structure](#repository-structure)
- [Abstract](#abstract)
- [Authors and affiliations](#authors-and-affiliations)
- [Running the experiments and reproducing the analysis](#running-the-experiments-and-reproducing-the-analysis)
  - [1 - Requirements](#1---requirements)
  - [2 - Datasets](#2---datasets)
  - [3 - Running the experiments](#3---running-the-experiments)
  - [4 - Analyzing the results](#4---analyzing-the-results)
  - [5 - Statistical analysis](#5---statistical-analysis)
- [License](#license)
- [Funding and acknowledgments](#funding-and-acknowledgments)
- [Citation](#citation)
- [Contact](#contact)


## Abstract

Multi-objective optimization problems often require producing high-quality solutions under strict time constraints, making good anytime performance a key desirable property. The Multi-Objective Variable Neighborhood Search (MO-VNS) framework extends Variable Neighborhood Search algorithms to multi-objective contexts. While effective, this framework exhibits structural limitations that hinder its anytime behavior, particularly as the number of objectives increases.
This work analyzes the aforementioned limitations and proposes enhanced versions of the MO-VNS framework, focusing on the Multi-Objective Variable Neighborhood Descent and Multi-Objective General Variable Neighborhood Search schemes.
Particularly, two enhancements are introduced. First, the schemes are modified to equally distribute the effort towards all objectives during the search. Then, the methods are extended to consider scalarizations.
The proposed methods are experimentally evaluated on two different benchmarks: the Multi-Objective Knapsack Problem and the Maximizing Cluster Approach problem, with two and five objectives, respectively.
The results show that the introduced enhancements lead to higher-quality Pareto fronts, substantially outperforming the original methods. Overall, this work advances the capabilities of MO-VNS-based algorithms and provides general insights into designing flexible and effective metaheuristics for multi-objective optimization.

## Authors and affiliations

- **Javier Yuste** <sup>1</sup> · <a href="mailto:javier.yuste@urjc.es" aria-label="Javier Yuste Email"><img src="https://upload.wikimedia.org/wikipedia/commons/b/b1/Email_Shiny_Icon.svg" alt="email" width="20" style="vertical-align:middle;"/></a> · <a href="https://orcid.org/0000-0002-5956-9977"><img src="https://upload.wikimedia.org/wikipedia/commons/0/06/ORCID_iD.svg" alt="ORCID" width="20" style="vertical-align:middle;"/></a>
- **Eduardo G. Pardo** <sup>1</sup> · <a href="https://orcid.org/0000-0002-6247-5269"><img src="https://upload.wikimedia.org/wikipedia/commons/0/06/ORCID_iD.svg" alt="ORCID" width="20" style="vertical-align:middle;"/></a>
- **Manuel López-Ibáñez** <sup>2</sup> · <a href="https://orcid.org/0000-0001-9974-1295"><img src="https://upload.wikimedia.org/wikipedia/commons/0/06/ORCID_iD.svg" alt="ORCID" width="20" style="vertical-align:middle;"/></a>

1. GRAFO Research, Universidad Rey Juan Carlos, Madrid, Spain.
2. Alliance Manchester Business School, University of Manchester, Manchester, United Kingdom.


## Repository structure

```text
.
├── instances/                     # Instances of the two benchmark problems used in the study
├── paper_results/                 # Results reported in the paper
├── results/                       # Output of the experiments run with the provided scripts
├── scripts/                       # Scripts for processing and plotting results
│   ├── python/                    # Scripts for processing and plotting results
│   ├── statistical_analysis/      # Scripts for performing bayesian statistical analysis
├── src/                          
│   ├── algorithms/                # Implemented methods 
│   ├── experiments/studies/       # Experiments to run
│   ├── problems/                  # Problem definitions and instance readers
│   └── ...
└── README.md                       # This file
```


## Running the experiments and reproducing the analysis

Results are already available in the `paper_results/` folder. To rerun the experiments and reproduce the analysis, follow the steps below.

### 1 - Requirements

- To run the experiments, dependencies are specified in [`pom.xml`](pom.xml).
- To run the Python analysis scripts, dependencies are specified in [`scripts/python/requirements.txt`](scripts/python/requirements.txt).
- To run the R analysis scripts, dependencies are specified in [`scripts/statistical_analysis/Packages install.R`](scripts/statistical_analysis/Packages install.R).

### 2 - Datasets

The project contains two sets of instances, one for the Multi-Objective Knapsack Problem (MOKP) and another one 
corresponding to the Maximizing Cluster Approach (MCA). 

Instances for the MOKP are placed under `instances/MOKP`. The folder contains 20 different instances of different classes
and sizes, as described in the paper, made available by .

Instances for the MCA problem are placed under `instances/MCA`. The folder contains 123 different instances for the family
of Software Module Clustering Problems. These instances were made available by Marlon et al., as described in the paper.

| Dataset directory | Files | Instance size | Description                                        |
|-------------------|---:|---:|----------------------------------------------------|
| `instances/MOKP`  | 20 | 100–500 items | Two-objective, one-constraint knapsack instances   |
| `instances/MCA`   | 123 | 6–1,161 vertices | Module Dependency Graphs of real software projects |


### 3 - Running the experiments

There are four experiments ready to be run within the project, as listed below.

| Problem | Methods                                                    | Class to run the experiment |
|---------|------------------------------------------------------------|---|
| MOKP    | MO-VND, Anytime MO-VND, Anytime MO-VND-s, Anytime MO-VND-g | `experiments.studies.Knapsack.MOKP_MOVND`                       |
| MOKP    | MO-GVNS, Anytime MO-GVNS, Anytime MO-GVNS-s, Anytime MO-GVNS-g | `experiments.studies.Knapsack.MOKP_MOGVNS`                      |
| MCA     | MO-VND, Anytime MO-VND, Anytime MO-VND-s, Anytime MO-VND-g | `experiments.studies.SoftwareModuleClustering.MCA_MOVND_Study`   |
| MCA     | MO-GVNS, Anytime MO-GVNS, Anytime MO-GVNS-s, Anytime MO-GVNS-g | `experiments.studies.SoftwareModuleClustering.MCA_MOGVNS_Study` |

Each `main` method accepts up to two positional arguments:

1. Instance-group directory name under `instances/` (for example, `MOKP` or
   `MCA`).
2. Time limit in minutes for each algorithm run.

For example, if a JAR file has been created to run `experiments.studies.Knapsack.MOKP_MOVND`, one can use the instances within `instances/MOKP` and specify a time limit of 5 minutes by running the program as follows:

```text
java -jar program.jar MOKP 5
```

By default, the program uses 8 cores to run the experiments, where each pair method-instance is considered as a different experiment and run on one of those cores. The number of cores to be used can be changed within the code.


### 4 - Analyzing the results

Process the generated study directories under `results/`. The processor's `--max_time` is in seconds; set it to the same run limit in seconds (300 for the five-minute example). Select `MOKP` or `MCA` with `--problem`:

   ```bash
   python scripts/python/process_results.py results --problem MOKP --max_time 300
   ```

   For MCA, use `--problem MCA` and the corresponding time limit. The script writes plots and intermediate files to `results/`.

### 5 - Statistical analysis

Open `scripts/statistical_analysis/Paired analysis of n methods.R` and change the input filename on line 13 to the desired `results/transformed_QIs_*.csv` file. Run the R script from `scripts/statistical_analysis/` so its path resolves, for example:

   ```bash
   Rscript "Paired analysis of n methods.R"
   ```

   The R analysis requires its listed R packages to be installed.

## License

This project is licensed under the GNU General Public License v3.0. See the
[`LICENSE`](LICENSE) file for the full license terms.

## Funding and acknowledgments

This research has been partially funded by the following projects: PID2024-160226OB-C22, funded by Ministerio de Ciencia, Innovación y Universidades, Agencia Estatal de Investigación, and the European Regional Development Fund; CIRMA-CM TEC-2024/COM-404, by Comunidad Autónoma de Madrid; TSI-100930-2023-3 ``Cátedra AI4DDS: Artificial Intelligence for Data Drive Solutions''; ``Cátedra de Innovación y Digitalización Empresarial entre Universidad Rey Juan Carlos y Second Episode''; ``Cátedra Institucional de Transición Energética''.

## Citation

Publication details and DOI will be added when the paper is available.

## Contact

For questions or issues contact Javier Yuste by email (<a href="mailto:javier.yuste@urjc.es" aria-label="Javier Yuste Email"><img src="https://upload.wikimedia.org/wikipedia/commons/b/b1/Email_Shiny_Icon.svg" alt="email" width="20" style="vertical-align:middle;"/></a>) or use the [GitHub issue tracker](https://github.com/JavierYuste/ScalarMOVNS/issues).
