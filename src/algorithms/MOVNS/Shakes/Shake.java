package algorithms.MOVNS.Shakes;

import algorithms.MOVNS.MOVNS;
import org.uma.jmetal.solution.Solution;

import java.util.logging.Logger;

public abstract class Shake<S extends Solution<?>> {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    protected int initialK = 1;
    protected int maxK;
    protected int stepsK = 1;
    protected int k = initialK;
    protected int counter = 0;
    protected boolean reachedMaxK = false;

    public Shake(int maxK) {
        this.maxK = maxK;
    }

    public boolean reachedMaxK() {
        return reachedMaxK;
    }

    public void updateK(boolean improved) {
        this.logger.fine("Updating K (improved=" + improved + "). Current value: " + k + ". Initial value: " + initialK + ". Max value: " + maxK + ". Steps: " + stepsK);
        counter = MOVNS.generalChange(improved, counter);
        this.k = this.initialK + (this.counter * this.stepsK);
        this.k = Math.min(this.k, this.maxK);
        this.reachedMaxK = this.k == this.maxK;
        this.logger.fine("Updated K. Current value: " + k + ". Initial value: " + initialK + ". Max value: " + maxK + ". Steps: " + stepsK);
    }

    public void restartK() {
        this.logger.fine("Restarting K. Current value: " + k + ". Initial value: " + initialK + ". Max value: " + maxK + ". Steps: " + stepsK);
        this.k = this.initialK;
        this.reachedMaxK = false;
        this.logger.fine("Restarted K. Current value: " + k + ". Initial value: " + initialK + ". Max value: " + maxK + ". Steps: " + stepsK);
    }

    public abstract S shake(S solution);

}
