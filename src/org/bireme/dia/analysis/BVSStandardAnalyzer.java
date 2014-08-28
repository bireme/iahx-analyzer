package org.bireme.dia.analysis;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.util.Version;

public class BVSStandardAnalyzer extends Analyzer {
    public static final boolean WORDS = true;
    public static final boolean CATEGORY = true;
    public static final boolean SYN = true;
    public static final boolean PRECOD = true;          // somente gera tokens para descritores que estiverem
                                                        // precodificados no formato ^didentificador^squalificador
    public static final boolean KEYQLF = true;
    public static final boolean ONLYQLF = false;

    private final SynonymEngine engine;

    public BVSStandardAnalyzer() throws IOException {
        final ClassLoader loader = this.getClass().getClassLoader();
        final URL dirUrl = loader.getResource("./"); // get current directory of classes

        engine = new DeCSEngine("resources/decs/main", CATEGORY, SYN, KEYQLF,
                                                                       ONLYQLF);
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName,
                                                     final Reader reader) {
        final Tokenizer source = new WhitespaceTokenizer(Version.LUCENE_4_9,
                                                                        reader);
        final TokenStream filter1 = new ASCIIFoldingFilter(source);
        final TokenStream filter2 = new LowerCaseFilter(Version.LUCENE_4_9,
                                                        filter1);
        final TokenStream filter3 = new SynonymFilter(filter2, engine,
                                                                 WORDS, PRECOD);
        final TokenStream filter4 = new LowerCaseFilter(Version.LUCENE_4_9,
                                                        filter3);
        final TokenStream filter5 = new ASCIIFoldingFilter(filter4);
        final TokenStream filter6 = new PunctuationFilter(filter5);

        return new TokenStreamComponents(source, filter6);
    }
}