package org.bireme.dia.analysis;

import org.apache.lucene.analysis.util.CharTokenizer;

import java.io.Reader;
import org.apache.lucene.util.Version;

/**
 * CharTokenizer limits token width to 255 characters, though.
 * This implementation assumes keywords are 255 in length or less.
 */
public class KeywordTokenizer extends CharTokenizer {

    public KeywordTokenizer(final Reader in) {
        super(Version.LUCENE_4_9, in);
    }

    @Override
    protected boolean isTokenChar(int c) {
        return true;
    }
}
