package org.bireme.dia.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Heitor Barbieri
 * date: 20141017
 */
public class DeCSEngineTest {
    public static final String INDEX = "resources/decs/main";
    
    private final DeCSEngine engine_1110;
    private final DeCSEngine engine_0011;
    
    public DeCSEngineTest() throws IOException {
        engine_1110 = new DeCSEngine(INDEX, true, true, true, false);
        engine_0011 = new DeCSEngine(INDEX, false, false, true, true);
    }
    
    /**
     * Test of getSynonyms method, of class DeCSEngine.
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    @Test
    public void testGetSynonyms() throws IOException, ParseException {
        System.out.println("getSynonyms");
        
        final String term1 = "Abreviaturas como Asunto";
        final Set<String> expResult1 = new HashSet<String>();
        expResult1.add("abbreviations as topic"); 
        expResult1.add("abreviatura de los medicamentos");
        expResult1.add("abreviatura dos medicamentos");   
        expResult1.add("abreviaturas como assunto");                                     
        expResult1.add("abreviaturas como asunto");                                       
        expResult1.add("abreviaturas como tema");
        expResult1.add("acronimos como asunto"); 
        expResult1.add("acronimos como tema"); 
        expResult1.add("acronimos como assunto");                                                                                                                                                     
        expResult1.add("drug abbreviations"); 
        expResult1.add("hp7.007.097.048"); 
        expResult1.add("l01.143.506.598.400.556.131");
        final Set<String> result1 = engine_1110.getSynonyms(term1);        
        assertEquals(expResult1, result1);        
        
        final String term2 = "000004";
        final Set<String> expResult2 = new HashSet<String>();
        expResult2.add("abbreviations as topic"); 
        expResult2.add("abreviatura de los medicamentos");
        expResult2.add("abreviatura dos medicamentos");   
        expResult2.add("abreviaturas como assunto");                                     
        expResult2.add("abreviaturas como asunto");                                       
        expResult2.add("abreviaturas como tema");
        expResult2.add("acronimos como asunto"); 
        expResult2.add("acronimos como tema"); 
        expResult2.add("acronimos como assunto");                                                                                                                                                     
        expResult2.add("drug abbreviations"); 
        expResult2.add("hp7.007.097.048"); 
        expResult2.add("l01.143.506.598.400.556.131");
        final Set<String> result2 = engine_1110.getSynonyms(term2);        
        assertEquals(expResult2, result2);
        
        final String term3 = "Temefós";
        final Set<String> expResult3 = new HashSet<String>();
        expResult3.add("d02.705.539.900"); 
        expResult3.add("d02.886.309.900");
        expResult3.add("temefos");          
        final Set<String> result3 = engine_1110.getSynonyms(term3);        
        assertEquals(expResult3, result3);
        
        final String term4 = "Temefos";
        final Set<String> expResult4 = new HashSet<String>();
        expResult4.add("d02.705.539.900"); 
        expResult4.add("d02.886.309.900");
        expResult4.add("temefos");          
        final Set<String> result4 = engine_1110.getSynonyms(term4);        
        assertEquals(expResult4, result4);
        
        final String term5 = "Abreviaturas como Asunto";
        final Set<String> expResult5 = null;
        final Set<String> result5 = engine_0011.getSynonyms(term5);        
        assertTrue(result5.isEmpty());
        
        final String term6 = "Temefos/blood";
        final Set<String> expResult6 = new HashSet<String>();
        expResult6.add("d02.705.539.900");
        expResult6.add("d02.886.309.900");
        expResult6.add("q50.040.020q05.010");
        expResult6.add("blood");         
        expResult6.add("sangre"); 
        expResult6.add("sangue");
        expResult6.add("temefos");
        expResult6.add("temefos/bl");                
        expResult6.add("temefos/blood");
        expResult6.add("temefos/sangre");
        expResult6.add("temefos/sangue");
        final Set<String> result6 = engine_1110.getSynonyms(term6);        
        assertEquals(expResult6, result6);
        
        final String term7 = "Temefós/blood";
        final Set<String> expResult7 = new HashSet<String>();
        expResult7.add("d02.705.539.900");
        expResult7.add("d02.886.309.900");
        expResult7.add("q50.040.020q05.010");
        expResult7.add("blood");         
        expResult7.add("sangre"); 
        expResult7.add("sangue");
        expResult7.add("temefos");
        expResult7.add("temefos/bl");                
        expResult7.add("temefos/blood");
        expResult7.add("temefos/sangre");
        expResult7.add("temefos/sangue");
        final Set<String> result7 = engine_1110.getSynonyms(term7);        
        assertEquals(expResult7, result7);
        
        final String term8 = "Temefós/blood";
        final Set<String> expResult8 = new HashSet<String>();
        expResult8.add("blood");
        expResult8.add("sangre");
        expResult8.add("sangue");
        final Set<String> result8 = engine_0011.getSynonyms(term8);        
        assertEquals(expResult8, result8);
    }    
}
