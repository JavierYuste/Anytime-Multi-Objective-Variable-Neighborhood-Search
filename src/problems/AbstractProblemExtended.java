package problems;

import java.io.File;

public abstract class AbstractProblemExtended {

    protected String instanceFile;

    public String getInstanceName() {
        return this.instanceFile;
    }

    public String getInstanceShortName() {
        return this.instanceFile.substring(this.instanceFile.lastIndexOf(File.separator) + 1);
    }

    public abstract int numberOfObjectives();

    public abstract double[] getNadirPoint();

    public abstract double[] getIdealPoint();
}
