package org.bireme.dia.tests;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.bireme.dia.analysis.DeCSEngine;
import org.bireme.dia.analysis.PunctuationFilter;
import org.bireme.dia.analysis.SynonymEngine;
import org.bireme.dia.analysis.SynonymFilter;
import org.bireme.dia.util.AnalyzerUtils;

/**
 *
 * @author Heitor Barbieri
 * date: 20141017
 */
public class TestTokenStream {
    public static void testKeywordTokenizer(final String in) 
                                                            throws IOException {
        if (in == null) {
            throw new NullPointerException("in");
        }
        final StringReader reader = new StringReader(in);
        final TokenStream stream = new KeywordTokenizer(reader);
        
        stream.reset();
        
        System.out.println("=======" + "KeywordTokenizer" + "=======");
        AnalyzerUtils.displayTokens(stream);
        System.out.println();
        
        stream.close();
    }
    
    public static void testPunctuationFilter(final String in) 
                                                            throws IOException {
        if (in == null) {
            throw new NullPointerException("in");
        }
        final StringReader reader = new StringReader(in);
        final TokenStream stream = new KeywordTokenizer(reader);
        final TokenStream filter = new PunctuationFilter(stream);
        
        stream.reset();
        
        System.out.println("=======" + "PunctuationFilter" + "=======");
        AnalyzerUtils.displayTokens(filter);
        System.out.println();
        
        stream.close();
    }
    
    public static void testSynonymFilter(final String in) throws IOException {
        if (in == null) {
            throw new NullPointerException("in");
        }
        final StringReader reader = new StringReader(in);
        final String path = "resources/decs/main";
        final SynonymEngine engine = new DeCSEngine(path, true, true, true, false);        
        //final TokenStream stream = new KeywordTokenizer(reader);
        final TokenStream stream = new WhitespaceTokenizer(reader);
        //final TokenStream filter2 = new LowerCaseFilter(stream);
        final TokenStream filter = new SynonymFilter(stream, engine, true, false);
        
        stream.reset();
        
        System.out.println("=======" + "SynonymFilter" + "=======");
        AnalyzerUtils.displayTokens(filter);
        System.out.println();
        
        stream.close();
    }
        
    public static void main(final String[] args) throws IOException {
        final String in = "^d3727"; //"Os Abatedouros Abattoirs locais onde se sacrificam animais, são fétidos!";
        
        testKeywordTokenizer(in);
        testPunctuationFilter(in);
        testSynonymFilter(in);
    }
}
