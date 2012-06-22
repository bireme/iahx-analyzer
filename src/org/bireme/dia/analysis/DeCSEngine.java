package org.bireme.dia.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class DeCSEngine implements SynonymEngine {
    private RAMDirectory ramDir;
    IndexSearcher searcher;
    HashMap map = new HashMap();
    
    // flag para informar se sera gerado chaves com as categorias e sinomimos dos descritores
    private boolean addCategory;
    private boolean addSyn;
    
    public DeCSEngine(String indexPath, boolean category, boolean syn) {
        
        this.addCategory = category;
        this.addSyn = syn;
        File indexDir = new File(indexPath);
        
        try{
            ramDir = new RAMDirectory();
            Directory.copy(FSDirectory.open(indexDir), ramDir, false);
            
            searcher = new IndexSearcher(ramDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public String[] getSynonyms(String term) throws IOException {
        ArrayList synList = new ArrayList();
        String descriptor = null, qualifier = null, searchIndex = null, abbreviation = null;
        Document decsTerm = null;
        Document decsQlf =  null;
        Document decs = null;
        
        // tratamento para descritor padrao LILACS
        if (term.startsWith("^d")){            
            //retira antes de indexar o qualificador
            if (term.contains("^s")){
                descriptor = term.substring(2,term.indexOf("^s"));
                qualifier =  term.substring(term.indexOf("^s")+2);
            }else{
                descriptor = term.substring(2);
            }           
        }else{
            // tratamento para descritores livres /
            if (term.contains("/")){
                descriptor = term.substring(0,term.indexOf('/'));
                qualifier = term.substring(term.indexOf('/')+1);
            }else{
                descriptor = term;
            }            
        }
        // verifica se eh um descritor codificado DeCS e seta o indice de busca
        if (descriptor.matches("[0-9]+")){
            searchIndex = "id";            
        }else{
            searchIndex = "descriptor";
        }

        if (descriptor != null && !descriptor.equals("")){
            decsTerm = decsKey(descriptor, searchIndex);
            
            if (decsTerm != null){
                synList.addAll(extractKeyValues(decsTerm));
            }
        }
        
        // realiza o join entre o descritor e qualificador (usando o campo de termo autorizado)
        if (qualifier != null && !qualifier.equals("")){
            decsQlf = decsKey(qualifier, searchIndex);            
           
            if (decsTerm != null && decsQlf != null){
                String[] joinDesc = decsTerm.getValues("descriptor_full");
                String[] joinQlf  = decsQlf.getValues("descriptor_full");
                
                String joinDescQlf;
                for (int i = 0; i < joinDesc.length; i++) {
                    joinDescQlf = joinDesc[i];
                    if ( ! joinQlf[i].startsWith("/") ){
                        joinDescQlf += "/";
                    }
                    joinDescQlf  += joinQlf[i];

                    synList.add(joinDescQlf);
                    
                    // adiciona tambem a chave contendo o descritor com o qualificador no formato abreviatura (ex. /di = diagnostico)
                    if (decsQlf.get("abbreviation") != null){
                        abbreviation = decsQlf.get("abbreviation");
                        joinDescQlf = joinDesc[i] + "/" + abbreviation;    
                        synList.add(joinDescQlf); 
                    }
               }
            }            
        }        
        
        return (String[]) synList.toArray(new String[0]);
    }
    
    private Document decsKey(String code, String index) throws IOException {
        PhraseQuery query = new PhraseQuery();
        Document key = null;
                
        // remove zeros a esquerda do codigo para match com indice de ID do DeCS
        code = code.replaceAll("^0*","");
        
        query.add(new Term(index, code));
        TopDocs hits = searcher.search(query, 1);
        
        if (hits.totalHits > 0){
            int docID = hits.scoreDocs[0].doc;
            key = searcher.doc(docID);
        }
        
        return key;
    }
    
    private List extractKeyValues(Document key) {
        String[] keySyn;
        ArrayList keyValues = new ArrayList();
        
        // add authorized terms in 3 languages
        for (String term : key.getValues("descriptor")) {            
            keyValues.add(term);
        }
        
        // add descriptor synonymous in search keys
        if (this.addSyn == true){            
            keySyn = key.getValues("syn");
            //System.out.println("DEBUG -- total of synonymous : " + keySyn.length);
            if (keySyn.length > 0 ){
                // extrai sinonimos
                for (String syn : keySyn) {
                    //System.out.println("DEBUG -- synonymous : " + syn );
                    keyValues.add(syn);
                }
            }
        }
        
        // add descriptor category in search keys
        if (this.addCategory == true){
            for (String category : key.getValues("category")) {
                //System.out.println("DEBUG -- category : " + category );
                keyValues.add(category);
            }
        }
        
        return keyValues;
    }
}
