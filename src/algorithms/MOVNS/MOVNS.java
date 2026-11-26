package algorithms.MOVNS;

import acceptanceCriteria.AcceptanceCriterion;
import algorithms.improvers.ParetoLocalSearch.PVNDFI;
import algorithms.improvers.ParetoLocalSearch.ParetoImprover;
import algorithms.improvers.neighborhoods.Neighborhood;
import archive.ArchiveExtended;
import archive.NDTree;
import model.ExtendedSolution;
import observers.AbstractByTimeObserver;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import org.uma.jmetal.util.observable.Observable;
import org.uma.jmetal.util.observable.ObservableEntity;
import org.uma.jmetal.util.observable.impl.DefaultObservable;
import problems.AbstractProblemExtended;
import selection.InterfacePFSolutionSelection;
import termination.TerminationByTimeWithPauses;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MOVNS<S extends ExtendedSolution<?>> implements Algorithm<List<S>>, ObservableEntity<Map<String, Object>> {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    protected final Observable<Map<String, Object>> observable = new DefaultObservable<>(this.getClass().getSimpleName());
    protected List<AcceptanceCriterion> acceptanceCriteria;
    protected ParetoImprover<S> paretoImprover = new PVNDFI<>();
    protected List<Neighborhood<S>> neighborhoods;
    protected InterfacePFSolutionSelection<S> selection;
    protected TerminationByTimeWithPauses terminationCriterion;
    protected ArchiveExtended<S> archive;
    protected SolutionsCreation<S> constructive;
    protected AbstractProblemExtended problem;
    protected String name;
    protected Map<String, Object> status = new HashMap<>();
    protected int frequencyUpdate = 10;
    protected int currentTimeFrame = 1;
    protected int completedIterations = 0;

    @Override
    public void run() {
        try {
            logger.info("Starting " + this.name() + " for instance " + this.problem.getInstanceName());
            this.checkBuild();
            logger.info("Configuration validated");
            if (this.archive == null) {
                this.archive = new NDTree<>(this.problem.numberOfObjectives());
            }
            logger.info("Creating initial solutions");
            this.constructive.create().forEach(this.archive::add);
            this.initProgress();
            this.terminationCriterion.start();
            this.updateProgress();
            logger.info("Initial solutions created (PFS: " + this.archive.size() + ")");
            logger.info("Starting improvement phase");
            this.improve();
            logger.info("Finished " + this.name());
            status.put("ENDED", true);
            this.updateProgress();
            this.terminationCriterion.stop();
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<S> result() {
        return this.archive.solutions();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String description() {
        return this.name;
    }

    @Override
    public Observable<Map<String, Object>> observable() {
        return this.observable;
    }

    protected void initProgress() {
        status.put("COMPUTING_TIME", (long) 0);
        status.put("TERMINATION_CRITERION", this.terminationCriterion);
        status.put("ENDED", false);
        status.put("START_INSTANT", Instant.now());
        status.put("EVALUATIONS", 0);
        status.put("PFS", 0);
        status.put("INSTANCE", this.problem.getInstanceName());
        status.put("PROBLEM", this.problem.getClass().getSimpleName());
        status.put("NAME", this.name());
        status.put("ARCHIVE", this.archive);
        status.put("OBSERVERS", this.observable);
        for (int i = 0; i < 5; i++) {
            status.put("ITERATIONS_OBJECTIVE_" + i, 0);
        }
        this.customInitProgress();
    }

    protected void customInitProgress() {
        // Custom initialization of progress to be implemented if desired by inheriting classes
    }

    protected boolean checkProgressController() {
        if (Duration.between((Instant) status.get("START_INSTANT"), Instant.now()).toSeconds() >= (long) this.frequencyUpdate * this.currentTimeFrame) {
            this.currentTimeFrame++;
            return true;
        }
        return false;
    }

    protected void updateProgress() {
        this.terminationCriterion.pause();
        if (this.checkProgressController()) {
            logger.info("Method: " + this.name() + "; Instance: " + this.problem.getInstanceName() + "; " +
                    "PFS: " + AbstractByTimeObserver.getCurrentArchive(this.status).size() + "; " +
                    "CPUt (s): " + this.status.get("COMPUTING_TIME"));
        }
        status.put("COMPUTING_TIME", TimeUnit.SECONDS.convert(Duration.between((Instant) status.get("START_INSTANT"), Instant.now())));
        status.put("PFS", this.archive.size());
        this.notifyProgress();
        this.terminationCriterion.resume();
    }

    protected void notifyProgress() {
        this.observable.setChanged();
        this.observable.notifyObservers(status);
    }

    protected void checkBuild() throws IllegalArgumentException {
        if (this.neighborhoods == null || this.neighborhoods.isEmpty()) {
            throw new IllegalArgumentException("Neighborhoods not set");
        }
        if (this.acceptanceCriteria == null || this.acceptanceCriteria.isEmpty()) {
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

    protected void customCheckBuild() throws IllegalArgumentException {

    }

    protected abstract void improve();

    /**
     * Similar to NeighborhoodChange, but in a general fashion (e.g., NeighborhoodExplorationChange, AcceptanceCriteriaChange, etc.)
     *
     * @param improvement
     * @param counter
     * @return
     */
    public static int generalChange(boolean improvement, int counter) {
        if (improvement) {
            return 0;
        } else {
            return counter + 1;
        }
    }

    public MOVNS<S> set(String name) {
        this.name = name;
        return this;
    }

    public MOVNS<S> set(AbstractProblemExtended problem) {
        this.problem = problem;
        return this;
    }

    public MOVNS<S> set(InterfacePFSolutionSelection<S> selection) {
        this.selection = selection;
        return this;
    }

    public MOVNS<S> set(SolutionsCreation<S> constructive) {
        this.constructive = constructive;
        return this;
    }

    @SafeVarargs
    public final MOVNS<S> set(Neighborhood<S>... neighborhoods) {
        this.neighborhoods = Arrays.asList(neighborhoods);
        return this;
    }

    public List<Neighborhood<S>> getNeighborhoods() {
        if (this.neighborhoods == null) {
            throw new IllegalArgumentException("Neighborhoods not set");
        }
        return this.neighborhoods;
    }

    public MOVNS<S> set(AcceptanceCriterion... acceptanceCriteria) {
        this.acceptanceCriteria = Arrays.asList(acceptanceCriteria);
        return this;
    }

    public final MOVNS<S> set(TerminationByTimeWithPauses terminationCriterion) {
        this.terminationCriterion = terminationCriterion;
        return this;
    }

    public final MOVNS<S> set(ArchiveExtended<S> archive) {
        this.archive = archive;
        return this;
    }

    public final MOVNS<S> set(ParetoImprover<S> paretoImprover) {
        this.paretoImprover = paretoImprover;
        return this;
    }
}
