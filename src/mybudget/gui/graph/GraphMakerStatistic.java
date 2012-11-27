package mybudget.gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import mybudget.database.element.Tag;
import mybudget.database.manager.TransactionManager;

/**
 * GraphMakerStatistic 07.06.2008 (15:52:45)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public abstract class GraphMakerStatistic extends AbstractGraphIncomeExpenseMaker {

    public static final int GRAPH_TYPE_1 = 0;
    public static final int GRAPH_TYPE_2 = 1;
    private Color bkgColor = new Color(220, 240, 250);

    protected boolean flagSpecificTagAnd = true;

    protected int graphType;
    protected List<Tag> listSpecificTags;
    
    public GraphMakerStatistic(TransactionManager transactionManager) {
        super(transactionManager);
        setGraphType(GraphMakerDayStatistic.GRAPH_TYPE_1);
        setXPadding(15);
        setYPadding(20);
        setXDivideCount(7);
        setYDivideCount(6);
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                try {
                    if (GRAPH_TYPE_2 == graphType) {
                        double xStepPixel = (getWidth() - 2 * getXPadding()) / ((double) incomeValues.length);
                        double indexPart = (getMousePosition().x - getXPadding()) / xStepPixel;
                        if (0 <= indexPart && incomeValues.length > indexPart) {
                            int index = (int) indexPart;
                            String tip = 0.5 >= indexPart - index 
                                    ? "+ " + (int) incomeValues[index]
                                    : "- " + (int) expenseValues[index];
                            setToolTipText(tip + getToolTipDescription(index));
                        }
                    } else {
                        setToolTipText(null);
                    }
                    super.mouseMoved(e);
                } catch (Exception ex) { }
            }
            
        });
    }

    public String getToolTipDescription(int index) {
        return "";
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (GRAPH_TYPE_1 == graphType) {
            paintGraphType1(g2);
            paintAxis(g2);
        } else if (GRAPH_TYPE_2 == graphType) {
            paintGraphType2(g2);
            paintAxis(g2);
        }
    }

    protected void paintGraphType1(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2));

        double xStepPixel = (getWidth() - 2 * getXPadding()) / ((double) incomeValues.length);
        int x1;
        int y1;
        int x2;
        int y2;

        g2.setColor(getColorIncome());
        x1 = getXPadding();
        y1 = getHeight() - getYPadding();
        for (int i = 0; i < incomeValues.length; i++) {
            x2 = (int) ((i + 1) * xStepPixel) + getXPadding();
            y2 = getHeight() - getYPadding() - (int) (incomeValues[i] * (getHeight() - 2 * getYPadding()) / maxValue);
            g2.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }

        g2.setColor(getColorExpense());
        x1 = getXPadding();
        y1 = getHeight() - getYPadding();
        for (int i = 0; i < expenseValues.length; i++) {
            x2 = (int) ((i + 1) * xStepPixel) + getXPadding();
            y2 = getHeight() - getYPadding() - (int) (expenseValues[i] * (getHeight() - 2 * getYPadding()) / maxValue);
            g2.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }
    }

    protected void paintGraphType2(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2));
        g2.setColor(bkgColor);

        int x1, y1, y2;
        int xStepPixel = (int) Math.round((getWidth() - 2 * getXPadding()) / ((double) incomeValues.length));
        
        x1 = getXPadding() + xStepPixel;
        y1 = getHeight() - getYPadding();
        y2 = getYPadding();
        for (int i = 0; i < incomeValues.length / 2; i++) {
            g2.fillRect(x1, y2, xStepPixel, y1 - y2);
            x1 = (i * 2 + 2) * xStepPixel + getXPadding() + xStepPixel;
        }

        int xRectWidth = (int) Math.round(xStepPixel / 2);
        g2.setColor(getColorIncome());
        y1 = getHeight() - getYPadding();
        for (int i = 0; i < incomeValues.length; i++) {
            y2 = getHeight() - getYPadding() - (int) (incomeValues[i] * (getHeight() - 2 * getYPadding()) / maxValue);
            g2.fillRect(i * xStepPixel + getXPadding(), y2, xRectWidth, y1 - y2);
        }

        g2.setColor(getColorExpense());
        y1 = getHeight() - getYPadding();
        for (int i = 0; i < expenseValues.length; i++) {
            y2 = getHeight() - getYPadding() - (int) (expenseValues[i] * (getHeight() - 2 * getYPadding()) / maxValue);
            g2.fillRect(i * xStepPixel + getXPadding() + xRectWidth, y2, xRectWidth, y1 - y2);
        }
    }

    public void setFlagSpecificTagAnd(boolean flagSpecificTagAnd) {
        this.flagSpecificTagAnd = flagSpecificTagAnd;
    }

    public void setGraphType(int type) {
        this.graphType = type;
    }

    public void setListSpecificTags(List<Tag> listSpecificTags) {
        this.listSpecificTags = listSpecificTags;
    }

}
