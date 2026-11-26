package selection;

import org.uma.jmetal.solution.Solution;

import java.util.Set;

public interface InterfacePFSolutionSelection<S extends Solution<?>> {

    S select(Iterable<S> solutions, int sizeSolutions);

    S select(Iterable<S> solutions, int sizeSolutions, double[] vector);

    S select(Iterable<S> solutions, int sizeSolutions, Set<S> solutionsToDiscard);

    S select(Iterable<S> solutions, int sizeSolutions, Set<S> solutionsToDiscard, double[] vector);

}
