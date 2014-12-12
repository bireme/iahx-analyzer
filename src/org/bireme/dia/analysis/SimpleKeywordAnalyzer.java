package org.bireme.dia.analysis;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

/**
 * CharTokenizer limits token width to 255 characters, though.
 * This implementation assumes keywords are 255 in length or less.
 */
public class SimpleKeywordAnalyzer extends Analyzer {
    public static final int MAX_KEY_LEN = 255;
    
    @Override
    protected TokenStreamComponents createComponents(final String fieldName,
                                                     final Reader reader) {
        final Tokenizer source = new KeywordTokenizer(reader);
        final TokenStream filter = new LimitTokenSizeFilter(source, MAX_KEY_LEN);
        final TokenStream filter1 = new ASCIIFoldingFilter(filter);
        final TokenStream filter2 = new LowerCaseFilter(filter1); // ????
        
        return new TokenStreamComponents(source, filter2);
    }
}
