
package mybudget.table;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import mybudget.option.Options;

/**
 * CurrencyCellRenderer 05.04.2009 (2:10:10)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class CurrencyCellRenderer extends DefaultTableCellRenderer {

    public CurrencyCellRenderer() {
        setHorizontalAlignment(JLabel.RIGHT);
    }
    
    @Override
    protected void setValue(Object value) {
        if (value instanceof Double) {
            super.setValue(Options.toCurrency((Double) value) + "  ");
        } else {
            super.setValue(value);
        }
    }    

}
