package selection;

import com.google.common.collect.Iterators;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class RandomSelection<S extends Solution<?>> extends AbstractSolutionSelection<S> {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private PseudoRandomGenerator random = JMetalRandom.getInstance().getRandomGenerator();

    public RandomSelection() {
        super();
    }

    public RandomSelection(PseudoRandomGenerator random) {
        super();
        this.random = random;
    }

    public RandomSelection(int numberOfSolutions) {
        super(numberOfSolutions);
    }

    public RandomSelection(int numberOfSolutions, PseudoRandomGenerator random) {
        super(numberOfSolutions);
        this.random = random;
    }

    @Override
    void selectSolutions(Iterable<S> solutions, int sizeSolutions, Set<S> solutionsToDiscard) {
        if (solutions instanceof List<S>) {
            for (int i = 0; i < this.numberOfSolutionsToSelect; i++) {
                int randomIndex = this.random.nextInt(0, sizeSolutions - 1);
                for (int offset = 0; offset < ((List<S>) solutions).size(); offset++) {
                    int index = (randomIndex + offset) % ((List<S>) solutions).size();
                    S solution = ((List<S>) solutions).get(index);
                    if (solutionsToDiscard == null || !solutionsToDiscard.contains(solution)) {
                        this.selectedSolutions.add(solution);
                        break;
                    }
                }
            }
        } else {
            Iterator<S> iterator = Iterators.cycle(solutions);
            for (int i = 0; i < this.numberOfSolutionsToSelect; i++) {
                int randomIndex = this.random.nextInt(0, sizeSolutions - 1);
                logger.finest("Random index: " + randomIndex);
                for (int j = 0; j < randomIndex; j++) {
                    iterator.next();
                }
                int count = 0;
                while (iterator.hasNext() && count < sizeSolutions) {
                    if (randomIndex + count == sizeSolutions) {
                        logger.finest("Cycling");
                    }
                    S solution = iterator.next();
                    if (!solutionsToDiscard.contains(solution)) {
                        this.selectedSolutions.add(solution);
                        break;
                    }
                    count++;
                }
            }
        }
    }
}
