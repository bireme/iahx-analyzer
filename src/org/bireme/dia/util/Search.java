
package org.bireme.dia.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.bireme.dia.analysis.BVSStandardAnalyzer;

/**
 *
 * @author heitor
 */
public class Search {
    public static void main(String[] args) throws IOException, ParseException {
        final String INDEX_DIR = "./index";
        
        final Path indexPath = new File(INDEX_DIR).toPath();
        final Directory directory = FSDirectory.open(indexPath);
        final DirectoryReader ireader = DirectoryReader.open(directory);
        final IndexSearcher isearcher = new IndexSearcher(ireader);
        final Analyzer abstractAnalyzer = new BVSStandardAnalyzer();
        final QueryParser parserAbstract = new QueryParser("abstract", abstractAnalyzer);
        final Query titleQuery = parserAbstract.parse("\\(24");
        final ScoreDoc[] titleHits = isearcher.search(titleQuery, 10000).scoreDocs;
        final TreeSet<String> ids = new TreeSet<String>();   
        
        // Iterate through the results:
        for (int i = 0; i < titleHits.length; i++) {
            final Document titleHitDoc = isearcher.doc(titleHits[i].doc);
      
            ids.add(titleHitDoc.get("id"));
        }
        for (String id: ids) {
            System.out.println("title id=" + id);
        }
        
        ireader.close();
        directory.close();
    }
}
