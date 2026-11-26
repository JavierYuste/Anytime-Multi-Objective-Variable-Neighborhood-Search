package acceptanceCriteria;

import model.ExtendedSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.Archive;

import java.util.Arrays;

public class LambdaAcceptanceCriterion implements AcceptanceCriterion {

    private final double[] lambdaVector;
    private final String name;

    public LambdaAcceptanceCriterion(double... lambdaValues) {
        double sum = 0.0;
        for (double lambda : lambdaValues) {
            if (lambda < 0.0 || lambda > 1.0) {
                throw new IllegalArgumentException("The lambda values must be in the range [0.0, 1.0]");
            }
            sum += lambda;
        }
        if (Math.abs(sum - 1.0) > 0.0001) {
            throw new IllegalArgumentException("The sum of the lambda values must be 1.0");
        }
        this.lambdaVector = lambdaValues;
        this.name = "LambdaAC(" +
                Arrays.toString(lambdaVector) +
                ')';
    }

    @Override
    public <S extends ExtendedSolution<?>> boolean accept(S solution, S previousSolution, Archive<S> archive) {
        double previousScalar = this.scalar(previousSolution.objectivesNormalized());
        double currentScalar = this.scalar(solution.objectivesNormalized());
        return currentScalar < previousScalar;
    }

    public double scalar(double[] objectives) {
        if (objectives.length != lambdaVector.length) {
            throw new IllegalArgumentException("The number of objectives must be equal to the number of lambda values");
        }
        double scalar = 0.0;
        for (int i = 0; i < objectives.length; i++) {
            scalar += objectives[i] * lambdaVector[i];
        }
        return scalar;
    }

    @Override
    public String getShortName() {
        StringBuilder sb = new StringBuilder("lambda{");
        for (int i = 0; i < lambdaVector.length; i++) {
            sb.append(lambdaVector[i]);
            if (i < lambdaVector.length - 1) {
                sb.append(", ");
            }
        }
        return sb.append("}").toString();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(lambdaVector);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public double[] getLambdaVector() {
        return lambdaVector;
    }
}
