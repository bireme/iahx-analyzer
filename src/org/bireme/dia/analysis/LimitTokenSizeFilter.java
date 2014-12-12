package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author Heitor Barbieri
 * date: 20141023
 */
public class LimitTokenSizeFilter extends TokenFilter {
    private final CharTermAttribute termAtt;
    private int maxSize;
    
     public LimitTokenSizeFilter(final TokenStream input,
                                 final int maxSize) {
        super(input);
        
        if ((maxSize < 1) || 
            (maxSize > StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH)) {
            throw new IllegalArgumentException("input=" + input);
        }
        this.termAtt = addAttribute(CharTermAttribute.class);
        this.maxSize = maxSize;
    }
     
    @Override
    public final boolean incrementToken() throws IOException {
        final boolean ret;
        
        if (input.incrementToken()) {
            if (termAtt.length() > maxSize) {
                termAtt.setLength(maxSize);
            }
            ret = true;
        } else {
            ret = false;
        }
        
        return ret;
    }
    
}
