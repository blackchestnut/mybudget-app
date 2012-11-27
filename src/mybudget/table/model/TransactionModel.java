package mybudget.table.model;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import mybudget.option.Options;
import mybudget.database.element.Transaction;

/**
 * TagModel 21.04.2008 (14:43:33)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class TransactionModel extends AbstractTableModel {

    private List<Transaction> list;

    public TransactionModel(List<Transaction> list) {
        this.list = list;
    }

    public int getRowCount() {
        return list.size();
    }

    public int getColumnCount() {
        return 5;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Transaction transaction  = list.get(rowIndex);
        switch (columnIndex) {
            case 0: 
                return transaction.getId();
            case 1: 
                return transaction.getTagsName();
            case 2: 
                return transaction.getValue();
            case 3: 
                return Options.DATE_FORMAT.format(transaction.getDate());
            case 4: 
                return transaction.getDescription();                            
            default :
                return null;
        }
    }
}
