package algorithms.improvers.neighborhoods;

import org.uma.jmetal.solution.Solution;

public interface Neighborhood<S extends Solution<?>> {
    NeighborhoodIterator<S> iterator(S solution);
}
