package algorithms.improvers.neighborhoods;

import model.SMCSolution;
import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SMCPExtract3Neighborhood<S extends SMCSolution> implements Neighborhood<S> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    public NeighborhoodIterator<S> iterator(S solution) {
        return new Extract3NeighborhoodIterator<>(solution);
    }

    private class Extract3NeighborhoodIterator<R extends SMCSolution> extends SMCPAbstractNeighborhoodIterator<R> {

        int startingPoint;
        int currentVertex;
        int newModule;
        List<Integer> adjacentVertices;
        int adjacentVertex1;
        int adjacentVertex2;
        int numberOfVariables;

        public Extract3NeighborhoodIterator(R solution) {
            super(solution);
            this.numberOfVariables = this.solution.variables().size();
            logger.finer("Solution " + this.solution.hashCode() + ": " + this.numberOfVariables + " vertices to extract");
            this.newModule = this.solution.obtainAvailableModule();
            this.startingPoint = JMetalRandom.getInstance().nextInt(0, this.numberOfVariables) % this.numberOfVariables;
            this.currentVertex = this.startingPoint;
            this.adjacentVertices = new ArrayList<>(this.solution.getProblem().getAdjacentVertices(this.currentVertex));
            this.adjacentVertex1 = 0;
            this.adjacentVertex2 = 0;
            this.updateState();
        }

        private void updateState() {
            if (this.adjacentVertices.size() < 2) {
                advanceCurrentVertex();
            } else if (this.adjacentVertex2 < this.adjacentVertices.size() - 1) {
                this.adjacentVertex2++;
            } else if (this.adjacentVertex1 < this.adjacentVertices.size() - 2) {
                this.adjacentVertex1++;
                this.adjacentVertex2 = this.adjacentVertex1;
                this.updateState();
            } else {
                advanceCurrentVertex();
            }
        }

        private void advanceCurrentVertex() {
            this.currentVertex = (this.currentVertex + 1) % this.numberOfVariables;
            if (this.currentVertex != this.startingPoint) {
                this.adjacentVertices = new ArrayList<>(this.solution.getProblem().getAdjacentVertices(this.currentVertex));
                // Avoid duplicated moves, since moving v0 and v1 to m1 is the same as moving v1 and v0 to m1
                this.adjacentVertices.removeIf(adjacentVertex -> adjacentVertex <= this.currentVertex);
                this.adjacentVertex1 = 0;
                this.adjacentVertex2 = 0;
                this.updateState();
            } else {
                this.currentVertex = -1;
                this.adjacentVertices = null;
            }
        }

        @Override
        public boolean hasNext() {
            return currentVertex != -1;
        }

        @Override
        public R _next() {
            int v1 = this.adjacentVertices.get(this.adjacentVertex1);
            int v2 = this.adjacentVertices.get(this.adjacentVertex2);
            logger.finest("Solution " + this.solution.hashCode() + ": " + "{v" + this.currentVertex + " (m" + this.solution.getModule(this.currentVertex) + "), " + "v" + v1 + " (m" + this.solution.getModule(v1) + "), " + "v" + v2 + " (m" + this.solution.getModule(v2) + ")} " + "-> m" + this.newModule);

            undoMove = List.of(new Pair<>(v1, this.solution.getModule(v1)), new Pair<>(v2, this.solution.getModule(v2)), new Pair<>(this.currentVertex, this.solution.getModule(this.currentVertex)));

            this.solution.moveVertex(this.currentVertex, this.newModule);
            this.solution.moveVertex(v1, this.newModule);
            this.solution.moveVertex(v2, this.newModule);

            this.updateState();

            return solution;
        }
    }

}
