package org.bireme.dia.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Heitor Barbieri
 * @date 20220406
 */
public class ShowTermDocs {
    public static TreeSet<String> getFieldNames(final DirectoryReader reader) throws IOException {
        final TreeSet<String> fieldnames = new TreeSet<String>();
    
        for (LeafReaderContext subReader : reader.leaves()) {
            final FieldInfos fieldInfos = subReader.reader().getFieldInfos();
            for (FieldInfo finfo: fieldInfos) {
                fieldnames.add(finfo.name);
            }
        }  
        return fieldnames; 
    }
    
    public static TreeSet<String> getTerms(final DirectoryReader reader,
                                           final String fieldName) throws IOException {
        final TreeSet<String> outTerms = new TreeSet<String>();
        
        final List<LeafReaderContext> leaves = reader.leaves(); 
        if (leaves.isEmpty()) {
            throw new IOException("empty leaf readers list");
        }    
        final Terms terms = leaves.get(0).reader().terms(fieldName);
        if (terms != null) {
            final TermsEnum tenum = terms.iterator();

            while (true) {
                final BytesRef br = tenum.next();
                if (br == null) {
                    break;
                }
                outTerms.add(br.utf8ToString());
            }
        }
        return outTerms;
    }
    
    public static TreeSet<String> getIds(final IndexSearcher searcher,
                                         final String fieldName,
                                         final String term,
                                         final String idFieldName) throws IOException {
        final TreeSet<String> ids = new TreeSet<String>();
        final TermQuery query = new TermQuery(new Term(fieldName, term));
        final int count = searcher.count(query);
        
        //if (count <= 1000) {
            final TopDocs topdocs = searcher.search(query, count);
            final IndexReader reader = searcher.getIndexReader();
        
            for (ScoreDoc sdoc: topdocs.scoreDocs) {
                final Document doc = reader.document(sdoc.doc);
                final String idField = doc.get(idFieldName);

                if (idField != null) ids.add(idField);
            } 
        //}
        return ids;
    }
    
    private static void usage() {
        System.err.println("usage: ShowTermDocs <options>");
        System.err.println("Options:");
        System.err.println("\t-index=<path> Path to the Lucene index");
        System.err.println("\t-idFieldName=<name> Name of the field that has the document id");
        System.err.println("\t-outFile=<path> Path of the output report file");
        System.exit(1);
    }
    
    public static void main(String[] args) throws IOException {
        String index = null;
        String idFieldName = null;
        String outFile = null;
        
        final long time1 = (new GregorianCalendar()).getTimeInMillis();
        
        for (String param: args) {
            if (param.startsWith("-index=")) index = param.substring(7);
            else if (param.startsWith("-idFieldName=")) idFieldName = param.substring(13);
            else if (param.startsWith("-outFile=")) outFile = param.substring(9);
        }
        if ((index == null) || (idFieldName == null) || (outFile == null)) usage();
        
        final BufferedWriter writer = Files.newBufferedWriter(
                                                 new File(outFile).toPath(),
                                                 Charset.forName("utf-8"));
        final Directory directory = FSDirectory.open(
                                           new File(index).toPath());
        final DirectoryReader reader = DirectoryReader.open(directory);
        final IndexSearcher searcher = new IndexSearcher(reader);
        int current = 0;
        
        for (String fieldName: getFieldNames(reader)) {
            for (String term: getTerms(reader, fieldName)) {
                final TreeSet<String> ids = getIds(searcher, fieldName, term, idFieldName);
                boolean first = true;
                
                if (!ids.isEmpty()) {
                    if (current++ % 10000 == 0) {
                        System.out.println("field=[" + fieldName + "] term=[" + term + "]");
                    }
                    writer.write("  >> field=[" + fieldName + "] term=[" + term + "]: ");
                    for (String id: ids) {
                        if (first) first = false; else writer.write(",");
                        writer.write(id);
                    }
                    writer.write("\n");
                }
            }
        }
        
        writer.close();
        reader.close();
        
        final long time2 = (new GregorianCalendar()).getTimeInMillis();
        System.out.println("Total time:" + ((time2 - time1) * 1000) + "s");
    }
}
