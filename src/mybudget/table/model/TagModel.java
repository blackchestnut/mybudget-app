package mybudget.table.model;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import mybudget.database.element.Tag;

/**
 * TagModel 19.04.2008 (22:52:33)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class TagModel extends AbstractTableModel {

    private List<Tag> list;

    public TagModel(List<Tag> list) {
        this.list = list;
    }

    public int getRowCount() {
        return list.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: 
                return list.get(rowIndex).getId();
            case 1: 
                return list.get(rowIndex).getName();                            
            default :
                return null;
        }
    }
}
