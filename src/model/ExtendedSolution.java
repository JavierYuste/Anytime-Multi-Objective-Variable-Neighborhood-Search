package model;

public interface ExtendedSolution<E> extends org.uma.jmetal.solution.Solution<E> {

    double[] objectivesNormalized();

}
