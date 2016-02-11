package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Elimina caracteres inválidos ,;:=? do token.
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
        if (!input.incrementToken()) {
            return false;
        }
        final int length = termAtt.length();
        final char[] buffer = termAtt.buffer();
        final String invalidChars = ",;:=?";
        int upto = 0;

        for (int i = 0; i < length; i++) {
            final char c = buffer[i];

            if (invalidChars.indexOf(c) >= 0) {
                //Do Nothing, (drop the character)
            } else {
                buffer[upto] = c;
                upto++;
            }
        }

        termAtt.setLength(upto);

        return true;
    }
}