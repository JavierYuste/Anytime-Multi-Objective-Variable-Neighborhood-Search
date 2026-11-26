package acceptanceCriteria;

import model.ExtendedSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.Archive;

public interface AcceptanceCriterion {
    <S extends ExtendedSolution<?>> boolean accept(S solution, S previousSolution, Archive<S> archive);
    String getShortName();
}
