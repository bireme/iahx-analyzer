package org.bireme.dia.analysis;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;

public class DeCSCode {
    private final IndexSearcher decs;

    public DeCSCode() throws IOException {
        final ClassLoader loader = this.getClass().getClassLoader();
        final URL dirUrl = loader.getResource("./"); // get current directory of classes
        final File indexDir = new File("resources/decs/code");
        final RAMDirectory ramDir = new RAMDirectory(
                                    FSDirectory.open(indexDir), IOContext.READ);
        final DirectoryReader reader = DirectoryReader.open(ramDir);

        decs = new IndexSearcher(reader);
    }

    public String getDescritorCode(final String descriptor) throws IOException,
                                                                ParseException {
        String descriptorCode = null;

        if (!descriptor.equals("")) {

            final String descriptorPhrase = "\"" + descriptor + "\"";
            final QueryParser qParser = new QueryParser(Version.LUCENE_4_9,
                                     "descriptor", new SimpleKeywordAnalyzer());
            final Query query = qParser.parse(descriptorPhrase);
            final TopDocs hits = decs.search(query, 1);

            if (hits.totalHits > 0){
                final int docID = hits.scoreDocs[0].doc;
                final Document doc = decs.doc(docID);
                descriptorCode = doc.get("id");
            }
        }
        return descriptorCode;
    }

    public String getDescriptorTerm(final String code,
                                    final String lang) throws IOException {
        final PhraseQuery query = new PhraseQuery();
        query.add(new Term("id", code));

        final TopDocs hits = decs.search(query, 1);
        String ret = null;

        if (hits.totalHits > 0) {
            final int docID = hits.scoreDocs[0].doc;
            final Document doc = decs.doc(docID);
            final String[] descriptorTerm = doc.getValues("descriptor");

            if (lang.equals("en")) {
                ret = descriptorTerm[0];
            } else if (lang.equals("es")) {
                ret = descriptorTerm[1];
            } else {
                ret = descriptorTerm[2];
            }
        }

        return ret;
    }

    public static void main(String[] args) throws IOException, ParseException {
        final DeCSCode decs = new DeCSCode();

        System.out.println(decs.getDescriptorTerm("1532","en"));
        System.out.println(decs.getDescritorCode("acido aminolevulINICO"));
    }
}
