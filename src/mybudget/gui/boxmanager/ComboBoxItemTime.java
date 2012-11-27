
package mybudget.gui.boxmanager;

import java.util.Date;

/**
 * ComboBoxItemTime 10.05.2008 (14:46:54)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class ComboBoxItemTime {
    
    private String name;
    private Date startDate;
    private Date finishDate;

    public ComboBoxItemTime(String name, Date startDate, Date finishDate) {
        this.name = name;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public Date getFinishDate() {
        return finishDate;
    }

}
