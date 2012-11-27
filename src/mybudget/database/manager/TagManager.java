package mybudget.database.manager;

import mybudget.database.element.Tag;
import java.util.List;
import org.hibernate.Session;
import mybudget.database.util.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.SQLQuery;

/**
 * TagManager 18.04.2008 (13:37:07)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class TagManager {

    private List<Tag> tagsCache;

    public Long insert(String name) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        session.beginTransaction();
        
        Tag tag = new Tag();
        tag.setName(name);
        
        session.save(tag);
        session.getTransaction().commit();
        tagsCache = null;

        return tag.getId();
    }
    
    public void update(Tag tag) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        session.beginTransaction();
        session.update(tag);
        session.getTransaction().commit();
        tagsCache = null;
    }
    
    public void delete(Tag tag) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        session.beginTransaction();
        SQLQuery q = session.createSQLQuery("delete from TRANSACTION_TAG where TAG_ID=?");
        q.setLong(0, tag.getId());
        q.executeUpdate();
        session.delete(tag);
        session.getTransaction().commit();
        tagsCache = null;
    }

    /**
     * Возвращает список ярлыков.
     * @return
     */
    public List<Tag> getListTag() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();        
        session.beginTransaction();
        
        List<Tag> list = session.createQuery("from Tag as t order by t.name asc").list();
        
        session.getTransaction().commit();
        return list;
    }

    /**
     * Возвращает список ярлыков из кэша, если список ярылков изменяется, то
     * буффер перечитывается из базы.
     * @return
     */
    public List<Tag> getListTagCache() {
        if (tagsCache == null) {
            tagsCache = getListTag();
        }
        return tagsCache;
    }
    
    public Tag getTag(Long id) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Tag tag = (Tag) session.get(Tag.class, id);
        
        session.getTransaction().commit();
        return tag;
    }
    
    public Tag getTagWithTransactions(Long id) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Query q = session.createQuery("from Tag as tag "+
                "left outer join fetch tag.transactions "+
                "where tag.id = ? order by tag.name asc");
        q.setLong(0, id);
        Tag tag = (Tag) q.uniqueResult();
        
        session.getTransaction().commit();
        return tag;
    }
    
}
