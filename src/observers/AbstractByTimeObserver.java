package observers;

import archive.ArchiveExtended;
import experiments.studies.UtilitiesStudies;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.Archive;
import org.uma.jmetal.util.observable.Observable;
import org.uma.jmetal.util.observer.Observer;
import termination.TerminationByTimeWithPauses;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class AbstractByTimeObserver<S extends Solution<?>> implements Observer<Map<String, Object>> {

    public static DecimalFormat df = new DecimalFormat("#.00");
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    protected final long timeLimitNanoseconds;
    protected final String baseDirectory;
    protected double currentTimeFrameNanoseconds;
    protected double i = 0.0;
    protected int runID;

    public AbstractByTimeObserver(long timeLimit) {
        this(timeLimit, UtilitiesStudies.experimentBaseDirectory, -1);
    }

    public AbstractByTimeObserver(long timeLimit, int runId) {
        this(timeLimit, UtilitiesStudies.experimentBaseDirectory, runId);
    }

    public AbstractByTimeObserver(long timeLimit, String baseDirectory, int runId) {
        this.timeLimitNanoseconds = TimeUnit.SECONDS.toNanos(timeLimit);
        this.baseDirectory = baseDirectory;
        this.runID = runId;
        this.advanceCurrentFrame();
    }

    public static <S extends Solution<?>> List<S> getCurrentArchive(Map<String, Object> data) {
        ArchiveExtended<S> archive = (ArchiveExtended<S>) data.get("ARCHIVE");
        List<S> listOfSolutions = archive.solutions();

        // Getting an archive of the same type leads to more accurate results, since some archives
        // might be bounded and the size of the archive plus the temporal archive might be greater than the bound
        Archive<S> front = archive.getEmptyArchive();
        for (S solution : listOfSolutions) {
            front.add(solution);
        }
        if (data.get("TEMP_ARCHIVE") != null) {
            Archive<S> tempArchive = (Archive<S>) data.get("TEMP_ARCHIVE");
            for (S solution : tempArchive.solutions()) {
                front.add(solution);
            }
        }
        listOfSolutions = front.solutions();
        front = null;

        return listOfSolutions;
    }

    protected void advanceCurrentFrame() {
        this.i++;
        this.currentTimeFrameNanoseconds = Math.pow(this.timeLimitNanoseconds + 1, this.i / 100) - 1.0;
    }

    protected double nextCurrentFrame() {
        return Math.pow(this.timeLimitNanoseconds + 1, (this.i + 1) / 100) - 1.0;
    }

    protected List<Double> getPossibleFrames() {
        List<Double> frames = new java.util.ArrayList<>(100);
        for (int i = 1; i <= 100; i++) {
            frames.add(Math.pow(this.timeLimitNanoseconds + 1, (double) i / 100) - 1.0);
        }
        return frames;
    }

    @Override
    public void update(Observable<Map<String, Object>> observable, Map<String, Object> data) {
        long CPUtNanoseconds = ((TerminationByTimeWithPauses) data.get("TERMINATION_CRITERION")).getElapsedTimeNanoseconds();
        if (CPUtNanoseconds >= this.currentTimeFrameNanoseconds || (boolean) data.get("ENDED")) {
            while (CPUtNanoseconds > this.nextCurrentFrame()) {
                this.advanceCurrentFrame();
            }
            this._update(observable, data);
        }
    }

    protected abstract void _update(Observable<Map<String, Object>> observable, Map<String, Object> data);
}
