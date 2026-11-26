package problems;

import model.SMCSolution;
import org.uma.jmetal.solution.Solution;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class MCAProblem extends SMCProblem {

    public MCAProblem(String instanceFile) {
        super(instanceFile);
    }

    public String name() {
        return "MCAProblem-" + new File(instanceFile).getName();
    }

    @Override
    public Solution<Integer> createSolution() {
        return new SMCSolution(this);
    }

    @Override
    public int numberOfVariables() {
        return this.dependencyMatrix.length;
    }

    public int numberOfObjectives() {
        return 5;
    }

    @Override
    public int numberOfConstraints() {
        return 0;
    }

    public double[] getMinNormalizedObjectives() {
        return new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
    }

    public double[] getMaxNormalizedObjectives() {
        return new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
    }

    @Override
    public Solution<Integer> evaluate(Solution<Integer> integerSolution) {
        List<Integer> modules = integerSolution.variables();
        int n = integerSolution.variables().size();
        int[] verticesInModule = new int[modules.size()];
        int[] intraclusterEdges = new int[modules.size()];
        int[] interclusterEdges = new int[modules.size()];
        // Get intercluster and intracluster edges
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.dependencyMatrix[i][j] > 0) {
                    if (modules.get(i) == modules.get(j)) {
                        intraclusterEdges[modules.get(i)] += this.dependencyMatrix[i][j];
                    } else {
                        interclusterEdges[modules.get(i)] += this.dependencyMatrix[i][j];
                        interclusterEdges[modules.get(j)] += this.dependencyMatrix[i][j];
                    }
                }
            }
            verticesInModule[modules.get(i)]++;
        }

        int numberOfModules = 0;
        int isolatedModules = 0;
        for (int verticesInModulei : verticesInModule) {
            if (verticesInModulei > 0) {
                numberOfModules++;
                if (verticesInModulei == 1) {
                    isolatedModules++;
                }
            }
        }

        double MQ = 0;
        int cohesion = 0;
        int coupling = 0;
        // Get sum of Modularization Factor
        for (int i = 0; i < intraclusterEdges.length; i++) {
            if (intraclusterEdges[i] != 0) {
                double MF = (intraclusterEdges[i] / (intraclusterEdges[i] + (0.5 * interclusterEdges[i])));
                MQ += (int) (MF * 10000);
            }
            cohesion += intraclusterEdges[i];
            coupling += interclusterEdges[i];
        }
        cohesion /= 2;
        coupling /= 4;


        integerSolution.objectives()[0] = coupling;
        integerSolution.objectives()[1] = -cohesion;
        integerSolution.objectives()[2] = -MQ;
        integerSolution.objectives()[3] = -numberOfModules;
        integerSolution.objectives()[4] = isolatedModules;
        return integerSolution;
    }

    @Override
    public void setIdealPoint() {
        this.idealPoint = new double[5];
        this.idealPoint[0] = 0; // Coupling
        this.idealPoint[1] = -this.sumEdgesWeights; // Cohesion
        // The maximum quality of a module in the MQ formulation is 1, which occurs when no interedges
        // are present (coupling = 0). Therefore, the theoretical upper bound of MQ is equal to
        // the maximum number of modules, which equals to the number of vertices in the instance.
        // Of course, the upper bound in practice is usually less than this value, because such a
        // solution does not exist. For a module to have quality 1, it must have at least one interedge,
        // which means that it has to contain at least 2 vertices. Therefore, the upper bound would be
        // the number of vertices divided by 2, and it could only be obtained if the instance is formed
        // by disconnected pairs of vertices.
        this.idealPoint[2] = -((double) this.numberOfVariables()) / 2.0; // MQ
        this.idealPoint[3] = -this.numberOfVariables(); // Modules
        this.idealPoint[4] = 0; // Isolated modules
    }

    @Override
    public void setNadirPoint() {
        this.nadirPoint = new double[5];
        this.nadirPoint[0] = this.sumEdgesWeights; // Coupling
        this.nadirPoint[1] = 0; // Cohesion
        this.nadirPoint[2] = 0; // MQ
        this.nadirPoint[3] = 0; // Modules
        this.nadirPoint[4] = this.numberOfVariables(); // Isolated modules
    }

}
