package acceptanceCriteria.scalarizationGenerators;

import acceptanceCriteria.LambdaAcceptanceCriterion;

public abstract class AbstractScalarizationGenerator implements ScalarizationGenerator {

    protected int dimensions;

    public AbstractScalarizationGenerator(int dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public int getDimensions() {
        return dimensions;
    }

    public double[] normalizeWeights(double[] weights) {
        double[] normalizedWeights = new double[weights.length];
        double sum = 0.0;
        for (double weight : weights) {
            sum += weight;
        }
        for (int i = 0; i < weights.length; i++) {
            normalizedWeights[i] = weights[i] / sum;
        }
        return normalizedWeights;
    }

    @Override
    public LambdaAcceptanceCriterion getScalarization() {
        return new LambdaAcceptanceCriterion(this.normalizeWeights(this.getNextWeights()));
    }
}
