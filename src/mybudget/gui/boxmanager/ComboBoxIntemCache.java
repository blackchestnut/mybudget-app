package mybudget.gui.boxmanager;

import mybudget.database.element.Transaction;
import mybudget.option.Options;

/**
 * Класс для элементов кэша в комбобоксе последних транзакций.
 * @author slash
 */
public class ComboBoxIntemCache {

    private Transaction t;

    public ComboBoxIntemCache(Transaction t) {
        this.t = t;
    }

    public Transaction getItem() {
        return t;
    }

    @Override
    public String toString() {
        return "    " + t.getTagsName() + " :  " + Options.toCurrency(t.getValue());
    }
}
