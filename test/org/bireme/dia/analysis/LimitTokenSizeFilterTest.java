package org.bireme.dia.analysis;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.bireme.dia.util.AnalyzerUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Heitor Barbieri
 * date: 20141023
 */
public class LimitTokenSizeFilterTest {
    
    public LimitTokenSizeFilterTest() {
    }
    
    /**
     * Test of incrementToken method, of class LimitTokenSizeFilter.
     * @throws java.io.IOException
     */
    @Test
    public void testIncrementToken() throws IOException {
        System.out.println("incrementToken");
                
        final String str1 = "John, Peter and Mary said: Is there life after love? =)";
        final int len1 = 10;
        final StringReader reader1 = new StringReader(str1);
        final Tokenizer source1 = new KeywordTokenizer(reader1);
        final TokenFilter instance1 = new LimitTokenSizeFilter(source1, len1);
        final String expResult1 = str1.substring(0, Math.min(len1, str1.length()));
        final String result1 = AnalyzerUtils.getTokens(instance1);       
        assertEquals(expResult1, result1);
                
        final String str2 = "John, Peter and Mary said: Is there life after love? =)";
        final int len2 = 100;
        final StringReader reader2 = new StringReader(str2);
        final Tokenizer source2 = new KeywordTokenizer(reader2);
        final TokenFilter instance2 = new LimitTokenSizeFilter(source2, len2);
        final String expResult2 = str2.substring(0, Math.min(len2, str2.length()));
        final String result2 = AnalyzerUtils.getTokens(instance2);       
        assertEquals(expResult2, result2);
        
        final String str3 = "John, Peter and Mary said: Is there life after love? =)";
        final int len3 = str3.length();
        final StringReader reader3 = new StringReader(str3);
        final Tokenizer source3 = new KeywordTokenizer(reader3);
        final TokenFilter instance3 = new LimitTokenSizeFilter(source3, len3);
        final String expResult3 = str3.substring(0, Math.min(len3, str3.length()));
        final String result3 = AnalyzerUtils.getTokens(instance3);       
        assertEquals(expResult3, result3);
    }    
}
