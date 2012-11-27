package mybudget.table;

import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import mybudget.database.element.Tag;
import mybudget.table.model.TagModel;

/**
 * TagTable 19.04.2008 (23:00:36)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class TagTable extends JTable {

    private List<Tag> list;

    public TagTable(List<Tag> list) {
        this.list = list;
        TagModel model = new TagModel(list);
        setModel(model);

        getColumnModel().getColumn(0).setHeaderValue("ID");
        getColumnModel().getColumn(1).setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Name"));

        getColumnModel().getColumn(0).setMaxWidth(0/*300*/);
        getColumnModel().getColumn(0).setMinWidth(0/*50*/);
        getColumnModel().getColumn(0).setWidth(0/*80*/);
        getColumnModel().getColumn(0).setPreferredWidth(0/*80*/);
        
        getTableHeader().setReorderingAllowed(false);
        
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        if (0 < list.size())
            setRowSelectionInterval(0, 0);
    }
}
