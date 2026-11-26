package algorithms.improvers.neighborhoods;

import model.SMCSolution;
import org.apache.commons.math3.util.Pair;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class SMCPInsertNeighborhood<S extends SMCSolution> implements Neighborhood<S> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    public NeighborhoodIterator<S> iterator(S solution) {
        return new InsertNeighborhoodIterator<>(solution);
    }

    private class InsertNeighborhoodIterator<R extends SMCSolution> extends SMCPAbstractNeighborhoodIterator<R> {

        int numberOfVariables;
        int startingPoint;
        int currentPoint;
        int moduleCurrentPoint;
        int targetModule;
        Iterator<Integer> connectedModules;

        public InsertNeighborhoodIterator(R solution) {
            super(solution);
            numberOfVariables = solution.getProblem().numberOfVariables();
            startingPoint = JMetalRandom.getInstance().nextInt(0, numberOfVariables - 1);
            currentPoint = startingPoint;
            moduleCurrentPoint = solution.getModule(currentPoint);
            connectedModules = new ArrayList<>(solution.getAdjacentModules(currentPoint)).iterator();
            this.updateState();
        }

        @Override
        public boolean hasNext() {
            return this.targetModule >= 0;
        }

        private void updateState() {
            if (this.connectedModules.hasNext()) {
                targetModule = connectedModules.next();
            } else {
                currentPoint = (currentPoint + 1) % this.numberOfVariables;
                if (currentPoint == startingPoint) {
                    targetModule = -1;
                } else {
                    moduleCurrentPoint = solution.getModule(currentPoint);
                    connectedModules = new ArrayList<>(solution.getAdjacentModules(currentPoint)).iterator();
                    this.updateState();
                }
            }
        }

        @Override
        public R _next() {
            logger.finer("Solution " + this.solution.hashCode() + ": Inserting vertex " + currentPoint + " into module " + targetModule);

            undoMove = List.of(new Pair<>(currentPoint, moduleCurrentPoint));

            solution.moveVertex(currentPoint, targetModule);

            this.updateState();

            return solution;
        }
    }

}
