package org.bireme.dia.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Token;
import java.io.IOException;
import java.util.Stack;

public class SynonymFilter extends TokenFilter {
    public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";
    public static final String TOKEN_TYPE_WORD = "WORD";
    
    private Stack synonymStack;
    private SynonymEngine engine;
    private Token token;
    private boolean addWords;
    private boolean processOnlyPrecodTerms = false;
    
    public SynonymFilter(TokenStream in, SynonymEngine engine, boolean words, boolean precod) {
        super(in);
        synonymStack = new Stack();
        this.engine = engine;
        this.addWords = words;        
        this.processOnlyPrecodTerms = precod;
    }
    
    public Token next() throws IOException {
        if (synonymStack.size() > 0) {
            return (Token) synonymStack.pop();
        }
        token = input.next();
        
        if (token == null) {
            return null;
        }
        if (this.processOnlyPrecodTerms == true && !token.termText().startsWith("^d")){
            return token;            
        }
        
        addAliasesToStack(token);
        
        if (token.termText().startsWith("^d")){
            // não armazena no índice a entrada codificada do termo decs
            // ex. ^d30600^s/dt/ps/*pc/vi == somente irar gerar as chaves contendo o termo nos diferentes idiomas
            token.setTermText("");
        }    
        return token;
    }
    
    private void addAliasesToStack(Token token) throws IOException {
        String[] synonyms = engine.getSynonyms(token.termText());
        String text;

        if (synonyms == null) return;
        
        for (int i = 0; i < synonyms.length; i++) {
            text = synonyms[i];
            Token synToken = new Token(text,
                                    0,text.length(),
                                    TOKEN_TYPE_SYNONYM);
            //synToken.setPositionIncrement(0);

            // gera palavras dos termos como tokens (opção default)
            if (this.addWords == true){
                addWordsToStack(synonyms[i]);
            }else{
                synonymStack.addElement(synToken);
            }
        }
    }
    
    private void addWordsToStack(String term) throws IOException {
        String text;
        String[] termWord;
        Integer text_start, text_offset;

        term = term.replaceAll("-", " "); // split omega-3 in 2 tokens (omega 3)
        termWord = term.split(" ");

        if (termWord.length > 0){
            // inverte a ordem de percorrer os termos devido ao stack sempre adicionar no inicio da pilha
            for (int i = termWord.length-1; i >= 0; i--) {
                text = termWord[i];
                text_start = term.indexOf(text);
                text_offset = text_start + text.length();
                Token wordToken = new Token(text,
                                        text_start, text_offset,
                                        TOKEN_TYPE_WORD);
                //wordToken.setPositionIncrement(0);
            
                synonymStack.addElement(wordToken);
            }        
        }      
    }
} 