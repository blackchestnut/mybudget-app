
package mybudget.table;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import mybudget.database.element.Transaction;
import mybudget.database.manager.TransactionManager;

/**
 * SumTable 22.03.2009 (22:54:48)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class SumTable extends JTable {
    
    private TransactionManager manager;
    
    private Date startDate;
    
    private Date endDate;
    
    private boolean incomeSum;
    
    private Object[][] data;
    
    public SumTable(TransactionManager manager, Date startDate, Date endDate) {
        this.manager = manager;
        this.incomeSum = false;
        setDateInterval(startDate, endDate);
    }
    
    public void setDateInterval(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public Object[][] getData() {
        return data;
    }
    
    public void setIncomeSum(boolean incomeSum) {
        this.incomeSum = incomeSum;
    }
    
    public void updateData() {
        List<Transaction> listTransaction = incomeSum
                ? manager.getListIncomeTransactionWithTags(startDate, endDate)
                : manager.getListExpenseTransactionWithTags(startDate, endDate);
        HashMap<String, Double> sumMap = new HashMap<String, Double>();
        for (Transaction t : listTransaction) {
            String tags = t.getTagsName();
            if (sumMap.containsKey(tags)) {
                sumMap.put(tags, sumMap.get(tags) + t.getValue());
            } else {
                sumMap.put(tags, t.getValue());
            }
        }
        data = new Object[sumMap.size()][2];
        int index = 0;
        for (Entry<String, Double> entry : sumMap.entrySet()) {
            data[index][0] = entry.getValue();
            data[index][1] = entry.getKey();
            index++;
        }
        sort(data);
        updateModel(data);
    }
    
    private void updateModel(Object[][] data) {
        setModel(new DefaultTableModel(data, new Object[] {"", ""}));

        TableColumn sumCol = getColumnModel().getColumn(0);
        sumCol.setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Value"));
        sumCol.setCellRenderer(new CurrencyCellRenderer());
        sumCol.setMaxWidth(200);
        sumCol.setMinWidth(50);
        sumCol.setWidth(110);
        
        getColumnModel().getColumn(1).setHeaderValue(mybudget.option.Options.I18N_BUNDLE.getString("Tags"));
    }
    
    private void sort(Object[][] mas) {
        for (int i = 0; i < mas.length - 1; i++) {
            for (int j = 0; j < mas.length - 1; j++) {
                if ((Double) mas[j][0] < (Double) mas[j + 1][0]) {
                    Object obj0 = mas[j][0];
                    Object obj1 = mas[j][1];
                    mas[j][0] = mas[j + 1][0];
                    mas[j][1] = mas[j + 1][1];
                    mas[j + 1][0] = obj0;
                    mas[j + 1][1] = obj1;
                }
            }
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    
}
