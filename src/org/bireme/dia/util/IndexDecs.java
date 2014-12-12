package org.bireme.dia.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.bireme.dia.analysis.SimpleKeywordAnalyzer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author vinicius.andrade
 * date: 20060630
 */
public class IndexDecs extends DefaultHandler {   
    /** A buffer for each XML element */
    private final HashMap<String,String> attributeMap;    
    private final StringBuilder elementBuffer;    
    private final IndexWriter writerMain;         //writer do indice principal
    private final IndexWriter writerCode;         //writer do indice que realiza encode/decode de descritores
    
    private final DecsSyn decsSyn;
    private final StringBuilder path;        
    private final Analyzer analyzer;
    
    public IndexDecs(final String xml) throws IOException /*throws Exception*/ {        
        
        final ClassLoader loader = this.getClass().getClassLoader();
        final URL dirUrl = loader.getResource("./"); // get current directory of classes        
        final Directory indexDirMain = FSDirectory.open(
                                              new File("resources/decs/main/"));
        final Directory indexDirCode = FSDirectory.open(
                                              new File("resources/decs/code/"));

        final IndexWriterConfig iwcMain = new IndexWriterConfig(
                                                   Version.LUCENE_4_10_1,
                                                   new SimpleKeywordAnalyzer());

        final IndexWriterConfig iwcCode = new IndexWriterConfig(
                                                   Version.LUCENE_4_10_1,
                                                   new SimpleKeywordAnalyzer());

        attributeMap = new HashMap<String,String>();
        elementBuffer = new StringBuilder();
        decsSyn = new DecsSyn();
        path = new StringBuilder("/");
        analyzer = new SimpleKeywordAnalyzer();
        
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwcMain.setOpenMode(OpenMode.CREATE);
        iwcCode.setOpenMode(OpenMode.CREATE);
        
        writerMain = new IndexWriter(indexDirMain, iwcMain);

        writerCode = new IndexWriter(indexDirCode, iwcCode);

        try {                        
            System.out.println("Indexing " + xml);
            
            final Date start = new Date();
            
            indexTerms(xml);
            
            System.out.println("Optimizing index...");     
            writerMain.forceMerge(1);
            writerMain.close();
            
            writerCode.forceMerge(1);
            writerCode.close();
            
            System.out.println((new Date()).getTime() - start.getTime() 
                                                       + " total milliseconds");
            
        } catch (IOException e) {
            System.out.println("caught a " + e.getClass() + "\n with message: "
                                                              + e.getMessage());
        }
        
    }
    
    private void indexTerms(String xml){
        final SAXParserFactory factory = SAXParserFactory.newInstance();                        
        final InputSource xmlInput = new InputSource(xml);
        xmlInput.setEncoding("ISO-8859-1");
        
        try {
            final SAXParser sax = factory.newSAXParser();
            final XMLReader reader = sax.getXMLReader();
            
            reader.setEntityResolver(null);
            reader.setContentHandler(this);
            reader.parse(xmlInput); 
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    @Override
    public void startDocument() {
        attributeMap.clear();
    }
    
    @Override
    public void startElement(final String uri, 
                             final String localName, 
                             final String qName, 
                             final Attributes atts) throws SAXException {
        
        path.append(qName).append("/");
        
        if (qName.equals("term")) {
            decsSyn.clear();
            decsSyn.setId(atts.getValue("mfn"));
        }
        
        elementBuffer.setLength(0);
        attributeMap.clear();
        if (atts.getLength() > 0) {
            for (int i = 0; i < atts.getLength(); i++) {
                attributeMap.put(atts.getQName(i), atts.getValue(i));
            }
        }
    }
    
    @Override
    public void characters(final char[] text, 
                           final int start, 
                           final int length) {
        elementBuffer.append(text, start, length);
    }
    
    @Override
    public void ignorableWhitespace(final char[] ch, 
                                    final int start, 
                                    final int length) throws SAXException {        
    }
    
    @Override
    public void endElement(final String uri, 
                           final String localName, 
                           final String qName) throws SAXException {
        
        final String text = elementBuffer.toString();
        
        if (qName.equals("descriptor")) {
            decsSyn.addDescriptor(text);            
        } else if (qName.equals("synonym")) {
            decsSyn.addSynonym(text);            
        } else if( qName.equals("category")) {
            decsSyn.addCategory(text);            
        } else if (qName.equals("abbreviation")) {
            decsSyn.setAbbreviation(text);
        }
        
        if (qName.equals("term")) {
            try {
                index(decsSyn);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void index(DecsSyn decs) throws IOException {        
        // monta indice principal para analizador DeCS
        final Document doc = new Document();        
        doc.add( new StringField("id", decs.getId(), Field.Store.YES));
        
        // adiciona categorias do descritor
        for (String category : decs.getCategory()) {
            doc.add(new StoredField("category", 
                                  AnalyzerUtils.getTokens(analyzer, category)));
        }      
        // adiciona o descritor nos 3 idiomas
        for (String descriptor : decs.getDescriptor()) {
            doc.add(new TextField("descriptor", 
                                  AnalyzerUtils.getTokens(analyzer, descriptor),
                                  Field.Store.YES));
        }
        // adiciona sinonimos nos 3 idiomas
        for (String synonym : decs.getSynonym()) {
            doc.add(new TextField("syn", 
                                  AnalyzerUtils.getTokens(analyzer, synonym),
                                  Field.Store.YES) );
        }        
        // adiciona abreviacao dos qualificadores (diagnostico = di)
        if (decs.getAbbreviation() != null) {
            doc.add(new StringField("abbreviation", 
                      AnalyzerUtils.getTokens(analyzer, decs.getAbbreviation()), 
                                    Field.Store.YES));
        }
        
        writerMain.addDocument(doc);
        
        // monta indice para encode/decode de termos DeCS
        final Document encodeDoc = new Document();
        encodeDoc.add(new StringField("id", decs.getId(), Field.Store.YES));
        
        // adiciona o descritor nos 3 idiomas
        for (String descriptor : decs.getDescriptor()) {
            encodeDoc.add(new TextField("descriptor", 
                                  AnalyzerUtils.getTokens(analyzer, descriptor),
                                        Field.Store.YES));
        }
        
        writerCode.addDocument(encodeDoc);        
    }
    
    public static void main(String args[]) throws Exception {
        new IndexDecs("resources/decs/xml/decs-metadata.xml");        
    }
}
