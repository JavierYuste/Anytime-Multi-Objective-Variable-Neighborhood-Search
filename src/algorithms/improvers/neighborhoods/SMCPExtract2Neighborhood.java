package algorithms.improvers.neighborhoods;

import model.SMCSolution;
import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class SMCPExtract2Neighborhood<S extends SMCSolution> implements Neighborhood<S> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    public NeighborhoodIterator<S> iterator(S solution) {
        return new Extract2NeighborhoodIterator<>(solution);
    }

    private class Extract2NeighborhoodIterator<R extends SMCSolution> extends SMCPAbstractNeighborhoodIterator<R> {

        int startingPoint;
        int currentVertex;
        int originalModule;
        int newModule;
        Iterator<Integer> adjacentVertices;
        int numberOfVariables;

        public Extract2NeighborhoodIterator(R solution) {
            super(solution);
            this.numberOfVariables = this.solution.variables().size();
            logger.finer("Solution " + this.solution.hashCode() + ": " + this.numberOfVariables + " vertices to extract");
            this.newModule = this.solution.obtainAvailableModule();
            this.startingPoint = JMetalRandom.getInstance().nextInt(0, this.numberOfVariables) % this.numberOfVariables;
            this.currentVertex = this.startingPoint;
            this.originalModule = solution.getModule(this.currentVertex);
            this.adjacentVertices = this.solution.getProblem().getAdjacentVertices(this.currentVertex).iterator();
            this.updateState();
        }

        private void updateState() {
            if (!this.adjacentVertices.hasNext()) {
                this.currentVertex = (this.currentVertex + 1) % this.numberOfVariables;
                this.originalModule = solution.getModule(this.currentVertex);
                if (this.currentVertex != this.startingPoint) {
                    this.adjacentVertices = this.solution.getProblem().getAdjacentVertices(this.currentVertex).iterator();
                    this.updateState();
                } else {
                    this.currentVertex = -1;
                    this.adjacentVertices = null;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return this.adjacentVertices != null && this.adjacentVertices.hasNext();
        }

        @Override
        public R _next() {
            int adjacentVertex = this.adjacentVertices.next();
            logger.finest("Solution " + this.solution.hashCode() + ": v" + this.currentVertex + " -> m" + this.newModule + ", " + "v" + adjacentVertex + " -> m" + this.newModule);

            undoMove = List.of(new Pair<>(adjacentVertex, this.solution.getModule(adjacentVertex)), new Pair<>(this.currentVertex, this.solution.getModule(this.currentVertex)));

            this.solution.moveVertex(adjacentVertex, this.newModule);
            this.solution.moveVertex(this.currentVertex, this.newModule);

            this.updateState();

            return solution;
        }
    }

}
