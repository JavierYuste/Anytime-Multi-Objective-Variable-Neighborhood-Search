package algorithms.contructives;

import org.uma.jmetal.solution.Solution;

import java.util.List;

public interface SolutionsCreation<S extends Solution<?>> {
    List<S> create();
}
