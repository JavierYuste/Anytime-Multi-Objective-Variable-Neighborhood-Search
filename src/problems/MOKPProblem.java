package problems;

import model.MOKPSolution;
import org.uma.jmetal.solution.Solution;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class MOKPProblem extends AbstractBinaryProblemExtended {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    protected List<Integer> numberOfBitsPerVariable;
    protected double[] nadirPoint;
    protected double[] idealPoint;
    protected double[] difference;
    protected double[][] rewards;
    protected double[] weights;
    protected double maxWeight;

    public MOKPProblem(String instanceFile) {
        this.instanceFile = instanceFile;
        this.readInstance(this.instanceFile);
        this.numberOfBitsPerVariable = new ArrayList<>(this.weights.length);
        for (int i = 0; i < this.weights.length; i++) {
            this.numberOfBitsPerVariable.add(1);
        }
        this.setIdealPoint();
        this.setNadirPoint();
        this.difference = new double[this.idealPoint.length];
        for (int i = 0; i < this.idealPoint.length; i++) {
            this.difference[i] = this.nadirPoint[i] - this.idealPoint[i];
        }
    }

    public double[][] getRewards() {
        return rewards;
    }

    public double[] getWeights() {
        return weights;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    private void readInstance(String instanceFile) {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(instanceFile))) {
            String line;
            int n = 0;
            List<Double> obj1 = new ArrayList<>();
            List<Double> obj2 = new ArrayList<>();
            List<Double> constraint = new ArrayList<>();
            double maxWeight = 0;
            boolean readObj1 = false, readObj2 = false, readConstraint = false;
            List<String> lastNonEmpty = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    if (line.equalsIgnoreCase("# N")) {
                        String nLine = br.readLine();
                        n = Integer.parseInt(nLine.trim());
                    } else if (line.equalsIgnoreCase("# Objectif 1")) {
                        readObj1 = true;
                        readObj2 = false;
                        readConstraint = false;
                    } else if (line.equalsIgnoreCase("# Objectif 2")) {
                        readObj1 = false;
                        readObj2 = true;
                        readConstraint = false;
                    } else if (line.equalsIgnoreCase("# Contrainte 1")) {
                        readObj1 = false;
                        readObj2 = false;
                        readConstraint = true;
                    }
                    continue;
                }
                if (readObj1) {
                    obj1.add(Double.parseDouble(line));
                } else if (readObj2) {
                    obj2.add(Double.parseDouble(line));
                } else if (readConstraint) {
                    constraint.add(Double.parseDouble(line));
                }
                if (!line.isEmpty()) lastNonEmpty.add(line);
            }
            this.rewards = new double[n][2];
            for (int i = 0; i < n; i++) {
                this.rewards[i][0] = obj1.get(i);
                this.rewards[i][1] = obj2.get(i);
            }
            this.weights = new double[n];
            for (int i = 0; i < n; i++) {
                this.weights[i] = constraint.get(i);
            }
            this.maxWeight = Double.parseDouble(lastNonEmpty.get(lastNonEmpty.size() - 1));
        } catch (Exception e) {
            logger.severe("Error leyendo la instancia: " + e.getMessage());
        }
    }

    public List<Integer> numberOfBitsPerVariable() {
        return this.numberOfBitsPerVariable;
    }

    @Override
    public int numberOfVariables() {
        return this.numberOfBitsPerVariable.size();
    }

    @Override
    public int numberOfObjectives() {
        return 2;
    }

    @Override
    public int numberOfConstraints() {
        return 1;
    }

    public String name() {
        return "MOKPProblem-" + new File(instanceFile).getName();
    }

    @Override
    public Solution<Boolean> createSolution() {
        return new MOKPSolution(this);
    }

    public Solution<Boolean> evaluate(Solution<Boolean> binarySolution) {
        double weight = 0.0;
        double rewards1 = 0;
        double rewards2 = 0;
        for (int i = 0; i < binarySolution.variables().size(); i++) {
            if (binarySolution.variables().get(i)) {
                weight += this.weights[i];
                if (weight > maxWeight) {
                    double[] objectives = binarySolution.objectives();
                    Arrays.fill(objectives, 1.0);
                    return binarySolution;
                }
                rewards1 += this.rewards[i][0];
                rewards2 += this.rewards[i][1];
            }
        }
        double[] objectives = binarySolution.objectives();
        objectives[0] = -rewards1;
        objectives[1] = -rewards2;
        return binarySolution;
    }

    public void setIdealPoint() {
        this.idealPoint = new double[2];
        int maxObj1 = 0;
        int maxObj2 = 0;
        for (int i = 0; i < this.rewards.length; i++) {
            if (this.rewards[i][0] > 0) {
                maxObj1 += this.rewards[i][0];
            }
            if (this.rewards[i][1] > 0) {
                maxObj2 += this.rewards[i][1];
            }
        }
        this.idealPoint[0] = -maxObj1;
        this.idealPoint[1] = -maxObj2;
    }

    public void setNadirPoint() {
        this.nadirPoint = new double[2];
        this.nadirPoint[0] = 0;
        this.nadirPoint[1] = 0;
    }

    public double[] getNadirPoint() {
        return this.nadirPoint;
    }

    public double[] getIdealPoint() {
        return this.idealPoint;
    }

    /**
     * Normalizes the values of a double[] variable in the range [0,1]
     * considering nadirPoint (maximum values) and idealPoint (minimum values)
     *
     * @param values The array of values to be normalized
     * @return A new array with normalized values in the range [0,1]
     */
    public double[] normalize(double[] values) {
        if (values == null || values.length != this.numberOfObjectives()) {
            throw new IllegalArgumentException("The values array must have the same length as the number of objectives");
        }

        double[] normalizedValues = new double[values.length];

        for (int i = 0; i < values.length; i++) {
            // Normalize using the formula: (value - idealPoint) / (nadirPoint - idealPoint)
            normalizedValues[i] = (values[i] - idealPoint[i]) / (difference[i]);

            // Ensure the normalized value is within the range [0, 1]
            if (normalizedValues[i] < 0.0 || normalizedValues[i] > 1.0) {
                throw new IllegalArgumentException("Normalized value out of bounds: " + normalizedValues[i]);
            }
        }

        return normalizedValues;
    }
}
