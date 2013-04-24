package org.bireme.dia.analysis;

import java.io.IOException;

public interface SynonymEngine {
  String[] getSynonyms(String s) throws IOException;  
}
