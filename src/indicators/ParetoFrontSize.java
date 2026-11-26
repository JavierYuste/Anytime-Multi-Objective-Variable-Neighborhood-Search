package indicators;

import org.uma.jmetal.qualityindicator.QualityIndicator;

import java.io.FileNotFoundException;

public class ParetoFrontSize extends QualityIndicator {
    public ParetoFrontSize() {
    }

    /**
     * Constructor
     *
     * @param referenceFront
     * @throws FileNotFoundException
     */
    public ParetoFrontSize(double[][] referenceFront) {
        super(referenceFront);
    }

    /**
     * Evaluate() method
     *
     * @param front
     * @return
     */
    @Override
    public double compute(double[][] front) {
        return front.length;
    }

    @Override
    public String name() {
        return "PFS";
    }

    @Override
    public String description() {
        return "Pareto Front Size";
    }

    @Override
    public boolean isTheLowerTheIndicatorValueTheBetter() {
        return false;
    }
}
