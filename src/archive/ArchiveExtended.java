package archive;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.Archive;

public interface ArchiveExtended<S extends Solution<?>> extends Archive<S>, Iterable<S> {

    ArchiveExtended<S> getEmptyArchive();

}
