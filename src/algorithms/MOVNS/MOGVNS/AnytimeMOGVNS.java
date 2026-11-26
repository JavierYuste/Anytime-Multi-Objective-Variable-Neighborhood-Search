package algorithms.MOVNS.MOGVNS;

import acceptanceCriteria.AcceptanceCriterion;
import algorithms.MOVNS.MOVNS;
import algorithms.MOVNS.Shakes.Shake;
import algorithms.managers.VisitedSolutions;
import model.ExtendedSolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

public class AnytimeMOGVNS<S extends ExtendedSolution<?>> extends MOVNS<S> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private Shake<S> shake;

    public AnytimeMOGVNS<S> set(Shake<S> shake) {
        this.shake = shake;
        return this;
    }

    @Override
    protected void improve() {
        VisitedSolutions<S> visitedSolutionsManager = new VisitedSolutions<>();
        boolean improved = false;
        while (!this.terminationCriterion.isMet(this.status)) {
            while (!this.shake.reachedMaxK()) {
                logger.fine("Size of archive: " + archive.size());

                // 1. Select a solution from the archive
                S selectedSolution = selection.select(this.archive, this.archive.size(), visitedSolutionsManager.solutions());
                if (selectedSolution == null) {
                    logger.finer("No available solution.");
                    visitedSolutionsManager.clear();
                    shake.updateK(improved);
                    improved = false;
                    continue;
                }
                visitedSolutionsManager.add(selectedSolution);
                logger.fine("Selected solution: " + Arrays.toString(selectedSolution.objectives()));

                // 2. Apply the shake operator
                S solution = (S) selectedSolution.copy();
                this.shake.shake(solution);

                // 3. Improve the shaked solution
                for (AcceptanceCriterion currentAcceptanceCriterion : acceptanceCriteria) {
                    logger.finer("Current acceptance criterion: " + currentAcceptanceCriterion.getShortName());
                    Collection<S> neighbors = this.paretoImprover.improve(solution, this.terminationCriterion, this.status, visitedSolutionsManager, this.archive, this.neighborhoods, currentAcceptanceCriterion);

                    if (neighbors != null && !neighbors.isEmpty()) {
                        for (S neighbor : neighbors) {
                            assert (solution.hashCode() != neighbor.hashCode());
                            improved |= archive.add(neighbor);
                            logger.finer("Neighbor added: " + Arrays.toString(neighbor.objectives()));
                            if (this.checkProgressController()) {
                                this.updateProgress();
                            }
                        }
                    }

                    if (this.terminationCriterion.isMet(this.status)) {
                        return;
                    } else if (this.checkProgressController()) {
                        this.updateProgress();
                        logger.info("Method: " + this.name() + "; Instance: " + this.problem.getInstanceName() + "; PFS: " + this.archive.size() + "; CPUt (s): " + this.status.get("COMPUTING_TIME"));
                    }
                }
            }
            this.shake.restartK();
        }
    }
}
