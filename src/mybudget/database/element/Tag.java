package mybudget.database.element;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tag 18.04.2008 (12:58:43)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class Tag {

    private Long id;
    private String name;
    private Set<Transaction> transactions = new LinkedHashSet();
    
    public Tag() {        
        
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set getTransactions() {
        return transactions;
    }

    public void setTransactions(Set transactions) {
        this.transactions = transactions;
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tag))
            return false;
        return getId().equals(((Tag) obj).getId());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
}
