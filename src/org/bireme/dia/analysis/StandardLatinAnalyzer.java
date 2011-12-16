/*
 * BVSAnalyzer.java
 *
 * Created on May 18, 2006, 12:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bireme.dia.analysis;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;

import java.io.Reader;
import java.util.Set;


public class StandardLatinAnalyzer extends Analyzer {
  private Set stopSet;

  /** An array containing some common English words that are usually not
  useful for searching. */
  public static final String[] STOP_WORDS = StopAnalyzer.ENGLISH_STOP_WORDS;

  /** Builds an analyzer. */
  public StandardLatinAnalyzer() {
    this(STOP_WORDS);
  }

  /** Builds an analyzer with the given stop words. */
  public StandardLatinAnalyzer(String[] stopWords) {
    stopSet = StopFilter.makeStopSet(stopWords);
  }

  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new StandardTokenizer(reader);
    result = new StandardFilter(result);
    result = new LowerCaseFilter(result);
    result = new StopFilter(result, stopSet);
    result = new ISOLatin1AccentFilter(result);
    return result;
  }
}

