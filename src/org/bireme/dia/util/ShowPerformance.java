package org.bireme.dia.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;
import org.bireme.dia.analysis.BVSStandardAnalyzer;
import org.bireme.dia.analysis.DeCSAnalyzer;

/**
 *
 * @author Heitor Barbieri
 * @date  20220411
 */
public class ShowPerformance {
    public static void main(String[] args) throws IOException, ParseException {
        final String INPUT_FILE = "./lilacs_2_12_83_utf8.txt";
        final String INDEX_DIR = "./index";
        
        final BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE));
        final Map<String,Analyzer> analyzerPerField = new HashMap<String,Analyzer>();
        final Analyzer titleAnalyzer = new DeCSAnalyzer();
        final Analyzer abstractAnalyzer = new BVSStandardAnalyzer();
        
        analyzerPerField.put("title", titleAnalyzer);
        analyzerPerField.put("abstract", abstractAnalyzer);
        
        final Path indexPath = new File(INDEX_DIR).toPath();
        IOUtils.rm(indexPath);
        
        final long time1 = (new GregorianCalendar()).getTimeInMillis();
        System.out.print("Creating the index ...");
        
        final PerFieldAnalyzerWrapper analyzer =
           new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);
        final Directory directory = FSDirectory.open(indexPath);
        final IndexWriterConfig config = new IndexWriterConfig(analyzer);
        final IndexWriter iwriter = new IndexWriter(directory, config);
        
        while (true) {
            final Document doc = getNextDoc(reader);
            if (doc == null) break;
            
            iwriter.addDocument(doc);
        }
        
        System.out.println(" OK");
        final long time2 = (new GregorianCalendar()).getTimeInMillis();
        System.out.print("Optimizing the index ...");
        iwriter.forceMerge(1); // optimize index
        iwriter.close();
        
        System.out.println(" OK");
        final long time3 = (new GregorianCalendar()).getTimeInMillis();
        System.out.print("Searching ...");
        
        final DirectoryReader ireader = DirectoryReader.open(directory);
        final IndexSearcher isearcher = new IndexSearcher(ireader);
        final QueryParser parserTitle = new QueryParser("title", titleAnalyzer);
        final Query titleQuery = parserTitle.parse("saude OR salud OR health");
        final ScoreDoc[] titleHits = isearcher.search(titleQuery, 10000).scoreDocs;
  
        // Iterate through the results:
        for (int i = 0; i < titleHits.length; i++) {
            final Document titleHitDoc = isearcher.doc(titleHits[i].doc);
      
            System.out.println("title id=" + titleHitDoc.get("id"));
        }
        
        final QueryParser parserAbstract = new QueryParser("abstract", abstractAnalyzer);
        final Query abstractQuery = parserAbstract.parse("saude OR salud OR health");
        final ScoreDoc[] abstractHits = isearcher.search(abstractQuery, 10000).scoreDocs;
  
        // Iterate through the results:
        for (int i = 0; i < abstractHits.length; i++) {
            final Document abstractHitDoc = isearcher.doc(abstractHits[i].doc);
      
            System.out.println("abstract id=" + abstractHitDoc.get("id"));
        }
        final long time4 = (new GregorianCalendar()).getTimeInMillis();
        
        ireader.close();
        directory.close();
        //IOUtils.rm(indexPath);
        
        printStatistics(time1, time2, time3, time4, titleHits.length, abstractHits.length);
    }
    
    private static Document getNextDoc(BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        if (line == null) return null;
        
        final String linet = line.trim();
        if (linet.isEmpty()) return getNextDoc(reader);
        else {
            final String[] split = linet.split("\\~\\^", 3);
            if (split.length != 3) return getNextDoc(reader);
            else {
                final Document doc = new Document();
                doc.add(new Field("id", split[0], TextField.TYPE_STORED));
                doc.add(new Field("title", split[1], TextField.TYPE_STORED));
                doc.add(new Field("abstract", split[2], TextField.TYPE_STORED));
                
                return doc;
            }
        }
    }
    
    private static void printStatistics(long time1,
                                        long time2,
                                        long time3,
                                        long time4,
                                        long titleHits,
                                        long abstractHits) {
        System.out.println("\n========================================");
        System.out.println("Index creation: " + (time2 - time1) + "ms");
        System.out.println("Index optmization: " + (time3 - time2) + "ms");
        System.out.println("Title search:" + titleHits + " documents found");
        System.out.println("Abstract search:" + abstractHits + " documents found");
        System.out.println("Search: " + (time4 - time3) + "ms");
        System.out.println("Total time: " + (time4 - time1) + "ms");
    }
}
