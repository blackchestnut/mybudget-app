
package mybudget.gui.boxmanager;

import java.util.ArrayList;
import java.util.List;
import mybudget.database.element.Tag;

/**
 * ComboBoxTag 09.05.2008 (15:51:42)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class ComboBoxItemTag {

    private Tag tag;
    private int type;
    
    public static final int TYPE_ADD = 0;
    public static final int TYPE_DELETE = 1;
    
    public ComboBoxItemTag(Tag tag, int type) {
        this.tag = tag;
        this.type = type;
    }

    public Tag getTag() {
        return tag;
    }
    
    public int getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return "      " + tag.getName();
    }
    
    public static List<ComboBoxItemTag> create(List<Tag> tagList, int type) {
        List<ComboBoxItemTag> list = new ArrayList<ComboBoxItemTag>();
        for (Tag tag : tagList) {
            list.add(new ComboBoxItemTag(tag, type));
        }
        return list;
    }
        
}
