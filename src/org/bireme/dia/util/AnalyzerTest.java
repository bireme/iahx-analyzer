package org.bireme.dia.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.bireme.dia.analysis.*;

public class AnalyzerTest {
    public static Token[] tokensFromAnalysis(Analyzer analyzer,
            String text) throws IOException {
        TokenStream stream =
                analyzer.tokenStream("contents", new StringReader(text));
        ArrayList tokenList = new ArrayList();
        while (true) {
            Token token = stream.next();
            if (token == null) break;
            
            tokenList.add(token);
        }
        
        return (Token[]) tokenList.toArray(new Token[0]);
    }
    
    public static void displayTokens(Analyzer analyzer,
            String text) throws IOException {
        Token[] tokens = tokensFromAnalysis(analyzer, text);
        
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            
            System.out.print("[" + token.termText() + "] ");
        }
    }
    
    public static void displayTokensWithPositions(Analyzer analyzer,
            String text) throws IOException {
        Token[] tokens = tokensFromAnalysis(analyzer, text);
        
        int position = 0;
        
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            
            int increment = token.getPositionIncrement();
            
            if (increment > 0) {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }
            
            System.out.print("[" + token.termText() + "] ");
        }
        System.out.println();
    }
    
    public static void displayTokensWithFullDetails(
            Analyzer analyzer, String text) throws IOException {
        Token[] tokens = tokensFromAnalysis(analyzer, text);
        
        int position = 0;
        
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            
            int increment = token.getPositionIncrement();
            
            if (increment > 0) {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }
            
            System.out.print("[" + token.termText() + ":" +
                    token.startOffset() + "->" +
                    token.endOffset() + ":" +
                    token.type() + "] ");
        }
        System.out.println();
    }
    
    public static void assertTokensEqual(Token[] tokens,
            String[] strings) {
        System.out.println(strings.length + tokens.length);
        
        for (int i = 0; i < tokens.length; i++) {
            System.out.println("index " + i + " -" + strings[i] + " - " + tokens[i].termText());
        }
    }
    
    public static void main(String[] args) throws IOException {
        
        System.out.print("Please enter text to analyze: ");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in) );
        String text = stdin.readLine( );
        
        if (text.equals("")){
            text = "^d30600^s22031 ^d4250 MalAria em PAIses da america Do SuL 0329023,0099 &878922 abel, laerte packer; tardelli, adalberto";
        }
        
/*
        System.out.println("text to be analyzed: " + text);
        
        System.out.println("BVSStandardAnalyzer");
        displayTokensWithFullDetails(new BVSStandardAnalyzer(), text);

        System.out.println("DeCSStandardAnalyzer");
        displayTokensWithFullDetails(new DeCSStandardAnalyzer(), text);

        System.out.println("DeCSSimpleAnalyzer");
        displayTokensWithFullDetails(new DeCSSimpleAnalyzer(), text);

        System.out.println("DeCSKeywordAnalyzer");
        displayTokensWithFullDetails(new DeCSKeywordAnalyzer(), text);

 */

/* lucene standards
        System.out.println("\n----");
        System.out.println("SimpleAnalyzer");
        displayTokensWithFullDetails(new SimpleAnalyzer(), text);
     
        System.out.println("\n----");
        System.out.println("StandardAnalyzer");
        displayTokensWithFullDetails(new StandardAnalyzer(), text);
 */

    }
}
