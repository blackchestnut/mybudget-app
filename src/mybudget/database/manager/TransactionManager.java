package mybudget.database.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import mybudget.database.element.Tag;
import mybudget.database.element.Transaction;
import mybudget.database.util.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * TagManager 21.04.2008 (14:30:07)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class TransactionManager {
   
    public Long insert(String desctiption, int type, Double value, Date date) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        session.beginTransaction();

        Transaction transaction = new Transaction();
        transaction.setDescription(desctiption);
        transaction.setType(type);
        transaction.setValue(value);
        transaction.setDate(date);
        
        session.save(transaction);
        session.getTransaction().commit();
        
        return transaction.getId();
    }
    
    public Long insert(Transaction transaction) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        session.beginTransaction();
        
        session.save(transaction);
        session.getTransaction().commit();
        
        return transaction.getId();        
    }
    
    public void update(Transaction transaction) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        session.beginTransaction();
        session.update(transaction);
        session.getTransaction().commit();
    }
    
    public void insertOrUpdate(Transaction transaction) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        session.beginTransaction();
        session.saveOrUpdate(transaction);
        session.getTransaction().commit();
    }

    public void delete(Transaction transaction) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        session.beginTransaction();
        session.delete(transaction);
        session.getTransaction().commit();
    }
    
    public List<Transaction> getListTransaction() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();        
        session.beginTransaction();
        
        List<Transaction> list = session.createQuery("from Transaction as t order by t.date asc").list();
        
        session.getTransaction().commit();
        return list;
    }
    
    public List<Transaction> getListTransaction(int type) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();        
        session.beginTransaction();
        
        List<Transaction> list = session.createQuery("from Transaction as t where t.type = ?").
                setInteger(0, type).list();
        
        session.getTransaction().commit();
        return list;
    }
    
    private List<Transaction> getListTransactionWithTags(int type) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();        
        session.beginTransaction();
        
        List<Transaction> list;
        Query q = session.createQuery("from Transaction as transaction "+                
                "left outer join fetch transaction.tags where transaction.type = ? "+
                "order by transaction.date asc, transaction.id asc");
        q.setInteger(0, type);
        Set<Transaction> unic = new LinkedHashSet(q.list());
        list = new ArrayList(unic);
        
        session.getTransaction().commit();
        return list;
    }

    /**
     * Возвращает все транзакции с тегами упорядоченные по дате и индексу.
     * @return Список транзакций с тегами.
     */
    public List<Transaction> getListTransactionWithTags() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        List<Transaction> list;
        Query q = session.createQuery("from Transaction as transaction "+
                "left outer join fetch transaction.tags "+
                "order by transaction.date asc, transaction.id asc");
        Set<Transaction> unic = new LinkedHashSet(q.list());
        list = new ArrayList(unic);

        session.getTransaction().commit();
        return list;
    }

    /**
     * Возвращает последнии транзакции с лимитом.
     * @param limit Количество последних транзакций.
     * @return Список транзакций.
     */
    public Collection<Transaction> getLastTransactionFull(int limit) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Query q = session.createQuery(
                "from Transaction as transaction left outer " +
                "join fetch transaction.tags order by transaction.id desc");
        q.setMaxResults(limit);

        List tList = q.list();

        session.getTransaction().commit();
        return tList;
    }
    
    private List<Transaction> getListTransactionWithTags(Date startDate, Date finishDate, int type) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();        
        session.beginTransaction();
        
        List<Transaction> list;
        Query q;
        if (null != startDate) {
            if (-1 == type) {
                q = session.createQuery("from Transaction as t "+
                        "left outer join fetch t.tags where "+
                        ":start <= t.date and :finish >= t.date " +
                        "order by t.date asc, t.id asc");
                q.setDate("start", startDate);
                q.setDate("finish", finishDate);
            } else {
                q = session.createQuery("from Transaction as t "+
                        "left outer join fetch t.tags where t.type = :tr_type "+
                        "and :start <= t.date and :finish >= t.date " +
                        "order by t.date asc, t.id asc");
                q.setInteger("tr_type", type);
                q.setDate("start", startDate);
                q.setDate("finish", finishDate);
            }
        } else {
            if (-1 == type) {
                q = session.createQuery("from Transaction as t "+                
                        "left outer join fetch t.tags where "+
                        ":finish >= t.date " +
                        "order by t.date asc, t.id asc");
                q.setDate("finish", finishDate);
            } else {
                q = session.createQuery("from Transaction as t "+                
                        "left outer join fetch t.tags where t.type = :tr_type "+
                        "and :finish >= t.date " +
                        "order by t.date asc, t.id asc");
                q.setInteger("tr_type", type);
                q.setDate("finish", finishDate);
            }
        }
        Set<Transaction> unic = new LinkedHashSet(q.list());
        list = new ArrayList(unic);
        
        session.getTransaction().commit();
        return list;
    }
    
    public List<Transaction> getListTransactionWithSpecificTags(Date startDate, Date finishDate, 
            List<Tag> tags) {
        return getListTransactionWithSpecificTags(startDate, finishDate, -1, tags);
    }
    
    public List<Transaction> getListTransactionWithOneOfTags(Date startDate, Date finishDate, 
            List<Tag> tags) {
        return getListTransactionWithOneOfTags(startDate, finishDate, -1, tags);
    }
    
    public List<Transaction> getListTransactionWithSpecificTags(Date startDate, Date finishDate,
            int type, List<Tag> tags) {
        List<Transaction> list = getListTransactionWithTags(startDate, finishDate, type);
        if (0 != tags.size()) {
            List<Transaction> toDelete = new ArrayList();
            for (Transaction tr : list) {
                if (!tr.getTags().containsAll(tags))
                    toDelete.add(tr);
            }
            list.removeAll(toDelete);
        }
        return list;
    }
    
    public List<Transaction> getListTransactionWithOneOfTags(Date startDate, Date finishDate, int type,
            List<Tag> tags) {
        List<Transaction> list = getListTransactionWithTags(startDate, finishDate, type);
        if (0 != tags.size()) {
            List<Transaction> toDelete = new ArrayList();
            for (Transaction tr : list) {
                boolean flagToDelete = true;
                for (Tag tg : tags) {
                    if (tr.getTags().contains(tg)) {
                        flagToDelete = false;
                        break;
                    }                    
                }
                if (flagToDelete) 
                    toDelete.add(tr);
            }
            list.removeAll(toDelete);
        }
        return list;
    }
    
    public List<Transaction> getListIncomeTransactionWithSpecificTags(Date startDate, Date finishDate, 
            List<Tag> tags) {
        return getListTransactionWithSpecificTags(startDate, finishDate, Transaction.TYPE_INCOME, tags);
    }

    public List<Transaction> getListExpenseTransactionWithSpecificTags(Date startDate, Date finishDate, 
            List<Tag> tags) {
        return getListTransactionWithSpecificTags(startDate, finishDate, Transaction.TYPE_EXPENSE, tags);
    } 
    
    public List<Transaction> getListIncomeTransactionWithOneOfTags(Date startDate, Date finishDate, 
            List<Tag> tags) {
        return getListTransactionWithOneOfTags(startDate, finishDate, Transaction.TYPE_INCOME, tags);
    }

    public List<Transaction> getListExpenseTransactionWithOneOfTags(Date startDate, Date finishDate, 
            List<Tag> tags) {
        return getListTransactionWithOneOfTags(startDate, finishDate, Transaction.TYPE_EXPENSE, tags);
    }    
    
    private List<Transaction> getListTransactionWithSpecificTags1(Date startDate, Date finishDate, 
            int type, List<Tag> tags) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();        
        session.beginTransaction();
        
        List<Transaction> list;
        Query q;
        if (null != startDate) {
            q = session.createQuery("from Transaction as t " +
                    "left outer join fetch t.tags where t.type = :tr_type " +
                    "and :start <= t.date and :finish >= t.date " +
                    "order by t.date asc, t.id asc");
            q.setInteger("tr_type", type);
            q.setDate("start", startDate);
            q.setDate("finish", finishDate);
        } else {
            q = session.createQuery("from Transaction as t " +                
                    "left outer join fetch t.tags where t.type = :tr_type " +
                    "and :finish >= t.date " +
                    "order by t.date asc, t.id asc");
            q.setInteger("tr_type", type);
            q.setDate("finish", finishDate);
        }
        Set<Transaction> unic = new LinkedHashSet(q.list());
        list = new ArrayList();
        for (Transaction tr : unic) {            
            if (tr.getTags().containsAll(tags))
                list.add(tr);
        }        
        
        session.getTransaction().commit();
        return list;
    }
    
    public List<Transaction> getListIncomeTransactionWithTags(Date startDate, Date finishDate) {
        return getListTransactionWithTags(startDate, finishDate, Transaction.TYPE_INCOME);
    }

    public List<Transaction> getListExpenseTransactionWithTags(Date startDate, Date finishDate) {
        return getListTransactionWithTags(startDate, finishDate, Transaction.TYPE_EXPENSE);
    }    
    
    public List<Transaction> getListIncomeTransaction() {
        return getListTransaction(Transaction.TYPE_INCOME);
    }
    
    public List<Transaction> getListExpenseTransaction() {
        return getListTransaction(Transaction.TYPE_EXPENSE);
    }

    public List<Transaction> getListIncomeTransactionWithTags() {
        return getListTransactionWithTags(Transaction.TYPE_INCOME);
    }
    
    public List<Transaction> getListExpenseTransactionWithTags() {
        return getListTransactionWithTags(Transaction.TYPE_EXPENSE);
    }
    
    public List<Transaction> getListTransaction(Date startDate, Date finishDate) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();        
        session.beginTransaction();
        
        Query q = session.createQuery("from Transaction as t where :start <= t.date and :finish >= t.date order by t.date asc");
        q.setDate("start", startDate);
        q.setDate("finish", finishDate);
        List<Transaction> list = q.list();
        
        session.getTransaction().commit();
        return list;
    }
    
    public Transaction getTransaction(Long id) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Transaction transaction = (Transaction) session.get(Transaction.class, id);
        
        session.getTransaction().commit();
        return transaction;
    }
    
    public Transaction getTransactionWithTags(Long id) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Query q = session.createQuery("from Transaction as transaction "+
                "left outer join fetch transaction.tags where transaction.id = ?").setLong(0, id);
        Transaction transaction = (Transaction) q.uniqueResult();
        
        session.getTransaction().commit();
        return transaction;
    }
    
    public void addTagToTransaction(Long idTransaction, Long idTag) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Transaction transaction = (Transaction) session.load(Transaction.class, idTransaction);
        Tag tag = (Tag) session.load(Tag.class, idTag);
        transaction.addToTag(tag);
        
        session.getTransaction().commit();
    }
    
    public void addTagToTransactionSoft(Long idTransaction, Long idTag) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();        
        session.beginTransaction();
        
        Query q = session.createQuery("from Transaction as transaction "+
                "left outer join fetch transaction.tags where transaction.id = ?").setLong(0, idTransaction);
        Transaction transaction = (Transaction) q.uniqueResult();
        Tag tag = (Tag) session.load(Tag.class, idTag);
        transaction.addToTag(tag);
        
        session.getTransaction().commit();
    }
    
    public void removeTagFromTransaction(Long idTransaction, Long idTag) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Transaction transaction = (Transaction) session.load(Transaction.class, idTransaction);
        Tag tag = (Tag) session.load(Tag.class, idTag);
        transaction.removeFromTag(tag);
        
        session.getTransaction().commit();
    }
    
    public void removeTagFromTransactionSoft(Long idTransaction, Long idTag) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        SQLQuery q = session.createSQLQuery("delete from TRANSACTION_TAG where TAG_ID=? and TRANSACTION_ID=?");
        q.setLong(0, idTag);
        q.setLong(1, idTransaction);
        q.executeUpdate();
        
        session.getTransaction().commit();
    }
    
    public Double getTotalIncomeValue() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Query q = session.createQuery("select sum(t.value) from Transaction t where t.type = ?");
        q.setInteger(0, Transaction.TYPE_INCOME);
        Double res = (Double) q.uniqueResult();
        if (null == res)
            res = 0.;
        session.getTransaction().commit();
        return res;
    }

    public Double getTotalExpenseValue() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Query q = session.createQuery("select sum(t.value) from Transaction t where t.type = ?");
        q.setInteger(0, Transaction.TYPE_EXPENSE);
        Double res = (Double) q.uniqueResult();
        if (null == res) 
            res = 0.;
        
        session.getTransaction().commit();
        return res;
    }
    
    public Double getValueTransaction(Date startDate, Date finishDate, int type) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        Query q = session.createQuery(
                "select sum(t.value) from Transaction t where t.type = ? and :start <= t.date and :finish >= t.date");        
        q.setInteger(0, type);
        q.setDate("start", startDate);
        q.setDate("finish", finishDate);
        Double res = (Double) q.uniqueResult();
        if (null == res) 
            res = 0.;
        
        session.getTransaction().commit();
        return res;
    }
    
}
