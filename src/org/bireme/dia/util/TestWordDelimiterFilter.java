package org.bireme.dia.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;

/**
 *
 * @author Heitor Barbieri
 * date: 20160211
 */
class MyAnalyzer extends Analyzer {
    private final int wordDelimiterConfig;

    MyAnalyzer() {
        /*wordDelimiterConfig = 
        WordDelimiterFilter.GENERATE_WORD_PARTS +
        WordDelimiterFilter.GENERATE_NUMBER_PARTS +
        //WordDelimiterFilter.CATENATE_WORDS +
        //WordDelimiterFilter.CATENATE_NUMBERS +
        WordDelimiterFilter.SPLIT_ON_NUMERICS +
        WordDelimiterFilter.STEM_ENGLISH_POSSESSIVE +
        WordDelimiterFilter.CATENATE_ALL;        
        //WordDelimiterFilter.PRESERVE_ORIGINAL;        */
        
        wordDelimiterConfig = 
        WordDelimiterGraphFilter.GENERATE_WORD_PARTS +
        WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS +
        WordDelimiterGraphFilter.SPLIT_ON_NUMERICS +
        WordDelimiterGraphFilter.STEM_ENGLISH_POSSESSIVE +
        WordDelimiterGraphFilter.CATENATE_ALL;        

    }

    @Override
    protected Analyzer.TokenStreamComponents createComponents(
                                                   final String fieldName) {
        final Tokenizer source = new WhitespaceTokenizer();
        final TokenStream filter1 = new ASCIIFoldingFilter(source);
        final TokenStream filter2 = new LowerCaseFilter(filter1);
        /*final TokenStream filter3 =  new WordDelimiterFilter(filter2, 
                                                 wordDelimiterConfig, null);*/
        final TokenStream filter3 =  new WordDelimiterGraphFilter(filter2,
                                                 wordDelimiterConfig, null);
        
        return new Analyzer.TokenStreamComponents(source, filter3);
    }
}

public class TestWordDelimiterFilter {        
    public static void main(String[] args) throws IOException {        
        /*
        int i=0;
        for (byte b : WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE) {
            System.out.println("[" + (++i) + "[" + (int)b + "]]");
        }
        */        
        System.out.print("Please enter text to be tokenzided and filtered: ");
        
        final BufferedReader stdin = new BufferedReader(
                                             new InputStreamReader(System.in) );
        String text = stdin.readLine( );        
        if (text.equals("")){
            text = "^d30600^s22031 ^d4250 Malária em Países da america Do SuL 0329023,0099 &878922" +
                    "\n abel, laerte packer; tardelli, adalberto (fim-de-semana-2016 'inesquecível')" +
                    "\n ponto final. ponto e vírgula; barra/ interrogacao?";
        }
        
        System.out.println("\ntext to be tokenized and filtered: " + text);

        System.out.println("\nWhitepaceTokenizer + WordDelimiterFilter");
        AnalyzerUtils.displayTokens(new MyAnalyzer(), text);       
    }
}
