package mybudget.gui.updater;

import mybudget.gui.*;

/**
 *
 * @author Slash
 */
public class TransactionTagUpdater implements ITagsUpdater {
    private MainWindow parent;
    private int type;

    public TransactionTagUpdater(MainWindow parent, int type) {
        this.parent = parent;
        this.type = type;
    }

    public void refresh() {
        parent.updateContent(type);
    }

}
