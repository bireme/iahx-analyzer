package org.bireme.dia.analysis;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.bireme.dia.util.AnalyzerUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Heitor Barbieri
 * date: 20141029
 */
public class SynonymFilterTest {
    private final static String SENTENCE = "A rainha Temefos (d^100, d^8) em "
            + "seus abatedouros, viu sangue/odor-de-morte em animais com "
            + "problemas no adomen ( Abdomen, Acute )";
    
    private final DeCSEngine engine;
    
    public SynonymFilterTest() throws IOException {
        engine = new DeCSEngine("resources/decs/main", true, true, true, false);
    }    

    /**
     * Test of incrementToken method, of class SynonymFilter.
     * @throws java.io.IOException
     */
    @Test
    public void testIncrementToken() throws IOException {
        System.out.println("incrementToken");
                
        final Tokenizer source1 = new WhitespaceTokenizer();
        source1.setReader(new StringReader(SENTENCE));
        final TokenStream filter1 = new ASCIIFoldingFilter(source1);
        final TokenStream filter2 = new LowerCaseFilter(filter1);
        final TokenStream instance1 = new SynonymFilter(filter2, engine,
                                                                   true, false);
        final List<String> expResult1 = new ArrayList<String>();
        // A rainha Temefos (d^100, d^8) em seus abatedouros,
        expResult1.add("a");
        expResult1.add("rainha");
        expResult1.add("temefos");
        expResult1.add("(d^100,");
        expResult1.add("d^8)");
        expResult1.add("em");
        expResult1.add("seus");
        expResult1.add("abatedouros");
        expResult1.add("abattoirs");
        expResult1.add("mataderos");
        expResult1.add("matadouros");
        expResult1.add("Slaughterhouses");
        
        // viu sangue/odor-de-morte em animais com problemas no adomen ( Abdomen, Acute )
        expResult1.add("viu");
        expResult1.add("sangue/odor-de-morte");
        expResult1.add("em");
        expResult1.add("animais");
        expResult1.add("com");
        expResult1.add("problemas");
        expResult1.add("no");
        expResult1.add("adomen");
        expResult1.add("(");
        expResult1.add("abdomen, acute");
        expResult1.add("abdomen,");
        expResult1.add("acute");
        expResult1.add("abdomen agudo");
        expResult1.add("abdomen");
        expResult1.add("agudo");
        expResult1.add("abdome agudo");
        expResult1.add("abdome");
        expResult1.add("agudo");
        expResult1.add("abdomen en tabla");
        expResult1.add("abdomen");
        expResult1.add("en");
        expResult1.add("tabla");
        expResult1.add(")");
                        
        final List<String> result1 = AnalyzerUtils.getTokenList(instance1);
        
        assertEquals(expResult1, result1);
    }    
}
