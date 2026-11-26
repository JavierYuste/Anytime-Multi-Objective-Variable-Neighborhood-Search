package termination;

import org.uma.jmetal.component.catalogue.common.termination.Termination;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TerminationByTimeWithPauses implements Termination {

    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    protected final long timeLimitNanoseconds;
    protected double currentLimit;
    protected double i = 0.0;
    private Duration elapsedTimeNanoseconds;
    private Duration elapsedTimeDuringPauseNanoseconds;
    private Instant lastStart;
    private Instant lastPause;

    public TerminationByTimeWithPauses(long maxComputingTime, TimeUnit timeUnit) {
        this.timeLimitNanoseconds = TimeUnit.NANOSECONDS.convert(maxComputingTime, timeUnit);
        this.elapsedTimeNanoseconds = Duration.ZERO;
        this.elapsedTimeDuringPauseNanoseconds = Duration.ZERO;
        this.lastStart = null;
        this.lastPause = null;
    }

    public TerminationByTimeWithPauses(long maxComputingTime) {
        this(maxComputingTime, TimeUnit.SECONDS);
    }


    protected void advanceTimeFrame() {
        this.i++;
        this.currentLimit = Math.pow(this.timeLimitNanoseconds + 1, this.i / 100) - 1.0;
    }

    /**
     * Starts the timer. If the timer is already running, it will be reset.
     */
    public void start() {
        elapsedTimeNanoseconds = Duration.ZERO;
        elapsedTimeDuringPauseNanoseconds = Duration.ZERO;
        lastPause = null;
        resume();
    }

    /**
     * Pauses the timer. If the timer is already paused, this method has no effect.
     */
    public void pause() {
        if (lastStart != null) {
            elapsedTimeNanoseconds = elapsedTimeNanoseconds.plus(Duration.between(lastStart, Instant.now()));
        }
        lastStart = null;
        lastPause = Instant.now();
    }

    /**
     * Stops the timer. If the timer is already stopped, this method has no effect.
     */
    public void stop() {
        this.pause();
    }

    /**
     * Resumes the timer after it has been paused. If the timer is already running, this method has no effect.
     */
    public void resume() {
        while (this.currentLimit < this.elapsedTimeNanoseconds.toNanos()) {
            this.advanceTimeFrame();
        }
        if (lastPause != null) {
            elapsedTimeDuringPauseNanoseconds = elapsedTimeDuringPauseNanoseconds.plus(Duration.between(lastPause, Instant.now()));
        }
        lastPause = null;
        lastStart = Instant.now();
    }

    public boolean beep() {
        return getElapsedTimeNanoseconds() >= this.currentLimit || getElapsedTimeNanoseconds() >= this.timeLimitNanoseconds;
    }

    /**
     * Returns the total elapsed time in nanoseconds, including time when the timer was running.
     * If the timer is currently running, it includes the time up to the current moment.
     *
     * @return the total elapsed time in nanoseconds
     */
    public long getElapsedTimeNanoseconds() {
        if (lastStart != null) {
            return elapsedTimeNanoseconds.plus(Duration.between(lastStart, Instant.now())).toNanos();
        } else {
            return elapsedTimeNanoseconds.toNanos();
        }
    }

    /**
     * Returns the total elapsed time in milliseconds, including time when the timer was running.
     * If the timer is currently running, it includes the time up to the current moment.
     *
     * @return the total elapsed time in milliseconds
     */
    public long getElapsedTimeMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(getElapsedTimeNanoseconds(), TimeUnit.NANOSECONDS);
    }

    /**
     * Returns the total elapsed time in seconds, including time when the timer was running.
     * If the timer is currently running, it includes the time up to the current moment.
     *
     * @return the total elapsed time in seconds
     */
    public long getElapsedTimeSeconds() {
        return TimeUnit.SECONDS.convert(getElapsedTimeNanoseconds(), TimeUnit.NANOSECONDS);
    }

    /**
     * Returns the elapsed time during pauses in nanoseconds.
     *
     * @return the elapsed time during pauses in nanoseconds
     */
    public long getElapsedTimeDuringPauseNanoseconds() {
        if (lastPause != null) {
            return elapsedTimeDuringPauseNanoseconds.plus(Duration.between(lastPause, Instant.now())).toNanos();
        } else {
            return elapsedTimeDuringPauseNanoseconds.toNanos();
        }
    }

    /**
     * Returns the elapsed time during pauses in milliseconds.
     *
     * @return the elapsed time during pauses in milliseconds
     */
    public long getElapsedTimeDuringPauseMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(getElapsedTimeDuringPauseNanoseconds(), TimeUnit.NANOSECONDS);
    }

    /**
     * Returns the elapsed time during pauses in seconds.
     *
     * @return the elapsed time during pauses in seconds
     */
    public long getElapsedTimeDuringPauseSeconds() {
        return TimeUnit.SECONDS.convert(getElapsedTimeDuringPauseNanoseconds(), TimeUnit.NANOSECONDS);
    }

    @Override
    public boolean isMet(Map<String, Object> algorithmStatusData) {
        boolean met = this.getElapsedTimeNanoseconds() >= this.timeLimitNanoseconds;
        if (met) {
            logger.info("Termination criteria " + this.getClass().getSimpleName() + " has been met");
        }
        return met;
    }

    public long getMaxComputingTimeSeconds() {
        return TimeUnit.NANOSECONDS.toSeconds(this.timeLimitNanoseconds);
    }
}
