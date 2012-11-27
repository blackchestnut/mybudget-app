package mybudget.database.element;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Transaction 18.04.2008 (13:19:57)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class Transaction {

    private Long id;
    private String description;
    private Integer type;    
    private Double value;
    private Date date;
    private Set tags = new LinkedHashSet();
    
    public final static int TYPE_INCOME = 0;
    public final static int TYPE_EXPENSE = 1;

    /**
     * Максимальная длина описани.
     */
    public final static int MAX_LENGHT_DESCRIPTION = 1024;
    
    public Transaction() {
        this(new Date());
    }

    public Transaction(Date tDate) {
        description = "";
        type = TYPE_EXPENSE;
        date = tDate;
        value = 0.0;
    }

    public Transaction(Transaction t, Collection<Tag> tags) {
        description = t.description;
        type = t.type;
        value = t.value;
        date = t.date;
        for (Tag x : tags) {
            getTags().add(x);
        }
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desctiption) {
        this.description = desctiption.length() > MAX_LENGHT_DESCRIPTION
                ? desctiption.substring(0, MAX_LENGHT_DESCRIPTION)
                : desctiption;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set getTags() {
        return tags;
    }

    public void setTags(Set tags) {
        this.tags = tags;
    }

    public void addToTag(Tag tag) {
        this.getTags().add(tag);
        tag.getTransactions().add(this);
    }

    public void removeFromTag(Tag tag) {
        this.getTags().remove(tag);
        tag.getTransactions().remove(this);
    }
    
    @Override
    public String toString() {
        return "Transaction: id=" + this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Transaction))
            return false;
        return getId().equals(((Transaction) obj).getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    public String getTagsName() {
        String textTagList = "";
        Set tagList = getTags();
        for (Object tag : tagList) {
            textTagList += ((Tag)tag).getName() + ", ";
        }
        if (!textTagList.equals("")) {
            textTagList = textTagList.substring(0, textTagList.lastIndexOf(","));
        }
        return textTagList;
    }
    
}
