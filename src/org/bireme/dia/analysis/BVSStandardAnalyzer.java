package org.bireme.dia.analysis;

import java.io.IOException;
import java.net.URL;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;

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

    public BVSStandardAnalyzer() throws IOException {
        final ClassLoader loader = this.getClass().getClassLoader();
        final URL dirUrl = loader.getResource("./"); // get current directory of classes

        engine = new DeCSEngine("resources/decs/main", CATEGORY, SYN, KEYQLF,
                                                                       ONLYQLF);
        wordDelimiterConfig = WordDelimiterFilter.GENERATE_WORD_PARTS +
                                  WordDelimiterFilter.GENERATE_NUMBER_PARTS +
                                //WordDelimiterFilter.CATENATE_WORDS +
                                //WordDelimiterFilter.CATENATE_NUMBERS +
                                  WordDelimiterFilter.SPLIT_ON_NUMERICS +
                                  WordDelimiterFilter.STEM_ENGLISH_POSSESSIVE +
                                  WordDelimiterFilter.CATENATE_ALL;        
                                //WordDelimiterFilter.PRESERVE_ORIGINAL;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        final Tokenizer source = new WhitespaceTokenizer();
        final TokenStream filter1 = new ASCIIFoldingFilter(source);
        final TokenStream filter2 = new LowerCaseFilter(filter1);
        final TokenStream filter3 = new SynonymFilter(filter2, engine,
                                                                 WORDS, PRECOD);
        final TokenStream filter4 =  new WordDelimiterFilter(filter3, 
                                                 wordDelimiterConfig, null);        
        final TokenStream filter5 = new LowerCaseFilter(filter4);
        final TokenStream filter6 = new ASCIIFoldingFilter(filter5);

        return new TokenStreamComponents(source, filter6);
    }
}