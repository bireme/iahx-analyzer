package org.bireme.dia.util;

import java.util.ArrayList;

/**
 *
 * @author vinicius.andrade
 * date: 20060721
 */
public class DecsSyn {    
    private final ArrayList<String> category;
    private final ArrayList<String> descriptor;
    private final ArrayList<String> synonym;
    private String id;
    private String treeId;
    private String abbreviation;
    
    /** Creates a new instance of Term */
    public DecsSyn() {
        category = new ArrayList<String>();
        descriptor = new ArrayList<String>();
        synonym = new ArrayList<String>();
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
        //return category;
        return new ArrayList<String>(category);
    }
    
    public ArrayList<String> getDescriptor() {
        //return descriptor;
        return new ArrayList<String>(descriptor);
    }
    
    /*
     * Description: retorna o descritor por idioma (1 - inglês, 2-espanhol, 3-português)
     */
    public String getDescriptor(final String lang) {
        final String desc;
        
        if (lang.equals("en")) {
            desc = this.descriptor.get(0);
        } else if (lang.equals("es")) {
            desc = this.descriptor.get(1);
        } else {
            desc = this.descriptor.get(2);
        }
        
        return desc;
    }
    
    public ArrayList<String> getSynonym() {
        //return synonym;
        return new ArrayList<String>(synonym);
    }    
}
