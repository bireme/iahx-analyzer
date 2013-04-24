package org.bireme.dia.analysis;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import java.io.Reader;
import java.net.URL;

public class DeCSStandardAnalyzer extends Analyzer {
    public static final boolean WORDS = true;
    public static final boolean CATEGORY = true;
    public static final boolean SYN = true;
    public static final boolean PRECOD = false;
    public static final boolean KEYQLF = false;
    public static final boolean ONLYQLF = false;
    

    private SynonymEngine engine;

    public DeCSStandardAnalyzer() throws IOException {
        final ClassLoader loader = this.getClass().getClassLoader();
        URL dirUrl = loader.getResource("./"); // get current directory of classes


        engine = new DeCSEngine("resources/decs/main", CATEGORY, SYN, KEYQLF, ONLYQLF);
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result;

        // passa a entrada (reader) pelo filtros de sinônimos, acentos e lowercase
        result = new SynonymFilter(
                    new LowerCaseFilter(
                        new ISOLatin1AccentFilter(
                            new KeywordTokenizer(reader))),
                                            engine, WORDS, PRECOD);

        // passa a saida pelo filtros de acentos e lowercase
        result = new LowerCaseFilter(result);
        result = new ISOLatin1AccentFilter(result);
        return result;
    }
}
