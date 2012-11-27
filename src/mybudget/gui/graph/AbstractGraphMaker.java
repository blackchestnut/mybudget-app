package mybudget.gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JPanel;
import mybudget.option.Options;

/**
 * GraphMakerStatistic 29.05.2008 (13:58:48)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public abstract class AbstractGraphMaker extends JPanel {
    protected Date startDate;
    protected Date finishDate;
    private int xPadding = 5;
    private int yPadding = 10;
    private int xDivideTopLength = 2;
    private int xDivideBottomLength = 2;
    private int yDivideLeftLength = 2;
    private int yDivideRightLength = 2;
    private int xDivideCount = 5;
    private int yDivideCount = 5;
    protected double maxValue = 0;
    protected double minValue = 0;
    protected SimpleDateFormat dateFormat = Options.DATE_FORMAT;
    protected int indexAxeCount = 0;

    
    private Color colorAxis = new Color(50, 50, 50);
    
    public final static long MLS_IN_DAY = 1000 * 3600 * 24;
    
    public AbstractGraphMaker() {
        setBackground(new Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEtchedBorder());
    }
    
    public void setDateInterval(Date startDate, Date finishDate) {
        if (startDate.after(finishDate))
            throw new IllegalArgumentException("Start Date - after Finish Date.");
        this.startDate = startDate;
        this.finishDate = finishDate;
    }
    
    public abstract void updateData();

    public int getXPadding() {
        return xPadding;
    }

    public void setXPadding(int xPadding) {
        if (0 > xPadding)
            throw new IllegalArgumentException("x padding < 0");
        this.xPadding = xPadding;
    }

    public int getYPadding() {
        return yPadding;
    }

    public void setYPadding(int yPadding) {
        if (0 > yPadding)
            throw new IllegalArgumentException("y padding < 0");
        this.yPadding = yPadding;
    }
    
    protected int getIndexAxeCount() {
        return indexAxeCount;
    }
    
    protected void setIndexAxeCount(int indexAxeCount) {
        if (0 > indexAxeCount)
            return;
        this.indexAxeCount = indexAxeCount;
    }
    
    public void paintAxis(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2));
        g2.setColor(getColorAxis());
        g2.drawLine(getXPadding(), getHeight() - getYPadding(), getXPadding(), getYPadding());
        g2.drawLine(getXPadding(), getHeight() - getYPadding(), getWidth() - getXPadding(), 
                getHeight() - getYPadding());
        
        g2.drawLine(getXPadding(), getYPadding(), getXPadding() - 2, getYPadding() + 6);
        g2.drawLine(getXPadding(), getYPadding(), getXPadding() + 2, getYPadding() + 6);
        g2.drawLine(getWidth() - getXPadding(), getHeight() - getYPadding(), 
                getWidth() - getXPadding() - 6, getHeight() - getYPadding() - 2);
        g2.drawLine(getWidth() - getXPadding(), getHeight() - getYPadding(), 
                getWidth() - getXPadding() - 6, getHeight() - getYPadding() + 2);
        paintAxisValue(g2);
        paintAxisTime(g2);
    }
    
    public void paintAxisTime(Graphics2D g2) {
        if (0 == getIndexAxeCount())
            return;
        g2.setStroke(new BasicStroke(2));
        g2.setColor(getColorAxis());
        g2.setFont(getFont().deriveFont(10));
        int idAxe = getIndexAxeCount();
        int countDevide = getXDivideCount() < idAxe ? getXDivideCount() : idAxe;
        double xStepPixel = (getWidth() - 2 * getXPadding()) / (double) countDevide;
        long timeStep = (finishDate.getTime() - startDate.getTime()) / countDevide;
        for (int i = 1; i < countDevide; i++) {
            int x = getXPadding() + (int) (i * xStepPixel);
            g2.drawLine(x, getHeight() - getYPadding() - 2, x, getHeight() - getYPadding() + 2);
            g2.drawString(dateFormat.format(new Date(startDate.getTime() + i * timeStep)), x - 20, 
                    getHeight() - getYPadding() + 15);
        }
    }
    
    public void paintAxisValue(Graphics g) {
        if (0 == getIndexAxeCount())
            return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.setColor(getColorAxis());
        int countDigits = getCountDigits(maxValue);
        if (0 == countDigits)
            return;
        double stepValue = (maxValue - minValue) / getYDivideCount();
        int roundDigits = countDigits - 3;
        double yStepPixel = (getHeight() - 2 * getYPadding()) / (double) getYDivideCount();
        for (int i = 1; i < getYDivideCount(); i++) {
            int y = getHeight() - getYPadding() - (int) (i * yStepPixel);
            g2.drawLine(getXPadding() - 2, y, getXPadding() + 2, y);
            g2.drawString(Integer.toString(roundVal(minValue + i * stepValue, roundDigits)), getXPadding() + 4, y + 5);
        }
    }    
    
    public int roundVal(double val, int roundDigits) {
        if (0 > roundDigits)
            return (int) val;
        int coef = (int) Math.pow(10, roundDigits);
        return ((int) (val / coef) * coef);
    }
    
    public int getCountDigits(double val) {
        int ival = (int) val;
        int count = 0;
        while (0 != ival) {
            count ++;
            ival /= 10;
        }
        return count;
    }

    public Color getColorAxis() {
        return colorAxis;
    }

    public void setColorAxis(Color colorAxis) {
        this.colorAxis = colorAxis;
    }
    
    public int getXDivideCount() {
        return xDivideCount;
    }

    public int getYDivideCount() {
        return yDivideCount;
    }

    public void setXDivideCount(int xDivideCount) {
        this.xDivideCount = xDivideCount;
    }

    public void setYDivideCount(int yDivideCount) {
        this.yDivideCount = yDivideCount;
    }
    
    public void setXDivideLength(int xDivideTopLength, int xDivideBottomLength) {
        this.xDivideTopLength = xDivideTopLength;
        this.xDivideBottomLength = xDivideBottomLength;
    }
    
    public void setYDivideLength(int yDivideLeftLength, int yDivideRightLength) {
        this.yDivideLeftLength = yDivideLeftLength;
        this.yDivideRightLength = yDivideRightLength;
    }
}
