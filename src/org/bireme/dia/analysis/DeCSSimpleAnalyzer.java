package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import java.io.Reader;
import java.net.URL;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.util.Version;

public class DeCSSimpleAnalyzer extends Analyzer {
    public static final boolean WORDS = false;
    public static final boolean CATEGORY = false;
    public static final boolean SYN = false;
    public static final boolean PRECOD = false;
    public static final boolean KEYQLF = false;
    public static final boolean ONLYQLF = false;

    private final SynonymEngine engine;

    public DeCSSimpleAnalyzer() throws IOException {
        final ClassLoader loader = this.getClass().getClassLoader();
        final URL dirUrl = loader.getResource("./"); // get current directory of classes

        engine = new DeCSEngine("resources/decs/main", CATEGORY, SYN, KEYQLF,
                                                                       ONLYQLF);
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName,
                                                     final Reader reader) {
        final Tokenizer source = new KeywordTokenizer(reader);
        final TokenStream filter1 = new ASCIIFoldingFilter(source);
        final TokenStream filter2 = new LowerCaseFilter(Version.LUCENE_4_9,
                                                        filter1);
        final TokenStream filter3 = new SynonymFilter(filter2, engine,
                                                                 WORDS, PRECOD);
        final TokenStream filter4 = new LowerCaseFilter(Version.LUCENE_4_9,
                                                        filter3);
        final TokenStream filter5 = new ASCIIFoldingFilter(filter4);

        return new TokenStreamComponents(source, filter5);
    }
}