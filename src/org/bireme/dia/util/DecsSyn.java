/*
 * Term.java
 *
 * Created on July 21, 2006, 11:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bireme.dia.util;

import java.util.ArrayList;

/**
 *
 * @author vinicius.andrade
 */
public class DecsSyn {
    private String id;
    private ArrayList<String> category;
    private ArrayList<String> descriptor;
    private ArrayList<String> synonym;
    private String treeId;
    private String abbreviation;
    
    /** Creates a new instance of Term */
    public DecsSyn() {
        category = new ArrayList();
        descriptor = new ArrayList();
        synonym = new ArrayList();
    }
    
    void setId(String id) {
        this.id = id;
    }

    public void setTreeId(String treeId) {
        this.treeId = treeId;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void addCategory(String category) {
        this.category.add(category);
    }
    
    public void addDescriptor(String desc) {
        this.descriptor.add(desc);
    }
    
    public void addSynonym(String synonym) {
        this.synonym.add(synonym);
    }
    
    public String getId() {
        return id;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getTreeId() {
        return treeId;
    }
    
    public ArrayList<String> getCategory() {
        return category;
    }
    
    public ArrayList<String> getDescriptor() {
        return descriptor;
    }
    
    /*
     * Description: retorna o descritor por idioma (1 - ingl�s, 2-espanhol, 3-portugu�s)
     */
    public String getDescriptor(String lang) {
        String desc = "";
        
        if (lang == "en"){
            desc = this.descriptor.get(0);
        }else if (lang == "es"){
            desc = this.descriptor.get(1);
        }else{
            desc = this.descriptor.get(2);
        }
        return desc;
    }
    
    public ArrayList<String> getSynonym() {
        return synonym;
    }
    
}
