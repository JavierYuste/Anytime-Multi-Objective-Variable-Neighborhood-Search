package observers;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.observable.Observable;
import termination.TerminationByTimeWithPauses;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ArchiveSaveByTimeObserver<S extends Solution<?>> extends AbstractByTimeObserver<S> {

    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    public ArchiveSaveByTimeObserver(long timeLimit, int runId) {
        super(timeLimit, runId);
    }

    public ArchiveSaveByTimeObserver(long timeLimit, String baseDirectory, int runId) {
        super(timeLimit, baseDirectory, runId);
    }

    @Override
    protected void _update(Observable<Map<String, Object>> observable, Map<String, Object> data) {
        long CPUtNanoseconds = ((TerminationByTimeWithPauses) data.get("TERMINATION_CRITERION")).getElapsedTimeNanoseconds();
        long CPUtNanosecondsDuringPauses = ((TerminationByTimeWithPauses) data.get("TERMINATION_CRITERION")).getElapsedTimeDuringPauseNanoseconds();
        double realCPUtNanoseconds = Duration.between((Instant) data.get("START_INSTANT"), Instant.now()).toNanos() / (double) TimeUnit.SECONDS.toNanos(1);
        Instant start = Instant.now();
        List<S> listOfSolutions = getCurrentArchive(data);

        logger.info("Saving archive (Method: " + data.get("NAME") + "; Instance: " + data.get("INSTANCE") + "; " + "CPUt (s): " + df.format(CPUtNanoseconds / (double) TimeUnit.SECONDS.toNanos(1)).replace(',', '.') + "; " + "PFS=" + listOfSolutions.size() + "; Real CPUt(s): " + df.format(realCPUtNanoseconds).replace(',', '.') + "; Frame (s): " + df.format(this.currentTimeFrameNanoseconds / (double) TimeUnit.SECONDS.toNanos(1)).replace(',', '.') + "; " + "CPUt (s) during pauses: " + df.format(CPUtNanosecondsDuringPauses / (double) TimeUnit.SECONDS.toNanos(1)).replace(',', '.') + "; " + "Run: " + this.runID + ")");

        try {
            File output = new File(baseDirectory + File.separator + "progressOverTime" + File.separator + data.get("NAME") + File.separator + "FUN" + this.runID + "-" + data.get("NAME") + "-" + new File(String.valueOf(data.get("INSTANCE"))).getName() + "-" + df.format(this.currentTimeFrameNanoseconds / (double) TimeUnit.SECONDS.toNanos(1)).replace(',', '.') + ".csv");
            if (!output.getParentFile().exists()) {
                output.getParentFile().mkdirs();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));
            for (S solution : listOfSolutions) {
                for (int j = 0; j < solution.objectives().length - 1; j++) {
                    bufferedWriter.write(solution.objectives()[j] + ",");
                }
                bufferedWriter.write("" + solution.objectives()[solution.objectives().length - 1]);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            listOfSolutions = null;
            this.advanceCurrentFrame();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if ((boolean) data.get("ENDED")) {
            try {
                File output = new File(baseDirectory + File.separator + "Study" + File.separator + "CPUt.csv");
                if (!output.getParentFile().exists()) {
                    output.getParentFile().mkdirs();
                }
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output, true));
                bufferedWriter.write(data.get("NAME") + "," + this.runID + "," + data.get("INSTANCE") + ",CPUt (s),0," + CPUtNanoseconds / (double) TimeUnit.SECONDS.toNanos(1) + "\n");
                bufferedWriter.write(data.get("NAME") + "," + this.runID + "," + data.get("INSTANCE") + ",CPUt (ms),0," + CPUtNanoseconds + "\n");
                bufferedWriter.write(data.get("NAME") + "," + this.runID + "," + data.get("INSTANCE") + ",CPUt (minutes),0," + CPUtNanoseconds / (double) TimeUnit.MINUTES.toNanos(1) + "\n");
                bufferedWriter.write(data.get("NAME") + "," + this.runID + "," + data.get("INSTANCE") + ",CPUt (hours),0," + CPUtNanoseconds / (double) TimeUnit.HOURS.toNanos(1) + "\n");
                bufferedWriter.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Duration duration = Duration.between(start, Instant.now());
        logger.fine("Saving archive took " + df.format(duration.toNanos() / (double) TimeUnit.SECONDS.toNanos(1)).replace(',', '.') + " s");
    }


}
