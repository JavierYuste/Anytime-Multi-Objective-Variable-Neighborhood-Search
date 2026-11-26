package acceptanceCriteria.scalarizationGenerators;

public abstract class AbstractScalarizationGeneratorPositiveSphere extends AbstractScalarizationGenerator {

    private static final double PI_2 = Math.PI / 2;

    public AbstractScalarizationGeneratorPositiveSphere(int dimensions) {
        super(dimensions);
    }

    /**
     * Applies the Fang-Wang Efficient Mapping Plus to an input array u and stores the result in w.
     * Only the positive orthant is sampled.
     */
    protected double[] transformationFangWang(double[] weights) {
        double u_times_pi2, product = 1.0;
        int m = this.dimensions / 2;
        // Initialize the output array w
        double[] w = new double[dimensions];

        for (int i = 0; i < m - 1; i++) {
            int l = dimensions - 2 * i - 2;
            double tmp = Math.pow(weights[l - 1], 1.0 / l);
            double dl = Math.sqrt(1.0 - tmp * tmp) * product;
            product *= tmp;
            u_times_pi2 = PI_2 * weights[l];
            w[2 * i] = dl * Math.sin(u_times_pi2);
            w[2 * i + 1] = dl * Math.cos(u_times_pi2);
        }

        if (dimensions % 2 == 0) { // dim is even
            u_times_pi2 = PI_2 * weights[0];
        } else { // dim is odd
            w[dimensions - 3] = product * weights[0];
            product *= Math.sqrt(1.0 - weights[0] * weights[0]);
            u_times_pi2 = PI_2 * weights[1];
        }
        w[dimensions - 2] = product * Math.sin(u_times_pi2);
        w[dimensions - 1] = product * Math.cos(u_times_pi2);
        return w;
    }
}
