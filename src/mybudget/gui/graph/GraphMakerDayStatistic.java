package mybudget.gui.graph;

import mybudget.database.element.Transaction;
import mybudget.database.manager.TransactionManager;

/**
 * GraphMakerStatistic 29.05.2008 (14:27:07)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class GraphMakerDayStatistic extends GraphMakerStatistic {
    
    private int tickDay = 1;
    
    public GraphMakerDayStatistic(TransactionManager transactionManager) {
        super(transactionManager);
    }
    
    public void setTickDay(int tickDay) {
        if (0 >= tickDay)
            throw new IllegalArgumentException("tickDay <= 0!");
        this.tickDay = tickDay;
    }
    
    public int getTickDay() {
        return tickDay;
    }
      
    protected int getTickCount() {
        return (int)((finishDate.getTime() - startDate.getTime()) / 
                (getTickDay() * AbstractGraphMaker.MLS_IN_DAY));
    }        
    
    @Override
    public void updateData() {
        int numTick = getTickCount();
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
        long coef = getTickDay() * AbstractGraphMaker.MLS_IN_DAY;
        for (Transaction tr : listTransaction) {
            int index = (int) ((tr.getDate().getTime() - startDate.getTime()) / coef);
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

}
