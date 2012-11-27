package mybudget.table;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import mybudget.database.element.Tag;
import mybudget.table.model.TagTransactionModel;

/**
 * TagTable 19.04.2008 (23:00:36)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class TagTransactionTable extends JTable {

    private List<Tag> list;

    public TagTransactionTable(List<Tag> list) {
        this.list = list;
        setModel(new TagTransactionModel(list));

        getColumnModel().getColumn(0).setHeaderValue("ID");
        getColumnModel().getColumn(1).setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Select"));
        getColumnModel().getColumn(2).setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Name"));

        getColumnModel().getColumn(0).setMaxWidth(0/*300*/);
        getColumnModel().getColumn(0).setMinWidth(0/*50*/);
        getColumnModel().getColumn(0).setWidth(0/*80*/);
        getColumnModel().getColumn(0).setPreferredWidth(0/*80*/);
        
        getColumnModel().getColumn(1).setMaxWidth(100);
        getColumnModel().getColumn(1).setMinWidth(50);
        getColumnModel().getColumn(1).setWidth(50);
        getColumnModel().getColumn(1).setPreferredWidth(50);
        
        getTableHeader().setReorderingAllowed(false);
        
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        if (0 < list.size()) {
            setRowSelectionInterval(0, 0);
        }
        
        TableColumn tc = getTableHeader().getColumnModel().getColumn(1);
        JCheckBox ch = new JCheckBox();
        tc.setCellEditor(new DefaultCellEditor(ch));
        tc.setCellRenderer(new DFCR());
    }

    public void selectTags(List<Tag> tags) {
        if (tags == null) {
            return;
        }
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt(tags.contains((Tag) getValueAt(i, 2)), i, 1);
        }
    }
    
    class DFCR implements TableCellRenderer {
        
        protected JCheckBox ch = new JCheckBox();
        
        public javax.swing.JComponent getTableCellRendererComponent(javax.swing.JTable table, 
                Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {

            ch.setSelected(((Boolean)table.getValueAt(row, column)).booleanValue());
            return ch;
        }
    }

    /**
     * Возвращает список ID-ов выбранных ярлыков
     * @return
     */
    public List<Long> getSelectedTagIDs() {
        List<Long> listId = new ArrayList<Long>(getRowCount());
        for (int i = 0; i < getRowCount(); i++) {
            if ((Boolean) getValueAt(i, 1)) {
                listId.add((Long) getValueAt(i, 0));
            }
        }
        return listId;
    }

    /**
     * Возвращает список выбранных ярлыков
     * @return
     */
    public List<Tag> getSelectedTags() {
        List<Tag> res = new ArrayList(getRowCount());
        for (int i = 0; i < getRowCount(); i++) {
            if ((Boolean) getValueAt(i, 1)) {
                res.add((Tag) getValueAt(i, 2));
            }
        }
        return res;
    }

    /**
     * Возвращает количество выбранных ярлыков
     * @return
     */
    public int getSelectedCount() {
        int count = 0;
        for (int i = 0; i < getRowCount(); i++) {
            if ((Boolean) getValueAt(i, 1)) {
                count++;
            }
        }
        return count;
    }

    public void setCheckAll(boolean b) {
        for (int i = 0; i < getRowCount(); i++) {
            if (((Boolean) getValueAt(i, 1)).booleanValue() != b) {
                setValueAt(b, i, 1);
            }
        }
        updateUI();
    }
}
