package acceptanceCriteria.scalarizationGenerators;


import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

public class RandomScalarizationGenerator extends AbstractScalarizationGenerator {

    private final PseudoRandomGenerator rand = JMetalRandom.getInstance().getRandomGenerator();

    public RandomScalarizationGenerator(int dimensions) {
        super(dimensions);
        this.rand.setSeed(JMetalRandom.getInstance().getSeed());
    }

    public double[] getNextWeights() {
        double[] lambda = new double[dimensions];
        for (int i = 0; i < lambda.length; i++) {
            double value = rand.nextDouble();
            lambda[i] = value;
        }
        // Create and return the acceptance criterion
        return lambda;
    }

}
