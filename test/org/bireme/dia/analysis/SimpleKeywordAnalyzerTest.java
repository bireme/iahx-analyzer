package org.bireme.dia.analysis;

import java.io.IOException;
import java.text.Normalizer;
import org.apache.lucene.analysis.Analyzer;
import org.bireme.dia.util.AnalyzerUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Heitor Barbieri
 * date: 20141023
 */
public class SimpleKeywordAnalyzerTest {
    
    public SimpleKeywordAnalyzerTest() {
    }
    
    private static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD)
                                               .replaceAll("[^\\p{ASCII}]", "");
    }
    /**
     * Test of createComponents method, of class SimpleKeywordAnalyzer.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateComponents() throws IOException {
        final String str1 = "A casa de José e Maria é muito quente: lá se toma café com açúcar!";
        final Analyzer instance1 = new SimpleKeywordAnalyzer();
        final String expResult1 = "a casa de jose e maria e muito quente: la se toma cafe com acucar!";
        final String result1 = AnalyzerUtils.getTokens(instance1, str1);       
        assertEquals(expResult1, result1);
        
        final String str2 = "Para compartilhar esse conteúdo, por favor utilize "
                + "o link http://www1.folha.uol.com.br/cotidiano/2014/10/15 "
                + "ou as ferramentas oferecidas na página. Textos, fotos, artes "
                + "e vídeos da Folha estão protegidos pela legislação brasileira "
                + "sobre direito autoral. Não reproduza o conteúdo do jornal em " 
                + "qualquer meio de comunicação, eletrônico ou impresso, sem "
                + "autorização da Folhapress (pesquisa@folhapress.com.br).";

        final Analyzer instance2 = new SimpleKeywordAnalyzer();
        final String expResult2 = removerAcentos(str2).toLowerCase();
        final String result2 = AnalyzerUtils.getTokens(instance2, str2);       
        assertEquals(expResult2, result2);
    }
    
}
