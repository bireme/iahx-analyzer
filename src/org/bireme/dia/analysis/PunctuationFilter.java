/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 *
 * @author vinicius
 */
public class PunctuationFilter extends TokenFilter {

    public PunctuationFilter(TokenStream input) {
        super(input);
    }

    public Token next() throws IOException {
        String tokenText;
        Token token = input.next();

        if (token == null) {
            return null;
        }

        tokenText = token.termText();
        tokenText = tokenText.replaceAll("\\.|,|;|:", "");
        //System.out.println("==" + tokenText + "==");
        token.setTermText(tokenText);

        return token;
    }
}
