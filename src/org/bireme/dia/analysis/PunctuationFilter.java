/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author vinicius
 */
public class PunctuationFilter extends TokenFilter {
    private CharTermAttribute termAtt;
    
    public PunctuationFilter(TokenStream input) {
        super(input);
        this.termAtt = addAttribute(CharTermAttribute.class);        
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }
        int length = termAtt.length();
        char[] buffer = termAtt.buffer();
        int upto = 0; 
        String invalidChars = ",;:=?";
        
        for (int i = 0; i < length; i++) {            
            char c = buffer[i]; 
            
            if ( invalidChars.indexOf(c) >= 0 ) {
                //Do Nothing, (drop the character) 
            }else{
                buffer[upto++] = c; 
            }
        }
            
        termAtt.setLength(upto);
        return true;
    }
}
