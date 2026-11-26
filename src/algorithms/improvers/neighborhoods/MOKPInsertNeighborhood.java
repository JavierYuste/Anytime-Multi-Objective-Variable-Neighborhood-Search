package algorithms.improvers.neighborhoods;

import model.MOKPSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.*;

public class MOKPInsertNeighborhood<S extends MOKPSolution> implements Neighborhood<S> {

    @Override
    public NeighborhoodIterator<S> iterator(S solution) {
        return new InsertNeighborhoodIterator<>(solution);
    }

    private class InsertNeighborhoodIterator<R extends MOKPSolution> extends MOKPAbstractNeighborhoodIterator<R> {

        List<Integer> indexesFirst;
        Iterator<Integer> iterator;
        int nextSwapFirst;

        public InsertNeighborhoodIterator(R solution) {
            super(solution);
            undoMove = null;
            indexesFirst = new ArrayList<>(solution.variables().size());
            for (int i = 0; i < solution.variables().size(); i++) {
                indexesFirst.add(i);
            }
            Collections.shuffle(indexesFirst, new Random(JMetalRandom.getInstance().nextInt(0, Integer.MAX_VALUE - 1)));
            iterator = indexesFirst.iterator();
            nextSwapFirst = iterator.next();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public R _next() {
            int next = this.iterator.next();
            this.undoMove = List.of(next);

            this.solution.setVariable(next);

            return this.solution;
        }
    }

}
