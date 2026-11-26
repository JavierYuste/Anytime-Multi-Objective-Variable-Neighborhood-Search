package algorithms.MOVNS.MOGVNS;

import acceptanceCriteria.AcceptanceCriterion;
import algorithms.MOVNS.MOVNS;
import algorithms.MOVNS.Shakes.Shake;
import algorithms.managers.VisitedSolutionsPerAcceptanceCriterionManager;
import archive.ArchiveExtended;
import model.ExtendedSolution;

import java.util.*;
import java.util.logging.Logger;

public class MOGVNS<S extends ExtendedSolution<?>> extends MOVNS<S> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private Shake<S> shake;

    public MOGVNS<S> set(Shake<S> shake) {
        this.shake = shake;
        return this;
    }

    @Override
    protected void improve() {
        VisitedSolutionsPerAcceptanceCriterionManager<S> visitedSolutionsManager = new VisitedSolutionsPerAcceptanceCriterionManager<>();
        while (!this.terminationCriterion.isMet(this.status)) {
            while (!this.shake.reachedMaxK()) {
                boolean improved = false;
                // 1. Obtain solutions and shake them
                List<S> solutionsToImprove = new ArrayList<>(this.archive.size());
                for (S solution : this.archive.solutions()) {
                    S solutionCopy = (S) solution.copy();
                    this.shake.shake(solutionCopy);
                    solutionsToImprove.add(solutionCopy);
                }
                Collections.shuffle(solutionsToImprove);

                // 2. MO-VND
                int counter = 0;
                int currentAcceptanceCriteriaIndex = 0;
                ArchiveExtended<S> miniArchive = this.archive.getEmptyArchive();
                while (currentAcceptanceCriteriaIndex < acceptanceCriteria.size()) {
                    AcceptanceCriterion currentAcceptanceCriterion = acceptanceCriteria.get(currentAcceptanceCriteriaIndex);
                    logger.finer("Current acceptance criterion: " + currentAcceptanceCriterion.getShortName());
                    Set<S> visitedSolutions = visitedSolutionsManager.get(currentAcceptanceCriterion);
                    if (visitedSolutions != null && !visitedSolutions.isEmpty()) {
                        visitedSolutions.clear();
                    }
                    boolean added = false;
                    while (visitedSolutions == null || !visitedSolutions.containsAll(solutionsToImprove)) {
                        logger.finer("Size of archive: " + archive.size());
                        S selectedSolution = selection.select(solutionsToImprove, solutionsToImprove.size(), visitedSolutions);
                        visitedSolutionsManager.add(selectedSolution, currentAcceptanceCriterion);
                        logger.finer("Selected solution: " + Arrays.toString(selectedSolution.objectives()));
                        Collection<S> neighbors = this.paretoImprover.improve(selectedSolution, this.terminationCriterion, this.status, visitedSolutionsManager, miniArchive, this.neighborhoods, currentAcceptanceCriterion);
                        if (neighbors != null && !neighbors.isEmpty()) { // If there is an acceptable neighbor
                            for (S neighbor : neighbors) {
                                assert (selectedSolution.hashCode() != neighbor.hashCode());
                                added |= miniArchive.add(neighbor);
                                improved |= this.archive.add(neighbor);
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
                        counter++;
                    }
                    currentAcceptanceCriteriaIndex = generalChange(added, currentAcceptanceCriteriaIndex);
                }

                // 4. Update k
                shake.updateK(improved);
            }
            this.shake.restartK();
        }
    }
}
