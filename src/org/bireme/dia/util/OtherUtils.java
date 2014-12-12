
package org.bireme.dia.util;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import static org.apache.lucene.index.FieldInfo.DocValuesType.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;


/**
 *
 * @author Heitor Barbieri
 * date: 20141016
 */
public class OtherUtils {
    public static void displayDocuments(final String index) throws IOException {
        if (index == null) {
            throw new NullPointerException("index");
        }
        final SimpleFSDirectory directory = 
                                         new SimpleFSDirectory(new File(index));
        final DirectoryReader reader = DirectoryReader.open(directory);
                
        for (int i=0; i < reader.maxDoc(); i++) {
            /*if (reader.isDeleted(i)) {
                continue;
            }*/
            System.out.println(getDocument(reader.document(i)));                        
            System.out.println("-------------------------");
        }
        reader.close();
    }
    
    public static String getDocument(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        
        builder.append("{\n [");
        
        for (IndexableField field : doc) {
            final IndexableFieldType ift = field.fieldType();
            final FieldInfo.DocValuesType type = ift.docValueType();
            
            if (first) {
                first = false;
            } else {
                builder.append(",");
            }
            builder.append("\n  {\n   \"");            
            builder.append(field.name());
            builder.append("\": \"");
            if ((type != null) && (type == NUMERIC)) {
                builder.append(field.numericValue());    
            } else {
                builder.append(field.stringValue());    
            }
            builder.append("\"\n   \"type\": \"");
            builder.append((type == null) ? "?" : type.name());
            builder.append("\"\n   \"indexed\": ");
            builder.append(ift.indexed());            
            builder.append("\n   \"stored\": ");
            builder.append(ift.stored());            
            builder.append("\n   \"tokenized\": ");
            builder.append(ift.tokenized());            
            
            builder.append("\n  }");
        }
        builder.append("\n ]\n}\n");
        
        return builder.toString();
    }
    
    public static void displayTerms(final String index) throws IOException {
        if (index == null) {
            throw new NullPointerException("index");
        }
        final StringBuilder builder = new StringBuilder();
        final SimpleFSDirectory directory = 
                                         new SimpleFSDirectory(new File(index));
        final DirectoryReader reader = DirectoryReader.open(directory);
        final TermsEnum tenum = TermsEnum.EMPTY;
        boolean first = true;
                
        for (String fname: MultiFields.getFields(reader)) {         
            if (first) {
                first = false;
            } else {
                builder.append(",\n");
            }
            builder.append("{\n \"fieldName\": \"");
            builder.append(fname);
            builder.append("\"\n \"terms\": [");            
            getTerms(reader, fname, tenum, builder);
            builder.append("\n ]\n}");
        }        
        
        reader.close();
        
        System.out.println(builder.toString());
    }
    
    public static void getTerms(final IndexReader reader,
                                final String field,
                                final TermsEnum tenum,
                                final StringBuilder builder) throws IOException {
        if (reader == null) {
            throw new NullPointerException("reader");
        }
        if (field == null) {
            throw new NullPointerException("field");
        }
        if (tenum == null) {
            throw new NullPointerException("tenum");
        }
        if (builder == null) {
            throw new NullPointerException("builder");
        }
        final Terms terms = MultiFields.getTerms(reader, field);        
        final TermsEnum iterator = terms.iterator(tenum);
        boolean first = true;
        
        while (true) {
            final BytesRef byteRef = iterator.next();
            
            if (byteRef == null) {
                break;
            }
            if (first) {
                first = false;
                builder.append("\n   \"");
            } else {
                builder.append(",\n   \"");                
            }
            builder.append(byteRef.utf8ToString());
            builder.append("\"");
        }        
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("==================== index[code] =====================");        
        displayDocuments("resources/decs/code");
        
        System.out.println("\n==================== index[main] =====================");
        displayDocuments("resources/decs/main");
        
        System.out.println("\n==================== terms[code] =====================");
        displayTerms("resources/decs/code");
        
        System.out.println("\n==================== terms[main] =====================");
        displayTerms("resources/decs/main");
    }
}
