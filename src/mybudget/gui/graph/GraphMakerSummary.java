
package mybudget.gui.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * GraphMakerSummary 23.03.2009 (23:10:35)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class GraphMakerSummary extends AbstractGraphMaker {
    
    private Object[][] data;
    
    private static final Color[] colors = {
        Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, new Color(0, 200, 255), 
        Color.BLUE, Color.MAGENTA, Color.GRAY, Color.PINK
    };

    public GraphMakerSummary() {
        setXPadding(15);
        setYPadding(15);
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                try {
                    if (null == data) {
                        return;
                    }
                    double xStepPixel = (getWidth() - 2 * getXPadding()) / ((double) data.length);
                    double indexPart = (getMousePosition().x - getXPadding()) / xStepPixel;
                    if (0 <= indexPart && data.length > indexPart) {
                        int index = (int) indexPart;
                        setToolTipText((String) data[index][1]);
                    } else {
                        setToolTipText(null);
                    }
                    super.mouseMoved(e);
                } catch (Exception ex) { }
            }
            
        });    
    }
    
    public void setGraphData(Object[][] data) {
        this.data = data;
        maxValue = 0.;
        for (int i = 0; i < data.length; i++) {
            double dTemp = (Double) data[i][0];
            if (maxValue < dTemp) {
                maxValue = dTemp;
            }
        }
    }

    @Override
    public void updateData() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int y1, y2;        
        int xRectWidth = (int) Math.round((getWidth() - 2 * getXPadding()) / ((double) data.length));
        
        y1 = getHeight() - getYPadding();
        for (int i = 0; i < data.length; i++) {
            g2.setColor(colors[i % colors.length]);
            y2 = getHeight() - getYPadding() - (int) ((Double) data[i][0] * (getHeight() - 2 * getYPadding()) / maxValue);
            g2.fillRect((i * xRectWidth) + getXPadding(), y2, xRectWidth, y1 - y2);
        }
        
    }

}
