package algorithms.MOVNS.Shakes;

import model.SMCSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SMCRandomShake<S extends SMCSolution> extends Shake<S> {

    /**
     * @param maxK percentage of vertices to randomly move from the solution. Must be between 0 and 100.
     */
    public SMCRandomShake(int maxK) {
        super(maxK);
        if (maxK < 0 || maxK > 100) {
            throw new IllegalArgumentException("maxK must be between 0 and 100");
        }
    }

    /**
     * Shakes the solution by moving a percentage of vertices randomly.
     * Beware that this method modifies the solution in place.
     *
     * @param solution
     * @return solution with a percentage of items removed
     */
    @Override
    public S shake(S solution) {
        int numberOfItems = solution.variables().size();
        int itemsToRemove = Math.max(1, (int) Math.floor(numberOfItems * ((float) this.k / 100))); // Remove k% of items
        List<Integer> options = new ArrayList<>(solution.variables().size());
        for (int i = 0; i < numberOfItems; i++) {
            options.add(i);
        }
        Collections.shuffle(options, new Random(JMetalRandom.getInstance().nextInt(0, Integer.MAX_VALUE - 1)));
        for (int i = 0; i < itemsToRemove; i++) {
            int randomIndex = JMetalRandom.getInstance().nextInt(0, options.size()) % (options.size() - 1);
            int targetModule = JMetalRandom.getInstance().nextInt(0, solution.variables().size() - 1) % (solution.variables().size() - 1);
            solution.moveVertex(options.remove(randomIndex), targetModule);
        }
        return solution;
    }
}
