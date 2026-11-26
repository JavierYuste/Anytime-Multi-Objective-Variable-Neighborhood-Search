package selection;

import org.uma.jmetal.solution.Solution;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public abstract class AbstractSolutionSelection<S extends Solution<?>> implements InterfacePFSolutionSelection<S> {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    int numberOfSolutionsToSelect;
    List<S> selectedSolutions = new LinkedList<>();

    public AbstractSolutionSelection() {
        this.numberOfSolutionsToSelect = 1;
    }

    public AbstractSolutionSelection(int numberOfSolutionsToSelect) {
        this.numberOfSolutionsToSelect = numberOfSolutionsToSelect;
    }

    public S select(Iterable<S> solutions, int sizeSolutions) {
        return this.select(solutions, sizeSolutions, Collections.emptySet());
    }

    public S select(Iterable<S> solutions, int sizeSolutions, Set<S> solutionsToDiscard) {
        if (this.selectedSolutions.isEmpty()) {
            this.selectSolutions(solutions, sizeSolutions, solutionsToDiscard);
        }
        if (this.selectedSolutions.isEmpty()) {
            return null;
        } else {
            return this.selectedSolutions.remove(0);
        }
    }

    public S select(Iterable<S> solutions, int sizeSolutions, double[] vector) {
        return this.select(solutions, sizeSolutions, Collections.emptySet(), vector);
    }

    public S select(Iterable<S> solutions, int sizeSolutions, Set<S> solutionsToDiscard, double[] vector) {
        return this.select(solutions, sizeSolutions, Collections.emptySet());
    }

    abstract void selectSolutions(Iterable<S> solutions, int sizeSolutions, Set<S> solutionsToDiscard);

}
