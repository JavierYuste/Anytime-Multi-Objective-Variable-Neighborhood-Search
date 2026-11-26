package utils;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.VectorUtils;

public class DominanceUtils {

    public static boolean weaklyDominates(Solution<?> solution, Solution<?> otherSolution) {
        return weaklyDominates(solution.objectives(), otherSolution.objectives());
    }

    public static boolean weaklyDominates(double[] objectives, Solution<?> otherSolution) {
        return weaklyDominates(objectives, otherSolution.objectives());
    }

    public static boolean weaklyDominates(Solution<?> solution, double[] otherObjectives) {
        return weaklyDominates(solution.objectives(), otherObjectives);
    }

    public static boolean weaklyDominates(double[] objectives, double[] otherObjectives) {
        for (int i = 0; i < objectives.length; i++)
            if (objectives[i] > otherObjectives[i]) return false; // worse on an objective so can't dominate

        return true; // not worse on any objective, so must (at least) weakly dominate
    }

    public static boolean stronglyDominates(Solution<?> solution, Solution<?> otherSolution) {
        return DominanceUtils.stronglyDominates(solution.objectives(), otherSolution.objectives());
    }

    public static boolean stronglyDominates(Solution<?> solution, double[] otherObjectives) {
        return DominanceUtils.stronglyDominates(solution.objectives(), otherObjectives);
    }

    public static boolean stronglyDominates(double[] objectives, Solution<?> otherSolution) {
        return DominanceUtils.stronglyDominates(objectives, otherSolution.objectives());
    }

    public static boolean stronglyDominates(double[] objectives, double[] otherObjectives) {
        return VectorUtils.dominanceTest(objectives, otherObjectives) < 0;
    }

}
