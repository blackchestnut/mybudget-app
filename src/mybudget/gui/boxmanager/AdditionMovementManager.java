
package mybudget.gui.boxmanager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import mybudget.database.element.Tag;
import mybudget.database.element.Transaction;
import mybudget.database.manager.TagManager;
import mybudget.database.manager.TransactionManager;
import mybudget.table.TransactionTable;

/**
 * AdditionMovementManager 09.05.2008 (15:36:02)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class AdditionMovementManager {

    private TagManager tagManager;
    private TransactionManager transactionManager;
    private JComboBox comboBox;
    private TransactionTable currentTable;
    
    public AdditionMovementManager(TagManager tagManager, TransactionManager transactionManager,
            JComboBox comboBox) {
       this.tagManager = tagManager;
       this.transactionManager = transactionManager;
       this.comboBox = comboBox;
       this.currentTable = null;
    }
    
    public TransactionTable getCurrentTable() {
        return currentTable;
    }
    
    public void initComboBox(TransactionTable table) {
        comboBox.removeAllItems();
        comboBox.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Addition_Actions..."));
        this.currentTable = table;
        if (0 == table.getSelectedRowCount())
            return;
        comboBox.addItem("   " + mybudget.option.Options.I18N_BUNDLE.getString("Add_Tag:"));
        List<Tag> tagList = tagManager.getListTagCache();
        List<ComboBoxItemTag> comboBoxTagList = ComboBoxItemTag.create(tagList, ComboBoxItemTag.TYPE_ADD);
        for (ComboBoxItemTag boxTag : comboBoxTagList) {
            comboBox.addItem(boxTag);
        }
        int[] selectRows = table.getSelectedRows();
        Set<Tag> addedTagSet = new LinkedHashSet();
        for (int i = 0; i < selectRows.length; i++) {
            Transaction transaction = transactionManager.getTransactionWithTags((Long) table.getValueAt(selectRows[i], 0));
            addedTagSet.addAll(transaction.getTags());
        }
        if (0 == addedTagSet.size())
            return;
        comboBox.addItem("   " + mybudget.option.Options.I18N_BUNDLE.getString("Remove_Tag:"));
        comboBoxTagList = ComboBoxItemTag.create(new ArrayList(addedTagSet), ComboBoxItemTag.TYPE_DELETE);
        for (ComboBoxItemTag boxTag : comboBoxTagList) {
            comboBox.addItem(boxTag);
        }
    }
    
    public void updateComboBox() {
        if (null == currentTable)
            return;
        initComboBox(currentTable);
    }

}
