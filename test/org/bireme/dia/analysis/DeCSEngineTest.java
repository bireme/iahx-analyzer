package org.bireme.dia.analysis;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.queryparser.classic.ParseException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

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
        
        final String term0 = "Abreviaturas como Asunto";
        final Set<String> result0 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term0)));        
        assertTrue(result0.isEmpty());     
        
        final String term1 = "Abreviaturas como Asunto".toLowerCase();
        final Set<String> expResult1 = new HashSet<String>();
        expResult1.add("Abbreviations as Topic"); 
        expResult1.add("Abreviatura de los Medicamentos");
        expResult1.add("Abreviatura dos Medicamentos");   
        expResult1.add("Abreviaturas como Assunto");                                     
        expResult1.add("Abreviaturas como Asunto");                                       
        expResult1.add("Abreviaturas como Tema");
        expResult1.add("Acrónimos como Asunto"); 
        expResult1.add("Acrónimos como Tema"); 
        expResult1.add("Acrônimos como Assunto");                                                                                                                                                     
        expResult1.add("Drug Abbreviations"); 
        expResult1.add("HP7.007.097.048"); 
        expResult1.add("L01.143.506.598.400.556.131");
        final Set<String> result1 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term1)));        
        assertEquals(expResult1, result1);        
        
        final String term2 = "000004";
        final Set<String> expResult2 = new HashSet<String>();
        expResult2.add("Abbreviations as Topic"); 
        expResult2.add("Abreviatura de los Medicamentos");
        expResult2.add("Abreviatura dos Medicamentos");   
        expResult2.add("Abreviaturas como Assunto");                                     
        expResult2.add("Abreviaturas como Asunto");                                       
        expResult2.add("Abreviaturas como Tema");
        expResult2.add("Acrónimos como Asunto"); 
        expResult2.add("Acrónimos como Tema"); 
        expResult2.add("Acrônimos como Assunto");                                                                                                                                                     
        expResult2.add("Drug Abbreviations"); 
        expResult2.add("HP7.007.097.048"); 
        expResult2.add("L01.143.506.598.400.556.131");
        final Set<String> result2 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term2)));        
        assertEquals(expResult2, result2);
        
        final String term3 = "temefos";
        final Set<String> expResult3 = new HashSet<String>();
        expResult3.add("D02.705.539.900"); 
        expResult3.add("D02.886.309.900");
        expResult3.add("Temefos");          
        expResult3.add("Temefós");          
        final Set<String> result3 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term3)));        
        assertEquals(expResult3, result3);
                
        final String term4 = "Abreviaturas como Asunto".toLowerCase();
        final String[] aresult4 = engine_0011.getSynonyms(term4);
        final Set<String> result4 = (aresult4 == null) ? new HashSet<String>()
                                         : new HashSet(Arrays.asList(aresult4));        
        assertTrue(result4.isEmpty());
        
        final String term5 = "temefos/blood";
        final Set<String> expResult5 = new HashSet<String>();
        expResult5.add("D02.705.539.900");
        expResult5.add("D02.886.309.900");
        expResult5.add("Q50.040.020Q05.010");
        expResult5.add("blood");         
        expResult5.add("sangre"); 
        expResult5.add("sangue");
        expResult5.add("Temefos");
        expResult5.add("Temefós");
        expResult5.add("Temefos/BL");                
        expResult5.add("Temefós/BL");
        expResult5.add("Temefos/blood");
        expResult5.add("Temefós/sangre");
        expResult5.add("Temefós/sangue");
        final Set<String> result5 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term5)));        
        assertEquals(expResult5, result5);
                        
        final String term6 = "Temefós/blood";
        final Set<String> expResult6 = new HashSet<String>();
        expResult6.add("blood");
        expResult6.add("sangre");
        expResult6.add("sangue");
        final Set<String> result6 = new HashSet(
                                 Arrays.asList(engine_0011.getSynonyms(term6)));        
        assertEquals(expResult6, result6);
        
        final String term7 = "temefos/xxxx";
        final Set<String> result7 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term7)));        
        assertTrue(result7.isEmpty());
        
        final String term8 = "xxx/blood";        
        final Set<String> result8 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term8)));        
        assertTrue(result8.isEmpty());
        
        final String term9 = "000004/blood";
        final Set<String> expResult9 = new HashSet<String>();
        expResult9.add("Abbreviations as Topic"); 
        expResult9.add("Abbreviations as Topic/blood");
        expResult9.add("Abbreviations as Topic/BL");
        expResult9.add("Abreviatura de los Medicamentos");
        expResult9.add("Abreviatura dos Medicamentos");   
        expResult9.add("Abreviaturas como Assunto");                                     
        expResult9.add("Abreviaturas como Assunto/sangue");
        expResult9.add("Abreviaturas como Assunto/BL");
        expResult9.add("Abreviaturas como Asunto");                                       
        expResult9.add("Abreviaturas como Asunto/sangre");
        expResult9.add("Abreviaturas como Asunto/BL");
        expResult9.add("Abreviaturas como Tema");
        expResult9.add("Acrónimos como Asunto"); 
        expResult9.add("Acrónimos como Tema"); 
        expResult9.add("Acrônimos como Assunto");
        expResult9.add("Drug Abbreviations"); 
        expResult9.add("HP7.007.097.048"); 
        expResult9.add("L01.143.506.598.400.556.131");
        expResult9.add("Q50.040.020Q05.010");
        expResult9.add("blood");         
        expResult9.add("sangre"); 
        expResult9.add("sangue");
        final Set<String> result9 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term9)));        
        assertEquals(expResult9, result9);
        
        final String term10 = "/blood";
        final Set<String> expResult10 = new HashSet<String>();
        expResult10.add("Q50.040.020Q05.010");
        expResult10.add("blood");         
        expResult10.add("sangre"); 
        expResult10.add("sangue");
        final Set<String> result10 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term10)));        
        assertEquals(expResult10, result10);
        
        final String term11 = "/blood";
        final Set<String> expResult11 = new HashSet<String>();
        expResult11.add("blood");         
        expResult11.add("sangre"); 
        expResult11.add("sangue");
        final Set<String> result11 = new HashSet(
                                 Arrays.asList(engine_0011.getSynonyms(term11)));        
        assertEquals(expResult11, result11);
        
        final String term12 = "^dtemefos ^sblood";
        final Set<String> expResult12 = new HashSet<String>();
        expResult12.add("D02.705.539.900");
        expResult12.add("D02.886.309.900");
        expResult12.add("Q50.040.020Q05.010");
        expResult12.add("blood");         
        expResult12.add("sangre"); 
        expResult12.add("sangue");
        expResult12.add("Temefos");
        expResult12.add("Temefós");
        expResult12.add("Temefos/BL");                
        expResult12.add("Temefós/BL");
        expResult12.add("Temefos/blood");
        expResult12.add("Temefós/sangre");
        expResult12.add("Temefós/sangue");
        final Set<String> result12 = new HashSet(
                                 Arrays.asList(engine_1110.getSynonyms(term12)));        
        assertEquals(expResult12, result12);
    }    
}
