package org.bireme.dia.analysis;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;


public class DeCSQualifierAnalyzer extends Analyzer {
    public static final boolean WORDS = false;
    public static final boolean CATEGORY = false;
    public static final boolean SYN = true;
    public static final boolean PRECOD = false;
    public static final boolean KEYQLF = true;
    public static final boolean ONLYQLF = true;
    
    private SynonymEngine engine;


    public DeCSQualifierAnalyzer() throws IOException {
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
        result = new RemoveSlashFilter(result);
        return result;
    }
}
