package org.bireme.dia.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.BytesRef;

/**
 * Utils for displaying the results of the Lucene analysis process
 */
public class AnalyzerUtils {
  
    public static void displayTokens(Analyzer analyzer, String text)
                                                            throws IOException {
        displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
    }
  
    public static void displayTokens(TokenStream stream) throws IOException {
    
        CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            System.out.println("[" + term.toString() + "] ");
        }    
    }
  
    public static void displayTokensWithPositions(Analyzer analyzer, String text)
                                                            throws IOException {    
        TokenStream stream = analyzer.tokenStream("contents",
                                                  new StringReader(text));
    
        CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
        PositionIncrementAttribute posIncr = stream
                                .addAttribute(PositionIncrementAttribute.class);
    
        int position = 0;
        stream.reset();
        while (stream.incrementToken()) {     
            int increment = posIncr.getPositionIncrement();
            if (increment > 0) {
                position = position + increment;
                System.out.println();
                System.out.print(position + ":");
            }
      
            System.out.print("[" + term.toString() + "] ");      
        }
        System.out.println();    
    }
  
    public static void displayTokensWithFullDetails(Analyzer analyzer, String text)
                                                            throws IOException {
    
        TokenStream stream = analyzer.tokenStream("contents",
                                                  new StringReader(text));
    
        CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
        PositionIncrementAttribute posIncr = stream
                                .addAttribute(PositionIncrementAttribute.class);
        OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);
        TypeAttribute type = stream.addAttribute(TypeAttribute.class);
        PayloadAttribute payload = stream.addAttribute(PayloadAttribute.class);
    
        int position = 0;
        stream.reset();
        while (stream.incrementToken()) {
      
            int increment = posIncr.getPositionIncrement();
            if (increment > 0) {
                position = position + increment;
                System.out.println();
                System.out.print(position + ":");
            }
      
            BytesRef pl = payload.getPayload();
      
            if (pl != null) {
                System.out.print("[" + term.toString() + ":" + offset.startOffset()
                + "->" + offset.endOffset() + ":" + type.type() + ":"
                + new String(pl.bytes) + "] ");        
            } else {
                System.out.print("[" + term.toString() + ":" + offset.startOffset()
                + "->" + offset.endOffset() + ":" + type.type() + "] ");        
            }      
        }
        System.out.println();
    }
  
    public static String getTokens(final Analyzer analyzer, 
                                   final String text) throws IOException {
        final TokenStream stream = 
                               analyzer.tokenStream("", new StringReader(text));        
        return getTokens(stream);
    }

    public static String getTokens(final TokenStream stream) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final CharTermAttribute term = 
                                   stream.addAttribute(CharTermAttribute.class);
        boolean first = true;

        stream.reset();
        while (stream.incrementToken()) {
            if (first) {
                first = false;
            } else {
                builder.append(" ");
            }
            builder.append(term.toString());
        }
        stream.close();

        return builder.toString();
    }  

    public static List<String> getTokenList(final Analyzer analyzer, 
                                            final String text) 
                                                            throws IOException {
        final TokenStream stream = 
                               analyzer.tokenStream("", new StringReader(text));        
        return getTokenList(stream);
    }

    public static List<String> getTokenList(final TokenStream stream) 
                                                            throws IOException {
        final List<String> ret = new ArrayList<String>();
        final CharTermAttribute term = 
                                   stream.addAttribute(CharTermAttribute.class);

        stream.reset();
        while (stream.incrementToken()) {
            ret.add(term.toString());
        }
        stream.close();

        return ret;
    }  
}