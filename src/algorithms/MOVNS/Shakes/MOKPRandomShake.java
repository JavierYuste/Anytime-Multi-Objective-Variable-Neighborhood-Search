package algorithms.MOVNS.Shakes;

import model.MOKPSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.List;

public class MOKPRandomShake<S extends MOKPSolution> extends Shake<S> {

    /**
     * @param maxK percentage of items to remove from the solution. Must be between 0 and 100.
     */
    public MOKPRandomShake(int maxK) {
        super(maxK);
        if (maxK < 0 || maxK > 100) {
            throw new IllegalArgumentException("maxK must be between 0 and 100");
        }
    }

    /**
     * Shakes the solution by removing a percentage of items randomly.
     * Beware that this method modifies the solution in place.
     *
     * @param solution
     * @return solution with a percentage of items removed
     */
    @Override
    public S shake(S solution) {
        int numberOfItems = solution.getNumberOfObjectsSelected();
        int itemsToRemove = Math.max(1, (int) Math.floor(numberOfItems * ((float) this.k / 100))); // Remove k% of items
        for (int i = 0; i < itemsToRemove; i++) {
            List<Boolean> variables = solution.variables();
            int randomIndex = JMetalRandom.getInstance().nextInt(0, variables.size()) % (variables.size() - 1);
            while (!variables.get(randomIndex)) {
                randomIndex = (randomIndex + 1) % (variables.size() - 1);
            }
            solution.setVariable(randomIndex);
        }
        return solution;
    }
}
