package org.bireme.dia.analysis;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.bireme.dia.util.AnalyzerUtils;

public class DeCSEngine implements SynonymEngine {
    private final Analyzer analyzer;
    private final DirectoryReader reader;
    private final IndexSearcher searcher;

    // flag para informar se sera gerado chaves com as categorias e sinomimos dos descritores
    private final boolean addCategory;
    private final boolean addSyn;
    private final boolean keysForQualifiers;
    private final boolean onlyQualifiers;

    public DeCSEngine(final String indexPath,
                      final boolean category, // add descriptor category in search keys
                      final boolean syn,      // add descriptor synonymous in search keys
                      final boolean keyqlf,   // add descriptor qualifier in search keys
                      final boolean onlyqlf) throws IOException {
        if (indexPath == null) {
            throw new NullPointerException("indexPath");
        }
        this.addCategory = category;
        this.addSyn = syn;
        this.keysForQualifiers = keyqlf;
        this.onlyQualifiers = onlyqlf;

        final File indexDir = new File(indexPath);
        final RAMDirectory ramDir = new RAMDirectory(
                                    FSDirectory.open(indexDir), IOContext.READ);
        analyzer = new SimpleKeywordAnalyzer();
        reader = DirectoryReader.open(ramDir);
        searcher = new IndexSearcher(reader);
    }
    
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public Set<String> getSynonyms(final String term) throws IOException {        
        final String descriptor;
        final String qualifier;
                
        if (term == null) {
            throw new NullPointerException("term");
        }
        
        if (term.startsWith("^d")) { // tratamento para descritor padrao LILACS
            //retira antes de indexar o qualificador
            final int idx = term.indexOf("^s");
            if (idx == -1) {
                descriptor = term.substring(2).trim();
                qualifier = null;
            } else {
                descriptor = term.substring(2, idx).trim();
                qualifier =  term.substring(idx + 2).trim();
            }
        } else {  // tratamento para descritores livres /            
            final int idx = term.indexOf("/");
            if (idx == -1) {
                descriptor = term.trim();
                qualifier = null;
            } else {
                descriptor = term.substring(0, idx).trim();
                qualifier = term.substring(idx).trim();
            }
        }
        
        return getSynonyms(descriptor, qualifier);
    }

    private Set<String> getSynonyms(final String descriptor,
                                    final String qualifier) throws IOException{
        assert !descriptor.isEmpty();
        
        final Set<String> synSet = new HashSet<String>();
        
        // verifica se eh um descritor codificado DeCS e seta o indice de busca
        final String searchIndex = (descriptor.matches("[0-9]+")) 
                                                         ? "id" : "descriptor";
        final Document decsTerm = decsKey(descriptor, searchIndex);        
        if (decsTerm != null) {
            if (! onlyQualifiers) {
                synSet.addAll(extractKeyValues(decsTerm, false));
            }            
            if ((qualifier != null) && (! qualifier.isEmpty())) {
                final Document decsQlf = decsKey(qualifier, searchIndex);
                if (decsQlf != null) {
                    if (keysForQualifiers) {
                        synSet.addAll(extractKeyValues(decsQlf, true));
                    }
                    if (! onlyQualifiers) {
                        final String[] joinDesc = decsTerm.getValues("descriptor");
                        final String[] joinQlf  = decsQlf.getValues("descriptor");
                        final String abbreviation = decsQlf.get("abbreviation");

                        for (String jDesc : joinDesc) {
                            for (String jQlf : joinQlf) {
                                synSet.add(jDesc + jQlf);
                            }
                            // adiciona tambem a chave contendo o descritor com o qualificador no formato abreviatura (ex. /di = diagnostico)
                            if (abbreviation != null) {                                
                                synSet.add(jDesc + "/" + abbreviation);
                            }
                        }
                    }
                }
            }
        }
        return synSet;
    }
    
    private Document decsKey(final String content,
                             final String fldName) throws IOException {
        assert content != null;
        assert fldName != null;                        

        // remove zeros a esquerda do codigo para match com indice de ID do DeCS
        final String code1 = (fldName.equals("id")) 
                                                ? content.replaceAll("^0*","")
                                                : content;
        final String code2 = AnalyzerUtils.getTokens(analyzer, code1);
        final TermQuery query = new TermQuery(new Term(fldName, code2));        
        final TopDocs hits = searcher.search(query, 1);
        final Document key = (hits.totalHits > 0) 
                                           ? searcher.doc(hits.scoreDocs[0].doc)
                                           : null;
        return key;
    }

    private Set<String> extractKeyValues(final Document key,
                                         final boolean stripSleash) {
        assert key != null;
        
        final Set<String> keyValues = new HashSet<String>();
        
        // add authorized terms in 3 languages        
        addArray(key.getValues("descriptor"), keyValues, stripSleash);
                
        if (addSyn) {  // add descriptor synonymous in search keys
            addArray(key.getValues("syn"), keyValues, stripSleash);
        }                
        if (addCategory) { // add descriptor category in search keys
            addArray(key.getValues("category"), keyValues, false);
        }

        return keyValues;
    }
    
    private void addArray(final String[] in,
                          final Set<String> out,
                          final boolean stripFirstChar) {        
        assert out != null;
        
        if (in != null) {
            for (String str : in) {
                out.add(stripFirstChar ? str.substring(1) : str);
            }
        }
    }
}
