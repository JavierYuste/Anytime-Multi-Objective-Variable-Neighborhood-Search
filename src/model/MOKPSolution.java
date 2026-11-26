package model;

import org.uma.jmetal.solution.Solution;
import problems.MOKPProblem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MOKPSolution implements ExtendedSolution<Boolean> {

    private final Logger log = Logger.getLogger(this.getClass().toString());
    private final MOKPProblem problem;
    private List<Boolean> variables;
    private double[] normalizedObjectives = new double[2];
    private boolean updatedNormalizedObjectives = false;
    private double[] objectives = new double[2];
    private double weight = 0.0;
    private int numberOfObjectsSelected = 0;
    private Map<Object, Object> attributes = new HashMap<>();


    public MOKPSolution(MOKPProblem problem) {
        this.problem = problem;
        this.variables = new ArrayList<>(this.problem.numberOfVariables());
        for (int i = 0; i < this.problem.numberOfVariables(); i++) {
            this.variables.add(i, false);
        }
        this.numberOfObjectsSelected = 0;
    }

    public int getNumberOfObjectsSelected() {
        return numberOfObjectsSelected;
    }

    public double getWeight() {
        return weight;
    }

    public boolean maxWeightSurpassed() {
        return weight > this.problem.getMaxWeight();
    }

    public MOKPProblem getProblem() {
        return this.problem;
    }

    @Override
    public List<Boolean> variables() {
        return this.variables;
    }

    @Override
    public double[] objectives() {
        if (this.weight > this.problem.getMaxWeight()) {
            return new double[]{0.0, 0.0};
        } else {
            return this.objectives;
        }
    }

    @Override
    public double[] constraints() {
        return new double[0];
    }

    public void setVariable(int variable) {
        if (this.variables.get(variable)) {
            // Removing an item from the knapsack
            this.variables.set(variable, false);
            this.numberOfObjectsSelected--;
            this.objectives[0] += this.problem.getRewards()[variable][0]; // Inverted to convert it into a minimization problem
            this.objectives[1] += this.problem.getRewards()[variable][1]; // Inverted to convert it into a minimization problem
            this.weight -= this.problem.getWeights()[variable];
        } else {
            // Adding an item to the knapsack
            this.variables.set(variable, true);
            this.numberOfObjectsSelected++;
            this.objectives[0] -= this.problem.getRewards()[variable][0]; // Inverted to convert it into a minimization problem
            this.objectives[1] -= this.problem.getRewards()[variable][1]; // Inverted to convert it into a minimization problem
            this.weight += this.problem.getWeights()[variable];
        }
        this.updatedNormalizedObjectives = false;
    }

    @Override
    public Map<Object, Object> attributes() {
        return this.attributes;
    }

    @Override
    public Solution<Boolean> copy() {
        MOKPSolution copy = new MOKPSolution(this.problem);
        copy.variables = new ArrayList<>(this.variables.size());
        for (int i = 0; i < this.variables.size(); i++) {
            if (this.variables.get(i)) {
                copy.variables.add(i, true);
            } else {
                copy.variables.add(i, false);
            }
        }
        copy.weight = this.weight;
        copy.normalizedObjectives = this.normalizedObjectives.clone();
        copy.objectives = this.objectives.clone();
        copy.attributes = new HashMap<>(this.attributes);
        copy.numberOfObjectsSelected = this.numberOfObjectsSelected;
        return copy;
    }

    @Override
    public double[] objectivesNormalized() {
        return this.problem.normalize(this.objectives());
    }
}
