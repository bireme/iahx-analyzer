package org.bireme.dia.analysis;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.bireme.dia.util.AnalyzerUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Heitor Barbieri
 * date: 20141023
 */
public class RemoveSlashFilterTest {
    
    public RemoveSlashFilterTest() {
    }
    
    /**
     * Test of incrementToken method, of class RemoveSlashFilter.
     * @throws java.io.IOException
     */
    @Test
    public void testIncrementToken() throws IOException {
        
        final String str1 = "a:/b/c/d/e";
        final StringReader reader1 = new StringReader(str1);
        final Tokenizer source1 = new KeywordTokenizer();
        source1.setReader(reader1);
        final TokenFilter instance1 = new RemoveSlashFilter(source1);
        final String expResult1 = str1.replaceAll("/", "");
        final String result1 = AnalyzerUtils.getTokens(instance1);       
        assertEquals(expResult1, result1);
        
        final String str2 = "https://lucene.apache.org/core/4_10_1/core/org/apache/lucene/analysis/TokenFilter.html?is-external=true /";
        final StringReader reader2 = new StringReader(str2);        
        final Tokenizer source2 = new KeywordTokenizer();
        source2.setReader(reader2);
        final TokenFilter instance2 = new RemoveSlashFilter(source2);
        final String expResult2 = str2.replaceAll("/", "");
        final String result2 = AnalyzerUtils.getTokens(instance2);       
        assertEquals(expResult2, result2);
    }
    
}
