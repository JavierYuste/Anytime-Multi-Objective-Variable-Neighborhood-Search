package algorithms.managers;

import org.uma.jmetal.solution.Solution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class VisitedSolutions<S extends Solution<?>> implements VisitedSolutionsManager<S> {

    private final Set<S> visitedSolutions = new HashSet<>();

    @Override
    public void add(S solution, Object... keys) {
        this.visitedSolutions.add(solution);
    }

    @Override
    public boolean visited(S solution, Object... keys) {
        return this.visitedSolutions.contains(solution);
    }

    public boolean containsAll(Collection<S> solutions) {
        return this.visitedSolutions.containsAll(solutions);
    }

    public Set<S> solutions() {
        return this.visitedSolutions;
    }

    public void clear() {
        this.visitedSolutions.clear();
    }
}
