package algorithms.improvers.ParetoLocalSearch;

import acceptanceCriteria.AcceptanceCriterion;
import algorithms.improvers.neighborhoods.Neighborhood;
import algorithms.improvers.neighborhoods.NeighborhoodIterator;
import algorithms.managers.VisitedSolutionsManager;
import model.ExtendedSolution;
import org.uma.jmetal.component.catalogue.common.termination.Termination;
import org.uma.jmetal.util.archive.Archive;
import org.uma.jmetal.util.observable.Observable;
import termination.TerminationByTimeWithPauses;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class ParetoImprover<S extends ExtendedSolution<?>> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    protected static void pauseAndNotifyObservers(Map<String, Object> algorithmStatusData) {
        TerminationByTimeWithPauses terminationCriterion = (TerminationByTimeWithPauses) algorithmStatusData.get("TERMINATION_CRITERION");
        if (terminationCriterion.beep()) {
            terminationCriterion.pause();
            org.uma.jmetal.util.observable.Observable<Map<String, Object>> observers = (Observable<Map<String, Object>>) algorithmStatusData.get("OBSERVERS");
            observers.setChanged();
            observers.notifyObservers(algorithmStatusData);
            terminationCriterion.resume();
        }
    }

    public Collection<S> improve(S s, Termination terminationCriterion, Map<String, Object> algorithmStatusData, VisitedSolutionsManager<S> visitedSolutionsManager, Archive<S> archive, List<Neighborhood<S>> neighborhoods, AcceptanceCriterion... acceptanceCriteria) {
        this.checkCall(s, terminationCriterion, acceptanceCriteria);
        return this.improve_(s, terminationCriterion, algorithmStatusData, visitedSolutionsManager, archive, neighborhoods, acceptanceCriteria);
    }

    protected abstract Collection<S> improve_(S s, Termination terminationCriterion, Map<String, Object> algorithmStatusData, VisitedSolutionsManager<S> visitedSolutionsManager, Archive<S> archive, List<Neighborhood<S>> neighborhoods, AcceptanceCriterion... acceptanceCriteria);

    public S getAcceptableNeighbor(S s, AcceptanceCriterion acceptanceCriteria, Termination terminationCriterion, Map<String, Object> algorithmStatusData, VisitedSolutionsManager<S> visitedSolutionsManager, Archive<S> archive, List<Neighborhood<S>> neighborhoods) {
        this.checkCall(s, terminationCriterion, acceptanceCriteria);
        S solution = (S) s.copy();
        double[] previousObjectives = solution.objectives();
        for (Neighborhood<S> neighborhood : neighborhoods) {
            NeighborhoodIterator<S> neighbors = neighborhood.iterator(solution);
            while (neighbors.hasNext()) {
                S neighbor = neighbors.next();
                if (!visitedSolutionsManager.visited(neighbor, acceptanceCriteria) && acceptanceCriteria.accept(neighbor, s, archive)) {
                    assert (s.hashCode() != neighbor.hashCode());
                    logger.finest("Found acceptable neighbor in " + neighborhood.getClass().getSimpleName() + " with AC " + acceptanceCriteria.getClass().getSimpleName());
                    logger.finest("Prev: " + Arrays.toString(previousObjectives));
                    logger.finest("Neighbor: " + Arrays.toString(neighbor.objectives()));
                    return neighbor;
                }
                neighbors.undoLastMove(neighbor);
                assert (s.hashCode() == neighbor.hashCode());
                pauseAndNotifyObservers(algorithmStatusData);
                if (terminationCriterion.isMet(algorithmStatusData)) {
                    return null;
                }
            }
        }
        return null;
    }

    public void checkCall(S s, Termination terminationCriterion, AcceptanceCriterion... acceptanceCriteria) {
        if (acceptanceCriteria == null) {
            throw new IllegalArgumentException("Acceptance criteria not set");
        }
        if (s == null) {
            throw new IllegalArgumentException("Invalid solution");
        }
        if (terminationCriterion == null) {
            throw new IllegalArgumentException("Termination criterion not set");
        }
    }

}
