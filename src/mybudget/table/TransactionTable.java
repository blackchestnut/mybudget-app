package mybudget.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import mybudget.table.model.*;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import mybudget.database.element.Transaction;
import mybudget.option.Options;

/**
 * TagTable 21.04.2008 (15:08:36)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class TransactionTable extends JTable {

    private List<Transaction> list;

    public TransactionTable(List<Transaction> list) {
        this.list = list;
        TransactionModel model = new TransactionModel(list);
        setModel(model);

        getColumnModel().getColumn(0).setHeaderValue("ID");
        getColumnModel().getColumn(1).setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Tags"));
        getColumnModel().getColumn(2).setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Value"));
        getColumnModel().getColumn(3).setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Date"));
        getColumnModel().getColumn(4).setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Description"));

        getColumnModel().getColumn(0).setMaxWidth(0/*300*/);
        getColumnModel().getColumn(0).setMinWidth(0/*50*/);
        getColumnModel().getColumn(0).setWidth(0/*80*/);
        
        getColumnModel().getColumn(2).setMaxWidth(300);
        getColumnModel().getColumn(2).setMinWidth(50);
        getColumnModel().getColumn(2).setWidth(100);
        getColumnModel().getColumn(2).setCellRenderer(new CurrencyCellRenderer());

        getColumnModel().getColumn(3).setMaxWidth(300);
        getColumnModel().getColumn(3).setMinWidth(50);
        getColumnModel().getColumn(3).setWidth(80);
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(JLabel.CENTER);
        getColumnModel().getColumn(3).setCellRenderer(render);

        for (int i = 0; i < Options.HEADER_SIZE_TRANSACTION.length; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(Options.HEADER_SIZE_TRANSACTION[i]);
        }

        getTableHeader().setReorderingAllowed(false);
        getTableHeader().addMouseListener(new MouseHeaderListener());
        
        if (0 < list.size()) {
            setRowSelectionInterval(0, 0);
        }        
    }
    
    class MouseHeaderListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            for (int i = 0; i < Options.HEADER_SIZE_TRANSACTION.length; i++) {
                Options.HEADER_SIZE_TRANSACTION[i] = getColumnModel().getColumn(i).getWidth();
            }            
        }        
    }
}
