package algorithms.contructives;

import model.SMCSolution;
import org.uma.jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import problems.SMCProblem;

import java.util.ArrayList;
import java.util.List;

public class RandomSMCSolutionsCreation<S extends SMCSolution> implements SolutionsCreation<S> {
    private final int numberOfSolutionsToCreate;
    private final SMCProblem problem;

    /**
     * Creates the list of solutions
     *
     * @param problem                   Problem defining the solutions
     * @param numberOfSolutionsToCreate
     */
    public RandomSMCSolutionsCreation(Problem<?> problem, int numberOfSolutionsToCreate) {
        if (problem instanceof SMCProblem) {
            this.problem = (SMCProblem) problem;
            this.numberOfSolutionsToCreate = numberOfSolutionsToCreate;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public List<S> create() {
        List<S> solutionList = new ArrayList<>(numberOfSolutionsToCreate);
        for (int i = 0; i < this.numberOfSolutionsToCreate; i++) {
            SMCSolution solution = new SMCSolution(this.problem);
            for (int j = 0; j < this.problem.numberOfVariables(); j++) {
                solution.moveVertex(j, JMetalRandom.getInstance().nextInt(0, this.problem.numberOfVariables() - 1));
            }
            solutionList.add((S) solution);
        }
        return solutionList;
    }
}

