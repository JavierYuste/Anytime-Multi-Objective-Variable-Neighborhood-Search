package algorithms.managers;

import org.uma.jmetal.solution.Solution;

public interface VisitedSolutionsManager<S extends Solution<?>> {
    void add(S solution, Object... keys);

    boolean visited(S solution, Object... keys);
}
