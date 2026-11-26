package problems;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public abstract class SMCProblem extends AbstractIntegerProblemExtended {

    protected int lowerBound;
    protected int upperBound;
    protected int[][] dependencyMatrix;
    protected int sumEdgesWeights = 0;
    protected List<List<Integer>> adjacentVertices;
    protected double[] nadirPoint;
    protected double[] idealPoint;
    protected double[] difference;

    public SMCProblem(String instanceFile) {
        this.instanceFile = instanceFile;
        dependencyMatrix = this.readProblem(instanceFile);
        lowerBound = 0;
        upperBound = dependencyMatrix.length - 1;

        List<Integer> lowerLimit = new ArrayList<>(dependencyMatrix.length);
        List<Integer> upperLimit = new ArrayList<>(dependencyMatrix.length);

        for (int i = 0; i < dependencyMatrix.length; i++) {
            lowerLimit.add(lowerBound);
            upperLimit.add(upperBound);
        }

        this.adjacentVertices = new ArrayList<>(this.dependencyMatrix.length);
        for (int i = 0; i < this.dependencyMatrix.length; i++) {
            List<Integer> adjV = new LinkedList<>();
            for (int j = 0; j < this.dependencyMatrix.length; j++) {
                if (i != j && this.getConnectionValue(i, j) > 0) {
                    adjV.add(j);
                }
            }
            this.adjacentVertices.add(i, new ArrayList<>(adjV));
        }
        this.setIdealPoint();
        this.setNadirPoint();
        this.difference = new double[this.idealPoint.length];
        for (int i = 0; i < this.idealPoint.length; i++) {
            this.difference[i] = this.nadirPoint[i] - this.idealPoint[i];
        }
    }

    public abstract void setIdealPoint();

    public abstract void setNadirPoint();

    public int getConnectionValue(int v1, int v2) {
        return this.dependencyMatrix[v1][v2];
    }

    public List<Integer> getAdjacentVertices(int vertex) {
        return this.adjacentVertices.get(vertex);
    }

    private int[][] readProblem(String instanceFile) {
        int[][] dependencyMatrix = null;
        File file = new File(instanceFile);
        try {
            Scanner sizeScanner = new Scanner(file);
            String[] temp = sizeScanner.nextLine().split(" ");
            sizeScanner.close();
            int nMatrix = temp.length;

            Scanner scanner = new Scanner(file);
            dependencyMatrix = new int[nMatrix][nMatrix];
            for (int i = 0; i < nMatrix; i++) {
                String[] numbers = scanner.nextLine().split(" ");
                for (int j = 0; j < nMatrix; j++) {
                    dependencyMatrix[i][j] = Integer.parseInt(numbers[j]);
                }
            }
            scanner.close();
            // Calculate the sum of all edge weights assuming the matrix is symmetric
            for (int i = 0; i < nMatrix; i++) {
                for (int j = 0; j <= i; j++) {
                    sumEdgesWeights += dependencyMatrix[i][j];
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dependencyMatrix;
    }

    public int getSumEdgesWeights() {
        return this.sumEdgesWeights;
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
                System.out.println("Values received: " + Arrays.toString(values));
                System.out.println("Normalized values: " + Arrays.toString(normalizedValues));
                System.out.println("Ideal point: " + Arrays.toString(idealPoint));
                System.out.println("Nadir point: " + Arrays.toString(nadirPoint));
                throw new IllegalArgumentException("Normalized value out of bounds: " + normalizedValues[i]);
            }
        }

        return normalizedValues;
    }
}
