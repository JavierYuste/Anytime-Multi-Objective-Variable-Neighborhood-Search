package algorithms.contructives;

import model.MOKPSolution;
import org.uma.jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import problems.MOKPProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomMOKPSolutionsCreation<S extends MOKPSolution> implements SolutionsCreation<S> {

    private final int numberOfSolutionsToCreate;
    private final MOKPProblem problem;

    /**
     * Creates the list of solutions
     *
     * @param problem                   Problem defining the solutions
     * @param numberOfSolutionsToCreate
     */
    public RandomMOKPSolutionsCreation(MOKPProblem problem, int numberOfSolutionsToCreate) {
        this.problem = problem;
        this.numberOfSolutionsToCreate = numberOfSolutionsToCreate;
    }

    public List<S> create() {
        List<S> solutionList = new ArrayList<>(numberOfSolutionsToCreate);
        for (int i = 0; i < this.numberOfSolutionsToCreate; i++) {
            solutionList.add(this.createSolution());
        }
        return solutionList;
    }

    private S createSolution() {
        MOKPSolution solution = new MOKPSolution(this.problem);
        List<Integer> indexes = new ArrayList<>(solution.variables().size());
        for (int i = 0; i < solution.variables().size(); i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes, new Random(JMetalRandom.getInstance().nextInt(0, Integer.MAX_VALUE - 1)));
        do {
            int variable = indexes.remove(indexes.size() - 1);
            solution.setVariable(variable);
            if (solution.maxWeightSurpassed()) {
                solution.setVariable(variable); // Undo last move
                break;
            }
        } while (!indexes.isEmpty());

        return (S) solution;
    }
}

