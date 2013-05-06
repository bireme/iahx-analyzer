package org.bireme.dia.analysis;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

public class SynonymFilter extends TokenFilter {
    public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";
    public static final String TOKEN_TYPE_WORD = "WORD";
    
    private Stack<String> synonymStack;
    private SynonymEngine engine;
    private AttributeSource.State current;
    private final TermAttribute termAtt;
    private final PositionIncrementAttribute posIncrAtt;
    //private Token token;
    private boolean addWords;
    private boolean processOnlyPrecodTerms = false;
    
    public SynonymFilter(TokenStream in, SynonymEngine engine, boolean words, boolean precod) {
        super(in);
        synonymStack = new Stack<String>();
        this.engine = engine;
        this.addWords = words;        
        this.processOnlyPrecodTerms = precod;

        this.termAtt = addAttribute(TermAttribute.class);
        this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    }
    
    public boolean incrementToken() throws IOException {
	if (synonymStack.size() > 0) {
            String syn = synonymStack.pop();                        
            restoreState(current);

            termAtt.setTermBuffer(syn);
            //posIncrAtt.setPositionIncrement(0);
            return true;
	}
        
	if (!input.incrementToken())
            return false;
       
	if (addAliasesToStack()) {
            current = captureState();
	}
	return true;
    }
    
    private boolean addAliasesToStack() throws IOException {
        String currentTerm = termAtt.term();
        //check if only process precod (^d28631) terms
        if ( this.processOnlyPrecodTerms == true && !currentTerm.startsWith("^d") ){
            return false;
        }
        
        String[] synonyms = engine.getSynonyms(currentTerm);
        String synonym_normalized;   
        List<String> splited_by_hyphen = null;
        
        if (synonyms == null) {
            return false;
        }
        for (String synonym : synonyms) {            
            // generate separate tokens (search keys) word by word or term
            if (this.addWords == true){                
                // first add the original term to the index
                synonymStack.push(synonym);                

                synonym_normalized = synonym.replaceAll("/", " "); 
                String[] termWords = synonym_normalized.split(" ");

                if (termWords.length > 1){                    
                    // inverte a ordem de percorrer os termos devido ao stack sempre adicionar no inicio da pilha
                    for (int i = termWords.length-1; i >= 0; i--) {
                        if (!termWords[i].isEmpty()){                            
                            synonymStack.push(termWords[i]);
                            if (termWords[i].contains("-")){
                                // split keys like omega-3 in 2 tokens (omega 3)
                                splited_by_hyphen = Arrays.asList(termWords[i].split("-"));
                                synonymStack.addAll(splited_by_hyphen);
                            }
                        }
                    }        
                }
            } else {
                synonymStack.push(synonym);
            } 
        }
        return true;
    }

} 