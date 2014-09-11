package org.bireme.dia.util;

/**
 * Copyright Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific lan      
*/

import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

// From chapter 4
public class AnalyzerUtils {
    final static String FIELD_NAME = "contents";
    
    public static void displayTokens(final Analyzer analyzer,
                                     final String text) throws IOException {
        displayTokens(analyzer.tokenStream(FIELD_NAME, new StringReader(text)));  //A
    }

    public static void displayTokens(final TokenStream stream) 
                                                            throws IOException {
        final CharTermAttribute cattr = stream
                                         .addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            System.out.println("[" + cattr.toString() + "] ");    //B
        }
        stream.end();
        stream.close();
    }
    /*
        #A Invoke analysis process
        #B Print token text surrounded by brackets
    */

    public static int getPositionIncrement(final AttributeSource source) {
        final PositionIncrementAttribute attr = source.addAttribute(
                                              PositionIncrementAttribute.class);
        return attr.getPositionIncrement();
    }

    public static String getTerm(final AttributeSource source) {
        final CharTermAttribute attr = source
                                         .addAttribute(CharTermAttribute.class);
        return attr.toString();
    }

    public static String getType(final AttributeSource source) {
        final TypeAttribute attr = source.addAttribute(TypeAttribute.class);
        return attr.type();
    }

    public static void setPositionIncrement(final AttributeSource source, 
                                            final int posIncr) {
        final PositionIncrementAttribute attr = source.addAttribute(
                                              PositionIncrementAttribute.class);
        attr.setPositionIncrement(posIncr);
    }

    public static void setTerm(final AttributeSource source, 
                               final String term) {
        final CharTermAttribute attr = source.addAttribute(
                                                       CharTermAttribute.class);
        attr.setEmpty();
        attr.append(term);
    }

    public static void setType(final AttributeSource source, 
                               final String type) {
        final TypeAttribute attr = source.addAttribute(TypeAttribute.class);
        attr.setType(type);
    }

    public static void displayTokensWithPositions(final Analyzer analyzer, 
                                                  final String text) 
                                                            throws IOException {

        final TokenStream stream = analyzer.tokenStream(FIELD_NAME,
                                                        new StringReader(text));
        final CharTermAttribute term = stream.addAttribute(
                                                       CharTermAttribute.class);
        final PositionIncrementAttribute posIncr = stream.addAttribute(
                                              PositionIncrementAttribute.class);

        int position = 0;
        
        stream.reset();
        while (stream.incrementToken()) {
            final int increment = posIncr.getPositionIncrement();
            if (increment > 0) {
                position += increment;
                System.out.println();
                System.out.print(position + ": ");
            }

            System.out.print("[" + term.toString() + "] ");
        }
        System.out.println();
    }

    public static void displayTokensWithFullDetails(final Analyzer analyzer,
                                                    final String text) 
                                                            throws IOException {
        final TokenStream stream = analyzer.tokenStream(FIELD_NAME,               // #A
                                                        new StringReader(text));

        final CharTermAttribute term = stream.addAttribute(
                                                       CharTermAttribute.class);  // #B
        final PositionIncrementAttribute posIncr = stream.addAttribute(
                                              PositionIncrementAttribute.class);  // #B
        final OffsetAttribute offset = stream.addAttribute(
                                                         OffsetAttribute.class);  // #B
        final TypeAttribute type = stream.addAttribute(TypeAttribute.class);      // #B

        int position = 0;
        
        stream.reset();
        while (stream.incrementToken()) {                                  // #C
            final int increment = posIncr.getPositionIncrement();          // #D
            if (increment > 0) {                                           // #D
                position += increment;                           // #D
                System.out.println();                                      // #D
                System.out.print(position + ": ");                         // #D
            }

            System.out.print("[" +                                         // #E
                             term.toString() + ":" +                       // #E
                             offset.startOffset() + "->" +                 // #E
                             offset.endOffset() + ":" +                    // #E
                             type.type() + "] ");                          // #E
        }
        System.out.println();
    }
    /*
        #A Perform analysis
        #B Obtain attributes of interest
        #C Iterate through all tokens
        #D Compute position and print
        #E Print all token details
    */

    public static void displayPositionIncrements(final Analyzer analyzer, 
                                                 final String text)
                                                            throws IOException {
        final TokenStream stream = analyzer.tokenStream(FIELD_NAME, 
                                                        new StringReader(text));
        final PositionIncrementAttribute posIncr = stream.addAttribute(
                                              PositionIncrementAttribute.class);
        while (stream.incrementToken()) {
            System.out.println("posIncr=" + posIncr.getPositionIncrement());
        }   
    }

    public static void main(String[] args) throws IOException {
        System.out.println("SimpleAnalyzer");
        displayTokensWithFullDetails(new SimpleAnalyzer(),
        "The quick brown fox....");

        System.out.println("\n----");
        System.out.println("StandardAnalyzer");
        displayTokensWithFullDetails(new StandardAnalyzer(),
        "I'll email you at xyz@example.com");
    }
}

/*
#1 Invoke analysis process
#2 Output token text surrounded by brackets
*/

