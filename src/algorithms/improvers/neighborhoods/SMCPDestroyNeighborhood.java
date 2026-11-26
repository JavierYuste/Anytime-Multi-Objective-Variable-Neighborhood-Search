package algorithms.improvers.neighborhoods;

import model.SMCSolution;
import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SMCPDestroyNeighborhood<S extends SMCSolution> implements Neighborhood<S> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    public NeighborhoodIterator<S> iterator(S solution) {
        return new DestroyNeighborhoodIterator<>(solution);
    }

    private class DestroyNeighborhoodIterator<R extends SMCSolution> extends SMCPAbstractNeighborhoodIterator<R> {

        List<Integer> usedModules;
        int startingModule;
        int currentModule;
        // Flag to know if we should start watching for the stopping criterion of reaching the starting module.
        // This is to allow the iterator to destroy the starting module at the beginning.
        boolean started = false;

        public DestroyNeighborhoodIterator(R solution) {
            super(solution);
            this.usedModules = new ArrayList<>(solution.getUsedModules());
            logger.finer("Solution " + this.solution.hashCode() + ": " + usedModules.size() + " modules to destroy");
            this.startingModule = JMetalRandom.getInstance().nextInt(0, usedModules.size()) % usedModules.size();
            this.currentModule = startingModule;
        }

        @Override
        public boolean hasNext() {
            return !started || this.currentModule != startingModule;
        }

        @Override
        public R _next() {
            logger.finest("Solution " + this.solution.hashCode() + ": Destroying module " + this.currentModule);
            this.started = true;
            Object[] affectedVertices = solution.getVerticesInModule(this.currentModule).toArray();
            undoMove = new ArrayList<>(affectedVertices.length);
            // Move each affected vertex to the most connected module
            for (Object affectedVertex : affectedVertices) {
                undoMove.add(new Pair<>((Integer) affectedVertex, this.currentModule));
                solution.moveVertex((Integer) affectedVertex, solution.getMostConnectedModule((Integer) affectedVertex));
            }

            // Update current state for the next call
            this.currentModule = (this.currentModule + 1) % usedModules.size();

            return solution;
        }
    }

}
