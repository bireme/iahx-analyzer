package org.bireme.dia.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;

/**
 *
 * @author Heitor Barbieri
 * date: 20160211
 */
class MyAnalyzer extends Analyzer {
    private final int wordDelimiterConfig;

    MyAnalyzer() {
        wordDelimiterConfig = 
        WordDelimiterFilter.GENERATE_WORD_PARTS +
        WordDelimiterFilter.GENERATE_NUMBER_PARTS +
        WordDelimiterFilter.CATENATE_WORDS +
        WordDelimiterFilter.CATENATE_NUMBERS +
        WordDelimiterFilter.PRESERVE_ORIGINAL;
    }

    @Override
    protected Analyzer.TokenStreamComponents createComponents(
                                                   final String fieldName) {
        final Tokenizer source = new WhitespaceTokenizer();
        final TokenStream filter =  new WordDelimiterFilter(source, 
                                                 wordDelimiterConfig, null);

        return new Analyzer.TokenStreamComponents(source, filter);
    }
}

public class TestWordDelimiterFilter {        
    public static void main(String[] args) throws IOException {
        
        System.out.print("Please enter text to be filtered: ");
        
        final BufferedReader stdin = new BufferedReader(
                                             new InputStreamReader(System.in) );
        String text = stdin.readLine( );        
        if (text.equals("")){
            text = "^d30600^s22031 ^d4250 Malária em Países da america Do SuL 0329023,0099 &878922 abel, laerte packer; tardelli, adalberto fim-de-semana-2016";
        }
        
        System.out.println("text to be filtered: " + text);

        System.out.println("WhitepaceTokenizer + WordDelimiterFilter");
        AnalyzerUtils.displayTokensWithFullDetails(new MyAnalyzer(), text);       
    }
}
