package algorithms.MOVNS.MOVND;

import acceptanceCriteria.AcceptanceCriterion;
import algorithms.MOVNS.MOVNS;
import algorithms.managers.VisitedSolutions;
import model.ExtendedSolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

public class AnytimeMOVND<S extends ExtendedSolution<?>> extends MOVNS<S> {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    protected void improve() {
        VisitedSolutions<S> visitedSolutionsManager = new VisitedSolutions<>();

        while (!this.terminationCriterion.isMet(this.status)) {
            logger.fine("Size of archive: " + archive.size());
            S selectedSolution = selection.select(this.archive, this.archive.size(), visitedSolutionsManager.solutions());
            if (selectedSolution == null) {
                logger.finer("No available solution.");
                break;
            }
            visitedSolutionsManager.add(selectedSolution);
            logger.fine("Selected solution: " + Arrays.toString(selectedSolution.objectives()));

            for (AcceptanceCriterion currentAcceptanceCriterion : acceptanceCriteria) {
                logger.finer("Current acceptance criterion: " + currentAcceptanceCriterion.getShortName());
                Collection<S> neighbors = this.paretoImprover.improve(selectedSolution, this.terminationCriterion, this.status, visitedSolutionsManager, this.archive, this.neighborhoods, currentAcceptanceCriterion);

                if (neighbors != null && !neighbors.isEmpty()) {
                    for (S neighbor : neighbors) {
                        assert (selectedSolution.hashCode() != neighbor.hashCode());
                        archive.add(neighbor);
                        logger.finer("Neighbor added: " + Arrays.toString(neighbor.objectives()));
                        if (this.checkProgressController()) {
                            this.updateProgress();
                        }
                    }
                    // Update the selected solution, so the next acceptance criterion can be applied to the last solution found by the previous one
                    selectedSolution = this.updateSelectedSolution(selectedSolution, neighbors);
                }

                if (this.terminationCriterion.isMet(this.status)) {
                    return;
                } else if (this.checkProgressController()) {
                    this.updateProgress();
                    logger.info("Method: " + this.name() + "; Instance: " + this.problem.getInstanceName() + "; PFS: " + this.archive.size() + "; CPUt (s): " + this.status.get("COMPUTING_TIME"));
                }
            }
        }
    }

    protected S updateSelectedSolution(S selectedSolution, Collection<S> neighbors) {
        // Do not update the solution
        return selectedSolution;
    }

}
