package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Heitor Barbieri
 * date: 20141017
 */
public class DeCSCodeTest {
    private final DeCSCode decs;
    
    public DeCSCodeTest() throws IOException {
        decs = new DeCSCode();
    }
    
    /**
     * Test of getDescritorCode method, of class DeCSCode.
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    @Test
    public void testGetDescritorCode() throws IOException, ParseException {
        
        System.out.println("getDescritorCode");
        
        String descriptor = "Temefós";        
        String expResult = "2";
        String result = decs.getDescritorCode(descriptor);
        assertEquals(expResult, result);
        
        descriptor = "Abreviaturas como Assunto";        
        expResult = "4";
        result = decs.getDescritorCode(descriptor);
        assertEquals(expResult, result);        
    }

    /**
     * Test of getDescriptorTerm method, of class DeCSCode.
     * @throws java.io.IOException
     */
    @Test
    public void testGetDescriptorTerm() throws IOException {
        System.out.println("getDescriptorTerm");
        
        String code = "7";
        String lang = "en";
        String expResult = "Abdominal Injuries";
        String result = decs.getDescriptorTerm(code, lang);
        assertEquals(expResult, result);
        
        code = "6";
        lang = "es";
        expResult = "Abdomen Agudo";
        result = decs.getDescriptorTerm(code, lang);
        assertEquals(expResult, result);
        
        code = "4";
        lang = "pt";
        expResult = "Abreviaturas como Assunto";
        result = decs.getDescriptorTerm(code, lang);
        assertEquals(expResult, result);
    }    
}
