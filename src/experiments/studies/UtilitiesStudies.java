package experiments.studies;

import experiments.studies.AlgorithmsConfigurations.Configurations;
import indicators.ParetoFrontSize;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import problems.MCAProblem;
import problems.MOKPProblem;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.*;

public class UtilitiesStudies {

    public static final int INDEPENDENT_RUNS = 1;
    public static final int NUMBER_OF_CORES_OPTIMIZATION = 8;
    private static final Date date = new Date();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_H_mm_ss");
    private static final String formattedDate = sdf.format(date);
    public static final String experimentBaseDirectory = "results/Study_" + formattedDate;
    private static final String referenceFrontDirectory = "results/Study_" + formattedDate + File.separator + "referenceFronts" + File.separator;

    public static List<ExperimentProblem<Solution<Integer>>> getMCAProblemList(String instances, boolean useReferenceFronts) {
        String instancesPath = "instances" + File.separator + instances + File.separator;
        return configureProblemListMCA(instancesPath, useReferenceFronts);
    }

    public static List<ExperimentProblem<Solution<Boolean>>> getMOKPProblemList(String instances, boolean useReferenceFronts) {
        String instancesPath = "instances" + File.separator + instances + File.separator;
        return configureProblemListMOKP(instancesPath, useReferenceFronts);
    }

    public static Experiment<Solution<?>, List<Solution<?>>> getExperiment(List<ExperimentProblem<Solution<?>>> problemList, List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithmList) {
        // Quality Indicators, independent runs, and number of cores
        Experiment<Solution<?>, List<Solution<?>>> experiment = new ExperimentBuilder<>("Study").setAlgorithmList(algorithmList).setProblemList(problemList).setReferenceFrontDirectory(referenceFrontDirectory).setExperimentBaseDirectory(experimentBaseDirectory).setOutputParetoFrontFileName("FUN").setOutputParetoSetFileName("VAR").setIndicatorList(List.of(new ParetoFrontSize())).setIndependentRuns(INDEPENDENT_RUNS).setNumberOfCores(NUMBER_OF_CORES_OPTIMIZATION).build();
        return experiment;
    }

    public static void executeExperiment(List<ExperimentProblem<Solution<?>>> problemList, List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithmList) throws IOException {
        configureLogger();

        Experiment<Solution<?>, List<Solution<?>>> experiment = UtilitiesStudies.getExperiment(problemList, algorithmList);

        new ExecuteAlgorithms<>(experiment).run();
    }

    private static void configureLogger() throws IOException {
        Logger logger = Logger.getLogger("");
        File logFile = new File(experimentBaseDirectory);
        if (!logFile.exists()) {
            logFile.mkdirs();
        }
        FileHandler fileHandler = new FileHandler(experimentBaseDirectory + File.separator + "MCAStudy.log");
        fileHandler.setFormatter(new SimpleFormatter());
        fileHandler.setLevel(Level.INFO);
        logger.addHandler(fileHandler);
        logger.setLevel(Level.INFO);
        Filter filter = new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                return !record.getSourceClassName().contains(".rmi.");
            }
        };

        // Remove existing console handlers
        for (Handler h : logger.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                logger.removeHandler(h);
            }
        }

        // Create a new console handler that uses System.out instead of System.err
        ConsoleHandler consoleHandler = new ConsoleHandler() {
            {
                setOutputStream(System.out);
            }
        };
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFilter(filter);
        logger.addHandler(consoleHandler);

        // Apply filter to remaining handlers
        for (Handler h : logger.getHandlers()) {
            if (!(h instanceof ConsoleHandler)) {
                h.setFilter(filter);
            }
        }

        logger.setFilter(filter);
    }

    public static void runExperimentMOKP(String INSTANCES, Set<Configurations.ObserversAvailable> selectedObservers, long TIME_LIMIT, Method configureAlgorithmsList) throws IOException, InvocationTargetException, IllegalAccessException {
        // --- Configuration ---
        JMetalRandom.getInstance();
        // -------------------------------------------------------

        List<ExperimentProblem<Solution<Boolean>>> problemList = UtilitiesStudies.getMOKPProblemList(INSTANCES, false);

        // Algorithms
        List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithmList = (List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>>) configureAlgorithmsList.invoke(null, problemList);

        UtilitiesStudies.executeExperiment((List<ExperimentProblem<Solution<?>>>) (List<?>) problemList, algorithmList);
    }

    public static void runExperimentMCA(String INSTANCES, Set<Configurations.ObserversAvailable> selectedObservers, long TIME_LIMIT, Method configureAlgorithmsList) throws IOException, InvocationTargetException, IllegalAccessException {
        // --- Configuration ---
        JMetalRandom.getInstance();
        // -------------------------------------------------------

        List<ExperimentProblem<Solution<Integer>>> problemList = UtilitiesStudies.getMCAProblemList(INSTANCES, false);

        // Algorithms
        List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>> algorithmList = (List<ExperimentAlgorithm<Solution<?>, List<Solution<?>>>>) configureAlgorithmsList.invoke(null, problemList);

        UtilitiesStudies.executeExperiment((List<ExperimentProblem<Solution<?>>>) (List<?>) problemList, algorithmList);
    }

    private static List<ExperimentProblem<Solution<Integer>>> configureProblemListMCA(String instancesPath, boolean useReferenceFronts) {
        List<ExperimentProblem<Solution<Integer>>> problemList = new ArrayList<>();
        File dir = new File(instancesPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isFile()) {
                    String instance = child.getName();
                    ExperimentProblem<Solution<Integer>> problem = new ExperimentProblem<>(new MCAProblem(instancesPath + instance));
                    problemList.add(problem);
                }
            }
        }
        return problemList;
    }

    private static List<ExperimentProblem<Solution<Boolean>>> configureProblemListMOKP(String instancesPath, boolean useReferenceFronts) {
        List<ExperimentProblem<Solution<Boolean>>> problemList = new ArrayList<>();
        File dir = new File(instancesPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isFile()) {
                    String instance = child.getName();
                    ExperimentProblem<Solution<Boolean>> problem = new ExperimentProblem<>(new MOKPProblem(instancesPath + instance));
                    problemList.add(problem);
                }
            }
        }
        return problemList;
    }

}
