/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mybudget.gui.listeners;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import mybudget.gui.updater.ITagsUpdater;
import mybudget.gui.TagsPanel;

/**
 *
 * @author slash
 */
public class TagFilterMouseListener extends MouseAdapter {
    private TagsPanel tagsPanel;
    private ITagsUpdater updater;

    public TagFilterMouseListener(TagsPanel panel, ITagsUpdater updater) {
        this.tagsPanel = panel;
        this.updater = updater;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int rowCount = tagsPanel.getTable().getRowCount();
        if (0 < rowCount) {
            tagsPanel.setAllCheckBox(rowCount <= tagsPanel.getSelectedCount());
            updater.refresh();
        }
        super.mouseClicked(e);
    }
}
