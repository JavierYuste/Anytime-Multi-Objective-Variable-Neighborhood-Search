package algorithms.MOVNS.MOVND;

import acceptanceCriteria.AcceptanceCriterion;
import acceptanceCriteria.LambdaAcceptanceCriterion;
import acceptanceCriteria.scalarizationGenerators.ScalarizationGenerator;
import algorithms.improvers.ParetoLocalSearch.PVNDFI;
import algorithms.managers.VisitedSolutions;
import model.ExtendedSolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

public class AnytimeMOVNDg<S extends ExtendedSolution<?>> extends AnytimeMOVND<S> {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    protected ScalarizationGenerator scalarizationGenerator;

    @Override
    public AnytimeMOVNDg<S> set(AcceptanceCriterion... acceptanceCriteria) {
        throw new IllegalArgumentException("This implementation does not accept predefined acceptance criteria " + "for the second phase.");
    }

    public AnytimeMOVNDg<S> set(ScalarizationGenerator scalarizationGenerator) {
        this.scalarizationGenerator = scalarizationGenerator;
        return this;
    }

    protected void improve() {
        VisitedSolutions<S> visitedSolutionsManager = new VisitedSolutions<>(); // Never updated, dummy used for PVND
        while (!this.terminationCriterion.isMet(this.status)) {
            // Select a scalarization
            LambdaAcceptanceCriterion currentAcceptanceCriterion;
            if (this.completedIterations == 0) {
                double[] lambda = new double[this.scalarizationGenerator.getDimensions()];
                double value = 1.0 / lambda.length;
                Arrays.fill(lambda, value);
                currentAcceptanceCriterion = new LambdaAcceptanceCriterion(lambda);
            } else {
                currentAcceptanceCriterion = this.scalarizationGenerator.getScalarization();
            }
            logger.fine("Current acceptance criterion: " + currentAcceptanceCriterion.toString());

            // Select a solution from the archive
            logger.fine("Size of archive: " + archive.size());
            S selectedSolution = selection.select(this.archive, this.archive.size(), Collections.emptySet(), currentAcceptanceCriterion.getLambdaVector());
            if (selectedSolution == null) {
                logger.finer("No available solution.");
                break;
            }
            logger.fine("Selected solution: " + Arrays.toString(selectedSolution.objectives()));

            // Get acceptable neighbors with this acceptance criterion
            Collection<S> neighbors = this.paretoImprover.improve(selectedSolution, this.terminationCriterion, this.status, visitedSolutionsManager, this.archive, this.neighborhoods, currentAcceptanceCriterion);
            if (neighbors != null && !neighbors.isEmpty()) {
                // If there are acceptable neighbors, add them to the archive
                for (S neighbor : neighbors) {
                    assert (selectedSolution.hashCode() != neighbor.hashCode());
                    archive.add(neighbor);
                    logger.finer("Neighbor added: " + Arrays.toString(neighbor.objectives()));
                }
            }

            this.completedIterations++;
            this.status.put("COMPLETED_ITERATIONS", this.completedIterations);

            if (this.terminationCriterion.isMet(this.status)) {
                this.updateProgress();
                return;
            } else if (this.checkProgressController()) {
                this.updateProgress();
                logger.info("Method: " + this.name() + "; Instance: " + this.problem.getInstanceName() + "; " + "PFS: " + this.archive.size() + "; CPUt (s): " + this.status.get("COMPUTING_TIME"));
            }
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
