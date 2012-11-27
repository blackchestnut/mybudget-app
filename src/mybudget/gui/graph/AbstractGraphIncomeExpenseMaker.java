
package mybudget.gui.graph;

import java.awt.Color;
import java.util.List;
import mybudget.database.element.Transaction;
import mybudget.database.manager.TransactionManager;

/**
 * AbstractGraphIncomeExpenseMaker 27.07.2008 (20:47:00)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public abstract class AbstractGraphIncomeExpenseMaker extends AbstractGraphMaker {

    protected double[] incomeValues;
    protected double[] expenseValues;
    
    private Color colorIncome = new Color(0, 200, 130);
    private Color colorExpense = new Color(255, 150, 0);
    protected TransactionManager transactionManager;
    protected List<Transaction> listTransaction;
    
    public AbstractGraphIncomeExpenseMaker(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    public Color getColorIncome() {
        return colorIncome;
    }

    public void setColorIncome(Color colorIncome) {
        this.colorIncome = colorIncome;
    }

    public Color getColorExpense() {
        return colorExpense;
    }

    public void setColorExpense(Color colorExpense) {
        this.colorExpense = colorExpense;
    }    

}
