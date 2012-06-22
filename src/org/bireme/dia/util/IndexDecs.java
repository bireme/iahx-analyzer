/*
 * Index.java
 *
 * Created on June 30, 2006, 11:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bireme.dia.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.bireme.dia.analysis.SimpleKeywordAnalyzer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author vinicius.andrade
 */
public class IndexDecs extends DefaultHandler {
    /** A buffer for each XML element */
    private StringBuilder elementBuffer;
    private StringBuilder path;
    private HashMap attributeMap;
    private Document doc;
    private Document encodeDoc;
    private IndexWriter writerMain;               //writer do indice principal
    private IndexWriter writerCode;               //writer do indice que realiza encode/decode de descritores
    private DecsSyn decsSyn;
    
    public IndexDecs(String xml) throws Exception {
        final ClassLoader loader = this.getClass().getClassLoader();
        URL dirUrl = loader.getResource("./"); // get current directory of classes
        
        Directory
 indexDirMain = FSDirectory.open(new File("resources/decs/main/"));
        Directory
 indexDirCode = FSDirectory.open(new File("resources/decs/code/"));
        
        System.out.println("xml :" + xml);
        try {
            
            writerMain = new IndexWriter(indexDirMain, new SimpleKeywordAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED
);
            writerMain.setMergeFactor(100);
            
            writerCode = new IndexWriter(indexDirCode, new SimpleKeywordAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED
);
            writerCode.setMergeFactor(100);
            
            System.out.println("Indexing ...");
            
            Date start = new Date();
            indexTerms(xml);
            
            System.out.println("Optimizing index...");            
            writerMain.optimize();
            writerMain.close();
            
            writerCode.optimize();
            writerCode.close();
            
            Date end = new Date();
            
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");
            
        } catch (IOException e) {
            System.out.println("caught a " + e.getClass() + "\n with message: " +
                    e.getMessage());
        }
        
    }
    
    private void indexTerms(String xml){
        SAXParser sax = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        InputSource xmlInput = new InputSource(xml);
        xmlInput.setEncoding("ISO-8859-1");
        
        try {
            sax = factory.newSAXParser();
            XMLReader reader = sax.getXMLReader();
            reader.setEntityResolver(null);
            reader.setContentHandler(this);
            reader.parse(xmlInput);
            
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public void startDocument() {
        this.path = new StringBuilder("/");
        attributeMap = new HashMap();
        elementBuffer = new StringBuilder();
    }
    
    public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException {
        
        this.path.append(qName + "/");
        
        if ( qName.equals("term") ) {
            decsSyn = new DecsSyn();
            decsSyn.setId( atts.getValue("mfn") );
        }
        
        elementBuffer.setLength(0);
        attributeMap.clear();
        if (atts.getLength() > 0) {
            attributeMap = new HashMap();
            for (int i = 0; i < atts.getLength(); i++) {
                attributeMap.put(atts.getQName(i), atts.getValue(i));
            }
        }
    }
    
    public void characters(char[] text, int start, int length) {
        elementBuffer.append(text, start, length);
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException {
        
    }
    
    public void endElement(String uri, String localName, String qName)
    throws SAXException {
        
        String text = elementBuffer.toString();
        
        if ( qName.equals("descriptor") ){
            decsSyn.addDescriptor(text);
            
        }else if (qName.equals("synonym")){
            decsSyn.addSynonym(text);
            
        }else if( qName.equals("category") ) {
            decsSyn.addCategory(text);
            
        }else if (qName.equals("abbreviation")){
            decsSyn.setAbbreviation(text);
        }
        
        if ( qName.equals("term") ){
            try {
                index(decsSyn);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void index(DecsSyn decs) throws IOException {
        
        // monta indice principal para analizador DeCS
        doc = new Document();
        
        doc.add( new Field("id", decs.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED) );
        // adiciona categorias do descritor
        for (String category : decs.getCategory() ) {
            doc.add( new Field("category", category, Field.Store.YES, Field.Index.NO) );
        }      
        // adiciona o descritor nos 3 idiomas
        for (String descriptor : decs.getDescriptor() ) {
            doc.add( new Field("descriptor", descriptor, Field.Store.YES, Field.Index.ANALYZED) );
        }

        // adiciona o descritor nos 3 idiomas sem tokenize
        for (String descriptor : decs.getDescriptor() ) {
            doc.add( new Field("descriptor_full", descriptor, Field.Store.YES, Field.Index.NOT_ANALYZED) );
        }

        // adiciona sinonimos nos 3 idiomas
        for (String synonym : decs.getSynonym() ) {
            doc.add( new Field("syn", synonym, Field.Store.YES, Field.Index.ANALYZED) );
        }
        // adiciona abreviacao dos qualificadores (diagnostico = di)
        if (decs.getAbbreviation() != null){
            doc.add( new Field("abbreviation", decs.getAbbreviation(), Field.Store.YES, Field.Index.NOT_ANALYZED) );
        }
        
        writerMain.addDocument(doc);
        
        // monta indice para encode/decode de termos DeCS
        encodeDoc = new Document();
        encodeDoc.add( new Field("id", decs.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED) );
        // adiciona o descritor nos 3 idiomas
        for (String descriptor : decs.getDescriptor() ) {
            encodeDoc.add( new Field("descriptor", descriptor, Field.Store.YES, Field.Index.ANALYZED) );
        }
        
        writerCode.addDocument(encodeDoc);
        
    }
    
    public static void main(String args[]) throws Exception {

        IndexDecs index = new IndexDecs("resources/decs/xml/decs-metadata.xml");
        
    }
}
