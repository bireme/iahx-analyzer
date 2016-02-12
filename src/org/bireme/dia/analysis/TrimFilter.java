package org.bireme.dia.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Elimina algums caracteres no inicio e no fim do token.
 * @author Heitor Barbieri
 * date: 20160212
 */
public class TrimFilter extends TokenFilter {
    private final CharTermAttribute termAtt;
    private final Set<Character> invalidChars;
    
    public TrimFilter(final TokenStream input) {
        super(input);
        this.termAtt = addAttribute(CharTermAttribute.class);
        invalidChars = new HashSet<>();
        invalidChars.add(' ');
        invalidChars.add('-');
        invalidChars.add('.');        
    }
    
    @Override
    public final boolean incrementToken() throws IOException {
        boolean ret = false;
        
        if (input.incrementToken()) {
            final int length = termAtt.length();
            final char[] buffer = termAtt.buffer();
            int start = 0;
            int end = length - 1;

            while (start < length) {
                if (invalidChars.contains(buffer[start])) {
                    start++;                    
                } else {
                    break;
                }                
            }
            while (end > start) {
                if (invalidChars.contains(buffer[end])) {
                    end--;
                } else {
                    break;
                }                 
            }
            final int len2 = end - start + 1;
            for (int count = 0; count < len2; count++) {
                buffer[count] = buffer[count + start];
            }
            termAtt.setLength(len2);
            ret = true;
        }

        return ret;
    }
}
