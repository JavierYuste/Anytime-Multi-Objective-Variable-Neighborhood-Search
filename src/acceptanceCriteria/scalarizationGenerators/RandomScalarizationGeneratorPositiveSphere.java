package acceptanceCriteria.scalarizationGenerators;


public class RandomScalarizationGeneratorPositiveSphere extends AbstractScalarizationGeneratorPositiveSphere {

    private RandomScalarizationGenerator scalarizationGenerator;

    public RandomScalarizationGeneratorPositiveSphere(int dimensions) {
        super(dimensions);
        this.scalarizationGenerator = new RandomScalarizationGenerator(dimensions - 1);
    }

    @Override
    public double[] getNextWeights() {
        return this.transformationFangWang(this.scalarizationGenerator.getNextWeights());
    }

    @Override
    public int getDimensions() {
        return dimensions;
    }

}
