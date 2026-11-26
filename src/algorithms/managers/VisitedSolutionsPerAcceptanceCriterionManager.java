package algorithms.managers;

import acceptanceCriteria.AcceptanceCriterion;
import org.uma.jmetal.solution.Solution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VisitedSolutionsPerAcceptanceCriterionManager<S extends Solution<?>> implements VisitedSolutionsManager<S> {

    private final Map<Key, Set<S>> visitedSolutions = new HashMap<>();

    public void add(S solution, Object... keys) {
        if (keys.length != 1) {
            throw new IllegalArgumentException("Invalid number of keys");
        }
        if (keys[0] == null) {
            throw new IllegalArgumentException("Acceptance criteria not set");
        }
        if (solution == null) {
            throw new IllegalArgumentException("Invalid solution");
        }
        if (keys[0] instanceof AcceptanceCriterion k) {
            Key key = new Key(k);
            Set<S> solutions = visitedSolutions.get(key);
            if (solutions == null) {
                solutions = new HashSet<>();
                visitedSolutions.put(key, solutions);
            }
            solutions.add(solution);
        } else {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    public boolean visited(S solution, Object... keys) {
        if (keys[0] instanceof AcceptanceCriterion acceptanceCriteria) {
            Key key = new Key(acceptanceCriteria);
            return this.visitedSolutions.containsKey(key) && this.visitedSolutions.get(key).contains(solution);
        } else {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    public Set<S> get(AcceptanceCriterion acceptanceCriteria) {
        return this.visitedSolutions.get(new Key(acceptanceCriteria));
    }

    private record Key(AcceptanceCriterion acceptanceCriteria) {
    }
}
