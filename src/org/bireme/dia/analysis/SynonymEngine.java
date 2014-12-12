package org.bireme.dia.analysis;

import java.io.IOException;
import java.util.Set;

public interface SynonymEngine {
    Set<String> getSynonyms(String s) throws IOException;
}
