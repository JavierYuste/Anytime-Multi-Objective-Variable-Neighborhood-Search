package algorithms.improvers.neighborhoods;

import model.MOKPSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.*;

public class MOKPSwapNeighborhood<S extends MOKPSolution> implements Neighborhood<S> {

    @Override
    public NeighborhoodIterator<S> iterator(S solution) {
        return new SwapNeighborhoodIterator<>(solution);
    }

    private class SwapNeighborhoodIterator<R extends MOKPSolution> extends MOKPAbstractNeighborhoodIterator<R> {

        List<Integer> indexesFirst;
        List<Integer> indexesSecond;
        Iterator<Integer> swapFirst;
        Iterator<Integer> swapSecond;
        int nextSwapFirst;
        int nextSwapSecond;

        public SwapNeighborhoodIterator(R solution) {
            super(solution);
            undoMove = null;
            indexesFirst = new ArrayList<>(solution.variables().size());
            for (int i = 0; i < solution.variables().size(); i++) {
                indexesFirst.add(i);
            }
            Collections.shuffle(indexesFirst, new Random(JMetalRandom.getInstance().nextInt(0, Integer.MAX_VALUE - 1)));
            swapFirst = indexesFirst.iterator();
            nextSwapFirst = swapFirst.next();
            indexesSecond = new ArrayList<>(solution.variables().size());
            for (int i = 0; i < solution.variables().size(); i++) {
                indexesSecond.add(i);
            }
            Collections.shuffle(indexesSecond, new Random(JMetalRandom.getInstance().nextInt(0, Integer.MAX_VALUE - 1)));
            swapSecond = indexesSecond.iterator();
            updateState();
        }

        private void updateState() {
            while (swapSecond.hasNext()) {
                nextSwapSecond = swapSecond.next();
                if (nextSwapSecond > nextSwapFirst) {
                    return;
                }
            }
            if (swapFirst.hasNext()) {
                nextSwapFirst = swapFirst.next();
                swapSecond = indexesSecond.iterator();
                updateState();
            } else {
                nextSwapFirst = -1;
            }
        }

        @Override
        public boolean hasNext() {
            return this.nextSwapFirst != -1;
        }

        @Override
        public R _next() {
            this.undoMove = List.of(nextSwapFirst, nextSwapSecond);

            this.solution.setVariable(nextSwapFirst);
            this.solution.setVariable(nextSwapSecond);

            updateState();

            return this.solution;
        }
    }

}
