package experiments.studies.SoftwareModuleClustering;

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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static experiments.studies.UtilitiesStudies.runExperimentMCA;

public class MCA_MOVND_Study {

    static long TIME_LIMIT = TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES);
    static String INSTANCES = "MCA";
    static Set<Configurations.ObserversAvailable> selectedObservers = Set.of(
            Configurations.ObserversAvailable.ArchiveSaveByTimeObserver
    );


    /**
     * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
     * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}.
     */
    public static List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> configureAlgorithmList(List<ExperimentProblem<Solution<Integer>>> problemList) {
        List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms = new ArrayList<>();
        for (int run = 0; run < UtilitiesStudies.INDEPENDENT_RUNS; run++) {
            for (var experimentProblem : problemList) {
                Configurations.addMCA_MOVND(algorithms, run, experimentProblem, TIME_LIMIT, selectedObservers);
                Configurations.addMCA_AnytimeMOVND(algorithms, run, experimentProblem, TIME_LIMIT, selectedObservers);
                Configurations.addMCA_AnytimeMOVNDs(algorithms, run, experimentProblem, TIME_LIMIT, selectedObservers);
                Configurations.addMCA_AnytimeMOVNDg(algorithms, run, experimentProblem, TIME_LIMIT, selectedObservers);
            }
        }
        return algorithms;
    }

    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            INSTANCES = args[0];
        }
        if (args.length > 1 && args[1] != null && !args[1].isEmpty()) {
            TIME_LIMIT = TimeUnit.SECONDS.convert(Integer.parseInt(args[1]), TimeUnit.MINUTES);
        }
        runExperimentMCA(INSTANCES, selectedObservers, TIME_LIMIT, MCA_MOVND_Study.class.getMethod("configureAlgorithmList", List.class));
    }

}