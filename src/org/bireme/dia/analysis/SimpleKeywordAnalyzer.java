package org.bireme.dia.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;

import java.io.Reader;

/**
 * CharTokenizer limits token width to 255 characters, though.
 * This implementation assumes keywords are 255 in length or less.
 */
public class SimpleKeywordAnalyzer extends Analyzer {
    
    public TokenStream tokenStream(String fieldName,
                                            Reader reader) {
        
        TokenStream result = new KeywordTokenizer(reader);
        result = new LowerCaseFilter(result);
        result = new ISOLatin1AccentFilter(result);
        return result;
    }
    
}
