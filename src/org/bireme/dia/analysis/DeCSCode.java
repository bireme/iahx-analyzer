package org.bireme.dia.analysis;

import java.io.IOException;
import java.net.URL;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;


public class DeCSCode {
    private RAMDirectory directory;
    private IndexSearcher decs;
    
    public DeCSCode() throws IOException {
        final ClassLoader loader = this.getClass().getClassLoader();
        URL dirUrl = loader.getResource("./"); // get current directory of classes

        try{
            directory = new RAMDirectory("resources/decs/code");
            decs = new IndexSearcher(directory);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getDescritorCode(String descriptor) throws IOException{
        String descriptorCode = null;
        Query query = null;
        
        if ( !descriptor.equals("")){
            
            String descriptorPhrase = "\"" + descriptor + "\"";            
            QueryParser qParser = new QueryParser("descriptor", new SimpleKeywordAnalyzer());
            
            try {
                query = qParser.parse(descriptorPhrase);
            } catch (ParseException ex) {
                System.out.println("metodo geDescriptorCode() -> params descriptor: " + descriptor);
                ex.printStackTrace();
            }
            
            Hits hits = decs.search(query);
            
            if (hits.length() > 0){
                Document doc = hits.doc(0);
                descriptorCode = doc.get("id");
            }
        }
        return descriptorCode;
    }
    
    public String getDescriptorTerm(String code, String lang) throws IOException{
        String[] descriptorTerm = null;
        PhraseQuery query = new PhraseQuery();
        
        query.add( new Term("id", code) );
        Hits hits = decs.search(query);
        
        if (hits.length() > 0){
            Document doc = hits.doc(0);
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
        
        System.out.println( decs.getDescriptorTerm("1532","es") );
        
        System.out.println( decs.getDescritorCode("acido aminolevulINICO") );
    }
}
