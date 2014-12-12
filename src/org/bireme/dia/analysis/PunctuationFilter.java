package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author vinicius
 */
public class PunctuationFilter extends TokenFilter {
    private final CharTermAttribute termAtt;

    public PunctuationFilter(final TokenStream input) {
        super(input);
        this.termAtt = addAttribute(CharTermAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        final boolean ret;
        
        if (input.incrementToken()) {
            final int length = termAtt.length();
            final char[] buffer = termAtt.buffer();
            final String invalidChars = ",;:=?";
            int upto = 0;

            for (int i = 0; i < length; i++) {
                final char c = buffer[i];

                if (invalidChars.indexOf(c) == -1) {
                    buffer[upto] = c;
                    upto++;
                }
            }
            termAtt.setLength(upto);
            ret = true;
        } else {
            input.end();
            ret = false;
        }
        
        return ret;
    }
}