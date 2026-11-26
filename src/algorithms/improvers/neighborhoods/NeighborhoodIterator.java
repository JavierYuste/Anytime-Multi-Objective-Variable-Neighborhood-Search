package algorithms.improvers.neighborhoods;

import org.uma.jmetal.solution.Solution;

import java.util.Iterator;

public interface NeighborhoodIterator<S extends Solution<?>> extends Iterator<S> {
    void undoLastMove(S solution);

    S _next();
}
