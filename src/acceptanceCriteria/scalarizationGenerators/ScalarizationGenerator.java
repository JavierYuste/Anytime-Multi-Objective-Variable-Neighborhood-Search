package acceptanceCriteria.scalarizationGenerators;

import acceptanceCriteria.LambdaAcceptanceCriterion;

public interface ScalarizationGenerator {
    LambdaAcceptanceCriterion getScalarization();
    double[] getNextWeights();
    int getDimensions();
}
