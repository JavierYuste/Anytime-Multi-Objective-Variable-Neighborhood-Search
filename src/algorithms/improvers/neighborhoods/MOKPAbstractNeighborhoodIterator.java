package algorithms.improvers.neighborhoods;

import model.MOKPSolution;

import java.util.List;

public abstract class MOKPAbstractNeighborhoodIterator<S extends MOKPSolution> implements NeighborhoodIterator<S> {

    protected S solution;
    protected int initialHashCode;
    protected List<Integer> undoMove = null;

    public MOKPAbstractNeighborhoodIterator(S solution) {
        this.initialHashCode = solution.hashCode();
        this.solution = solution;
    }

    @Override
    public void undoLastMove(S solution) {
        if (undoMove == null) {
            throw new IllegalStateException("No move to undo");
        }
        for (Integer variableSet : undoMove) {
            solution.setVariable(variableSet);
        }
    }

    @Override
    public S next() {
        if (!this.hasNext()) {
            throw new IllegalStateException("No more moves to explore");
        }

        // The last move should be undone before calling next.
        assert (this.solution.hashCode() == this.initialHashCode) : "Solution hash code changed before calling next. " + "Caller should undo last move before continuing";

        return this._next();
    }
}
