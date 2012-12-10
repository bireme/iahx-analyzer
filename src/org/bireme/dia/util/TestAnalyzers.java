package org.bireme.dia.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.bireme.dia.analysis.*;


public class TestAnalyzers {   
    public static void main(String[] args) throws IOException {
        
        System.out.print("Please enter text to analyze: ");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in) );
        String text = stdin.readLine( );
        
        if (text.equals("")){
            //text = "^d30600^s22031 ^d4250 MalAria em PAIses da america Do SuL 0329023,0099 &878922 abel, laerte packer; tardelli, adalberto";
            text = "^d3727";
        }

        System.out.println("BVSStandardAnalyzer");
        AnalyzerUtils.displayTokensWithFullDetails(new BVSStandardAnalyzer(), text);

/*
        System.out.println("text to be analyzed: " + text);
        
        System.out.println("BVSStandardAnalyzer");
        AnalyzerUtils.displayTokensWithFullDetails(new BVSStandardAnalyzer(), text);

        System.out.println("DeCSStandardAnalyzer");
        AnalyzerUtils.displayTokensWithFullDetails(new DeCSStandardAnalyzer(), text);

        System.out.println("DeCSSimpleAnalyzer");
        AnalyzerUtils.displayTokensWithFullDetails(new DeCSSimpleAnalyzer(), text);

        System.out.println("DeCSKeywordAnalyzer");
        AnalyzerUtils.displayTokensWithFullDetails(new DeCSKeywordAnalyzer(), text);

        System.out.println("DeCSAuthorizedTermAndCategoryAnalyzer");
        AnalyzerUtils.displayTokensWithFullDetails(new DeCSAuthorizedTermAndCategoryAnalyzer(), text);

*/
    }
}
