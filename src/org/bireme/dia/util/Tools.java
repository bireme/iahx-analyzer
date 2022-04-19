package org.bireme.dia.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.bireme.dia.analysis.SimpleKeywordAnalyzer;

/**
 *
 * @author Heitor Barbieri
 * date: 20150408
 */
public class Tools {
    public static void showDocuments(final String indexName) throws IOException {
        if (indexName == null) {
            throw new NullPointerException("indexName");
        }
        final Directory directory = FSDirectory.open(
                                           new File(indexName).toPath());
        final DirectoryReader ireader = DirectoryReader.open(directory);
        
        //final Bits liveDocs = MultiFields.getLiveDocs(ireader);
        final Bits liveDocs = MultiBits.getLiveDocs(ireader);
        for (int i = 0; i < ireader.maxDoc(); i++) {
            if (liveDocs != null && !liveDocs.get(i)) {
                continue;
            }

            final Document doc = ireader.document(i);
            System.out.println(doc);
        }
        directory.close();
    }
    
    public static void showTerms(final String indexName,
                                 final String fieldName) throws IOException {
        if (indexName == null) {
            throw new NullPointerException("indexName");
        }
        if (fieldName == null) {
            throw new NullPointerException("fieldName");
        }
        final Directory directory = FSDirectory.open(
                                           new File(indexName).toPath());
        final DirectoryReader ireader = DirectoryReader.open(directory);        
        //final Terms terms = SlowCompositeReaderWrapper.wrap(ireader)
        //                                                      .terms(fieldName); 
        final List<LeafReaderContext> leaves = ireader.leaves(); 
        if (leaves.isEmpty()) {
            throw new IOException("empty leaf readers list");
        }    
        final Terms terms = leaves.get(0).reader().terms(fieldName);
        final TermsEnum tenum = terms.iterator();
        
        while (true) {
            final BytesRef br = tenum.next();
            if (br == null) {
                break;
            }
            System.out.println("term =[" + br.utf8ToString() + "]");
        }
                
        directory.close();
    }
    
    public static void showTokens(final Analyzer analyzer,
                                  final String fieldName,
                                  final String text) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream(fieldName, text);
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            //int startOffset = offsetAttribute.startOffset();
            //int endOffset = offsetAttribute.endOffset();
            final String term = charTermAttribute.toString();
            
            System.out.println(term);
        }
    }
    
    public static void main(final String[] args) throws IOException {
        final String iname = "resources/decs/main";
        
        showDocuments(iname);
        
        final Analyzer analyzer = new SimpleKeywordAnalyzer();
        final String str = "Abreviaturas como Asunto";
        
        showTokens(analyzer, "synonym", str);
    }
}
