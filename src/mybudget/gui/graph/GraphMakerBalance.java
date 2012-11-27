
package mybudget.gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import mybudget.database.element.Transaction;
import mybudget.database.manager.TransactionManager;
import mybudget.option.Options;

/**
 * GraphMakerBalance 27.07.2008 (21:43:06)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class GraphMakerBalance extends AbstractGraphMaker {
    
    private double[] balance;
    
    private Color colorBalance = new Color(0, 0, 255);
    protected TransactionManager transactionManager;
    protected List<Transaction> listTransaction;

    public GraphMakerBalance(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        setXPadding(15);
        setYPadding(20);
        setXDivideCount(7);
        setYDivideCount(6);
    }
    
    private void paintZeroLine(Graphics2D g2, int yStep) {
        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();
        
        g2.setColor(Color.RED);
        float[] dash = {10.1f};
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        
        int y = getHeight() - getYPadding() + yStep;
        g2.drawLine(getXPadding(), y, getWidth() - 2 * getXPadding(), y);
        
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setStroke(new BasicStroke(2));
        g2.setColor(getColorBalance());
        
        double xStepPixel = (getWidth() - 2 * getXPadding()) / ((double) balance.length);
        int x1, y1, x2, y2;
        double coef = (getHeight() - 2 * getYPadding()) / (maxValue - minValue);
        int yAddedStep = 0 > minValue ? (int) (minValue * coef): 0;
        x1 = getXPadding();
        y1 = yAddedStep + getHeight() - getYPadding();
        if (0 != yAddedStep) {
            paintZeroLine(g2, yAddedStep);
        }
        for (int i = 0; i < balance.length; i++) {
            x2 = (int) ((i + 1) * xStepPixel) + getXPadding();
            y2 = yAddedStep + getHeight() - getYPadding() - (int) (balance[i] * coef);
            g2.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }
        paintAxis(g2);
    }    

    @Override
    public void updateData() {
        GregorianCalendar calen = Options.CALENDAR;
        calen.setTime(finishDate);
        int finishYear = calen.get(Calendar.YEAR);
        int finishMonth = calen.get(Calendar.MONTH);
        calen.setTime(startDate);
        int startYear = calen.get(Calendar.YEAR);
        int startMonth = calen.get(Calendar.MONTH);
        int numTick = (finishYear - startYear) * 12 + finishMonth - startMonth + 1;
        balance = new double[numTick];
        setIndexAxeCount(balance.length);
        
        listTransaction = transactionManager.getListTransaction(startDate, finishDate);
        for (Transaction tr : listTransaction) {
            calen.setTime(tr.getDate());
            int currYear = calen.get(Calendar.YEAR);
            int currMonth = calen.get(Calendar.MONTH);
            int index = (currYear - startYear) * 12 + currMonth - startMonth;
            if (tr.getType().equals(Transaction.TYPE_INCOME)) {
                balance[index] += tr.getValue();
            } else {
                balance[index] -= tr.getValue();
            }
        }
        for (int i = 1; i < balance.length; i++) {
            balance[i] += balance[i - 1];
        }
        maxValue = minValue = 0.;
        for (int i = 0; i < balance.length; i++) {
            if (maxValue < balance[i])
                maxValue = balance[i];
            if (minValue > balance[i])
                minValue = balance[i];
        }
    }

    public Color getColorBalance() {
        return colorBalance;
    }

    public void setColorBalance(Color colorBalance) {
        this.colorBalance = colorBalance;
    }

}
