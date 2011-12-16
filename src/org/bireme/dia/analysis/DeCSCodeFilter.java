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
public class DeCSCodeFilter extends TokenFilter {

    private Token token;

    public DeCSCodeFilter(TokenStream input) {
        super(input);
    }

    public Token next() throws IOException {
        token = input.next();
        
        if (token == null){
            return null;
        }
        if (!token.termText().startsWith("^d")){
            return token;            
        }
        return null;
    }
}
