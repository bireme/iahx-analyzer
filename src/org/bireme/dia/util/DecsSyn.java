package org.bireme.dia.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author vinicius.andrade
 * date: 20060721
 */
public class DecsSyn {    
    private final Set<String> category;
    private final List<String> descriptor;
    private final Set<String> synonym;
    private String id;
    private String treeId;
    private String abbreviation;
    
    /** Creates a new instance of Term */
    public DecsSyn() {
        category = new TreeSet<String>();
        descriptor = new ArrayList<String>();
        synonym = new TreeSet<String>();
    }
    
    public void clear() {
        category.clear();
        descriptor.clear();
        synonym.clear();
    }
    
    public void setId(String id) {
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
    
    public void addDescriptor(String descriptor) {
        this.descriptor.add(descriptor);
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
    
    public Set<String> getCategory() {
        //return category;
        return new HashSet<String>(category);
    }
    
    public List<String> getDescriptor() {
        //return descriptor;
        return new ArrayList<String>(descriptor);
    }
    
    /*
     * Description: retorna o descritor por idioma (1 - inglês, 2-espanhol, 3-português)
     */
    public String getDescriptor(final String lang) {
        final String desc;
        
        if (lang.equals("en")) {
            desc = descriptor.get(0);
        } else if (lang.equals("es")) {
            desc = this.descriptor.get(1);
        } else if (lang.equals("pt")) {
            desc = this.descriptor.get(2);
        } else {
            desc = null;
        }
        
        return desc;
    }
    
    public Set<String> getSynonym() {
        //return synonym;
        return new HashSet<String>(synonym);
    }    
}
