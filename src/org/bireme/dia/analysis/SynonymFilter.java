package org.bireme.dia.analysis;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Set;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public class SynonymFilter extends TokenFilter {
    private final Deque<String> synonymDeque;
    private final SynonymEngine engine;
    private final boolean addWords;
    private final boolean processOnlyPrecodTerms;
    private final CharTermAttribute termAtt;
    private final PositionIncrementAttribute posIncrAtt;

    private AttributeSource.State current;

    public SynonymFilter(final TokenStream in,
                         final SynonymEngine engine,
                         final boolean words, // generate separate tokens (search keys) word by word or term
                         final boolean precod) { //check if only process precod (^d28631) terms
        super(in);
        synonymDeque = new ArrayDeque<String>();
        this.engine = engine;
        this.addWords = words;
        this.processOnlyPrecodTerms = precod;

        this.termAtt = addAttribute(CharTermAttribute.class);
        this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        final boolean ret;
        
        if (! synonymDeque.isEmpty()) {
            final String syn = synonymDeque.remove();
            restoreState(current);
            termAtt.setEmpty();
            termAtt.append(syn);
            //posIncrAtt.setPositionIncrement(0);
            ret = true;
        } else if (!input.incrementToken()) {
            input.end();
            ret = false;
        } else {
            if (addAliasesToDeque()) {
                current = captureState();
            }
            ret = true;
        }
        return ret;        
    }

    private boolean addAliasesToDeque() throws IOException {
        final boolean ret;
        final String currentTerm = termAtt.toString();
        //check if only process precod (^d28631) terms
        if (processOnlyPrecodTerms && !currentTerm.startsWith("^d")) {
            ret = false;
        } else {                
            final Set<String> synonyms = engine.getSynonyms(currentTerm);
            if (synonyms == null) {
                ret = false;
            } else {
                for (String synonym : synonyms) {
                    // first add the original term to the index
                        synonymDeque.add(synonym);
                    // generate separate tokens (search keys) word by word or term
                    if (addWords) {
                        synonymDeque.addAll(
                                          Arrays.asList(synonym.split(" +-/")));
                    }
                }
                ret = true;
            }
        }        
        return ret;
    }
}