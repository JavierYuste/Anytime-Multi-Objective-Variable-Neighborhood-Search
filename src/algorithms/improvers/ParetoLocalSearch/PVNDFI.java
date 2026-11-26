package algorithms.improvers.ParetoLocalSearch;

import acceptanceCriteria.AcceptanceCriterion;
import algorithms.improvers.neighborhoods.Neighborhood;
import algorithms.managers.VisitedSolutionsManager;
import archive.NDTree;
import model.ExtendedSolution;
import org.uma.jmetal.component.catalogue.common.termination.Termination;
import org.uma.jmetal.util.archive.Archive;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PVNDFI<S extends ExtendedSolution<?>> extends ParetoImprover<S> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    protected Collection<S> improve_(S s, Termination terminationCriterion, Map<String, Object> algorithmStatusData, VisitedSolutionsManager<S> visitedSolutionsManager, Archive<S> archive, List<Neighborhood<S>> neighborhoods, AcceptanceCriterion... acceptanceCriteria) {
        if (acceptanceCriteria.length > 1) {
            throw new IllegalArgumentException("Only one acceptance criterion is allowed");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Neighborhoods: ");
        for (Neighborhood<S> neighborhood : neighborhoods) {
            sb.append(neighborhood.getClass().getSimpleName()).append(", ");
        }
        logger.finest(sb.toString());
        NDTree<S> obtainedSolutions = new NDTree<>(s.objectives().length);
        S neighbor = s;
        do {
            neighbor = this.getAcceptableNeighbor(neighbor, acceptanceCriteria[0], terminationCriterion, algorithmStatusData, visitedSolutionsManager, archive, neighborhoods);
            if (neighbor != null) {
                obtainedSolutions.add(neighbor);
                algorithmStatusData.put("TEMP_ARCHIVE", obtainedSolutions);
                pauseAndNotifyObservers(algorithmStatusData);
            }
            algorithmStatusData.put("COMPUTING_TIME", TimeUnit.SECONDS.convert(Duration.between((Instant) algorithmStatusData.get("START_INSTANT"), Instant.now())));
        } while (neighbor != null && !terminationCriterion.isMet(algorithmStatusData));
        pauseAndNotifyObservers(algorithmStatusData);
        return obtainedSolutions.solutions();
    }

}
