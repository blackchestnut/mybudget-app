package mybudget.table.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import mybudget.database.element.Tag;

/**
 * TagModel 19.04.2008 (22:52:33)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class TagTransactionModel extends AbstractTableModel {

    private List<Tag> list;
    private List<Boolean> flagList;

    public TagTransactionModel(List<Tag> list) {
        this.list = list;
        this.flagList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            this.flagList.add(false);
        }
    }

    public int getRowCount() {
        return list.size();
    }

    public int getColumnCount() {
        return 3;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: 
                return list.get(rowIndex).getId();
            case 1: 
                return flagList.get(rowIndex);
            case 2: 
                return list.get(rowIndex);
                //return list.get(rowIndex).getName();                            
            default :
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (1 == columnIndex) {
            return true;
        }
        return super.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (1 == columnIndex) {
            flagList.set(rowIndex, (Boolean) aValue);
        }
        super.setValueAt(aValue, rowIndex, columnIndex);
    }
 
}
