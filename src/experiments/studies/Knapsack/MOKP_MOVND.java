package experiments.studies.Knapsack;

import experiments.studies.AlgorithmsConfigurations.Configurations;
import experiments.studies.UtilitiesStudies;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static experiments.studies.UtilitiesStudies.runExperimentMOKP;

public class MOKP_MOVND {

    static long TIME_LIMIT = TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES);
    static String INSTANCES = "MOKP";
    static Set<Configurations.ObserversAvailable> selectedObservers = Set.of(
            Configurations.ObserversAvailable.ArchiveSaveByTimeObserver
    );

    /**
     * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
     * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}.
     */
    public static List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> configureAlgorithmList(
            List<ExperimentProblem<Solution<Boolean>>> problemList) {
        List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms = new ArrayList<>();
        for (int run = 0; run < UtilitiesStudies.INDEPENDENT_RUNS; run++) {
            for (var experimentProblem : problemList) {
                int solutionsToCreate = 1;
                Configurations.addMOKP_MOVND(algorithms, run, experimentProblem, TIME_LIMIT, selectedObservers, solutionsToCreate);
                Configurations.addMOKP_AnytimeMOVND(algorithms, run, experimentProblem, TIME_LIMIT, selectedObservers, solutionsToCreate);
                Configurations.addMOKP_AnytimeMOVNDs(algorithms, run, experimentProblem, TIME_LIMIT, selectedObservers, solutionsToCreate);
                Configurations.addMOKP_AnytimeMOVNDg(algorithms, run, experimentProblem, TIME_LIMIT, selectedObservers, solutionsToCreate);
            }
        }
        return algorithms;
    }

    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Locale.setDefault(Locale.US);
        if (args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            INSTANCES = args[0];
        }
        if (args.length > 1 && args[1] != null && !args[1].isEmpty()) {
            TIME_LIMIT = TimeUnit.SECONDS.convert(Integer.parseInt(args[1]), TimeUnit.MINUTES);
        }
        runExperimentMOKP(INSTANCES, selectedObservers, TIME_LIMIT,
                MOKP_MOVND.class.getMethod("configureAlgorithmList", List.class));
    }

}