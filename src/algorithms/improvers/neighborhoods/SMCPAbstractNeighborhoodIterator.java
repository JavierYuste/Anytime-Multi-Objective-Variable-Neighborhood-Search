package algorithms.improvers.neighborhoods;

import model.SMCSolution;
import org.apache.commons.math3.util.Pair;

import java.util.List;

public abstract class SMCPAbstractNeighborhoodIterator<S extends SMCSolution> implements NeighborhoodIterator<S> {

    protected S solution;
    protected int initialHashCode;
    protected List<Pair<Integer, Integer>> undoMove = null;

    public SMCPAbstractNeighborhoodIterator(S solution) {
        this.initialHashCode = solution.hashCode();
        this.solution = solution;
    }

    @Override
    public void undoLastMove(S solution) {
        if (undoMove == null) {
            throw new IllegalStateException("No move to undo");
        }
        for (Pair<Integer, Integer> pair : undoMove) {
            int variable = pair.getFirst();
            int value = pair.getSecond();
            solution.moveVertex(variable, value);
        }
    }

    @Override
    public S next() {
        if (!this.hasNext()) {
            throw new IllegalStateException("No more moves to explore");
        }

        // The last move should be undone before calling next.
        assert (this.solution.hashCode() == this.initialHashCode) : "Solution hash code changed before calling next. " + "Calling code should undo last move before continuing";

        return this._next();
    }
}
