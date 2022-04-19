package org.bireme.dia.analysis;

import java.io.IOException;
import java.net.URL;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterIterator;

public class BVSStandardAnalyzer extends Analyzer {
    public static final boolean WORDS = true;
    public static final boolean CATEGORY = true;
    public static final boolean SYN = true;
    public static final boolean PRECOD = true;          // somente gera tokens para descritores que estiverem
                                                        // precodificados no formato ^didentificador^squalificador
    public static final boolean KEYQLF = true;
    public static final boolean ONLYQLF = false;

    private final SynonymEngine engine;
    private final int wordDelimiterConfig;
    private final byte[] table;

    public BVSStandardAnalyzer() throws IOException {
        final ClassLoader loader = this.getClass().getClassLoader();
        final URL dirUrl = loader.getResource("./"); // get current directory of classes
        
        table = new byte[256];        
        for (int pos = 0; pos < 256; pos++) {
            if (pos == 32) {            
                table[pos] = 4;  // Não delimita em espaços
            //} else if (pos == 45) {    // VA/HB 20161124
            //    table[pos] = 1;  // Não delimita em hifens    
            } else if (pos == 46) { 
                table[pos] = 1;  // Não delimita em pontos
            } else {
                table[pos] = WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE[pos];
            }
        }
        engine = new DeCSEngine("resources/decs/main", CATEGORY, SYN, KEYQLF,
                                                                       ONLYQLF);
        /*
        wordDelimiterConfig = WordDelimiterFilter.GENERATE_WORD_PARTS +
                                  WordDelimiterFilter.GENERATE_NUMBER_PARTS +
                                //WordDelimiterFilter.CATENATE_WORDS +
                                //WordDelimiterFilter.CATENATE_NUMBERS +
                                  //WordDelimiterFilter.SPLIT_ON_NUMERICS +
                                  WordDelimiterFilter.STEM_ENGLISH_POSSESSIVE +
                                  WordDelimiterFilter.CATENATE_ALL +        
                                  WordDelimiterFilter.PRESERVE_ORIGINAL; // VA/HB 20161128
        */
        
        wordDelimiterConfig = WordDelimiterGraphFilter.GENERATE_WORD_PARTS +
                              WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS +
                              WordDelimiterGraphFilter.STEM_ENGLISH_POSSESSIVE +
                              WordDelimiterGraphFilter.CATENATE_ALL +        
                              WordDelimiterGraphFilter.PRESERVE_ORIGINAL; // VA/HB 20161128
    }

    @Override
    public void close() {
        try {
            engine.close();
            super.close();
        } catch(IOException ioe) {}
    }
    
    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        final Tokenizer source = new WhitespaceTokenizer();
        final TokenStream filter1 = new ASCIIFoldingFilter(source);
        final TokenStream filter2 = new LowerCaseFilter(filter1);
        final TokenStream filter3 = new SynonymFilter(filter2, engine,
                                                                 WORDS, PRECOD);
        //final TokenStream filter4 =  new WordDelimiterFilter(filter3, 
        //                                         wordDelimiterConfig, null);        
        //final TokenStream filter4 =  new WordDelimiterFilter(filter3, table, 
        //                                             wordDelimiterConfig, null);
        final TokenStream filter4 =  new WordDelimiterGraphFilter(filter3, false,
                                           table, wordDelimiterConfig, null);
        
        final TokenStream filter5 = new TrimFilter(filter4);
        final TokenStream filter6 = new LowerCaseFilter(filter5);
        final TokenStream filter7 = new ASCIIFoldingFilter(filter6);

        return new TokenStreamComponents(source, filter7);
    }
}