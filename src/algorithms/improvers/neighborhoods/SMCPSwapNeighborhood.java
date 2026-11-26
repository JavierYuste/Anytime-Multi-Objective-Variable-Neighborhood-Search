package algorithms.improvers.neighborhoods;

import model.SMCSolution;
import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SMCPSwapNeighborhood<S extends SMCSolution> implements Neighborhood<S> {

    @Override
    public NeighborhoodIterator<S> iterator(S solution) {
        return new SwapNeighborhoodIterator<>(solution);
    }

    private class SwapNeighborhoodIterator<R extends SMCSolution> extends SMCPAbstractNeighborhoodIterator<R> {

        int numberOfVariables;
        int startingPoint;
        int currentPoint;
        int moduleCurrentPoint;
        int nextVertex;
        Iterator<Integer> connectedModules;
        Iterator<Integer> connectedVerticesInCurrentModule;

        public SwapNeighborhoodIterator(R solution) {
            super(solution);
            undoMove = null;
            numberOfVariables = solution.getProblem().numberOfVariables();
            startingPoint = JMetalRandom.getInstance().nextInt(0, numberOfVariables - 1);
            currentPoint = startingPoint;
            moduleCurrentPoint = solution.getModule(currentPoint);
            connectedModules = new ArrayList<>(solution.getAdjacentModules(currentPoint)).iterator();
            connectedVerticesInCurrentModule = new ArrayList<>(solution.getVerticesInModule(moduleCurrentPoint)).iterator();
            updateState();
        }

        private void updateState() {
            try {
                if (connectedVerticesInCurrentModule.hasNext()) {
                    nextVertex = connectedVerticesInCurrentModule.next();
                } else if (connectedModules.hasNext()) {
                    int nextModule = connectedModules.next();
                    connectedVerticesInCurrentModule = new ArrayList<>(solution.getVerticesInModule(nextModule)).iterator();
                    updateState();
                } else {
                    currentPoint = (currentPoint + 1) % numberOfVariables;
                    if (currentPoint == startingPoint) {
                        nextVertex = -1;
                    } else {
                        moduleCurrentPoint = solution.getModule(currentPoint);
                        connectedModules = new ArrayList<>(solution.getAdjacentModules(currentPoint)).iterator();
                        connectedVerticesInCurrentModule = new ArrayList<>(solution.getVerticesInModule(moduleCurrentPoint)).iterator();
                        updateState();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean hasNext() {
            return this.nextVertex != -1;
        }

        @Override
        public R _next() {
            this.undoMove = List.of(new Pair<>(this.currentPoint, this.solution.getModule(this.currentPoint)), new Pair<>(this.nextVertex, this.solution.getModule(this.nextVertex)));

            this.solution.moveVertex(this.currentPoint, this.solution.getModule(this.nextVertex));
            this.solution.moveVertex(this.nextVertex, this.moduleCurrentPoint);

            updateState();

            return this.solution;
        }
    }

}
