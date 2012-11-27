/*
 * DialogTransaction.java
 *
 * Created on 25 Апрель 2008 г., 16:13
 */
package mybudget.gui.dialog;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import javax.swing.JOptionPane;
import mybudget.database.element.Tag;
import mybudget.option.Options;
import mybudget.database.element.Transaction;
import mybudget.database.manager.TagManager;
import mybudget.database.manager.TransactionManager;
import mybudget.gui.boxmanager.ComboBoxIntemCache;
import mybudget.option.PrefValue;
import mybudget.table.TagTransactionTable;

/**
 *
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class DialogTransaction extends javax.swing.JDialog {

    private boolean result = false;
    private TransactionManager transactionManager;
    private TagManager tagManager;
    private Transaction transaction;
    private List<Long> tagIdList = new ArrayList<Long>();

    /**
     * Предыдущая дата для дохода/расхода.
     */
    private Date prevDate = new Date();

    private TagTransactionTable table;

    /**
     * Разделитель для дробных чисел в ОС.
     */
    private static char separator = new DecimalFormatSymbols().getDecimalSeparator();

    /**
     * Кэш для последних транзакций.
     */
    private Deque<Transaction> cache;

    /**
     * Размер кэша для последних транзакций.
     */
    private static final int CACHE_SIZE = 20;

    public DialogTransaction(java.awt.Frame parent,
            TransactionManager transactionManager, TagManager tagManager) {
        super(parent);
        initComponents();
        setSize(Options.PREF.getInt(PrefValue.KEY_DTR_WIDTH, PrefValue.VAL_DTR_WIDTH), 
                Options.PREF.getInt(PrefValue.KEY_DTR_HEIGHT, PrefValue.VAL_DTR_HEIGHT));
        setLocation(Options.PREF.getInt(PrefValue.KEY_DTR_X, PrefValue.VAL_DTR_X), 
                Options.PREF.getInt(PrefValue.KEY_DTR_Y, PrefValue.VAL_DTR_Y));
        this.transactionManager = transactionManager;
        this.tagManager = tagManager;
        this.jCCField.setOwner(this);
        cache = new ArrayDeque<Transaction>(transactionManager.getLastTransactionFull(CACHE_SIZE));
    }

    private void initComboBoxCache() {
        if (cache == null) {
            cache = new ArrayDeque<Transaction>(transactionManager.getLastTransactionFull(CACHE_SIZE));
        }
        jComboBoxCache.removeAllItems();
        jComboBoxCache.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Last_Transaction"));
        for (Transaction t : cache) {
            jComboBoxCache.addItem(new ComboBoxIntemCache(t));
        }
        jComboBoxCache.setSelectedIndex(0);
    }

    public Transaction getResultTransaction() {
        return transaction;
    }
    
    public boolean getResult() {
        return result;
    }

    public List<Long> getTagIdList() {
        return tagIdList;
    }

    public void showForNewIncome() {
        setTitle(mybudget.option.Options.I18N_BUNDLE.getString("New_Income_Transaction"));
        showForEdit(new Transaction(prevDate));
    }
    
    public void showForNewExpense() {
        setTitle(mybudget.option.Options.I18N_BUNDLE.getString("New_Expense_Transaction"));
        showForEdit(new Transaction(prevDate));
    }

    private void showForEdit(Transaction editTransaction) {
        if (null == editTransaction) {
            throw new NullPointerException("void showForEdit(Transaction editTransaction) - editTransaction = null");
        }
        result = false;
        this.transaction = editTransaction;
        jTextFieldDescription.setText("" + transaction.getDescription());
        jTextFieldValue.setText(Options.toCurrency(transaction.getValue()));
        jCCField.setDate(transaction.getDate());
        jTextFieldDescription.requestFocus();
        table = createTagTable();
        jScrollPaneTagList.setViewportView(table);
        for (Object tag : transaction.getTags()) {
            Long id = ((Tag) tag).getId();
            for (int i = 0; i < table.getRowCount(); i++) {
                if (id.equals(table.getValueAt(i, 0))) {
                    table.setValueAt(true, i, 1);
                }
            }
        }
        jTextFieldValue.select(0, jTextFieldValue.getText().length());
        jTextFieldValue.requestFocus();
        initComboBoxCache();
        setVisible(true);
    }
    
    public void showForEditIncome(Transaction editTransaction) {
        setTitle(mybudget.option.Options.I18N_BUNDLE.getString("Edit_Income_Transaction"));
        showForEdit(editTransaction);
    }
    
    public void showForEditExpense(Transaction editTransaction) {
        setTitle(mybudget.option.Options.I18N_BUNDLE.getString("Edit_Expense_Transaction"));
        showForEdit(editTransaction);
    }

    private TagTransactionTable createTagTable() {
        return new TagTransactionTable(tagManager.getListTagCache());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldValue = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldDescription = new javax.swing.JTextField();
        jCCField = new com.artcodesys.jcc.JCCField();
        jComboBoxCache = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneTagList = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(mybudget.option.Options.I18N_BUNDLE.getString("Description:")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jLabel1, gridBagConstraints);

        jLabel2.setText(mybudget.option.Options.I18N_BUNDLE.getString("Value:")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jLabel2, gridBagConstraints);

        jTextFieldValue.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jTextFieldValue, gridBagConstraints);

        jLabel3.setText(mybudget.option.Options.I18N_BUNDLE.getString("Date:")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
        jPanel4.add(jLabel3, gridBagConstraints);

        jTextFieldDescription.setMaximumSize(new java.awt.Dimension(250, 20));
        jTextFieldDescription.setMinimumSize(new java.awt.Dimension(250, 20));
        jTextFieldDescription.setPreferredSize(new java.awt.Dimension(250, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jTextFieldDescription, gridBagConstraints);
        jPanel4.add(jCCField, new java.awt.GridBagConstraints());

        jComboBoxCache.setMaximumRowCount(21);
        jComboBoxCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCacheActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel4.add(jComboBoxCache, gridBagConstraints);

        jPanel1.add(jPanel4, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(jScrollPaneTagList, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButtonCancel.setText(mybudget.option.Options.I18N_BUNDLE.getString("Cancel")); // NOI18N
        jButtonCancel.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonCancel.setPreferredSize(new java.awt.Dimension(90, 23));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonCancel);

        jButtonOK.setText(mybudget.option.Options.I18N_BUNDLE.getString("OK")); // NOI18N
        jButtonOK.setPreferredSize(new java.awt.Dimension(90, 23));
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonOK);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        try {
            if (null == transaction) {
                transaction = new Transaction();
            }
            Date date = jCCField.getDate();
            if (date.after(new Date())) {
                throw new IllegalArgumentException(mybudget.option.Options.I18N_BUNDLE.getString("Date_in_future"));
            }
            transaction.setDate(date);
            transaction.setDescription(jTextFieldDescription.getText().trim());

            String strValue = jTextFieldValue.getText().trim().replace(',', separator).replace('.', separator);
            Double value = NumberFormat.getInstance().parse(strValue).doubleValue();
                    
            if (0 > value) {
                throw new IllegalArgumentException(mybudget.option.Options.I18N_BUNDLE.getString("") + value + " < 0");
            }
            transaction.setValue(value);
            tagIdList.clear();
            tagIdList.addAll(table.getSelectedTagIDs());
            result = true;
            cache.addFirst(new Transaction(transaction, table.getSelectedTags()));
            if (cache.size() > CACHE_SIZE) {
                cache.removeLast();
            }
            prevDate = transaction.getDate();
            setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), mybudget.option.Options.I18N_BUNDLE.getString("Error"), JOptionPane.ERROR_MESSAGE);
            //Logger.getLogger(DialogTransaction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        transaction = null;
        result = false;
        setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jComboBoxCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCacheActionPerformed
        int id = jComboBoxCache.getSelectedIndex();
        if (id < 0) {
            return;
        }
        Object item = jComboBoxCache.getSelectedItem();
        if (!(item instanceof ComboBoxIntemCache)) {
            return;
        }
        Transaction elem = ((ComboBoxIntemCache) item).getItem();
        jTextFieldValue.setText(Options.toCurrency(elem.getValue()));
        jTextFieldDescription.setText(elem.getDescription());
        table.selectTags(new ArrayList(elem.getTags()));
        table.updateUI();
        jComboBoxCache.setSelectedIndex(0);

    }//GEN-LAST:event_jComboBoxCacheActionPerformed
    // <editor-fold defaultstate="collapsed" desc="Gui Variables">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private com.artcodesys.jcc.JCCField jCCField;
    private javax.swing.JComboBox jComboBoxCache;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPaneTagList;
    private javax.swing.JTextField jTextFieldDescription;
    private javax.swing.JTextField jTextFieldValue;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
}
