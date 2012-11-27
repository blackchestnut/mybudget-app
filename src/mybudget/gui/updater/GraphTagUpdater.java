package mybudget.gui.updater;

import mybudget.gui.*;

/**
 *
 * @author Home
 */
public class GraphTagUpdater implements ITagsUpdater {
    private MainWindow parent;

    public GraphTagUpdater(MainWindow parent) {
        this.parent = parent;
    }

    public void refresh() {
        parent.updateGraphForTag();
    }
}
