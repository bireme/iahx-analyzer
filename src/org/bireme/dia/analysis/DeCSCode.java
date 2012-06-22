package org.bireme.dia.analysis;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;


public class DeCSCode {
    private RAMDirectory ramDir;
    private IndexSearcher decs;
    
    public DeCSCode() throws IOException {
        final ClassLoader loader = this.getClass().getClassLoader();
        URL dirUrl = loader.getResource("./"); // get current directory of classes

        File indexDir = new File("resources/decs/code");
        
        try{            
            ramDir = new RAMDirectory();
            Directory.copy(FSDirectory.open(indexDir), ramDir, false);
            
            //directory = new RAMDirectory("resources/decs/code");
            decs = new IndexSearcher(ramDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getDescritorCode(String descriptor) throws IOException{
        String descriptorCode = null;
        Query query = null;
        
        if ( !descriptor.equals("")){
            
            String descriptorPhrase = "\"" + descriptor + "\"";            
            QueryParser qParser = new QueryParser(Version.LUCENE_30,
"descriptor", new SimpleKeywordAnalyzer());
            
            try {
                query = qParser.parse(descriptorPhrase);
            } catch (ParseException ex) {
                System.out.println("metodo geDescriptorCode() -> params descriptor: " + descriptor);
                ex.printStackTrace();
            }
            
            TopDocs hits = decs.search(query, 1);
            
            if (hits.totalHits > 0){
                int docID = hits.scoreDocs[0].doc;
                Document doc = decs.doc(docID);
                descriptorCode = doc.get("id");
            }
        }
        return descriptorCode;
    }
    
    public String getDescriptorTerm(String code, String lang) throws IOException{
        String[] descriptorTerm = null;
        PhraseQuery query = new PhraseQuery();
        
        query.add( new Term("id", code) );
        TopDocs hits = decs.search(query, 1);
        
        if (hits.totalHits > 0){
            int docID = hits.scoreDocs[0].doc;
            Document doc = decs.doc(docID);
            descriptorTerm = doc.getValues("descriptor");
        
            if (lang == "en"){
                return descriptorTerm[0];
            }else if (lang == "es"){
                return descriptorTerm[1];
            }else{
                return descriptorTerm[2];
            }
        }

        return null;
    }
    
    public static void main(String[] args) throws IOException {
        DeCSCode decs = new DeCSCode();
        
        System.out.println( decs.getDescriptorTerm("1532","en") );
        
        System.out.println( decs.getDescritorCode("acido aminolevulINICO") );
    }
}
