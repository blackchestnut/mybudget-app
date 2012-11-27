
package mybudget.gui.graph;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import mybudget.database.element.Transaction;
import mybudget.database.manager.TransactionManager;
import mybudget.option.Options;

/**
 * GraphMakerYearStatistic 13.06.2008 (15:05:17)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class GraphMakerYearStatistic extends GraphMakerStatistic {
    /**
     * Формат даты для тултипа.
     */
    private static SimpleDateFormat descFormat = new SimpleDateFormat("yyyy");

    public GraphMakerYearStatistic(TransactionManager transactionManager) {
        super(transactionManager);
    }

    @Override
    public void updateData() {
        GregorianCalendar calen = Options.CALENDAR;
        calen.setTime(finishDate);
        int finishYear = calen.get(Calendar.YEAR);
        calen.setTime(startDate);
        int startYear = calen.get(Calendar.YEAR);
        int numTick = (finishYear - startYear) + 1;
        incomeValues = new double[numTick];
        expenseValues = new double[numTick];
        setIndexAxeCount(incomeValues.length);
        if (null == listSpecificTags) {
            listTransaction = transactionManager.getListTransaction(startDate, finishDate);
        } else if (flagSpecificTagAnd) {
                listTransaction = transactionManager.getListTransactionWithSpecificTags(startDate, 
                        finishDate, listSpecificTags);
        } else {
                listTransaction = transactionManager.getListTransactionWithOneOfTags(startDate, 
                        finishDate, listSpecificTags);
        }
        for (Transaction tr : listTransaction) {
            calen.setTime(tr.getDate());
            int currYear = calen.get(Calendar.YEAR);
            int index = currYear - startYear;
            if (tr.getType().equals(Transaction.TYPE_INCOME)) {
                incomeValues[index] += tr.getValue();
            } else {
                expenseValues[index] += tr.getValue();
            }
        }
        if (GRAPH_TYPE_1 == graphType) {
            for (int i = 1; i < incomeValues.length; i++) {
                incomeValues[i] += incomeValues[i - 1];
                expenseValues[i] += expenseValues[i - 1];
            }
        }
        maxValue = 0.;
        for (int i = 0; i < incomeValues.length; i++) {
            if (maxValue < incomeValues[i])
                maxValue = incomeValues[i];
            if (maxValue < expenseValues[i])
                maxValue = expenseValues[i];
        }
        
    }

    @Override
    public String getToolTipDescription(int index) {
        Options.CALENDAR.setTime(startDate);
        Options.CALENDAR.add(Calendar.YEAR, index);
        return " / " + descFormat.format(Options.CALENDAR.getTime());
    }
}
