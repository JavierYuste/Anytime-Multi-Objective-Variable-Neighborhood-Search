package algorithms.improvers.neighborhoods;

import model.SMCSolution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class SMCPExtractNeighborhood<S extends SMCSolution> implements Neighborhood<S> {

    @Override
    public NeighborhoodIterator<S> iterator(S solution) {
        return new ExtractNeighborhoodIterator<>(solution);
    }

    private class ExtractNeighborhoodIterator<R extends SMCSolution> extends SMCPAbstractNeighborhoodIterator<R> {

        Iterator<NeighborhoodIterator<R>> iterators;
        NeighborhoodIterator<R> extractIterator;

        public ExtractNeighborhoodIterator(R solution) {
            super(solution);
            List<NeighborhoodIterator<R>> listIterators = new ArrayList<>(2);
            listIterators.add(new SMCPExtract2Neighborhood<R>().iterator(solution));
            listIterators.add(new SMCPExtract3Neighborhood<R>().iterator(solution));
            this.iterators = listIterators.iterator();
            this.extractIterator = this.iterators.next();
        }

        @Override
        public boolean hasNext() {
            return this.extractIterator.hasNext() || this.iterators.hasNext();
        }

        @Override
        public R _next() {
            if (!this.extractIterator.hasNext()) {
                if (this.iterators.hasNext()) {
                    this.extractIterator = this.iterators.next();
                } else {
                    return null;
                }
            }
            return this.extractIterator.next();
        }

        @Override
        public void undoLastMove(R solution) {
            this.extractIterator.undoLastMove(solution);
        }
    }

}
