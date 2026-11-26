package algorithms.MOVNS.MOVND;

import acceptanceCriteria.AcceptanceCriterion;
import algorithms.managers.VisitedSolutionsPerAcceptanceCriterionManager;
import model.ExtendedSolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MOVND<S extends ExtendedSolution<?>> extends AnytimeMOVND<S> {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    protected void improve() {
        VisitedSolutionsPerAcceptanceCriterionManager<S> visitedSolutionsManager = new VisitedSolutionsPerAcceptanceCriterionManager<>();

        int counter = 0;
        int currentAcceptanceCriteriaIndex = 0;
        while (currentAcceptanceCriteriaIndex < acceptanceCriteria.size()) {
            AcceptanceCriterion currentAcceptanceCriterion = acceptanceCriteria.get(currentAcceptanceCriteriaIndex);
            logger.finer("Current acceptance criterion: " + currentAcceptanceCriterion.getShortName());
            Set<S> visitedSolutions = visitedSolutionsManager.get(currentAcceptanceCriterion);
            boolean added = false;
            List<S> nonDominatedSolutions = archive.solutions();
            while (visitedSolutions == null || !visitedSolutions.containsAll(nonDominatedSolutions)) {
                logger.finer("Size of archive: " + archive.size());
                S selectedSolution = selection.select(nonDominatedSolutions, nonDominatedSolutions.size(), visitedSolutions);
                visitedSolutionsManager.add(selectedSolution, currentAcceptanceCriterion);
                logger.finer("Selected solution: " + Arrays.toString(selectedSolution.objectives()));
                Collection<S> neighbors = this.paretoImprover.improve(selectedSolution,
                        this.terminationCriterion, this.status, visitedSolutionsManager, this.archive, this.neighborhoods, currentAcceptanceCriterion);
                if (neighbors != null && !neighbors.isEmpty()) { // If there is an acceptable neighbor
                    for (S neighbor : neighbors) {
                        assert (selectedSolution.hashCode() != neighbor.hashCode());
                        boolean lastAdded = archive.add(neighbor);
                        added |= lastAdded;
                        visitedSolutionsManager.add(neighbor, currentAcceptanceCriterion);
                        logger.finer("Neighbor added: " + Arrays.toString(neighbor.objectives()));
                        if (this.checkProgressController()) {
                            this.updateProgress();
                        }
                    }
                }

                if (this.terminationCriterion.isMet(this.status)) {
                    this.terminationCriterion.pause();
                    logger.info("Method: " + this.name() + "; Instance: " + this.problem.getInstanceName() + "; PFS: " + this.archive.size() + "; CPUt (s): " + this.status.get("COMPUTING_TIME") + "; Solutions tackled: " + counter);
                    this.terminationCriterion.resume();
                    return;
                } else if (this.checkProgressController()) {
                    this.updateProgress();
                    logger.info("Method: " + this.name() + "; Instance: " + this.problem.getInstanceName() + "; PFS: " + this.archive.size() + "; CPUt (s): " + this.status.get("COMPUTING_TIME"));
                }
                visitedSolutions = visitedSolutionsManager.get(currentAcceptanceCriterion);
                nonDominatedSolutions = archive.solutions();
                counter++;
            }
            currentAcceptanceCriteriaIndex = this.generalChange(added, currentAcceptanceCriteriaIndex);
        }
        this.terminationCriterion.pause();
        logger.info("Method: " + this.name() + "; Instance: " + this.problem.getInstanceName() + "; PFS: " + this.archive.size() + "; CPUt (s): " + this.status.get("COMPUTING_TIME") + "; Solutions tackled: " + counter);
        this.terminationCriterion.resume();
    }

}
