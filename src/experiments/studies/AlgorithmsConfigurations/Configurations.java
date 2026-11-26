package experiments.studies.AlgorithmsConfigurations;

import acceptanceCriteria.LambdaAcceptanceCriterion;
import acceptanceCriteria.scalarizationGenerators.RandomScalarizationGeneratorPositiveSphere;
import algorithms.MOVNS.MOGVNS.AnytimeMOGVNS;
import algorithms.MOVNS.MOGVNS.AnytimeMOGVNSg;
import algorithms.MOVNS.MOGVNS.MOGVNS;
import algorithms.MOVNS.MOVND.AnytimeMOVND;
import algorithms.MOVNS.MOVND.AnytimeMOVNDg;
import algorithms.MOVNS.MOVND.MOVND;
import algorithms.MOVNS.Shakes.MOKPRandomShake;
import algorithms.MOVNS.Shakes.SMCRandomShake;
import algorithms.contructives.RandomMOKPSolutionsCreation;
import algorithms.contructives.RandomSMCSolutionsCreation;
import algorithms.improvers.ParetoLocalSearch.PVNDFI;
import algorithms.improvers.neighborhoods.*;
import archive.NDTree;
import model.MOKPSolution;
import model.SMCSolution;
import observers.ArchiveSaveByTimeObserver;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.observable.ObservableEntity;
import problems.MOKPProblem;
import problems.SMCProblem;
import selection.RandomSelection;
import termination.TerminationByTimeWithPauses;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Configurations {

    // *******************************************************************************
    // Experiments for the MOKP with MO-VND-based schemes

    public static void addMOKP_MOVND(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Boolean>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "MOVND";
        MOVND<MOKPSolution> algorithm = new MOVND<>();
        algorithm.set(new RandomSelection<>()).set(new MOKPInsertNeighborhood<>(), new MOKPSwapNeighborhood<>()).set(new LambdaAcceptanceCriterion(1, 0), new LambdaAcceptanceCriterion(0, 1)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((MOKPProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomMOKPSolutionsCreation<>((MOKPProblem) experimentProblem.getProblem(), solutionsToCreate));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMOKP_AnytimeMOVND(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Boolean>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOVND";
        AnytimeMOVND<MOKPSolution> algorithm = new AnytimeMOVND<>();
        algorithm.set(new RandomSelection<>()).set(new MOKPInsertNeighborhood<>(), new MOKPSwapNeighborhood<>()).set(new LambdaAcceptanceCriterion(1, 0), new LambdaAcceptanceCriterion(0, 1)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((MOKPProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomMOKPSolutionsCreation<>((MOKPProblem) experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMOKP_AnytimeMOVNDs(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Boolean>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOVND_s";
        AnytimeMOVND<MOKPSolution> algorithm = new AnytimeMOVND<>();
        algorithm.set(new RandomSelection<>()).set(new MOKPInsertNeighborhood<>(), new MOKPSwapNeighborhood<>()).set(new LambdaAcceptanceCriterion(0.5, 0.5), new LambdaAcceptanceCriterion(0.95, 0.05), new LambdaAcceptanceCriterion(0.05, 0.95)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((MOKPProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomMOKPSolutionsCreation<>((MOKPProblem) experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMOKP_AnytimeMOVNDg(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Boolean>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOVND_g";
        AnytimeMOVNDg<MOKPSolution> algorithm = new AnytimeMOVNDg<>();
        algorithm.set(new RandomScalarizationGeneratorPositiveSphere(experimentProblem.getProblem().numberOfObjectives())).set(new RandomSelection<>()).set(new MOKPInsertNeighborhood<>(), new MOKPSwapNeighborhood<>()).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((MOKPProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomMOKPSolutionsCreation<>((MOKPProblem) experimentProblem.getProblem(), solutionsToCreate));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    // *******************************************************************************

    // Experiments for the MOKP with MO-GVNS-based schemes

    public static void addMOKP_MOGVNS(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Boolean>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "MOGVNS";
        MOGVNS<MOKPSolution> algorithm = new MOGVNS<>();
        algorithm.set(new MOKPRandomShake<>(100)).set(new RandomSelection<>()).set(new MOKPInsertNeighborhood<>(), new MOKPSwapNeighborhood<>()).set(new LambdaAcceptanceCriterion(1, 0), new LambdaAcceptanceCriterion(0, 1)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((MOKPProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomMOKPSolutionsCreation<>((MOKPProblem) experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMOKP_AnytimeMOGVNS(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Boolean>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOGVNS";
        AnytimeMOGVNS<MOKPSolution> algorithm = new AnytimeMOGVNS<>();
        algorithm.set(new MOKPRandomShake<>(100)).set(new RandomSelection<>()).set(new MOKPInsertNeighborhood<>(), new MOKPSwapNeighborhood<>()).set(new LambdaAcceptanceCriterion(1, 0), new LambdaAcceptanceCriterion(0, 1)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((MOKPProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomMOKPSolutionsCreation<>((MOKPProblem) experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMOKP_AnytimeMOGVNSs(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Boolean>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOGVNS_s";
        AnytimeMOGVNS<MOKPSolution> algorithm = new AnytimeMOGVNS<>();
        algorithm.set(new MOKPRandomShake<>(100)).set(new RandomSelection<>()).set(new MOKPInsertNeighborhood<>(), new MOKPSwapNeighborhood<>()).set(new LambdaAcceptanceCriterion(0.5, 0.5), new LambdaAcceptanceCriterion(0.95, 0.05), new LambdaAcceptanceCriterion(0.05, 0.95)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((MOKPProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomMOKPSolutionsCreation<>((MOKPProblem) experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMOKP_AnytimeMOGVNSg(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Boolean>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOGVNS_g";
        AnytimeMOGVNSg<MOKPSolution> algorithm = new AnytimeMOGVNSg<>();
        algorithm.set(new RandomScalarizationGeneratorPositiveSphere(experimentProblem.getProblem().numberOfObjectives())).set(new MOKPRandomShake<>(100)).set(new RandomSelection<>()).set(new MOKPInsertNeighborhood<>(), new MOKPSwapNeighborhood<>()).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((MOKPProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomMOKPSolutionsCreation<>((MOKPProblem) experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    // *******************************************************************************

    // Experiments for the MCA problem with MO-VND-based schemes

    public static void addMCA_MOVND(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Integer>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers) {
        String name = "MOVND";
        MOVND<SMCSolution> algorithm = new MOVND<>();
        algorithm.set(new RandomSelection<>()).set(new SMCPSwapNeighborhood<>(), new SMCPInsertNeighborhood<>(), new SMCPDestroyNeighborhood<>(), new SMCPExtract2Neighborhood<>(), new SMCPExtract3Neighborhood<>()).set(new LambdaAcceptanceCriterion(1, 0, 0, 0, 0), new LambdaAcceptanceCriterion(0, 1, 0, 0, 0), new LambdaAcceptanceCriterion(0, 0, 1, 0, 0), new LambdaAcceptanceCriterion(0, 0, 0, 1, 0), new LambdaAcceptanceCriterion(0, 0, 0, 0, 1)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((SMCProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomSMCSolutionsCreation<>(experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMCA_AnytimeMOVND(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Integer>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers) {
        String name = "AnytimeMOVND";
        AnytimeMOVND<SMCSolution> algorithm = new AnytimeMOVND<>();
        algorithm.set(new RandomSelection<>()).set(new SMCPSwapNeighborhood<>(), new SMCPInsertNeighborhood<>(), new SMCPDestroyNeighborhood<>(), new SMCPExtractNeighborhood<>()).set(new LambdaAcceptanceCriterion(1, 0, 0, 0, 0), new LambdaAcceptanceCriterion(0, 1, 0, 0, 0), new LambdaAcceptanceCriterion(0, 0, 1, 0, 0), new LambdaAcceptanceCriterion(0, 0, 0, 1, 0), new LambdaAcceptanceCriterion(0, 0, 0, 0, 1)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((SMCProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomSMCSolutionsCreation<>(experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMCA_AnytimeMOVNDs(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Integer>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers) {
        String name = "AnytimeMOVND_s";
        AnytimeMOVND<SMCSolution> algorithm = new AnytimeMOVND<>();
        algorithm.set(new RandomSelection<>()).set(new SMCPSwapNeighborhood<>(), new SMCPInsertNeighborhood<>(), new SMCPDestroyNeighborhood<>(), new SMCPExtract2Neighborhood<>(), new SMCPExtract3Neighborhood<>()).set(new LambdaAcceptanceCriterion(0.2, 0.2, 0.2, 0.2, 0.2), new LambdaAcceptanceCriterion(0.95, 0.0125, 0.0125, 0.0125, 0.0125), new LambdaAcceptanceCriterion(0.0125, 0.95, 0.0125, 0.0125, 0.0125), new LambdaAcceptanceCriterion(0.0125, 0.0125, 0.95, 0.0125, 0.0125), new LambdaAcceptanceCriterion(0.0125, 0.0125, 0.0125, 0.95, 0.0125), new LambdaAcceptanceCriterion(0.0125, 0.0125, 0.0125, 0.0125, 0.95)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((SMCProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomSMCSolutionsCreation<>(experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMCA_AnytimeMOVNDg(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Integer>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers) {
        String name = "AnytimeMOVND_g";
        AnytimeMOVNDg<SMCSolution> algorithm = new AnytimeMOVNDg<>();
        algorithm.set(new RandomScalarizationGeneratorPositiveSphere(experimentProblem.getProblem().numberOfObjectives())).set(new RandomSelection<>()).set(new SMCPSwapNeighborhood<>(), new SMCPInsertNeighborhood<>(), new SMCPDestroyNeighborhood<>(), new SMCPExtract2Neighborhood<>(), new SMCPExtract3Neighborhood<>()).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((SMCProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomSMCSolutionsCreation<>(experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    // *******************************************************************************

    // Experiments for the MCA problem with MO-GVNS-based schemes

    public static void addMCA_MOGVNS(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Integer>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "MOGVNS";
        MOGVNS<SMCSolution> algorithm = new MOGVNS<>();
        algorithm.set(new SMCRandomShake<>(100)).set(new RandomSelection<>()).set(new SMCPSwapNeighborhood<>(), new SMCPInsertNeighborhood<>(), new SMCPDestroyNeighborhood<>(), new SMCPExtractNeighborhood<>()).set(new LambdaAcceptanceCriterion(1, 0, 0, 0, 0), new LambdaAcceptanceCriterion(0, 1, 0, 0, 0), new LambdaAcceptanceCriterion(0, 0, 1, 0, 0), new LambdaAcceptanceCriterion(0, 0, 0, 1, 0), new LambdaAcceptanceCriterion(0, 0, 0, 0, 1)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((SMCProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomSMCSolutionsCreation<>(experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMCA_AnytimeMOGVNS(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Integer>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOGVNS";
        AnytimeMOGVNS<SMCSolution> algorithm = new AnytimeMOGVNS<>();
        algorithm.set(new SMCRandomShake<>(100)).set(new RandomSelection<>()).set(new SMCPSwapNeighborhood<>(), new SMCPInsertNeighborhood<>(), new SMCPDestroyNeighborhood<>(), new SMCPExtractNeighborhood<>()).set(new LambdaAcceptanceCriterion(1, 0, 0, 0, 0), new LambdaAcceptanceCriterion(0, 1, 0, 0, 0), new LambdaAcceptanceCriterion(0, 0, 1, 0, 0), new LambdaAcceptanceCriterion(0, 0, 0, 1, 0), new LambdaAcceptanceCriterion(0, 0, 0, 0, 1)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((SMCProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomSMCSolutionsCreation<>(experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMCA_AnytimeMOGVNSs(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Integer>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOGVNS_s";
        AnytimeMOGVNS<SMCSolution> algorithm = new AnytimeMOGVNS<>();
        algorithm.set(new SMCRandomShake<>(100)).set(new RandomSelection<>()).set(new SMCPSwapNeighborhood<>(), new SMCPInsertNeighborhood<>(), new SMCPDestroyNeighborhood<>(), new SMCPExtractNeighborhood<>()).set(new LambdaAcceptanceCriterion(0.2, 0.2, 0.2, 0.2, 0.2), new LambdaAcceptanceCriterion(0.95, 0.0125, 0.0125, 0.0125, 0.0125), new LambdaAcceptanceCriterion(0.0125, 0.95, 0.0125, 0.0125, 0.0125), new LambdaAcceptanceCriterion(0.0125, 0.0125, 0.95, 0.0125, 0.0125), new LambdaAcceptanceCriterion(0.0125, 0.0125, 0.0125, 0.95, 0.0125), new LambdaAcceptanceCriterion(0.0125, 0.0125, 0.0125, 0.0125, 0.95)).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((SMCProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomSMCSolutionsCreation<>(experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    public static void addMCA_AnytimeMOGVNSg(List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithms, int run, ExperimentProblem<Solution<Integer>> experimentProblem, long timeLimit, Set<ObserversAvailable> selectedObservers, int solutionsToCreate) {
        String name = "AnytimeMOGVNS_g";
        AnytimeMOGVNSg<SMCSolution> algorithm = new AnytimeMOGVNSg<>();
        algorithm.set(new RandomScalarizationGeneratorPositiveSphere(experimentProblem.getProblem().numberOfObjectives())).set(new SMCRandomShake<>(100)).set(new RandomSelection<>()).set(new SMCPSwapNeighborhood<>(), new SMCPInsertNeighborhood<>(), new SMCPDestroyNeighborhood<>(), new SMCPExtractNeighborhood<>()).set(new TerminationByTimeWithPauses(timeLimit)).set(name).set((SMCProblem) experimentProblem.getProblem()).set(new NDTree<>(experimentProblem.getProblem().numberOfObjectives())).set(new PVNDFI<>()).set(new RandomSMCSolutionsCreation<>(experimentProblem.getProblem(), 1));
        registerObservers(selectedObservers, algorithm, name, timeLimit, experimentProblem.getProblem().name(), run);
        algorithms.add(new ExperimentAlgorithm<>((Algorithm) algorithm, algorithm.name(), experimentProblem, run));
    }

    // *******************************************************************************

    private static void registerObservers(Set<ObserversAvailable> selectedObservers, ObservableEntity<Map<String, Object>> algorithm, String name, long timeLimit, String problemName, int run) {
        if (!name.contains("HistoricalReferenceFront") && !name.contains("MOGVNSOriginalParetoFront")) {
            if (selectedObservers.contains(ObserversAvailable.ArchiveSaveByTimeObserver))
                algorithm.observable().register(new ArchiveSaveByTimeObserver<>(timeLimit, run));
        }
    }

    public enum ObserversAvailable {
        ArchiveSaveByTimeObserver
    }

}
