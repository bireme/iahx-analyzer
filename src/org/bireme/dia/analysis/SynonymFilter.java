package org.bireme.dia.analysis;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public class SynonymFilter extends TokenFilter {
    public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";
    public static final String TOKEN_TYPE_WORD = "WORD";

    private final Stack<String> synonymStack;
    private final SynonymEngine engine;
    private final boolean addWords;
    private final boolean processOnlyPrecodTerms;
    private final CharTermAttribute termAtt;
    private final PositionIncrementAttribute posIncrAtt;

    private AttributeSource.State current;

    public SynonymFilter(final TokenStream in,
                         final SynonymEngine engine,
                         final boolean words,
                         final boolean precod) {
        super(in);
        synonymStack = new Stack<String>();
        this.engine = engine;
        this.addWords = words;
        this.processOnlyPrecodTerms = precod;

        this.termAtt = addAttribute(CharTermAttribute.class);
        this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
    if (! synonymStack.isEmpty()) {
            final String syn = synonymStack.pop();
            restoreState(current);

            termAtt.setEmpty();
            termAtt.append(syn.trim());
            //posIncrAtt.setPositionIncrement(0);
            return true;
    }

    if (!input.incrementToken()) {
            return false;
        }

    if (addAliasesToStack()) {
            current = captureState();
    }

    return true;
    }

    private boolean addAliasesToStack() throws IOException {
        String currentTerm = termAtt.toString();
        //check if only process precod (^d28631) terms
        if (processOnlyPrecodTerms && !currentTerm.startsWith("^d")) {
            return false;
        }

        final String[] synonyms = engine.getSynonyms(currentTerm);
        if (synonyms == null) {
            return false;
        }

        for (String synonym : synonyms) {
            // generate separate tokens (search keys) word by word or term
            if (addWords) {
                // first add the original term to the index
                synonymStack.push(synonym);

                final String synonym_normalized = synonym.replaceAll("/", " ");
                final String[] termWords = synonym_normalized.split(" ");

                if (termWords.length > 1){
                    // inverte a ordem de percorrer os termos devido ao stack sempre adicionar no inicio da pilha
                    for (int i = termWords.length-1; i >= 0; i--) {
                        if (!termWords[i].isEmpty()) {
                            synonymStack.push(termWords[i]);
                            if (termWords[i].contains("-")) {
                                // split keys like omega-3 in 2 tokens (omega 3)
                                final List<String> splited_by_hyphen =
                                        Arrays.asList(termWords[i].split("-"));
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