package org.bireme.dia.analysis;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import org.apache.lucene.analysis.Analyzer;
import org.bireme.dia.util.AnalyzerUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Heitor Barbieri
 * date: 20150714
 */
public class DeCSKeywordAnalyzerTest {
    /**
     * Test of createComponents method, of class DeCSKeywordAnalyzer.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateComponents() throws IOException {
        final String str1 = "Abdomen, Acute/síntesis química";
        final Analyzer instance1 = new DeCSKeywordAnalyzer();
        final String[] arr1 = {};
        final Set<String> expResult1 = new TreeSet(Arrays.asList(arr1));
        final Set<String> result1 = new TreeSet(
                                   AnalyzerUtils.getTokenList(instance1, str1));       
        assertEquals(expResult1, result1);
    }
    
    @Test
    public void testAnalyzer() throws IOException {
        final String in18 = "^d8";
        final String[] arr18 = { "^d8", "abdominal neoplasms",  "c04.588.033",
            "neoplasias abdominais", "neoplasias abdominales", 
            "tumores abdominais", "tumores abdominales", "tumores do abdome"};
        final Analyzer analyzer = new DeCSKeywordAnalyzer();
        final Set<String> expResult18 = new TreeSet(Arrays.asList(arr18));
        final Set<String> result18 = new TreeSet(
                                    AnalyzerUtils.getTokenList(analyzer, in18));
        assertEquals(expResult18, result18);
        System.out.println("Test 18");
    }
}
