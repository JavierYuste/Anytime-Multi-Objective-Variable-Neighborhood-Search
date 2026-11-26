package algorithms.MOVNS.MOGVNS;

import acceptanceCriteria.AcceptanceCriterion;
import acceptanceCriteria.LambdaAcceptanceCriterion;
import acceptanceCriteria.scalarizationGenerators.ScalarizationGenerator;
import algorithms.MOVNS.MOVNS;
import algorithms.MOVNS.Shakes.Shake;
import algorithms.improvers.ParetoLocalSearch.PVNDFI;
import algorithms.managers.VisitedSolutions;
import model.ExtendedSolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

public class AnytimeMOGVNSg<S extends ExtendedSolution<?>> extends MOVNS<S> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    protected ScalarizationGenerator scalarizationGenerator;
    private Shake<S> shake;

    public AnytimeMOGVNSg<S> set(Shake<S> shake) {
        this.shake = shake;
        return this;
    }

    @Override
    public AnytimeMOGVNSg<S> set(AcceptanceCriterion... acceptanceCriteria) {
        throw new IllegalArgumentException("This implementation does not accept predefined acceptance criteria " + "for the second phase.");
    }

    public AnytimeMOGVNSg<S> set(ScalarizationGenerator scalarizationGenerator) {
        this.scalarizationGenerator = scalarizationGenerator;
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
                // Select a scalarization
                LambdaAcceptanceCriterion currentAcceptanceCriterion;
                if (this.completedIterations == 0) {
                    // First ever scalarizing function is a well-balanced, centered one
                    double[] lambda = new double[this.scalarizationGenerator.getDimensions()];
                    double value = 1.0 / lambda.length;
                    Arrays.fill(lambda, value);
                    currentAcceptanceCriterion = new LambdaAcceptanceCriterion(lambda);
                } else {
                    currentAcceptanceCriterion = this.scalarizationGenerator.getScalarization();
                }
                logger.fine("Current acceptance criterion: " + currentAcceptanceCriterion.toString());

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

                this.completedIterations++;
                this.status.put("COMPLETED_ITERATIONS", this.completedIterations);

                if (this.terminationCriterion.isMet(this.status)) {
                    return;
                } else if (this.checkProgressController()) {
                    this.updateProgress();
                    logger.info("Method: " + this.name() + "; Instance: " + this.problem.getInstanceName() + "; PFS: " + this.archive.size() + "; CPUt (s): " + this.status.get("COMPUTING_TIME"));
                }
            }
            this.shake.restartK();
        }
    }

    @Override
    protected void checkBuild() throws IllegalArgumentException {
        if (this.neighborhoods == null || this.neighborhoods.isEmpty()) {
            throw new IllegalArgumentException("Neighborhoods not set");
        }
        if (this.scalarizationGenerator == null) {
            throw new IllegalArgumentException("Acceptance criteria not set");
        }
        if (this.selection == null) {
            throw new IllegalArgumentException("Selection not set");
        }
        if (this.terminationCriterion == null) {
            throw new IllegalArgumentException("Termination criterion not set");
        }
        if (this.constructive == null) {
            throw new IllegalArgumentException("Constructive not set");
        }
        if (this.problem == null) {
            throw new IllegalArgumentException("Problem not set");
        }
        if (this.name == null) {
            throw new IllegalArgumentException("Name not set");
        }
        if (this.paretoImprover == null) {
            this.paretoImprover = new PVNDFI<>();
            logger.warning("ParetoImprover not set. Setting default ParetoImprover: " + this.paretoImprover.getClass().getSimpleName());
        }
        this.customCheckBuild();
    }
}
