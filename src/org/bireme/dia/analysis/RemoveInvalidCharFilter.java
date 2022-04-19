package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Remove qualquer caracter que não seja a-zA-Z0-9 do token.
 * Obs: caracteres que ocupem mais de dois bytes (255) devem ser previamente convertidos para não serem filtrados
 * @author Heitor Barbieri
 * @date 20220418
 */
public class RemoveInvalidCharFilter extends TokenFilter {
    private final CharTermAttribute termAtt;

    public RemoveInvalidCharFilter(final TokenStream input) {
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
        int upto = 0;
        char lastChar = 0;

        for (int i = 0; i < length; i++) {
            final char c = (Character.isLetterOrDigit(buffer[i]) ? buffer[i] : ' ');

            if ((lastChar != ' ') || (c != ' ')) {
                buffer[upto] = c;
                upto++;
            }
            lastChar = c;
        }

        termAtt.setLength(upto);

        return true;
    }
}

