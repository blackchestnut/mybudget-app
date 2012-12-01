/*
 * MainWindow.java
 *
 * Created on 19 Апрель 2008 г., 20:40
 */
package mybudget.gui;

import mybudget.gui.updater.TransactionTagUpdater;
import mybudget.gui.updater.GraphTagUpdater;
import java.awt.event.MouseEvent;
import java.util.GregorianCalendar;
import mybudget.gui.dialog.DialogTransaction;
import mybudget.gui.dialog.DialogTag;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import mybudget.Main;
import mybudget.database.element.Tag;
import mybudget.database.element.Transaction;
import mybudget.database.manager.TagManager;
import mybudget.database.manager.TransactionManager;
import mybudget.database.util.HibernateUtil;
import mybudget.gui.boxmanager.AdditionMovementManager;
import mybudget.gui.boxmanager.ComboBoxItemTag;
import mybudget.gui.boxmanager.ComboBoxItemTime;
import mybudget.gui.dialog.DialogOptions;
import mybudget.gui.dialog.DialogAbout;
import mybudget.gui.graph.AbstractGraphMaker;
import mybudget.gui.graph.GraphMakerBalance;
import mybudget.gui.graph.GraphMakerDayStatistic;
import mybudget.gui.graph.GraphMakerMonthStatistic;
import mybudget.gui.graph.GraphMakerStatistic;
import mybudget.gui.graph.GraphMakerSummary;
import mybudget.gui.graph.GraphMakerYearStatistic;
import mybudget.option.Options;
import mybudget.option.PrefValue;
import mybudget.table.SumTable;
import mybudget.table.TagTable;
import mybudget.table.TransactionTable;
import org.apache.derby.drda.NetworkServerControl;
/**
 *
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class MainWindow extends javax.swing.JFrame implements PrefValue {
    private Preferences pref = Options.PREF;

    private NetworkServerControl server;

    private TagManager tagManager = new TagManager();
    private TransactionManager transactionManager = new TransactionManager();
    private TagsPanel tagsIncomePanel = new TagsPanel(tagManager, new TransactionTagUpdater(this, Transaction.TYPE_INCOME));
    private TagsPanel tagsExpensePanel = new TagsPanel(tagManager, new TransactionTagUpdater(this, Transaction.TYPE_EXPENSE));
    private TagsPanel tagsGraphPanel = new TagsPanel(tagManager, new GraphTagUpdater(this));

    private DialogTag dialogTag;
    private DialogTransaction dialogTransaction;
    private DialogOptions dialogOptions;
    private DialogAbout dialogAbout;
    private GraphMakerMonthStatistic graphMakerGeneral;
    private GraphMakerDayStatistic graphMakerDayStatistic;
    private GraphMakerDayStatistic graphMakerWeekStatistic;
    private GraphMakerMonthStatistic graphMakerMonthStatistic;
    private GraphMakerYearStatistic graphMakerYearStatistic;
    private GraphMakerBalance graphMakerBalance;
    private GraphMakerSummary graphMakerSummary;
    
    private int[] balanceSliderNumMonth = { 120, 60, 36, 24, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2 };
    private int prevTimeDayIndex = -1;
    private int prevTimeWeekIndex = -1;
    private int prevTimeGeneralIndex = - 1;
    private int prevTimeMonthIndex = -1;
    private int prevTimeYearIndex = -1;
    private int prevTimeBalanceIndex = -1;
    
    private AdditionMovementManager additionMovementIncomeManager;
    private AdditionMovementManager additionMovementExpenseManager;
    
    private Date incomeStartDate;
    private Date incomeFinishDate;
    private Date expenseStartDate;
    private Date expenseFinishDate;
    private SumTable tableSummary;
    
    private Color colorIncome = new Color(pref.getInt(KEY_INCOME_R, VAL_INCOME_R),
                pref.getInt(KEY_INCOME_G, VAL_INCOME_G),
                pref.getInt(KEY_INCOME_B, VAL_INCOME_B));
    private Color colorExpense = new Color(pref.getInt(KEY_EXPENSE_R, VAL_EXPENSE_R),
                pref.getInt(KEY_EXPENSE_G, VAL_EXPENSE_G),
                pref.getInt(KEY_EXPENSE_B, VAL_EXPENSE_B));
    private Color colorBalance = new Color(pref.getInt(KEY_BALANCE_R, VAL_BALANCE_R),
                pref.getInt(KEY_BALANCE_G, VAL_BALANCE_G),
                pref.getInt(KEY_BALANCE_B, VAL_BALANCE_B));
    
    ResourceBundle i18n = mybudget.option.Options.I18N_BUNDLE;
    
    private int lastIndexIncomePeriod = pref.getInt(KEY_INDEX_PERIOD_INCOME, 
                VAL_INDEX_PERIOD_INCOME);
    private int lastIndexExpensePeriod = pref.getInt(KEY_INDEX_PERIOD_EXPENSE, 
                VAL_INDEX_PERIOD_EXPENSE);
    private int indexGraphType = pref.getInt(KEY_INDEX_GRAPH_TYPE, VAL_INDEX_GRAPH_TYPE);
    private int indexGraphTickTime = pref.getInt(KEY_INDEX_GRAPH_TICK_TIME, VAL_INDEX_GRAPH_TICK_TIME);
    
    private int indexSelectLanguage = -1;
    private int indexSelectDatePattern = -1;

    /** Creates new form MainWindow */
    public MainWindow() {
        initComponents();
        serverStart();
        initMainWindow();               
        Main.splashForm.setVisible(false);              
    }

    /**
     * Обновляет данные доходов или расходов
     * @param type тип транзакций
     */
    public void updateContent(int type) {
        if (type == Transaction.TYPE_INCOME) {
            updateIncomeContent();
        } else {
            updateExpenseContent();
        }
    }

    private String getInfoForDeleteTransaction(TransactionTable table, int[] selectRows) {
        String info = (String) table.getValueAt(selectRows[0], 4);
        if (info.equals("")) {
            info = (String) table.getValueAt(selectRows[0], 1) + " " + table.getValueAt(selectRows[0], 2);
        }
        return info;
    }

    /**
     * Обновляет данные в гриде расходов
     */
    private void updateExpenseContent() {
        TransactionTable table = createNewExpenseTransactionTable();
        TransactionTable prevTable = (TransactionTable) jScrollPaneExpense.getViewport().getView();
        int prevIndex = prevTable == null ? -1 : prevTable.getSelectedRow();
        jScrollPaneExpense.setViewportView(table);
        if (-1 != prevIndex && table.getRowCount() >= prevTable.getRowCount()) {
            table.setRowSelectionInterval(prevIndex, prevIndex);
        }
        additionMovementExpenseManager.initComboBox(table);
        updateSumTransaction(table, jLabelExpenseSumLabel, jLabelExpenseSum, jLabelExpenseSelectedSumLabel, jLabelExpenseSelectedSum);
    }

    /**
     * Обновляет данные в гриде доходов
     */
    private void updateIncomeContent() {
        TransactionTable table = createNewIncomeTransactionTable();
        TransactionTable prevTable = (TransactionTable) jScrollPaneIncome.getViewport().getView();
        int prevIndex = prevTable == null ? -1 : prevTable.getSelectedRow();
        jScrollPaneIncome.setViewportView(table);
        if (-1 != prevIndex && table.getRowCount() >= prevTable.getRowCount()) {
            table.setRowSelectionInterval(prevIndex, prevIndex);
        }
        additionMovementIncomeManager.initComboBox(table);
        updateSumTransaction(table, jLabelIncomeSumLabel, jLabelIncomeSum, jLabelIncomeSelectedSumLabel, jLabelIncomeSelectedSum);
    }

    private void initMainWindow() {
        setSize(pref.getInt(KEY_MW_WIDTH, VAL_MW_WIDTH), pref.getInt(KEY_MW_HEIGHT, VAL_MW_HEIGHT));
        setLocation(pref.getInt(KEY_MW_X, VAL_MW_X), pref.getInt(KEY_MW_Y, VAL_MW_Y));

        jDialogTagsList.setSize(pref.getInt(KEY_DTL_WIDTH, VAL_DTL_WIDTH), 
                pref.getInt(KEY_DTL_HEIGHT, VAL_DTL_HEIGHT));
        jDialogTagsList.setLocation(pref.getInt(KEY_DTL_X, VAL_DTL_X), 
                pref.getInt(KEY_DTL_Y, VAL_DTL_Y));
        dialogTag = new DialogTag(jDialogTagsList);
        dialogTransaction = new DialogTransaction(this, transactionManager, tagManager);
        dialogAbout = new DialogAbout(this);

        additionMovementIncomeManager = new AdditionMovementManager(tagManager, transactionManager, 
                jComboBoxIncomeAdditionMovement);
        additionMovementExpenseManager = new AdditionMovementManager(tagManager, transactionManager, 
                jComboBoxExpenseAdditionMovement);

        try {
            initComboBoxPeriods();
            initComboBoxSummaryPeriods();
            initComboBoxStatistic();
            initComboBoxAverage();
            initStatisticSliders();
            initLabelColor();
            
            initGeneralPanel();
            initIncomePanel();
            initExpensePanel();
            initStatisticPanel();
            updateBottomPanel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex, "Fatal Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            formWindowClosing(null);
            System.exit(1);
        }
        
        jMenuItemHelpContents.setVisible(false);
        jButtonToolBarGeneral.setText("");
        jButtonToolBarIncome.setText("");
        jButtonToolBarExpense.setText("");
        jButtonToolBarStatistic.setText("");
    }
    
    private void initLabelColor() {
        jTextFieldBalance.setForeground(colorBalance);
        jTextFieldIncomeTotal.setForeground(colorIncome);
        jTextFieldExpenseTotal.setForeground(colorExpense);
        jLabel5.setForeground(colorIncome);
        jLabel4.setForeground(colorExpense);
    }
    
    private void initComboBoxAverage() {
        int id = pref.getInt(KEY_INDEX_AVERAGE_INCOME, VAL_INDEX_AVERAGE_INCOME);
        jComboBoxMonthAverageIncome.setSelectedIndex(-1 != id ? id : 0);
        id = pref.getInt(KEY_INDEX_AVERAGE_EXPENSE, VAL_INDEX_AVERAGE_EXPENSE);
        jComboBoxMonthAverageExpense.setSelectedIndex(-1 != id ? id : 0);
        id = pref.getInt(KEY_INDEX_AVERAGE_BALANCE, VAL_INDEX_AVERAGE_BALANCE);
        jComboBoxMonthAverageBalance.setSelectedIndex(-1 != id ? id : 0);
    }
    
    private void initComboBoxStatistic() {
        jComboBoxStatisticDiagramType.removeAllItems();
        jComboBoxStatisticDiagramType.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Type_1"));
        jComboBoxStatisticDiagramType.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Type_2"));
        
        jComboBoxStatisticTimeTick.removeAllItems();
        jComboBoxStatisticTimeTick.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Day"));
        jComboBoxStatisticTimeTick.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Week"));
        jComboBoxStatisticTimeTick.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Month"));
        jComboBoxStatisticTimeTick.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Year"));
        
        jComboBoxStatisticDiagramType.setSelectedIndex(indexGraphType > 0 ? indexGraphType : 0);
        jComboBoxStatisticTimeTick.setSelectedIndex(indexGraphTickTime > 0 ? indexGraphTickTime : 0);
    }
    
    private void initComboBoxSummaryPeriods() {
        jComboBoxSumPeriod.removeAllItems();
        jComboBoxSumPeriod.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Select_Period..."));
        ComboBoxItemTime tmpTime;
        Date today = new Date();
        Calendar calen = Calendar.getInstance();
        
        calen.add(Calendar.MONTH, -1);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Summary_Last_Month"), 
                calen.getTime(), today);
        jComboBoxSumPeriod.addItem(tmpTime);
        
        calen.setTime(today);
        calen.add(Calendar.MONTH, -2);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Summary_Last_2_Months"), 
                calen.getTime(), today);
        jComboBoxSumPeriod.addItem(tmpTime);
        
        calen.setTime(today);
        calen.add(Calendar.MONTH, -4);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Summary_Last_4_Months"), 
                calen.getTime(), today);
        jComboBoxSumPeriod.addItem(tmpTime);
        
        calen.setTime(today);
        calen.add(Calendar.MONTH, -6);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Summary_Last_6_Months"), 
                calen.getTime(), today);
        jComboBoxSumPeriod.addItem(tmpTime);
        
        calen.setTime(today);
        calen.add(Calendar.MONTH, -12);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Summary_Last_12_Months"), 
                calen.getTime(), today);
        jComboBoxSumPeriod.addItem(tmpTime);        
        
    }
    
    private void initComboBoxPeriods() {
        jComboBoxIncomePeriod.removeAllItems();
        jComboBoxExpensePeriod.removeAllItems();
        jComboBoxIncomePeriod.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Select_Period..."));
        jComboBoxExpensePeriod.addItem(mybudget.option.Options.I18N_BUNDLE.getString("Select_Period..."));
        Date dateToday = new Date();
        ComboBoxItemTime tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Today"), 
                dateToday, dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Last_7_Days"), 
                new Date(dateToday.getTime() - Options.MLS_IN_DAY * 7), dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Last_15_Days"), 
                new Date(dateToday.getTime() - Options.MLS_IN_DAY * 15), dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Last_30_Days"), 
                new Date(dateToday.getTime() - Options.MLS_IN_DAY * 30), dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Last_60_Days"), 
                new Date(dateToday.getTime() - Options.MLS_IN_DAY * 60), dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Last_90_Days"), 
                new Date(dateToday.getTime() - Options.MLS_IN_DAY * 90), dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Last_150_Days"), 
                new Date(dateToday.getTime() - Options.MLS_IN_DAY * 150), dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("Last_300_Days"), 
                new Date(dateToday.getTime() - Options.MLS_IN_DAY * 300), dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        tmpTime = new ComboBoxItemTime("   " + mybudget.option.Options.I18N_BUNDLE.getString("All_Time"), null, dateToday);
        jComboBoxIncomePeriod.addItem(tmpTime);
        jComboBoxExpensePeriod.addItem(tmpTime);
        
        ComboBoxItemTime saveLast = tmpTime;

        tmpTime = (ComboBoxItemTime) jComboBoxIncomePeriod.getItemAt(lastIndexIncomePeriod);
        if (null == tmpTime)
            tmpTime = saveLast;
        incomeStartDate = tmpTime.getStartDate();
        incomeFinishDate = tmpTime.getFinishDate();
        tmpTime = (ComboBoxItemTime) jComboBoxExpensePeriod.getItemAt(lastIndexExpensePeriod);
        if (null == tmpTime)
            tmpTime = saveLast;
        expenseStartDate = tmpTime.getStartDate();
        expenseFinishDate = tmpTime.getFinishDate();
    }

    private void initStatisticSliders() {
        int tmpVal = -1;
        // general
        jSliderTime.setMinimum(1);
        jSliderTime.setMaximum(24);
        jSliderTime.setMajorTickSpacing(1);
        tmpVal = pref.getInt(KEY_GENERAL_SLIDER_TIME, VAL_GENERAL_SLIDER_TIME);
        if (jSliderTime.getMinimum() > tmpVal || jSliderTime.getMaximum() < tmpVal) {
            jSliderTime.setValue(jSliderTime.getMaximum());
        } else {
            jSliderTime.setValue(tmpVal);
        }
        jLabelSliderTime.setText("" + (jSliderTime.getMaximum() - jSliderTime.getValue() + 1));
        //
        jSliderStatisticDay.setMinimum(1);
        jSliderStatisticDay.setMaximum(15);
        jSliderStatisticDay.setMajorTickSpacing(1);
        tmpVal = pref.getInt(KEY_DAY_SLIDER_TIME, VAL_DAY_SLIDER_TIME);
        if (jSliderStatisticDay.getMinimum() > tmpVal || jSliderStatisticDay.getMaximum() < tmpVal) {
            jSliderStatisticDay.setValue(jSliderStatisticDay.getMaximum());
        } else {
            jSliderStatisticDay.setValue(tmpVal);
        }
        jLabelStatisticDay.setText("" + ((jSliderStatisticDay.getMaximum() - 
            jSliderStatisticDay.getValue() + 1) * 10));
    
        jSliderStatisticWeek.setMinimum(1);
        jSliderStatisticWeek.setMaximum(50);
        jSliderStatisticWeek.setMajorTickSpacing(2);
        tmpVal = pref.getInt(KEY_WEEK_SLIDER_TIME, VAL_WEEK_SLIDER_TIME);
        if (jSliderStatisticWeek.getMinimum() > tmpVal || jSliderStatisticWeek.getMaximum() < tmpVal) {
            jSliderStatisticWeek.setValue(jSliderStatisticWeek.getMaximum());
        } else {
            jSliderStatisticWeek.setValue(tmpVal);
        }
        jLabelStatisticWeek.setText("" + (jSliderStatisticWeek.getMaximum() - 
                jSliderStatisticWeek.getValue() + 1));
        
        jSliderStatisticMonth.setMinimum(1);
        jSliderStatisticMonth.setMaximum(24);
        jSliderStatisticMonth.setMajorTickSpacing(1);
        tmpVal = pref.getInt(KEY_MONTH_SLIDER_TIME, VAL_MONTH_SLIDER_TIME);
        if (jSliderStatisticMonth.getMinimum() > tmpVal || jSliderStatisticMonth.getMaximum() < tmpVal) {
            jSliderStatisticMonth.setValue(jSliderStatisticMonth.getMaximum());
        } else {
            jSliderStatisticMonth.setValue(tmpVal);
        }
        jLabelStatisticMonth.setText("" + (jSliderStatisticMonth.getMaximum() - 
               jSliderStatisticMonth.getValue() + 1));
        
        jSliderStatisticYear.setMinimum(1);
        jSliderStatisticYear.setMaximum(10);
        jSliderStatisticYear.setMajorTickSpacing(1);
        tmpVal = pref.getInt(KEY_YEAR_SLIDER_TIME, VAL_YEAR_SLIDER_TIME);
        if (jSliderStatisticYear.getMinimum() > tmpVal || jSliderStatisticYear.getMaximum() < tmpVal) {
            jSliderStatisticYear.setValue(jSliderStatisticYear.getMaximum());
        } else {
            jSliderStatisticYear.setValue(tmpVal);
        }
        jLabelStatisticYear.setText("" + (jSliderStatisticYear.getMaximum() - 
                jSliderStatisticYear.getValue() + 1));
        
        jSliderBalance.setMinimum(0);
        jSliderBalance.setMaximum(balanceSliderNumMonth.length - 1);
        jSliderBalance.setMajorTickSpacing(1);
        tmpVal = pref.getInt(KEY_BALANCE_SLIDER_TIME, VAL_BALANCE_SLIDER_TIME);
        if (jSliderBalance.getMinimum() > tmpVal || jSliderBalance.getMaximum() < tmpVal) {
            jSliderBalance.setValue(jSliderBalance.getMaximum());
        } else {
            jSliderBalance.setValue(tmpVal);
        }
        changeTextBalanceSlieder();
    }
    
    private TagTable createNewTagTable() {
        TagTable table = new TagTable(tagManager.getListTagCache());        
        boolean flagNoRow = -1 == table.getSelectedRow();
        if (!flagNoRow) {
            table.addMouseListener(new DoubleClickTableListener(jButtonTagListEdit));
        }
        jButtonTagListEdit.setEnabled(!flagNoRow);
        jButtonTagListDelete.setEnabled(!flagNoRow);
        return table;
    }

    private TransactionTable createNewIncomeTransactionTable() {
        List<Transaction> list = getTransactionList(incomeStartDate,
                incomeFinishDate, Transaction.TYPE_INCOME, tagsIncomePanel.isAndSelected(),
                tagsIncomePanel.getSpecificTags());

        TransactionTable table = new TransactionTable(list);
        boolean flagNoRow = -1 == table.getSelectedRow();
        if (!flagNoRow) {
            // 1
            table.addMouseListener(new CalcSumClickTableListener(table, jLabelIncomeSumLabel, 
                    jLabelIncomeSum, jLabelIncomeSelectedSumLabel, jLabelIncomeSelectedSum));
            // 2
            table.addMouseListener(new DoubleClickTableListener(jButtonIncomeEdit));
        }
        jButtonIncomeEdit.setEnabled(!flagNoRow);
        jButtonIncomeDelete.setEnabled(!flagNoRow);
        return table;
    }

    private List<Transaction> getTransactionList(Date start, Date finish,
            int type, boolean isAnd, List<Tag> tags) {
        return isAnd
                ? transactionManager.getListTransactionWithSpecificTags(start, finish, type, tags)
                : transactionManager.getListTransactionWithOneOfTags(start, finish, type, tags);
    }

    private TransactionTable createNewExpenseTransactionTable() {
        List<Transaction> list = getTransactionList(expenseStartDate, expenseFinishDate,
                Transaction.TYPE_EXPENSE, tagsExpensePanel.isAndSelected(),
                tagsExpensePanel.getSpecificTags());

        TransactionTable table = new TransactionTable(list);
        boolean flagNoRow = -1 == table.getSelectedRow();
        if (!flagNoRow) {
            // 1
            table.addMouseListener(new CalcSumClickTableListener(table, jLabelExpenseSumLabel, 
                    jLabelExpenseSum, jLabelExpenseSelectedSumLabel, jLabelExpenseSelectedSum));
            // 2
            table.addMouseListener(new DoubleClickTableListener(jButtonExpenseEdit));
        }
        jButtonExpenseEdit.setEnabled(!flagNoRow);
        jButtonExpenseDelete.setEnabled(!flagNoRow);
        return table;
    }
    
    private void updateBottomPanel() {
        Double incomeValue = transactionManager.getTotalIncomeValue();
        Double expenseValue = transactionManager.getTotalExpenseValue();
        jTextFieldBalance.setText(Options.toCurrency(incomeValue - expenseValue));
        jTextFieldIncomeTotal.setText(Options.toCurrency(incomeValue));
        jTextFieldExpenseTotal.setText(Options.toCurrency(expenseValue));
//        Double incomeValue = transactionManager.getTotalIncomeValue();
//        Double expenseValue = transactionManager.getTotalExpenseValue();
//        jTextFieldBalance.setText(Options.toCurrency(Math.rint(incomeValue - expenseValue)));
//        jTextFieldIncomeTotal.setText(Options.toCurrency(Math.rint(incomeValue)));
//        jTextFieldExpenseTotal.setText(Options.toCurrency(Math.rint(expenseValue)));
//        int incomeValue = transactionManager.getTotalIncomeValue().intValue();
//        int expenseValue = transactionManager.getTotalExpenseValue().intValue();
//        jTextFieldBalance.setText(Integer.toString(incomeValue - expenseValue));
//        jTextFieldIncomeTotal.setText(Integer.toString(incomeValue));
//        jTextFieldExpenseTotal.setText(Integer.toString(expenseValue));
    }

    private void initGeneralPanel() {
        updateGraphGeneral();
        jLabelSliderTime.setText("" + (jSliderTime.getMaximum() - jSliderTime.getValue() + 1));
        jPanelGeneral2.setPreferredSize(new Dimension(500, 100));
        Date today = new Date();
        Options.CALENDAR.setTime(today);
        Options.CALENDAR.set(GregorianCalendar.DATE, 1);
        Date startDate = Options.CALENDAR.getTime();
        double currentIncome = transactionManager.getValueTransaction(startDate, today, Transaction.TYPE_INCOME);
        double currentExpense = transactionManager.getValueTransaction(startDate,  today, Transaction.TYPE_EXPENSE);
        jTextFieldCurrentMonthIncome.setText(Options.toCurrency(currentIncome));
        jTextFieldCurrentMonthExpense.setText(Options.toCurrency(currentExpense));
        jTextFieldCurrentMonthBalance.setText(Options.toCurrency(currentIncome - currentExpense));
        
        int currentMonth = Options.CALENDAR.get(GregorianCalendar.MONTH);
        int prevMonth = currentMonth == 0 ? GregorianCalendar.DECEMBER : currentMonth - 1;
        Options.CALENDAR.set(GregorianCalendar.DATE, 1);
        Options.CALENDAR.set(GregorianCalendar.MONTH, prevMonth);
        if (0 == currentMonth) {
            Options.CALENDAR.set(GregorianCalendar.YEAR, Options.CALENDAR.get(GregorianCalendar.YEAR) - 1);
        }
        Date firstDatePrevMonth = Options.CALENDAR.getTime();
        Options.CALENDAR.set(GregorianCalendar.DATE, Options.CALENDAR.getActualMaximum(GregorianCalendar.DATE));
        Date lastDatePrevMonth = Options.CALENDAR.getTime();
        double lastIncome = transactionManager.getValueTransaction(
                firstDatePrevMonth, lastDatePrevMonth, Transaction.TYPE_INCOME);
        jTextFieldLastMonthIncome.setText(Options.toCurrency(lastIncome));
        double lastExpense = transactionManager.getValueTransaction(
                firstDatePrevMonth, lastDatePrevMonth, Transaction.TYPE_EXPENSE);
        jTextFieldLastMonthExpense.setText(Options.toCurrency(lastExpense));
        jTextFieldLastMonthBalance.setText(Options.toCurrency(lastIncome - lastExpense));        
    }

    private void updateGUIAfterUpdateBase(boolean withTagPanel) {
        if (jPanelGeneral.isShowing()) {
            jButtonToolBarGeneralActionPerformed(null);
        } else if (jPanelIncome.isShowing()) {
            if (withTagPanel) {
                tagsIncomePanel.updateContent();
            }
            updateIncomeContent();
        } else if (jPanelExpense.isShowing()) {
            if (withTagPanel) {
                tagsExpensePanel.updateContent();
            }
            updateExpenseContent();
        } else if (jPanelStatistic.isShowing()) {
            jButtonToolBarStatisticActionPerformed(null);
        }
    }

    private void updateGraphGeneral() {
        if (null == graphMakerGeneral) {
            graphMakerGeneral = new GraphMakerMonthStatistic(transactionManager);
            graphMakerGeneral.setColorIncome(colorIncome);
            graphMakerGeneral.setColorExpense(colorExpense);
            jPanelGeneral2.add(graphMakerGeneral, java.awt.BorderLayout.CENTER);
        }
        graphMakerGeneral.setGraphType(0 == indexGraphType ? GraphMakerStatistic.GRAPH_TYPE_1 :
            GraphMakerStatistic.GRAPH_TYPE_2);
        int indexMonth = jSliderTime.getMaximum() - jSliderTime.getValue();
        Date today = new Date();
        Options.CALENDAR.setTime(today);
        Options.CALENDAR.set(Calendar.DATE, Options.CALENDAR.getActualMaximum(Calendar.DATE));
        Date finishDate = Options.CALENDAR.getTime();
        Options.CALENDAR.set(Calendar.DATE, 1);

        if (12 < indexMonth) {
            int subYear = indexMonth % 12;
            Options.CALENDAR.set(Calendar.YEAR, Options.CALENDAR.get(Calendar.YEAR) - subYear);
            int subMonth = indexMonth - subYear * 12;
            Options.CALENDAR.set(Calendar.MONTH, Options.CALENDAR.get(Calendar.MONTH) - subMonth);
        } else {
            Options.CALENDAR.set(Calendar.MONTH, Options.CALENDAR.get(Calendar.MONTH) - indexMonth);
        }
        Date startDate = Options.CALENDAR.getTime();
        graphMakerGeneral.setDateInterval(startDate, finishDate);
        graphMakerGeneral.updateData();
        graphMakerGeneral.updateUI();
    }

    public void updateIncomeTableWithSpecificTags(List<Tag> tags) {
        jButtonToolBarIncomeActionPerformed(null);
    }

    public void updateExpenseTableWithSpecificTags(List<Tag> tags) {
        jButtonToolBarExpenseActionPerformed(null);
    }
    
    private Double getAverageValue(int numMonth, int type) {
        Double sum = 0.;
        Options.CALENDAR.setTime(new Date());
        int currentMonth = Options.CALENDAR.get(GregorianCalendar.MONTH);
        int prevMonth = 0;
        
        for (int i = 0; i < numMonth; i++) {
            Options.CALENDAR.set(GregorianCalendar.DATE, 1);
            prevMonth = currentMonth == 0 ? GregorianCalendar.DECEMBER : currentMonth - 1;
            if (0 == currentMonth) {
                Options.CALENDAR.set(GregorianCalendar.YEAR, Options.CALENDAR.get(GregorianCalendar.YEAR) - 1);
            }
            Options.CALENDAR.set(GregorianCalendar.MONTH, prevMonth);
            Date firstDatePrevMonth = Options.CALENDAR.getTime();
            Options.CALENDAR.set(GregorianCalendar.DATE, Options.CALENDAR.getActualMaximum(GregorianCalendar.DATE));
            Date lastDatePrevMonth = Options.CALENDAR.getTime();
            sum += transactionManager.getValueTransaction(firstDatePrevMonth, lastDatePrevMonth, type);
            currentMonth = prevMonth;
        }
        
        return sum / numMonth;
    }
    
    private static Double getCurrentSumTransaction(TransactionTable table) {
        Double sum = 0.;
        for (int i = 0; i < table.getRowCount(); i++) {
            sum += (Double) table.getValueAt(i, 2);
        }
        return sum;
    }
    
    private static Double getCurrentSelectedSumTransaction(TransactionTable table) {
        Double sum = 0.;
        int [] indexes = table.getSelectedRows();
        for (int index : indexes) {
            sum += (Double) table.getValueAt(index, 2);
        }
        return sum;
    }
    
    private static void updateSumTransaction(TransactionTable table, JLabel sumLabel, JLabel sum,
            JLabel selectedSumLabel, JLabel selectedSum) {
        if (0 < table.getRowCount()) {
            sumLabel.setVisible(true);
            sum.setVisible(true);
            sum.setText(Options.toCurrency(getCurrentSumTransaction(table)));
            selectedSumLabel.setVisible(true);
            selectedSum.setVisible(true);
            selectedSum.setText(Options.toCurrency(getCurrentSelectedSumTransaction(table)));
        } else {
            sumLabel.setVisible(false);
            sum.setVisible(false);
            selectedSumLabel.setVisible(false);
            selectedSum.setVisible(false);
        }
        sumLabel.updateUI();
        sum.updateUI();
    }

    private void updateIncomeExpenseDayStatisticGraph() {
        if (jPanelSGraphDay.isShowing()) {
            if (null == graphMakerDayStatistic) {
                graphMakerDayStatistic = new GraphMakerDayStatistic(transactionManager);
                graphMakerDayStatistic.setColorIncome(colorIncome);
                graphMakerDayStatistic.setColorExpense(colorExpense);

                graphMakerDayStatistic.setTickDay(1);
                jPanelSGraphDay.add(graphMakerDayStatistic, java.awt.BorderLayout.CENTER);
            }
            graphMakerDayStatistic.setGraphType(0 == indexGraphType ? GraphMakerStatistic.GRAPH_TYPE_1 :
                GraphMakerStatistic.GRAPH_TYPE_2);
            graphMakerDayStatistic.setListSpecificTags(tagsGraphPanel.getSpecificTags());
            graphMakerDayStatistic.setFlagSpecificTagAnd(tagsGraphPanel.isAndSelected());
            Date finishDate = new Date();
            Date startDate = new Date(finishDate.getTime() - (jSliderStatisticDay.getMaximum() - 
                    jSliderStatisticDay.getValue() + 1) * AbstractGraphMaker.MLS_IN_DAY * 10);
            graphMakerDayStatistic.setDateInterval(startDate, finishDate);
            graphMakerDayStatistic.updateData();
            graphMakerDayStatistic.updateUI();
        }
    }
    
    private void updateIncomeExpenseWeekStatisticGraph() {
        if (jPanelSGraphWeek.isShowing()) {
            if (null == graphMakerWeekStatistic) {
                graphMakerWeekStatistic = new GraphMakerDayStatistic(transactionManager);
                graphMakerWeekStatistic.setColorIncome(colorIncome);
                graphMakerWeekStatistic.setColorExpense(colorExpense);
                graphMakerWeekStatistic.setTickDay(7);
                jPanelSGraphWeek.add(graphMakerWeekStatistic, java.awt.BorderLayout.CENTER);
            }
            graphMakerWeekStatistic.setGraphType(0 == indexGraphType ? GraphMakerStatistic.GRAPH_TYPE_1 :
                GraphMakerStatistic.GRAPH_TYPE_2);
            graphMakerWeekStatistic.setListSpecificTags(tagsGraphPanel.getSpecificTags());
            graphMakerWeekStatistic.setFlagSpecificTagAnd(tagsGraphPanel.isAndSelected());
            int indexWeek = jSliderStatisticWeek.getMaximum() - jSliderStatisticWeek.getValue() + 1;
            Date today = new Date();
            Options.CALENDAR.setTime(today);
            Options.CALENDAR.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SUNDAY);
            Date finishDate = Options.CALENDAR.getTime();
            Date startDate = new Date(finishDate.getTime() - (indexWeek * 
                    AbstractGraphMaker.MLS_IN_DAY * 7));
            graphMakerWeekStatistic.setDateInterval(startDate, finishDate);
            graphMakerWeekStatistic.updateData();
            graphMakerWeekStatistic.updateUI();
        }
    }
    
    private void updateIncomeExpenseMonthStatisticGraph() {
        if (jPanelSGraphMonth.isShowing()) {
            if (null == graphMakerMonthStatistic) {
                graphMakerMonthStatistic = new GraphMakerMonthStatistic(transactionManager);
                graphMakerMonthStatistic.setColorIncome(colorIncome);
                graphMakerMonthStatistic.setColorExpense(colorExpense);
                jPanelSGraphMonth.add(graphMakerMonthStatistic, java.awt.BorderLayout.CENTER);
            }
            graphMakerMonthStatistic.setGraphType(0 == indexGraphType ? GraphMakerStatistic.GRAPH_TYPE_1 :
                GraphMakerStatistic.GRAPH_TYPE_2);
            graphMakerMonthStatistic.setListSpecificTags(tagsGraphPanel.getSpecificTags());
            graphMakerMonthStatistic.setFlagSpecificTagAnd(tagsGraphPanel.isAndSelected());
            int indexMonth = jSliderStatisticMonth.getMaximum() - jSliderStatisticMonth.getValue();
            Date today = new Date();
            Options.CALENDAR.setTime(today);
            Options.CALENDAR.set(Calendar.DATE, Options.CALENDAR.getActualMaximum(Calendar.DATE));
            Date finishDate = Options.CALENDAR.getTime();
            Options.CALENDAR.set(Calendar.DATE, 1);

            if (12 < indexMonth) {
                int subYear = indexMonth % 12;
                Options.CALENDAR.set(Calendar.YEAR, Options.CALENDAR.get(Calendar.YEAR) - subYear);
                int subMonth = indexMonth - subYear * 12;
                Options.CALENDAR.set(Calendar.MONTH, Options.CALENDAR.get(Calendar.MONTH) - subMonth);
            } else {
                Options.CALENDAR.set(Calendar.MONTH, Options.CALENDAR.get(Calendar.MONTH) - indexMonth);
            }
            Date startDate = Options.CALENDAR.getTime();
            graphMakerMonthStatistic.setDateInterval(startDate, finishDate);
            graphMakerMonthStatistic.updateData();
            graphMakerMonthStatistic.updateUI();
        }
    }
    
    private void updateIncomeExpenseYearStatisticGraph() {
        if (jPanelSGraphYear.isShowing()) {
            if (null == graphMakerYearStatistic) {
                graphMakerYearStatistic = new GraphMakerYearStatistic(transactionManager);
                graphMakerYearStatistic.setColorIncome(colorIncome);
                graphMakerYearStatistic.setColorExpense(colorExpense);
                jPanelSGraphYear.add(graphMakerYearStatistic, java.awt.BorderLayout.CENTER);
            }
            graphMakerYearStatistic.setGraphType(0 == indexGraphType ? GraphMakerStatistic.GRAPH_TYPE_1 :
                GraphMakerStatistic.GRAPH_TYPE_2);
            graphMakerYearStatistic.setListSpecificTags(tagsGraphPanel.getSpecificTags());
            graphMakerYearStatistic.setFlagSpecificTagAnd(tagsGraphPanel.isAndSelected());
            int indexYear = jSliderStatisticYear.getMaximum() - jSliderStatisticYear.getValue();
            Date today = new Date();
            Options.CALENDAR.setTime(today);
            Options.CALENDAR.set(Calendar.MONTH, Calendar.DECEMBER);
            Options.CALENDAR.set(Calendar.DATE, 31);
            Date finishDate = Options.CALENDAR.getTime();
            
            Options.CALENDAR.set(Calendar.YEAR, Options.CALENDAR.get(Calendar.YEAR) - indexYear);
            Options.CALENDAR.set(Calendar.MONTH, Calendar.JANUARY);
            Options.CALENDAR.set(Calendar.DATE, 1);
            Date startDate = Options.CALENDAR.getTime();
            graphMakerYearStatistic.setDateInterval(startDate, finishDate);
            graphMakerYearStatistic.updateData();
            graphMakerYearStatistic.updateUI();
        }
    }    
    
    private void updateBalanceStatisticGraph() {
        if (!jPanelStatisticBalanceGraph.isShowing()) {
            return;
        }
        if (null == graphMakerBalance) {
            graphMakerBalance = new GraphMakerBalance(transactionManager);
            graphMakerBalance.setColorBalance(colorBalance);
            jPanelStatisticBalanceGraph.add(graphMakerBalance, java.awt.BorderLayout.CENTER);
        }
        int indexMonth = balanceSliderNumMonth[jSliderBalance.getValue()] - 1;
        Date today = new Date();
        Options.CALENDAR.setTime(today);
        Options.CALENDAR.set(Calendar.DATE, Options.CALENDAR.getActualMaximum(Calendar.DATE));
        Date finishDate = Options.CALENDAR.getTime();
        Options.CALENDAR.set(Calendar.DATE, 1);

        if (12 < indexMonth) {
            int subYear = indexMonth % 12;
            Options.CALENDAR.set(Calendar.YEAR, Options.CALENDAR.get(Calendar.YEAR) - subYear);
            int subMonth = indexMonth - subYear * 12;
            Options.CALENDAR.set(Calendar.MONTH, Options.CALENDAR.get(Calendar.MONTH) - subMonth);
        } else {
            Options.CALENDAR.set(Calendar.MONTH, Options.CALENDAR.get(Calendar.MONTH) - indexMonth);
        }
        Date startDate = Options.CALENDAR.getTime();
        graphMakerBalance.setDateInterval(startDate, finishDate);
        graphMakerBalance.updateData();
        graphMakerBalance.updateUI();
    }

    public void updateGraphForTag() {
        if (null != graphMakerDayStatistic && graphMakerDayStatistic.isShowing()) {
            graphMakerDayStatistic.setListSpecificTags(tagsGraphPanel.getSpecificTags());
            graphMakerDayStatistic.setFlagSpecificTagAnd(tagsGraphPanel.isAndSelected());
        }
        if (null != graphMakerWeekStatistic && graphMakerWeekStatistic.isShowing()) {
            graphMakerWeekStatistic.setListSpecificTags(tagsGraphPanel.getSpecificTags());
            graphMakerWeekStatistic.setFlagSpecificTagAnd(tagsGraphPanel.isAndSelected());
        }
        if (null != graphMakerMonthStatistic && graphMakerMonthStatistic.isShowing()) {
            graphMakerMonthStatistic.setListSpecificTags(tagsGraphPanel.getSpecificTags());
            graphMakerMonthStatistic.setFlagSpecificTagAnd(tagsGraphPanel.isAndSelected());
        }
        if (null != graphMakerYearStatistic && graphMakerYearStatistic.isShowing()) {
            graphMakerYearStatistic.setListSpecificTags(tagsGraphPanel.getSpecificTags());
            graphMakerYearStatistic.setFlagSpecificTagAnd(tagsGraphPanel.isAndSelected());
        }
        jComboBoxStatisticTimeTickActionPerformed(null);
    }
    
    private void changeTextBalanceSlieder() {
        jLabelSliderBalance.setText("" + balanceSliderNumMonth[jSliderBalance.getValue()]);
    }
    
    private void updateSummaryStatistics() {
        if (!jPanelSumGraph.isShowing()) {
            return;
        }
        if (null == jCCFieldFrom.getOwner()) {
            jCCFieldFrom.setOwner(this);
            jCCFieldTo.setOwner(this);
            Calendar calen = Calendar.getInstance();
            calen.add(Calendar.MONTH, -1);
            jCCFieldFrom.setDate(calen.getTime());
        }
        Date startDate;
        Date endDate;
        try {
            startDate = jCCFieldFrom.getDate();
            endDate = jCCFieldTo.getDate();
        } catch (ParseException ex) {
            startDate = new Date();
            endDate = new Date();
            jCCFieldFrom.setDate(startDate);
            jCCFieldTo.setDate(endDate);
        }
        if (null == tableSummary) {
            tableSummary = new SumTable(transactionManager, startDate, endDate);
            tableSummary.setIncomeSum(jRadioButtonIncome.isSelected());
            jScrollPaneSum.setViewportView(tableSummary);
        } else {
            tableSummary.setDateInterval(startDate, endDate);
        }
        tableSummary.updateData();
        if (null == graphMakerSummary) {
            graphMakerSummary = new GraphMakerSummary();
            jPanelSumGraph.add(graphMakerSummary, java.awt.BorderLayout.CENTER);
        }
        graphMakerSummary.setGraphData(tableSummary.getData());
        graphMakerSummary.updateUI();
    }
    
    private void savePreference() {
        if (null != dialogOptions) {
            switch (dialogOptions.getI18NComboBox().getSelectedIndex()) {
                case 0:
                    pref.put(KEY_I18N, "ru");
                    break;
                case 1:
                    pref.put(KEY_I18N, "en");
                    break;
                }
        }

        pref.putInt(KEY_GENERAL_SLIDER_TIME, jSliderTime.getValue());
        pref.putInt(KEY_DAY_SLIDER_TIME, jSliderStatisticDay.getValue());
        pref.putInt(KEY_WEEK_SLIDER_TIME, jSliderStatisticWeek.getValue());
        pref.putInt(KEY_MONTH_SLIDER_TIME, jSliderStatisticMonth.getValue());
        pref.putInt(KEY_YEAR_SLIDER_TIME, jSliderStatisticYear.getValue());
        pref.putInt(KEY_BALANCE_SLIDER_TIME, jSliderBalance.getValue());

        pref.putInt(KEY_MW_X, getX());
        pref.putInt(KEY_MW_Y, getY());
        pref.putInt(KEY_MW_WIDTH, getWidth());
        pref.putInt(KEY_MW_HEIGHT, getHeight());

        pref.putInt(KEY_DTL_X, jDialogTagsList.getX());
        pref.putInt(KEY_DTL_Y, jDialogTagsList.getY());
        pref.putInt(KEY_DTL_WIDTH, jDialogTagsList.getWidth());
        pref.putInt(KEY_DTL_HEIGHT, jDialogTagsList.getHeight());

        if (null != dialogOptions) {
            pref.putInt(KEY_DO_X, dialogOptions.getX());
            pref.putInt(KEY_DO_Y, dialogOptions.getY());
        }

        if (null != dialogTag) {
            pref.putInt(KEY_DTAG_X, dialogTag.getX());
            pref.putInt(KEY_DTAG_Y, dialogTag.getY());
            pref.putInt(KEY_DTAG_WIDTH, dialogTag.getWidth());
            pref.putInt(KEY_DTAG_HEIGHT, dialogTag.getHeight());
        }

        if (null != dialogTransaction) {
            pref.putInt(KEY_DTR_X, dialogTransaction.getX());
            pref.putInt(KEY_DTR_Y, dialogTransaction.getY());
            pref.putInt(KEY_DTR_WIDTH, dialogTransaction.getWidth());
            pref.putInt(KEY_DTR_HEIGHT, dialogTransaction.getHeight());
        }

        // save colors
        pref.putInt(KEY_INCOME_R, colorIncome.getRed());
        pref.putInt(KEY_INCOME_G, colorIncome.getGreen());
        pref.putInt(KEY_INCOME_B, colorIncome.getBlue());
        pref.putInt(KEY_EXPENSE_R, colorExpense.getRed());
        pref.putInt(KEY_EXPENSE_G, colorExpense.getGreen());
        pref.putInt(KEY_EXPENSE_B, colorExpense.getBlue());
        pref.putInt(KEY_BALANCE_R, colorBalance.getRed());
        pref.putInt(KEY_BALANCE_G, colorBalance.getGreen());
        pref.putInt(KEY_BALANCE_B, colorBalance.getBlue());

        pref.put(KEY_DATE_FORMAT, Options.DATE_FORMAT.toPattern());
        
        pref.putInt(KEY_INDEX_AVERAGE_INCOME, jComboBoxMonthAverageIncome.getSelectedIndex());
        pref.putInt(KEY_INDEX_AVERAGE_EXPENSE, jComboBoxMonthAverageExpense.getSelectedIndex());
        pref.putInt(KEY_INDEX_AVERAGE_BALANCE, jComboBoxMonthAverageBalance.getSelectedIndex());
        pref.putInt(KEY_INDEX_PERIOD_INCOME, lastIndexIncomePeriod);
        pref.putInt(KEY_INDEX_PERIOD_EXPENSE, lastIndexExpensePeriod);
        
        pref.putInt(KEY_INDEX_GRAPH_TYPE, indexGraphType);
        pref.putInt(KEY_INDEX_GRAPH_TICK_TIME, indexGraphTickTime);
        
        pref.putInt(KEY_HEADER_WIDTH_0, Options.HEADER_SIZE_TRANSACTION[0]);
        pref.putInt(KEY_HEADER_WIDTH_1, Options.HEADER_SIZE_TRANSACTION[1]);
        pref.putInt(KEY_HEADER_WIDTH_2, Options.HEADER_SIZE_TRANSACTION[2]);
        pref.putInt(KEY_HEADER_WIDTH_3, Options.HEADER_SIZE_TRANSACTION[3]);
        pref.putInt(KEY_HEADER_WIDTH_4, Options.HEADER_SIZE_TRANSACTION[4]);

        pref.putInt(KEY_TAG_DEVISION_INCOME,
                Math.min(jSplitPane1.getDividerLocation(),
                Math.min(jSplitPaneExpense.getDividerLocation(),
                         jSplitPaneIncome.getDividerLocation())));
    }

    /**
     * Инициализация панели с доходами
     */
    private void initIncomePanel() {
        jSplitPaneIncome.setDividerLocation(pref.getInt(KEY_TAG_DEVISION_INCOME,
                VAL_TAG_DEVISION_INCOME));
        jSplitPaneIncome.setLeftComponent(tagsIncomePanel);
    }

    /**
     * Инициализация панели с расходами
     */
    private void initExpensePanel() {
        jSplitPaneExpense.setDividerLocation(pref.getInt(KEY_TAG_DEVISION_INCOME,
                VAL_TAG_DEVISION_INCOME));
        jSplitPaneExpense.setLeftComponent(tagsExpensePanel);
    }

    private void initStatisticPanel() {
        jSplitPane1.setDividerLocation(pref.getInt(KEY_TAG_DEVISION_INCOME,
                VAL_TAG_DEVISION_INCOME));
        jSplitPane1.setLeftComponent(tagsGraphPanel);
    }
    
    class DoubleClickTableListener extends MouseAdapter {
        private JButton button;

        public DoubleClickTableListener(JButton button) {
            this.button = button;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (2 == e.getClickCount()) {
                button.getActionListeners()[0].actionPerformed(null);
            }
        }
                
    }
    
    class CalcSumClickTableListener extends MouseAdapter {
        private JLabel sumLabel;
        private JLabel sum;
        private JLabel selectedSumLabel;
        private JLabel selectedSum;
        private TransactionTable table;

        public CalcSumClickTableListener(TransactionTable table,
                JLabel sumLabel, JLabel sum, JLabel selectedSumLabel, JLabel selectedSum) {
            this.table = table;
            this.sumLabel = sumLabel;
            this.sum = sum;
            this.selectedSumLabel = selectedSumLabel;
            this.selectedSum = selectedSum;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            MainWindow.updateSumTransaction(table, sumLabel, sum, 
                    selectedSumLabel, selectedSum);
            super.mouseClicked(e);
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            mouseClicked(e);
            super.mouseReleased(e);
        }

    }

    // <editor-fold defaultstate="collapsed" desc="public void serverStart()">
    public void serverStart() {
        try {
            server = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
            server.start(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="public void serverStop()">

    public void serverStop() {
        try {
            if (null == server) {
                return;
            }
            server.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }// </editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jDialogTagsList = new javax.swing.JDialog();
        jPanelTagsListTop = new javax.swing.JPanel();
        jButtonTagListNew = new javax.swing.JButton();
        jButtonTagListEdit = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jButtonTagListDelete = new javax.swing.JButton();
        jPanelTagsListCenter = new javax.swing.JPanel();
        jScrollPaneTagsList = new javax.swing.JScrollPane();
        buttonGroupStatistic = new javax.swing.ButtonGroup();
        buttonGroupSummary = new javax.swing.ButtonGroup();
        buttonGroupIncome = new javax.swing.ButtonGroup();
        jExportFileChooser = new javax.swing.JFileChooser();
        jToolBar = new javax.swing.JToolBar();
        jButtonToolBarGeneral = new javax.swing.JButton();
        jButtonToolBarIncome = new javax.swing.JButton();
        jButtonToolBarExpense = new javax.swing.JButton();
        jButtonToolBarStatistic = new javax.swing.JButton();
        jPanelMainCenter = new javax.swing.JPanel();
        jPanelGeneral = new javax.swing.JPanel();
        jPanelGeneral1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jButtonNewExpressIncome = new javax.swing.JButton();
        jButtonNewExpressExpense = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldCurrentMonthIncome = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldLastMonthIncome = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldMonthAverageIncome = new javax.swing.JTextField();
        jComboBoxMonthAverageIncome = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldCurrentMonthExpense = new javax.swing.JTextField();
        jTextFieldLastMonthExpense = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jComboBoxMonthAverageExpense = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldMonthAverageExpense = new javax.swing.JTextField();
        jPanel18 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jTextFieldCurrentMonthBalance = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jTextFieldLastMonthBalance = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jComboBoxMonthAverageBalance = new javax.swing.JComboBox();
        jLabel23 = new javax.swing.JLabel();
        jTextFieldMonthAverageBalance = new javax.swing.JTextField();
        jPanelGeneral2 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSliderTime = new javax.swing.JSlider();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelSliderTime = new javax.swing.JLabel();
        jLabelSliderTime1 = new javax.swing.JLabel();
        jPanelMainBottom = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldBalance = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldIncomeTotal = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldExpenseTotal = new javax.swing.JTextField();
        jPanelIncome = new javax.swing.JPanel();
        jPanelIncomeTop = new javax.swing.JPanel();
        jButtonIncomeNew = new javax.swing.JButton();
        jButtonIncomeEdit = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jButtonIncomeDelete = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jComboBoxIncomeAdditionMovement = new javax.swing.JComboBox();
        jSeparator6 = new javax.swing.JSeparator();
        jComboBoxIncomePeriod = new javax.swing.JComboBox();
        jPanelIncomeCenter = new javax.swing.JPanel();
        jSplitPaneIncome = new javax.swing.JSplitPane();
        jScrollPaneIncome = new javax.swing.JScrollPane();
        jPanelIncomeBottom = new javax.swing.JPanel();
        jLabelIncomeSumLabel = new javax.swing.JLabel();
        jLabelIncomeSum = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabelIncomeSelectedSumLabel = new javax.swing.JLabel();
        jLabelIncomeSelectedSum = new javax.swing.JLabel();
        jPanelExpense = new javax.swing.JPanel();
        jPanelExpenseTop = new javax.swing.JPanel();
        jButtonExpenseNew = new javax.swing.JButton();
        jButtonExpenseEdit = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jButtonExpenseDelete = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        jComboBoxExpenseAdditionMovement = new javax.swing.JComboBox();
        jSeparator7 = new javax.swing.JSeparator();
        jComboBoxExpensePeriod = new javax.swing.JComboBox();
        jPanelExpenseCenter = new javax.swing.JPanel();
        jSplitPaneExpense = new javax.swing.JSplitPane();
        jScrollPaneExpense = new javax.swing.JScrollPane();
        jPanelExpenseBottom = new javax.swing.JPanel();
        jLabelExpenseSumLabel = new javax.swing.JLabel();
        jLabelExpenseSum = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabelExpenseSelectedSumLabel = new javax.swing.JLabel();
        jLabelExpenseSelectedSum = new javax.swing.JLabel();
        jPanelStatistic = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jTabbedPaneStatistic = new javax.swing.JTabbedPane();
        jPanelStatisticIncomeExpense = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel13 = new javax.swing.JPanel();
        jPanelStatisticGraph = new javax.swing.JPanel();
        jPanelSGraphDay = new javax.swing.JPanel();
        jPanelSGraphWeek = new javax.swing.JPanel();
        jPanelSGraphMonth = new javax.swing.JPanel();
        jPanelSGraphYear = new javax.swing.JPanel();
        jPanelStatisticGraphParam = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jComboBoxStatisticDiagramType = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        jComboBoxStatisticTimeTick = new javax.swing.JComboBox();
        jPanelStatisticSliders = new javax.swing.JPanel();
        jPanelStatisticSliderDay = new javax.swing.JPanel();
        jSliderStatisticDay = new javax.swing.JSlider();
        jLabelStatisticDay = new javax.swing.JLabel();
        jPanelStatisticSliderWeek = new javax.swing.JPanel();
        jSliderStatisticWeek = new javax.swing.JSlider();
        jLabelStatisticWeek = new javax.swing.JLabel();
        jPanelStatisticSliderMonth = new javax.swing.JPanel();
        jSliderStatisticMonth = new javax.swing.JSlider();
        jLabelStatisticMonth = new javax.swing.JLabel();
        jPanelStatisticSliderYear = new javax.swing.JPanel();
        jSliderStatisticYear = new javax.swing.JSlider();
        jLabelStatisticYear = new javax.swing.JLabel();
        jPanelStatisticBalance = new javax.swing.JPanel();
        jPanelStatisticBalanceGraph = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jSliderBalance = new javax.swing.JSlider();
        jPanel17 = new javax.swing.JPanel();
        jLabelSliderBalance = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanelStatisticSummary = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel19 = new javax.swing.JPanel();
        jPanelDateInterval = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jCCFieldFrom = new com.artcodesys.jcc.JCCField();
        jLabel25 = new javax.swing.JLabel();
        jCCFieldTo = new com.artcodesys.jcc.JCCField();
        jComboBoxSumPeriod = new javax.swing.JComboBox();
        jPanel22 = new javax.swing.JPanel();
        jScrollPaneSum = new javax.swing.JScrollPane();
        jPanel23 = new javax.swing.JPanel();
        jRadioButtonIncome = new javax.swing.JRadioButton();
        jRadioButtonExpense = new javax.swing.JRadioButton();
        jPanel20 = new javax.swing.JPanel();
        jPanelSumGraph = new javax.swing.JPanel();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuData = new javax.swing.JMenu();
        jMenuItemNewIncome = new javax.swing.JMenuItem();
        jMenuItemNewExpense = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        jMenuItemTagsList = new javax.swing.JMenuItem();
        jMenuNavigate = new javax.swing.JMenu();
        jMenuItemGeneral = new javax.swing.JMenuItem();
        jMenuItemIncome = new javax.swing.JMenuItem();
        jMenuItemExpense = new javax.swing.JMenuItem();
        jMenuItemStatistic = new javax.swing.JMenuItem();
        jMenuTools = new javax.swing.JMenu();
        jMenuItemOptions = new javax.swing.JMenuItem();
        jMenuItemExport = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelpContents = new javax.swing.JMenuItem();
        jMenuItemAbout = new javax.swing.JMenuItem();

        jDialogTagsList.setTitle(i18n.getString("Tags_List")); // NOI18N
        jDialogTagsList.setIconImage((new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/logo.png"))).getImage());
        jDialogTagsList.setModal(true);

        jButtonTagListNew.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonTagListNew.setText(i18n.getString("New")); // NOI18N
        jButtonTagListNew.setPreferredSize(new java.awt.Dimension(90, 23));
        jButtonTagListNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTagListNewActionPerformed(evt);
            }
        });
        jPanelTagsListTop.add(jButtonTagListNew);

        jButtonTagListEdit.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonTagListEdit.setText(i18n.getString("Edit")); // NOI18N
        jButtonTagListEdit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonTagListEdit.setPreferredSize(new java.awt.Dimension(90, 23));
        jButtonTagListEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTagListEditActionPerformed(evt);
            }
        });
        jPanelTagsListTop.add(jButtonTagListEdit);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(2, 23));
        jPanelTagsListTop.add(jSeparator1);

        jButtonTagListDelete.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonTagListDelete.setText(i18n.getString("Delete")); // NOI18N
        jButtonTagListDelete.setPreferredSize(new java.awt.Dimension(90, 23));
        jButtonTagListDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTagListDeleteActionPerformed(evt);
            }
        });
        jPanelTagsListTop.add(jButtonTagListDelete);

        jDialogTagsList.getContentPane().add(jPanelTagsListTop, java.awt.BorderLayout.PAGE_START);

        jPanelTagsListCenter.setLayout(new java.awt.BorderLayout());
        jPanelTagsListCenter.add(jScrollPaneTagsList, java.awt.BorderLayout.CENTER);

        jDialogTagsList.getContentPane().add(jPanelTagsListCenter, java.awt.BorderLayout.CENTER);

        jExportFileChooser.setDialogTitle("Export");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("myBudget v.0.6");
        setIconImage((new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/logo.png"))).getImage());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jToolBar.setRollover(true);

        jButtonToolBarGeneral.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/general.png"))); // NOI18N
        jButtonToolBarGeneral.setText(i18n.getString("General")); // NOI18N
        jButtonToolBarGeneral.setToolTipText(mybudget.option.Options.I18N_BUNDLE.getString("General")); // NOI18N
        jButtonToolBarGeneral.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 2, 1, 2));
        jButtonToolBarGeneral.setBorderPainted(false);
        jButtonToolBarGeneral.setFocusable(false);
        jButtonToolBarGeneral.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonToolBarGeneral.setPreferredSize(new java.awt.Dimension(40, 51));
        jButtonToolBarGeneral.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonToolBarGeneral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonToolBarGeneralActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonToolBarGeneral);

        jButtonToolBarIncome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/income.png"))); // NOI18N
        jButtonToolBarIncome.setText(i18n.getString("Income")); // NOI18N
        jButtonToolBarIncome.setToolTipText(mybudget.option.Options.I18N_BUNDLE.getString("Income")); // NOI18N
        jButtonToolBarIncome.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 2, 1, 2));
        jButtonToolBarIncome.setBorderPainted(false);
        jButtonToolBarIncome.setFocusable(false);
        jButtonToolBarIncome.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonToolBarIncome.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonToolBarIncome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonToolBarIncomeActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonToolBarIncome);

        jButtonToolBarExpense.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/expense.png"))); // NOI18N
        jButtonToolBarExpense.setText(i18n.getString("Expense")); // NOI18N
        jButtonToolBarExpense.setToolTipText(mybudget.option.Options.I18N_BUNDLE.getString("Expense")); // NOI18N
        jButtonToolBarExpense.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 2, 1, 2));
        jButtonToolBarExpense.setBorderPainted(false);
        jButtonToolBarExpense.setFocusable(false);
        jButtonToolBarExpense.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonToolBarExpense.setMargin(new java.awt.Insets(2, 104, 2, 14));
        jButtonToolBarExpense.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonToolBarExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonToolBarExpenseActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonToolBarExpense);

        jButtonToolBarStatistic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/statistic.png"))); // NOI18N
        jButtonToolBarStatistic.setText(i18n.getString("Statistic")); // NOI18N
        jButtonToolBarStatistic.setToolTipText(mybudget.option.Options.I18N_BUNDLE.getString("Statistic")); // NOI18N
        jButtonToolBarStatistic.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 2, 1, 2));
        jButtonToolBarStatistic.setFocusable(false);
        jButtonToolBarStatistic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonToolBarStatistic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonToolBarStatistic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonToolBarStatisticActionPerformed(evt);
            }
        });
        jToolBar.add(jButtonToolBarStatistic);

        getContentPane().add(jToolBar, java.awt.BorderLayout.PAGE_START);

        jPanelMainCenter.setLayout(new java.awt.CardLayout());

        jPanelGeneral.setLayout(new java.awt.BorderLayout());

        jPanelGeneral1.setLayout(new javax.swing.BoxLayout(jPanelGeneral1, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jButtonNewExpressIncome.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonNewExpressIncome.setText(i18n.getString("New_Income")); // NOI18N
        jButtonNewExpressIncome.setMaximumSize(new java.awt.Dimension(130, 23));
        jButtonNewExpressIncome.setMinimumSize(new java.awt.Dimension(130, 23));
        jButtonNewExpressIncome.setPreferredSize(new java.awt.Dimension(130, 23));
        jButtonNewExpressIncome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIncomeNewActionPerformed(evt);
            }
        });
        jPanel6.add(jButtonNewExpressIncome);

        jButtonNewExpressExpense.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonNewExpressExpense.setText(i18n.getString("New_Expense")); // NOI18N
        jButtonNewExpressExpense.setMaximumSize(new java.awt.Dimension(130, 23));
        jButtonNewExpressExpense.setMinimumSize(new java.awt.Dimension(130, 23));
        jButtonNewExpressExpense.setPreferredSize(new java.awt.Dimension(130, 23));
        jButtonNewExpressExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpenseNewActionPerformed(evt);
            }
        });
        jPanel6.add(jButtonNewExpressExpense);

        jPanelGeneral1.add(jPanel6);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(mybudget.option.Options.I18N_BUNDLE.getString("Income"))); // NOI18N
        jPanel7.setLayout(new java.awt.GridBagLayout());

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel7.setText(mybudget.option.Options.I18N_BUNDLE.getString("Income_for_current_month_:_")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel7.add(jLabel7, gridBagConstraints);

        jTextFieldCurrentMonthIncome.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldCurrentMonthIncome.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldCurrentMonthIncome.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldCurrentMonthIncome.setText("0");
        jTextFieldCurrentMonthIncome.setBorder(null);
        jTextFieldCurrentMonthIncome.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel7.add(jTextFieldCurrentMonthIncome, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel8.setText(mybudget.option.Options.I18N_BUNDLE.getString("Income_for_last_month_:_")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel7.add(jLabel8, gridBagConstraints);

        jTextFieldLastMonthIncome.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldLastMonthIncome.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldLastMonthIncome.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldLastMonthIncome.setText("0");
        jTextFieldLastMonthIncome.setBorder(null);
        jTextFieldLastMonthIncome.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel7.add(jTextFieldLastMonthIncome, gridBagConstraints);

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel11.setText(mybudget.option.Options.I18N_BUNDLE.getString("Average_income_for")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel7.add(jLabel11, gridBagConstraints);

        jTextFieldMonthAverageIncome.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldMonthAverageIncome.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldMonthAverageIncome.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldMonthAverageIncome.setText("0");
        jTextFieldMonthAverageIncome.setBorder(null);
        jTextFieldMonthAverageIncome.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel7.add(jTextFieldMonthAverageIncome, gridBagConstraints);

        jComboBoxMonthAverageIncome.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3", "4", "5", "6", "12" }));
        jComboBoxMonthAverageIncome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMonthAverageIncomeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        jPanel7.add(jComboBoxMonthAverageIncome, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel12.setText(mybudget.option.Options.I18N_BUNDLE.getString("months_:")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel7.add(jLabel12, gridBagConstraints);

        jPanelGeneral1.add(jPanel7);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(mybudget.option.Options.I18N_BUNDLE.getString("Expense"))); // NOI18N
        jPanel8.setLayout(new java.awt.GridBagLayout());

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel9.setText(mybudget.option.Options.I18N_BUNDLE.getString("Expense_for_current_month_:_")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(jLabel9, gridBagConstraints);

        jTextFieldCurrentMonthExpense.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldCurrentMonthExpense.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldCurrentMonthExpense.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldCurrentMonthExpense.setText("0");
        jTextFieldCurrentMonthExpense.setBorder(null);
        jTextFieldCurrentMonthExpense.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(jTextFieldCurrentMonthExpense, gridBagConstraints);

        jTextFieldLastMonthExpense.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldLastMonthExpense.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldLastMonthExpense.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldLastMonthExpense.setText("0");
        jTextFieldLastMonthExpense.setBorder(null);
        jTextFieldLastMonthExpense.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(jTextFieldLastMonthExpense, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel10.setText(mybudget.option.Options.I18N_BUNDLE.getString("Expense_for_last_month_:_")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(jLabel10, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel13.setText(mybudget.option.Options.I18N_BUNDLE.getString("Average_expense_for")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(jLabel13, gridBagConstraints);

        jComboBoxMonthAverageExpense.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3", "4", "5", "6", "12" }));
        jComboBoxMonthAverageExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMonthAverageExpenseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        jPanel8.add(jComboBoxMonthAverageExpense, gridBagConstraints);

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel14.setText(mybudget.option.Options.I18N_BUNDLE.getString("months_:")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(jLabel14, gridBagConstraints);

        jTextFieldMonthAverageExpense.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldMonthAverageExpense.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldMonthAverageExpense.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldMonthAverageExpense.setText("0");
        jTextFieldMonthAverageExpense.setBorder(null);
        jTextFieldMonthAverageExpense.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel8.add(jTextFieldMonthAverageExpense, gridBagConstraints);

        jPanelGeneral1.add(jPanel8);

        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder(mybudget.option.Options.I18N_BUNDLE.getString("Balance"))); // NOI18N
        jPanel18.setLayout(new java.awt.GridBagLayout());

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel20.setText(mybudget.option.Options.I18N_BUNDLE.getString("Balance_for_current_month_:_")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel18.add(jLabel20, gridBagConstraints);

        jTextFieldCurrentMonthBalance.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldCurrentMonthBalance.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldCurrentMonthBalance.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldCurrentMonthBalance.setText("0");
        jTextFieldCurrentMonthBalance.setBorder(null);
        jTextFieldCurrentMonthBalance.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel18.add(jTextFieldCurrentMonthBalance, gridBagConstraints);

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel21.setText(mybudget.option.Options.I18N_BUNDLE.getString("Balance_for_last_month_:_")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel18.add(jLabel21, gridBagConstraints);

        jTextFieldLastMonthBalance.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldLastMonthBalance.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldLastMonthBalance.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldLastMonthBalance.setText("0");
        jTextFieldLastMonthBalance.setBorder(null);
        jTextFieldLastMonthBalance.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel18.add(jTextFieldLastMonthBalance, gridBagConstraints);

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel22.setText(mybudget.option.Options.I18N_BUNDLE.getString("Average_balance_for")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel18.add(jLabel22, gridBagConstraints);

        jComboBoxMonthAverageBalance.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3", "4", "5", "6", "12" }));
        jComboBoxMonthAverageBalance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMonthAverageBalanceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        jPanel18.add(jComboBoxMonthAverageBalance, gridBagConstraints);

        jLabel23.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel23.setText(mybudget.option.Options.I18N_BUNDLE.getString("months_:")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel18.add(jLabel23, gridBagConstraints);

        jTextFieldMonthAverageBalance.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldMonthAverageBalance.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jTextFieldMonthAverageBalance.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldMonthAverageBalance.setText("0");
        jTextFieldMonthAverageBalance.setBorder(null);
        jTextFieldMonthAverageBalance.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel18.add(jTextFieldMonthAverageBalance, gridBagConstraints);

        jPanelGeneral1.add(jPanel18);

        jPanelGeneral.add(jPanelGeneral1, java.awt.BorderLayout.CENTER);

        jPanelGeneral2.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jSliderTime.setMajorTickSpacing(1);
        jSliderTime.setMaximum(5);
        jSliderTime.setPaintTicks(true);
        jSliderTime.setSnapToTicks(true);
        jSliderTime.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderTimeStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel2.add(jSliderTime, gridBagConstraints);

        jLabel5.setForeground(new java.awt.Color(0, 200, 130));
        jLabel5.setText(i18n.getString("Income")); // NOI18N
        jPanel1.add(jLabel5);

        jLabel6.setText("/");
        jPanel1.add(jLabel6);

        jLabel4.setForeground(new java.awt.Color(255, 150, 0));
        jLabel4.setText(mybudget.option.Options.I18N_BUNDLE.getString("Expense")); // NOI18N
        jPanel1.add(jLabel4);

        jPanel2.add(jPanel1, new java.awt.GridBagConstraints());

        jLabelSliderTime.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabelSliderTime.setForeground(new java.awt.Color(102, 102, 102));
        jLabelSliderTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelSliderTime.setText("0");
        jLabelSliderTime.setPreferredSize(new java.awt.Dimension(16, 13));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel2.add(jLabelSliderTime, gridBagConstraints);

        jLabelSliderTime1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabelSliderTime1.setForeground(new java.awt.Color(102, 102, 102));
        jLabelSliderTime1.setText(mybudget.option.Options.I18N_BUNDLE.getString("Balance_Slider_Months"));
        jLabelSliderTime1.setPreferredSize(new java.awt.Dimension(70, 13));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 10, 0);
        jPanel2.add(jLabelSliderTime1, gridBagConstraints);

        jPanelGeneral2.add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanelGeneral.add(jPanelGeneral2, java.awt.BorderLayout.EAST);

        jPanelMainBottom.setMaximumSize(new java.awt.Dimension(32767, 30));
        jPanelMainBottom.setMinimumSize(new java.awt.Dimension(0, 30));
        jPanelMainBottom.setLayout(new javax.swing.BoxLayout(jPanelMainBottom, javax.swing.BoxLayout.LINE_AXIS));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 2));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel1.setText(mybudget.option.Options.I18N_BUNDLE.getString("Balance_:")); // NOI18N
        jPanel3.add(jLabel1);

        jTextFieldBalance.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldBalance.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jTextFieldBalance.setForeground(new java.awt.Color(0, 80, 200));
        jTextFieldBalance.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextFieldBalance.setText("0");
        jTextFieldBalance.setBorder(null);
        jTextFieldBalance.setFocusable(false);
        jTextFieldBalance.setPreferredSize(new java.awt.Dimension(100, 20));
        jPanel3.add(jTextFieldBalance);

        jPanelMainBottom.add(jPanel3);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 2));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText(i18n.getString("Total_Income:_")); // NOI18N
        jPanel4.add(jLabel2);

        jTextFieldIncomeTotal.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldIncomeTotal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jTextFieldIncomeTotal.setForeground(new java.awt.Color(0, 200, 130));
        jTextFieldIncomeTotal.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextFieldIncomeTotal.setText("0");
        jTextFieldIncomeTotal.setBorder(null);
        jTextFieldIncomeTotal.setFocusable(false);
        jTextFieldIncomeTotal.setPreferredSize(new java.awt.Dimension(100, 20));
        jPanel4.add(jTextFieldIncomeTotal);

        jPanelMainBottom.add(jPanel4);

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 2));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setText(i18n.getString("Total_Expense:_")); // NOI18N
        jPanel5.add(jLabel3);

        jTextFieldExpenseTotal.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextFieldExpenseTotal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jTextFieldExpenseTotal.setForeground(new java.awt.Color(255, 150, 0));
        jTextFieldExpenseTotal.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextFieldExpenseTotal.setText("0");
        jTextFieldExpenseTotal.setBorder(null);
        jTextFieldExpenseTotal.setFocusable(false);
        jTextFieldExpenseTotal.setPreferredSize(new java.awt.Dimension(100, 20));
        jPanel5.add(jTextFieldExpenseTotal);

        jPanelMainBottom.add(jPanel5);

        jPanelGeneral.add(jPanelMainBottom, java.awt.BorderLayout.SOUTH);

        jPanelMainCenter.add(jPanelGeneral, "general");

        jPanelIncome.setBorder(javax.swing.BorderFactory.createTitledBorder(mybudget.option.Options.I18N_BUNDLE.getString("Income"))); // NOI18N
        jPanelIncome.setLayout(new java.awt.BorderLayout());

        jPanelIncomeTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jButtonIncomeNew.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonIncomeNew.setText(i18n.getString("New")); // NOI18N
        jButtonIncomeNew.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonIncomeNew.setPreferredSize(new java.awt.Dimension(80, 23));
        jButtonIncomeNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIncomeNewActionPerformed(evt);
            }
        });
        jPanelIncomeTop.add(jButtonIncomeNew);

        jButtonIncomeEdit.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonIncomeEdit.setText(i18n.getString("Edit")); // NOI18N
        jButtonIncomeEdit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonIncomeEdit.setPreferredSize(new java.awt.Dimension(80, 23));
        jButtonIncomeEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIncomeEditActionPerformed(evt);
            }
        });
        jPanelIncomeTop.add(jButtonIncomeEdit);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setPreferredSize(new java.awt.Dimension(2, 23));
        jPanelIncomeTop.add(jSeparator2);

        jButtonIncomeDelete.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonIncomeDelete.setText(i18n.getString("Delete")); // NOI18N
        jButtonIncomeDelete.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonIncomeDelete.setPreferredSize(new java.awt.Dimension(80, 23));
        jButtonIncomeDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIncomeDeleteActionPerformed(evt);
            }
        });
        jPanelIncomeTop.add(jButtonIncomeDelete);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setPreferredSize(new java.awt.Dimension(2, 23));
        jPanelIncomeTop.add(jSeparator3);

        jComboBoxIncomeAdditionMovement.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBoxIncomeAdditionMovement.setMaximumRowCount(50);
        jComboBoxIncomeAdditionMovement.setMinimumSize(new java.awt.Dimension(220, 22));
        jComboBoxIncomeAdditionMovement.setPreferredSize(new java.awt.Dimension(220, 22));
        jComboBoxIncomeAdditionMovement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxIncomeAdditionMovementActionPerformed(evt);
            }
        });
        jComboBoxIncomeAdditionMovement.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBoxIncomeAdditionMovementFocusGained(evt);
            }
        });
        jPanelIncomeTop.add(jComboBoxIncomeAdditionMovement);

        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator6.setPreferredSize(new java.awt.Dimension(2, 23));
        jPanelIncomeTop.add(jSeparator6);

        jComboBoxIncomePeriod.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBoxIncomePeriod.setMaximumRowCount(50);
        jComboBoxIncomePeriod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select Period...", "  Today", "   Last 7 Days", "   Last 15 Days", "   Last 30 Days", "   Last 60 Days", "   Last 90 Days", "   Last 150 Days", "   Last 300 Days", "   All Time" }));
        jComboBoxIncomePeriod.setMinimumSize(new java.awt.Dimension(160, 22));
        jComboBoxIncomePeriod.setPreferredSize(new java.awt.Dimension(160, 22));
        jComboBoxIncomePeriod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxIncomePeriodActionPerformed(evt);
            }
        });
        jPanelIncomeTop.add(jComboBoxIncomePeriod);

        jPanelIncome.add(jPanelIncomeTop, java.awt.BorderLayout.NORTH);

        jPanelIncomeCenter.setLayout(new java.awt.BorderLayout());

        jSplitPaneIncome.setDividerLocation(200);
        jSplitPaneIncome.setRightComponent(jScrollPaneIncome);

        jPanelIncomeCenter.add(jSplitPaneIncome, java.awt.BorderLayout.CENTER);

        jPanelIncome.add(jPanelIncomeCenter, java.awt.BorderLayout.CENTER);

        jPanelIncomeBottom.setPreferredSize(new java.awt.Dimension(51, 24));
        jPanelIncomeBottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabelIncomeSumLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelIncomeSumLabel.setText(i18n.getString("Sum_:_")); // NOI18N
        jPanelIncomeBottom.add(jLabelIncomeSumLabel);

        jLabelIncomeSum.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelIncomeSum.setText("0");
        jPanelIncomeBottom.add(jLabelIncomeSum);

        jLabel18.setText("        "); // NOI18N
        jPanelIncomeBottom.add(jLabel18);

        jLabelIncomeSelectedSumLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelIncomeSelectedSumLabel.setText(i18n.getString("________Selected_Sum_:_")); // NOI18N
        jPanelIncomeBottom.add(jLabelIncomeSelectedSumLabel);

        jLabelIncomeSelectedSum.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelIncomeSelectedSum.setText("0");
        jPanelIncomeBottom.add(jLabelIncomeSelectedSum);

        jPanelIncome.add(jPanelIncomeBottom, java.awt.BorderLayout.SOUTH);

        jPanelMainCenter.add(jPanelIncome, "income");

        jPanelExpense.setBorder(javax.swing.BorderFactory.createTitledBorder(mybudget.option.Options.I18N_BUNDLE.getString("Expense"))); // NOI18N
        jPanelExpense.setLayout(new java.awt.BorderLayout());

        jPanelExpenseTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jButtonExpenseNew.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonExpenseNew.setText(i18n.getString("New")); // NOI18N
        jButtonExpenseNew.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExpenseNew.setPreferredSize(new java.awt.Dimension(80, 23));
        jButtonExpenseNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpenseNewActionPerformed(evt);
            }
        });
        jPanelExpenseTop.add(jButtonExpenseNew);

        jButtonExpenseEdit.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonExpenseEdit.setText(i18n.getString("Edit")); // NOI18N
        jButtonExpenseEdit.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExpenseEdit.setPreferredSize(new java.awt.Dimension(80, 23));
        jButtonExpenseEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpenseEditActionPerformed(evt);
            }
        });
        jPanelExpenseTop.add(jButtonExpenseEdit);

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setPreferredSize(new java.awt.Dimension(2, 23));
        jPanelExpenseTop.add(jSeparator4);

        jButtonExpenseDelete.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButtonExpenseDelete.setText(i18n.getString("Delete")); // NOI18N
        jButtonExpenseDelete.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonExpenseDelete.setPreferredSize(new java.awt.Dimension(80, 23));
        jButtonExpenseDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpenseDeleteActionPerformed(evt);
            }
        });
        jPanelExpenseTop.add(jButtonExpenseDelete);

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setPreferredSize(new java.awt.Dimension(2, 23));
        jPanelExpenseTop.add(jSeparator5);

        jComboBoxExpenseAdditionMovement.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBoxExpenseAdditionMovement.setMaximumRowCount(50);
        jComboBoxExpenseAdditionMovement.setMinimumSize(new java.awt.Dimension(220, 22));
        jComboBoxExpenseAdditionMovement.setPreferredSize(new java.awt.Dimension(220, 22));
        jComboBoxExpenseAdditionMovement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxExpenseAdditionMovementActionPerformed(evt);
            }
        });
        jComboBoxExpenseAdditionMovement.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBoxExpenseAdditionMovementFocusGained(evt);
            }
        });
        jPanelExpenseTop.add(jComboBoxExpenseAdditionMovement);

        jSeparator7.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator7.setPreferredSize(new java.awt.Dimension(2, 23));
        jPanelExpenseTop.add(jSeparator7);

        jComboBoxExpensePeriod.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBoxExpensePeriod.setMaximumRowCount(50);
        jComboBoxExpensePeriod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select Period...", "   Today", "   Last 15 Days", "   Last 30 Days", "   Last 60 Days", "   Last 90 Days", "   Last 150 Days", "   Last 300 Days", "   All Time" }));
        jComboBoxExpensePeriod.setMinimumSize(new java.awt.Dimension(160, 22));
        jComboBoxExpensePeriod.setPreferredSize(new java.awt.Dimension(160, 22));
        jComboBoxExpensePeriod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxExpensePeriodActionPerformed(evt);
            }
        });
        jPanelExpenseTop.add(jComboBoxExpensePeriod);

        jPanelExpense.add(jPanelExpenseTop, java.awt.BorderLayout.NORTH);

        jPanelExpenseCenter.setLayout(new java.awt.BorderLayout());

        jSplitPaneExpense.setRightComponent(jScrollPaneExpense);

        jPanelExpenseCenter.add(jSplitPaneExpense, java.awt.BorderLayout.CENTER);

        jPanelExpense.add(jPanelExpenseCenter, java.awt.BorderLayout.CENTER);

        jPanelExpenseBottom.setPreferredSize(new java.awt.Dimension(195, 24));
        jPanelExpenseBottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabelExpenseSumLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelExpenseSumLabel.setText(i18n.getString("Sum_:_")); // NOI18N
        jPanelExpenseBottom.add(jLabelExpenseSumLabel);

        jLabelExpenseSum.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelExpenseSum.setText("0");
        jPanelExpenseBottom.add(jLabelExpenseSum);

        jLabel17.setText("        "); // NOI18N
        jPanelExpenseBottom.add(jLabel17);

        jLabelExpenseSelectedSumLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelExpenseSelectedSumLabel.setText(i18n.getString("________Selected_Sum_:_")); // NOI18N
        jPanelExpenseBottom.add(jLabelExpenseSelectedSumLabel);

        jLabelExpenseSelectedSum.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelExpenseSelectedSum.setText("0");
        jPanelExpenseBottom.add(jLabelExpenseSelectedSum);

        jPanelExpense.add(jPanelExpenseBottom, java.awt.BorderLayout.SOUTH);

        jPanelMainCenter.add(jPanelExpense, "expense");

        jPanelStatistic.setLayout(new java.awt.BorderLayout());

        jPanel9.setLayout(new java.awt.BorderLayout());

        jTabbedPaneStatistic.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPaneStatisticMouseClicked(evt);
            }
        });

        jPanelStatisticIncomeExpense.setLayout(new java.awt.BorderLayout());

        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel13.setLayout(new java.awt.BorderLayout());

        jPanelStatisticGraph.setLayout(new java.awt.CardLayout());

        jPanelSGraphDay.setLayout(new java.awt.BorderLayout());
        jPanelStatisticGraph.add(jPanelSGraphDay, "Day");

        jPanelSGraphWeek.setLayout(new java.awt.BorderLayout());
        jPanelStatisticGraph.add(jPanelSGraphWeek, "Week");

        jPanelSGraphMonth.setLayout(new java.awt.BorderLayout());
        jPanelStatisticGraph.add(jPanelSGraphMonth, "Month");

        jPanelSGraphYear.setLayout(new java.awt.BorderLayout());
        jPanelStatisticGraph.add(jPanelSGraphYear, "Year");

        jPanel13.add(jPanelStatisticGraph, java.awt.BorderLayout.CENTER);

        jPanelStatisticGraphParam.setLayout(new java.awt.GridBagLayout());

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel15.setText(i18n.getString("Diagram_Type_:")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanelStatisticGraphParam.add(jLabel15, gridBagConstraints);

        jComboBoxStatisticDiagramType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBoxStatisticDiagramType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Type 1", "Type 2" }));
        jComboBoxStatisticDiagramType.setMinimumSize(new java.awt.Dimension(90, 23));
        jComboBoxStatisticDiagramType.setPreferredSize(new java.awt.Dimension(90, 23));
        jComboBoxStatisticDiagramType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxStatisticDiagramTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 15);
        jPanelStatisticGraphParam.add(jComboBoxStatisticDiagramType, gridBagConstraints);

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel16.setText(i18n.getString("Time_Tick_:")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 15, 3, 3);
        jPanelStatisticGraphParam.add(jLabel16, gridBagConstraints);

        jComboBoxStatisticTimeTick.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBoxStatisticTimeTick.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Day", "Week", "Month", "Year" }));
        jComboBoxStatisticTimeTick.setMinimumSize(new java.awt.Dimension(90, 22));
        jComboBoxStatisticTimeTick.setPreferredSize(new java.awt.Dimension(90, 22));
        jComboBoxStatisticTimeTick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxStatisticTimeTickActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanelStatisticGraphParam.add(jComboBoxStatisticTimeTick, gridBagConstraints);

        jPanelStatisticSliders.setLayout(new java.awt.CardLayout());

        jPanelStatisticSliderDay.setMinimumSize(new java.awt.Dimension(230, 45));
        jPanelStatisticSliderDay.setPreferredSize(new java.awt.Dimension(230, 45));
        jPanelStatisticSliderDay.setLayout(new java.awt.GridBagLayout());

        jSliderStatisticDay.setPaintTicks(true);
        jSliderStatisticDay.setSnapToTicks(true);
        jSliderStatisticDay.setMinimumSize(new java.awt.Dimension(200, 32));
        jSliderStatisticDay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderStatisticDayStateChanged(evt);
            }
        });
        jPanelStatisticSliderDay.add(jSliderStatisticDay, new java.awt.GridBagConstraints());

        jLabelStatisticDay.setForeground(new java.awt.Color(102, 102, 102));
        jLabelStatisticDay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelStatisticDay.setText("days");
        jLabelStatisticDay.setPreferredSize(new java.awt.Dimension(26, 13));
        jLabelStatisticDay.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanelStatisticSliderDay.add(jLabelStatisticDay, gridBagConstraints);

        jPanelStatisticSliders.add(jPanelStatisticSliderDay, "Day");

        jPanelStatisticSliderWeek.setMinimumSize(new java.awt.Dimension(230, 45));
        jPanelStatisticSliderWeek.setPreferredSize(new java.awt.Dimension(230, 45));
        jPanelStatisticSliderWeek.setLayout(new java.awt.GridBagLayout());

        jSliderStatisticWeek.setPaintTicks(true);
        jSliderStatisticWeek.setSnapToTicks(true);
        jSliderStatisticWeek.setMinimumSize(new java.awt.Dimension(200, 32));
        jSliderStatisticWeek.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderStatisticWeekStateChanged(evt);
            }
        });
        jPanelStatisticSliderWeek.add(jSliderStatisticWeek, new java.awt.GridBagConstraints());

        jLabelStatisticWeek.setForeground(new java.awt.Color(102, 102, 102));
        jLabelStatisticWeek.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelStatisticWeek.setText("weeks");
        jLabelStatisticWeek.setPreferredSize(new java.awt.Dimension(18, 13));
        jLabelStatisticWeek.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanelStatisticSliderWeek.add(jLabelStatisticWeek, gridBagConstraints);

        jPanelStatisticSliders.add(jPanelStatisticSliderWeek, "Week");

        jPanelStatisticSliderMonth.setMinimumSize(new java.awt.Dimension(230, 45));
        jPanelStatisticSliderMonth.setPreferredSize(new java.awt.Dimension(230, 45));
        jPanelStatisticSliderMonth.setLayout(new java.awt.GridBagLayout());

        jSliderStatisticMonth.setPaintTicks(true);
        jSliderStatisticMonth.setSnapToTicks(true);
        jSliderStatisticMonth.setMinimumSize(new java.awt.Dimension(200, 32));
        jSliderStatisticMonth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderStatisticMonthStateChanged(evt);
            }
        });
        jPanelStatisticSliderMonth.add(jSliderStatisticMonth, new java.awt.GridBagConstraints());

        jLabelStatisticMonth.setForeground(new java.awt.Color(102, 102, 102));
        jLabelStatisticMonth.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelStatisticMonth.setText("months");
        jLabelStatisticMonth.setPreferredSize(new java.awt.Dimension(18, 13));
        jLabelStatisticMonth.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanelStatisticSliderMonth.add(jLabelStatisticMonth, gridBagConstraints);

        jPanelStatisticSliders.add(jPanelStatisticSliderMonth, "Month");

        jPanelStatisticSliderYear.setMinimumSize(new java.awt.Dimension(230, 45));
        jPanelStatisticSliderYear.setPreferredSize(new java.awt.Dimension(230, 45));
        jPanelStatisticSliderYear.setLayout(new java.awt.GridBagLayout());

        jSliderStatisticYear.setPaintTicks(true);
        jSliderStatisticYear.setSnapToTicks(true);
        jSliderStatisticYear.setMinimumSize(new java.awt.Dimension(200, 32));
        jSliderStatisticYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderStatisticYearStateChanged(evt);
            }
        });
        jPanelStatisticSliderYear.add(jSliderStatisticYear, new java.awt.GridBagConstraints());

        jLabelStatisticYear.setForeground(new java.awt.Color(102, 102, 102));
        jLabelStatisticYear.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelStatisticYear.setText(i18n.getString("years")); // NOI18N
        jLabelStatisticYear.setPreferredSize(new java.awt.Dimension(18, 13));
        jLabelStatisticYear.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanelStatisticSliderYear.add(jLabelStatisticYear, gridBagConstraints);

        jPanelStatisticSliders.add(jPanelStatisticSliderYear, "Year");

        jPanelStatisticGraphParam.add(jPanelStatisticSliders, new java.awt.GridBagConstraints());

        jPanel13.add(jPanelStatisticGraphParam, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(jPanel13);

        jPanel10.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanelStatisticIncomeExpense.add(jPanel10, java.awt.BorderLayout.CENTER);

        jTabbedPaneStatistic.addTab(mybudget.option.Options.I18N_BUNDLE.getString("Income_/_Expense_Graphics"), jPanelStatisticIncomeExpense); // NOI18N

        jPanelStatisticBalance.setLayout(new java.awt.BorderLayout());

        jPanelStatisticBalanceGraph.setLayout(new java.awt.BorderLayout());
        jPanelStatisticBalance.add(jPanelStatisticBalanceGraph, java.awt.BorderLayout.CENTER);

        jPanel16.setLayout(new java.awt.GridBagLayout());

        jSliderBalance.setPaintTicks(true);
        jSliderBalance.setSnapToTicks(true);
        jSliderBalance.setPreferredSize(new java.awt.Dimension(300, 32));
        jSliderBalance.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderBalanceStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel16.add(jSliderBalance, gridBagConstraints);

        jLabelSliderBalance.setForeground(new java.awt.Color(102, 102, 102));
        jLabelSliderBalance.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelSliderBalance.setText("jLabel19");
        jLabelSliderBalance.setPreferredSize(new java.awt.Dimension(26, 14));
        jPanel17.add(jLabelSliderBalance);

        jLabel19.setForeground(new java.awt.Color(102, 102, 102));
        jLabel19.setText(mybudget.option.Options.I18N_BUNDLE.getString("Balance_Slider_Months")); // NOI18N
        jPanel17.add(jLabel19);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel16.add(jPanel17, gridBagConstraints);

        jPanelStatisticBalance.add(jPanel16, java.awt.BorderLayout.SOUTH);

        jTabbedPaneStatistic.addTab(mybudget.option.Options.I18N_BUNDLE.getString("Balance_Graphic"), jPanelStatisticBalance); // NOI18N

        jPanelStatisticSummary.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setDividerLocation(340);

        jPanel19.setMinimumSize(new java.awt.Dimension(320, 70));
        jPanel19.setPreferredSize(new java.awt.Dimension(320, 70));
        jPanel19.setLayout(new java.awt.BorderLayout());

        jPanelDateInterval.setBorder(javax.swing.BorderFactory.createTitledBorder(mybudget.option.Options.I18N_BUNDLE.getString("Date_Interval"))); // NOI18N
        jPanelDateInterval.setLayout(new java.awt.GridBagLayout());

        jLabel24.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel24.setText(mybudget.option.Options.I18N_BUNDLE.getString("From")); // NOI18N
        jPanelDateInterval.add(jLabel24, new java.awt.GridBagConstraints());

        jCCFieldFrom.setExtButtonVisible(false);
        jPanelDateInterval.add(jCCFieldFrom, new java.awt.GridBagConstraints());

        jLabel25.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel25.setText(mybudget.option.Options.I18N_BUNDLE.getString("To")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelDateInterval.add(jLabel25, gridBagConstraints);

        jCCFieldTo.setExtButtonVisible(false);
        jPanelDateInterval.add(jCCFieldTo, new java.awt.GridBagConstraints());

        jComboBoxSumPeriod.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBoxSumPeriod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxSumPeriod.setPreferredSize(new java.awt.Dimension(160, 22));
        jComboBoxSumPeriod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSumPeriodActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanelDateInterval.add(jComboBoxSumPeriod, gridBagConstraints);

        jPanel19.add(jPanelDateInterval, java.awt.BorderLayout.NORTH);

        jPanel22.setLayout(new java.awt.BorderLayout());
        jPanel22.add(jScrollPaneSum, java.awt.BorderLayout.CENTER);

        buttonGroupSummary.add(jRadioButtonIncome);
        jRadioButtonIncome.setText(mybudget.option.Options.I18N_BUNDLE.getString("Income")); // NOI18N
        jRadioButtonIncome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonIncomeActionPerformed(evt);
            }
        });
        jPanel23.add(jRadioButtonIncome);

        buttonGroupSummary.add(jRadioButtonExpense);
        jRadioButtonExpense.setSelected(true);
        jRadioButtonExpense.setText(mybudget.option.Options.I18N_BUNDLE.getString("Expense")); // NOI18N
        jRadioButtonExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonExpenseActionPerformed(evt);
            }
        });
        jPanel23.add(jRadioButtonExpense);

        jPanel22.add(jPanel23, java.awt.BorderLayout.SOUTH);

        jPanel19.add(jPanel22, java.awt.BorderLayout.CENTER);

        jSplitPane2.setLeftComponent(jPanel19);

        jPanel20.setLayout(new java.awt.BorderLayout());

        jPanelSumGraph.setLayout(new java.awt.BorderLayout());
        jPanel20.add(jPanelSumGraph, java.awt.BorderLayout.CENTER);

        jSplitPane2.setRightComponent(jPanel20);

        jPanelStatisticSummary.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        jTabbedPaneStatistic.addTab(mybudget.option.Options.I18N_BUNDLE.getString("Summary_Statistic"), jPanelStatisticSummary); // NOI18N

        jPanel9.add(jTabbedPaneStatistic, java.awt.BorderLayout.CENTER);
        jTabbedPaneStatistic.getAccessibleContext().setAccessibleName(mybudget.option.Options.I18N_BUNDLE.getString("Income_/_Expense_Graphics")); // NOI18N

        jPanelStatistic.add(jPanel9, java.awt.BorderLayout.CENTER);

        jPanelMainCenter.add(jPanelStatistic, "statistic");

        getContentPane().add(jPanelMainCenter, java.awt.BorderLayout.CENTER);

        jMenuFile.setText(i18n.getString("File")); // NOI18N
        jMenuFile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemExit.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemExit.setText(i18n.getString("Exit")); // NOI18N
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);

        jMenuData.setText(i18n.getString("Data")); // NOI18N
        jMenuData.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jMenuItemNewIncome.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemNewIncome.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemNewIncome.setText(i18n.getString("New_Income...")); // NOI18N
        jMenuItemNewIncome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIncomeNewActionPerformed(evt);
            }
        });
        jMenuData.add(jMenuItemNewIncome);

        jMenuItemNewExpense.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemNewExpense.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemNewExpense.setText(i18n.getString("New_Expense...")); // NOI18N
        jMenuItemNewExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpenseNewActionPerformed(evt);
            }
        });
        jMenuData.add(jMenuItemNewExpense);
        jMenuData.add(jSeparator10);

        jMenuItemTagsList.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemTagsList.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemTagsList.setText(i18n.getString("Tags_List...")); // NOI18N
        jMenuItemTagsList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTagsListActionPerformed(evt);
            }
        });
        jMenuData.add(jMenuItemTagsList);

        jMenuBar.add(jMenuData);

        jMenuNavigate.setText(i18n.getString("Navigate")); // NOI18N
        jMenuNavigate.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jMenuItemGeneral.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemGeneral.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemGeneral.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/iconGeneral16.png"))); // NOI18N
        jMenuItemGeneral.setText(i18n.getString("Select_in_General")); // NOI18N
        jMenuItemGeneral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGeneralActionPerformed(evt);
            }
        });
        jMenuNavigate.add(jMenuItemGeneral);

        jMenuItemIncome.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemIncome.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemIncome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/iconIncome16.png"))); // NOI18N
        jMenuItemIncome.setText(i18n.getString("Select_in_Income")); // NOI18N
        jMenuItemIncome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemIncomeActionPerformed(evt);
            }
        });
        jMenuNavigate.add(jMenuItemIncome);

        jMenuItemExpense.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemExpense.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemExpense.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/iconExpense16.png"))); // NOI18N
        jMenuItemExpense.setText(i18n.getString("Select_in_Expense")); // NOI18N
        jMenuItemExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExpenseActionPerformed(evt);
            }
        });
        jMenuNavigate.add(jMenuItemExpense);

        jMenuItemStatistic.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemStatistic.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemStatistic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mybudget/gui/images/iconStatistic16.png"))); // NOI18N
        jMenuItemStatistic.setText(i18n.getString("Select_in_Statistic")); // NOI18N
        jMenuItemStatistic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemStatisticActionPerformed(evt);
            }
        });
        jMenuNavigate.add(jMenuItemStatistic);

        jMenuBar.add(jMenuNavigate);

        jMenuTools.setText(i18n.getString("Tools")); // NOI18N
        jMenuTools.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jMenuItemOptions.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemOptions.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemOptions.setText(i18n.getString("Options...")); // NOI18N
        jMenuItemOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOptionsActionPerformed(evt);
            }
        });
        jMenuTools.add(jMenuItemOptions);

        jMenuItemExport.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemExport.setText(i18n.getString("Export...")); // NOI18N
        jMenuItemExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportActionPerformed(evt);
            }
        });
        jMenuTools.add(jMenuItemExport);

        jMenuBar.add(jMenuTools);

        jMenuHelp.setText(i18n.getString("Help")); // NOI18N
        jMenuHelp.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jMenuItemHelpContents.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemHelpContents.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemHelpContents.setText(i18n.getString("Help_Contents")); // NOI18N
        jMenuHelp.add(jMenuItemHelpContents);

        jMenuItemAbout.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jMenuItemAbout.setText(i18n.getString("About")); // NOI18N
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        HibernateUtil.shotdown();
        serverStop();
        savePreference();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonToolBarGeneralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonToolBarGeneralActionPerformed
        initGeneralPanel();
        updateBottomPanel();
        ((CardLayout) jPanelMainCenter.getLayout()).show(jPanelMainCenter, "general"); // NOI18N
    }//GEN-LAST:event_jButtonToolBarGeneralActionPerformed

    private void jButtonToolBarIncomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonToolBarIncomeActionPerformed
        if (jPanelIncome.isShowing()) {
            return;
        }

        tagsIncomePanel.updateContent();
        updateIncomeContent();

        ((CardLayout) jPanelMainCenter.getLayout()).show(jPanelMainCenter, "income"); // NOI18N
    }//GEN-LAST:event_jButtonToolBarIncomeActionPerformed

    private void jButtonToolBarExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonToolBarExpenseActionPerformed
        if (jPanelExpense.isShowing()) {
            return;
        }

        tagsExpensePanel.updateContent();
        updateExpenseContent();
        
        ((CardLayout) jPanelMainCenter.getLayout()).show(jPanelMainCenter, "expense"); // NOI18N
}//GEN-LAST:event_jButtonToolBarExpenseActionPerformed

    private void jButtonToolBarStatisticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonToolBarStatisticActionPerformed
        ((CardLayout) jPanelMainCenter.getLayout()).show(jPanelMainCenter, "statistic"); // NOI18N
        if (jPanelStatisticIncomeExpense.isShowing()) {
            tagsGraphPanel.updateContent();
            jSliderStatisticDayStateChanged(null);
            jComboBoxStatisticTimeTickActionPerformed(evt);
        } else if (jPanelStatisticBalance.isShowing()) {
            updateBalanceStatisticGraph();
        } else if (jPanelStatisticSummary.isShowing()) {
            updateSummaryStatistics();
        }
    }//GEN-LAST:event_jButtonToolBarStatisticActionPerformed

    private void jMenuItemTagsListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTagsListActionPerformed
        jScrollPaneTagsList.setViewportView(createNewTagTable());
        jButtonTagListNew.requestFocus();
        jDialogTagsList.setVisible(true);
    }//GEN-LAST:event_jMenuItemTagsListActionPerformed

    private void jButtonTagListNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTagListNewActionPerformed
        dialogTag.showForNew();
        String res = dialogTag.getResult();
        if (null != res) {
            tagManager.insert(res);
            jScrollPaneTagsList.setViewportView(createNewTagTable());
            updateGUIAfterUpdateBase(true);
        }
    }//GEN-LAST:event_jButtonTagListNewActionPerformed

    private void jButtonTagListEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTagListEditActionPerformed
        TagTable table = (TagTable) jScrollPaneTagsList.getViewport().getView();
        if (-1 == table.getSelectedRow()) {
            return;
        }

        dialogTag.showForEdit((String) table.getValueAt(table.getSelectedRow(), 1));
        String res = dialogTag.getResult();
        if (null != res) {
            Tag tag = tagManager.getTag((Long) table.getValueAt(table.getSelectedRow(), 0));
            tag.setName(res);
            tagManager.update(tag);
            jScrollPaneTagsList.setViewportView(createNewTagTable());
            updateGUIAfterUpdateBase(true);
        }
}//GEN-LAST:event_jButtonTagListEditActionPerformed

    private void jButtonTagListDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTagListDeleteActionPerformed
        TagTable table = (TagTable) jScrollPaneTagsList.getViewport().getView();
        if (-1 == table.getSelectedRow()) {
            return;
        }
        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(jDialogTagsList,
                mybudget.option.Options.I18N_BUNDLE.getString("Do_you_want_to_delete_tag_-_'") +
                table.getValueAt(table.getSelectedRow(), 1) + "'?", mybudget.option.Options.I18N_BUNDLE.getString("Delete_Tag"),
                JOptionPane.YES_NO_OPTION)) {
            return;
        }
        Tag tag = tagManager.getTag((Long) table.getValueAt(table.getSelectedRow(), 0));
        tagManager.delete(tag);
        jScrollPaneTagsList.setViewportView(createNewTagTable());
        updateGUIAfterUpdateBase(true);
    }//GEN-LAST:event_jButtonTagListDeleteActionPerformed

    private void jButtonIncomeNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIncomeNewActionPerformed
        dialogTransaction.showForNewIncome();        
        if (dialogTransaction.getResult()) {
            Transaction transaction = dialogTransaction.getResultTransaction();
            if (null == transaction)
                return;
            transaction.setType(Transaction.TYPE_INCOME);
            transactionManager.insert(transaction);
            for (Long id : dialogTransaction.getTagIdList()) {
                transactionManager.addTagToTransaction(transaction.getId(), id);
            }
            updateGUIAfterUpdateBase(false);
        }
}//GEN-LAST:event_jButtonIncomeNewActionPerformed

    private void jButtonIncomeDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIncomeDeleteActionPerformed
        TransactionTable table = (TransactionTable) jScrollPaneIncome.getViewport().getView();
        int[] selectRows = table.getSelectedRows();
        if (0 == selectRows.length) {
            return;
        }
        if (1 < selectRows.length) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    mybudget.option.Options.I18N_BUNDLE.getString("Do_you_want_to_delete_") + " " + 
                    selectRows.length + " " + mybudget.option.Options.I18N_BUNDLE.getString("_income_transactions?"), 
                    mybudget.option.Options.I18N_BUNDLE.getString("Delete_Transactions"),
                    JOptionPane.YES_NO_OPTION)) {
                return;
            }
        } else {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    mybudget.option.Options.I18N_BUNDLE.getString("Do_you_want_to_delete_income_transaction_-_'") +
                    getInfoForDeleteTransaction(table, selectRows) + "'?",
                    mybudget.option.Options.I18N_BUNDLE.getString("Delete_Transaction"),
                    JOptionPane.YES_NO_OPTION)) {
                return;
            }
        }
        for (int i = 0; i < selectRows.length; i++) {
            Transaction transaction = transactionManager.getTransaction((Long) table.getValueAt(selectRows[i], 0));
            transactionManager.delete(transaction);
        }
        updateIncomeContent();
    }//GEN-LAST:event_jButtonIncomeDeleteActionPerformed

    private void jButtonIncomeEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIncomeEditActionPerformed
        TransactionTable table = (TransactionTable) jScrollPaneIncome.getViewport().getView();
        Transaction transaction = transactionManager.getTransactionWithTags(
                (Long) table.getValueAt(table.getSelectedRow(), 0));
        dialogTransaction.showForEditIncome(transaction);        
        if (dialogTransaction.getResult()) {
            transaction = dialogTransaction.getResultTransaction();
            if (null == transaction) 
                return;
            transaction.setType(Transaction.TYPE_INCOME);
            for (Object obj : transaction.getTags()) {
                transactionManager.removeTagFromTransaction(transaction.getId(), ((Tag) obj).getId());
            }
            for (Long id : dialogTransaction.getTagIdList()) {
                transactionManager.addTagToTransaction(transaction.getId(), id);
            }
            transactionManager.update(transaction);
            updateIncomeContent();
        }
}//GEN-LAST:event_jButtonIncomeEditActionPerformed

    private void jButtonExpenseEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExpenseEditActionPerformed
        TransactionTable table = (TransactionTable) jScrollPaneExpense.getViewport().getView();
        Transaction transaction = transactionManager.getTransactionWithTags(
                (Long) table.getValueAt(table.getSelectedRow(), 0));
        dialogTransaction.showForEditExpense(transaction);
        if (dialogTransaction.getResult()) {
            transaction = dialogTransaction.getResultTransaction();
            if (null == transaction) 
                return;
            transaction.setType(Transaction.TYPE_EXPENSE);
            for (Object obj : transaction.getTags()) {
                transactionManager.removeTagFromTransaction(transaction.getId(), ((Tag) obj).getId());
            }
            for (Long id : dialogTransaction.getTagIdList()) {
                transactionManager.addTagToTransaction(transaction.getId(), id);
            }
            transactionManager.update(transaction);
            updateExpenseContent();
        }
}//GEN-LAST:event_jButtonExpenseEditActionPerformed

    private void jButtonExpenseDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExpenseDeleteActionPerformed
        TransactionTable table = (TransactionTable) jScrollPaneExpense.getViewport().getView();
        int[] selectRows = table.getSelectedRows();
        if (0 == selectRows.length) {
            return;
        }
        if (1 < selectRows.length) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    mybudget.option.Options.I18N_BUNDLE.getString("Do_you_want_to_delete_") + " " + 
                    selectRows.length + " " + mybudget.option.Options.I18N_BUNDLE.getString("_expense_transactions?"), 
                    mybudget.option.Options.I18N_BUNDLE.getString("Delete_Transactions"),
                    JOptionPane.YES_NO_OPTION)) {
                return;
            }
        } else {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    mybudget.option.Options.I18N_BUNDLE.getString("Do_you_want_to_delete_expense_transaction_-_'") +
                    getInfoForDeleteTransaction(table, selectRows) + "'?",
                    mybudget.option.Options.I18N_BUNDLE.getString("Delete_Transaction"),
                    JOptionPane.YES_NO_OPTION)) {
                return;
            }
        }
        for (int i = 0; i < selectRows.length; i++) {
            Transaction transaction = transactionManager.getTransaction((Long) table.getValueAt(selectRows[i], 0));
            transactionManager.delete(transaction);
        }
        updateExpenseContent();
}//GEN-LAST:event_jButtonExpenseDeleteActionPerformed

    private void jButtonExpenseNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExpenseNewActionPerformed
        dialogTransaction.showForNewExpense();
        if (dialogTransaction.getResult()) {
            Transaction transaction = dialogTransaction.getResultTransaction();
            if (null == transaction) 
                return;
            transaction.setType(Transaction.TYPE_EXPENSE);
            transactionManager.insert(transaction);
            for (Long id : dialogTransaction.getTagIdList()) {
                transactionManager.addTagToTransaction(transaction.getId(), id);
            }
            updateGUIAfterUpdateBase(false);
        }
}//GEN-LAST:event_jButtonExpenseNewActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        formWindowClosing(null);
        System.exit(0);
}//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jSliderTimeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderTimeStateChanged
        if (!jSliderTime.isShowing())
            return;
        if (prevTimeGeneralIndex == jSliderTime.getValue()) {
            return;
        }
        jLabelSliderTime.setText("" + (jSliderTime.getMaximum() - jSliderTime.getValue() + 1));
        prevTimeGeneralIndex = jSliderTime.getValue();
        if (null != graphMakerGeneral && jSliderTime.isShowing()) {
            updateGraphGeneral();
        }        
    }//GEN-LAST:event_jSliderTimeStateChanged

    private void jComboBoxIncomeAdditionMovementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxIncomeAdditionMovementActionPerformed
        Object obj = jComboBoxIncomeAdditionMovement.getSelectedItem();
        TransactionTable table = additionMovementIncomeManager.getCurrentTable();
        if (obj instanceof ComboBoxItemTag && 0 < table.getSelectedRowCount()) {
            ComboBoxItemTag boxTag = (ComboBoxItemTag) obj;            
            int[] selectRows = table.getSelectedRows();
            if (ComboBoxItemTag.TYPE_ADD == boxTag.getType()) {
                for (int i = 0; i < selectRows.length; i++) {
                    transactionManager.addTagToTransactionSoft((Long) table.getValueAt(selectRows[i], 0), boxTag.getTag().getId());
                }
            } else if (ComboBoxItemTag.TYPE_DELETE == boxTag.getType()) {
                for (int i = 0; i < selectRows.length; i++) {
                    transactionManager.removeTagFromTransactionSoft((Long) table.getValueAt(selectRows[i], 0), boxTag.getTag().getId());
                }
            }
            updateIncomeContent();
        }
        if (0 < jComboBoxIncomeAdditionMovement.getItemCount()) {
            jComboBoxIncomeAdditionMovement.setSelectedIndex(0);
        }
}//GEN-LAST:event_jComboBoxIncomeAdditionMovementActionPerformed

private void jComboBoxIncomeAdditionMovementFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBoxIncomeAdditionMovementFocusGained
    additionMovementIncomeManager.updateComboBox();
}//GEN-LAST:event_jComboBoxIncomeAdditionMovementFocusGained

private void jComboBoxExpenseAdditionMovementFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBoxExpenseAdditionMovementFocusGained
    additionMovementExpenseManager.updateComboBox();
}//GEN-LAST:event_jComboBoxExpenseAdditionMovementFocusGained

private void jComboBoxExpenseAdditionMovementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxExpenseAdditionMovementActionPerformed
        Object obj = jComboBoxExpenseAdditionMovement.getSelectedItem();
        TransactionTable table = additionMovementExpenseManager.getCurrentTable();
        if (obj instanceof ComboBoxItemTag && 0 < table.getSelectedRowCount()) {
            ComboBoxItemTag boxTag = (ComboBoxItemTag) obj;            
            int[] selectRows = table.getSelectedRows();
            if (ComboBoxItemTag.TYPE_ADD == boxTag.getType()) {
                for (int i = 0; i < selectRows.length; i++) {
                    transactionManager.addTagToTransactionSoft((Long) table.getValueAt(selectRows[i], 0), boxTag.getTag().getId());
                }
            } else if (ComboBoxItemTag.TYPE_DELETE == boxTag.getType()) {
                for (int i = 0; i < selectRows.length; i++) {
                    transactionManager.removeTagFromTransactionSoft((Long) table.getValueAt(selectRows[i], 0), boxTag.getTag().getId());
                }
            }
            updateExpenseContent();
        }
        if (0 < jComboBoxExpenseAdditionMovement.getItemCount()) {
            jComboBoxExpenseAdditionMovement.setSelectedIndex(0);
        }
}//GEN-LAST:event_jComboBoxExpenseAdditionMovementActionPerformed

private void jComboBoxIncomePeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxIncomePeriodActionPerformed
    Object obj = jComboBoxIncomePeriod.getSelectedItem();
    if (obj instanceof ComboBoxItemTime) {
        ComboBoxItemTime tmpTime = (ComboBoxItemTime) obj;
        incomeStartDate = tmpTime.getStartDate();
        incomeFinishDate = tmpTime.getFinishDate();
        lastIndexIncomePeriod = jComboBoxIncomePeriod.getSelectedIndex();
        updateIncomeContent();
    }
    if (0 < jComboBoxIncomePeriod.getItemCount()) {
        jComboBoxIncomePeriod.setSelectedIndex(0);
    }
}//GEN-LAST:event_jComboBoxIncomePeriodActionPerformed

private void jComboBoxExpensePeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxExpensePeriodActionPerformed
    Object obj = jComboBoxExpensePeriod.getSelectedItem();
    if (obj instanceof ComboBoxItemTime) {
        ComboBoxItemTime tmpTime = (ComboBoxItemTime) obj;
        expenseStartDate = tmpTime.getStartDate();
        expenseFinishDate = tmpTime.getFinishDate();
        lastIndexExpensePeriod = jComboBoxExpensePeriod.getSelectedIndex();
        updateExpenseContent();
    }
    if (0 < jComboBoxExpensePeriod.getItemCount()) {
        jComboBoxExpensePeriod.setSelectedIndex(0);
    }
}//GEN-LAST:event_jComboBoxExpensePeriodActionPerformed

private void jMenuItemGeneralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGeneralActionPerformed
    jButtonToolBarGeneralActionPerformed(evt);
}//GEN-LAST:event_jMenuItemGeneralActionPerformed

private void jMenuItemIncomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemIncomeActionPerformed
    jButtonToolBarIncomeActionPerformed(evt);
}//GEN-LAST:event_jMenuItemIncomeActionPerformed

private void jMenuItemExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExpenseActionPerformed
    jButtonToolBarExpenseActionPerformed(evt);
}//GEN-LAST:event_jMenuItemExpenseActionPerformed

private void jMenuItemStatisticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemStatisticActionPerformed
    jButtonToolBarStatisticActionPerformed(evt);
}//GEN-LAST:event_jMenuItemStatisticActionPerformed

private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
    
    dialogAbout.setVisible(true);
    
}//GEN-LAST:event_jMenuItemAboutActionPerformed

private void jComboBoxMonthAverageIncomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMonthAverageIncomeActionPerformed
    
    jTextFieldMonthAverageIncome.setText(Options.toCurrency(getAverageValue(
            Integer.parseInt(jComboBoxMonthAverageIncome.getSelectedItem().toString()), 
            Transaction.TYPE_INCOME)));
    
}//GEN-LAST:event_jComboBoxMonthAverageIncomeActionPerformed

private void jComboBoxMonthAverageExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMonthAverageExpenseActionPerformed

    jTextFieldMonthAverageExpense.setText(Options.toCurrency(getAverageValue(
            Integer.parseInt(jComboBoxMonthAverageExpense.getSelectedItem().toString()), 
            Transaction.TYPE_EXPENSE)));
    
}//GEN-LAST:event_jComboBoxMonthAverageExpenseActionPerformed

private void jComboBoxStatisticTimeTickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxStatisticTimeTickActionPerformed
    if (!jComboBoxStatisticTimeTick.isShowing()) {
        return;
    }
    indexGraphTickTime = jComboBoxStatisticTimeTick.getSelectedIndex();
    switch (indexGraphTickTime) {
        case 0 : {
            ((CardLayout) jPanelStatisticSliders.getLayout()).show(jPanelStatisticSliders, "Day"); //NOI18N
            ((CardLayout) jPanelStatisticGraph.getLayout()).show(jPanelStatisticGraph, "Day"); //NOI18N
            updateIncomeExpenseDayStatisticGraph();
        } break;
        case 1 : {
            ((CardLayout) jPanelStatisticSliders.getLayout()).show(jPanelStatisticSliders, "Week"); //NOI18N
            ((CardLayout) jPanelStatisticGraph.getLayout()).show(jPanelStatisticGraph, "Week"); //NOI18N
            updateIncomeExpenseWeekStatisticGraph();
        } break;
        case 2 : {
            ((CardLayout) jPanelStatisticSliders.getLayout()).show(jPanelStatisticSliders, "Month"); //NOI18N
            ((CardLayout) jPanelStatisticGraph.getLayout()).show(jPanelStatisticGraph, "Month"); //NOI18N
            updateIncomeExpenseMonthStatisticGraph();
        } break;
        case 3 : {
            ((CardLayout) jPanelStatisticSliders.getLayout()).show(jPanelStatisticSliders, "Year"); //NOI18N
            ((CardLayout) jPanelStatisticGraph.getLayout()).show(jPanelStatisticGraph, "Year"); //NOI18N
            updateIncomeExpenseYearStatisticGraph();
        } break;
    }
}//GEN-LAST:event_jComboBoxStatisticTimeTickActionPerformed

private void jSliderStatisticDayStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderStatisticDayStateChanged
    if (!jSliderStatisticDay.isShowing()) {
        return;
    }
    jLabelStatisticDay.setText("" + ((jSliderStatisticDay.getMaximum() - jSliderStatisticDay.getValue() + 1) * 10));
    if (prevTimeDayIndex == jSliderStatisticDay.getValue()) {
        return;
    }
    prevTimeDayIndex = jSliderStatisticDay.getValue();
    if (null != graphMakerDayStatistic && jSliderStatisticDay.isShowing()) {
        updateIncomeExpenseDayStatisticGraph();
    }
}//GEN-LAST:event_jSliderStatisticDayStateChanged

private void jSliderStatisticWeekStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderStatisticWeekStateChanged
    if (!jSliderStatisticWeek.isShowing())
        return;
    jLabelStatisticWeek.setText("" + (jSliderStatisticWeek.getMaximum() - 
            jSliderStatisticWeek.getValue() + 1));
    if (prevTimeWeekIndex == jSliderStatisticWeek.getValue())
        return;
    prevTimeWeekIndex = jSliderStatisticWeek.getValue();
    if (null != graphMakerWeekStatistic && jSliderStatisticWeek.isShowing()) {
        updateIncomeExpenseWeekStatisticGraph();    
    }
}//GEN-LAST:event_jSliderStatisticWeekStateChanged

private void jSliderStatisticMonthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderStatisticMonthStateChanged
    if (!jSliderStatisticMonth.isShowing())
        return;
    jLabelStatisticMonth.setText("" + (jSliderStatisticMonth.getMaximum() - 
            jSliderStatisticMonth.getValue() + 1));
    if (prevTimeMonthIndex == jSliderStatisticMonth.getValue())
        return;
    prevTimeMonthIndex = jSliderStatisticMonth.getValue();
    if (null != graphMakerMonthStatistic && jSliderStatisticMonth.isShowing()) {
        updateIncomeExpenseMonthStatisticGraph();
    }
}//GEN-LAST:event_jSliderStatisticMonthStateChanged

private void jSliderStatisticYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderStatisticYearStateChanged
    if (!jSliderStatisticYear.isShowing())
        return;
    jLabelStatisticYear.setText("" + (jSliderStatisticYear.getMaximum() - 
            jSliderStatisticYear.getValue() + 1));
    if (prevTimeYearIndex == jSliderStatisticYear.getValue())
        return;
    prevTimeYearIndex = jSliderStatisticYear.getValue();
    if (null != graphMakerYearStatistic && jSliderStatisticYear.isShowing()) {
        updateIncomeExpenseYearStatisticGraph();
    }
}//GEN-LAST:event_jSliderStatisticYearStateChanged

private void jComboBoxStatisticDiagramTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxStatisticDiagramTypeActionPerformed
    if (!jComboBoxStatisticDiagramType.isShowing())
        return;
    indexGraphType = jComboBoxStatisticDiagramType.getSelectedIndex();
    String selectVal = (String) jComboBoxStatisticTimeTick.getSelectedItem();
    if (selectVal.equals(mybudget.option.Options.I18N_BUNDLE.getString("Day"))) {
        updateIncomeExpenseDayStatisticGraph();
    } else if (selectVal.equals(mybudget.option.Options.I18N_BUNDLE.getString("Week"))) {
        updateIncomeExpenseWeekStatisticGraph();
    } else if (selectVal.equals(mybudget.option.Options.I18N_BUNDLE.getString("Month"))) {
        updateIncomeExpenseMonthStatisticGraph();
    } else if (selectVal.equals(mybudget.option.Options.I18N_BUNDLE.getString("Year"))) {
        updateIncomeExpenseYearStatisticGraph();
    }
}//GEN-LAST:event_jComboBoxStatisticDiagramTypeActionPerformed

private void jMenuItemOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOptionsActionPerformed
    if (null == dialogOptions) {
        dialogOptions = new DialogOptions(this);
        for (int i = 0; i < Options.PATTERN_DATE_FORMATS.length; i++) {
            dialogOptions.getDatePatternComboBox().addItem(Options.PATTERN_DATE_FORMATS[i]);
        }
        indexSelectLanguage = Options.I18N_BUNDLE.getLocale().getLanguage().equals("ru") ? 0 : 1;
        dialogOptions.getI18NComboBox().setSelectedIndex(indexSelectLanguage);
        dialogOptions.getDatePatternComboBox().setSelectedItem(Options.DATE_FORMAT.toPattern());
        indexSelectDatePattern = dialogOptions.getDatePatternComboBox().getSelectedIndex();
    } else {
        dialogOptions.getI18NComboBox().setSelectedIndex(indexSelectLanguage);
        if (-1 != indexSelectDatePattern)
            dialogOptions.getDatePatternComboBox().setSelectedIndex(indexSelectDatePattern);
    }
    dialogOptions.setColorBalance(colorBalance);
    dialogOptions.setColorExpense(colorExpense);
    dialogOptions.setColorIncome(colorIncome);
    dialogOptions.setVisible(true);
    if (dialogOptions.getResult()) {
        Options.DATE_FORMAT.applyPattern((String) dialogOptions.getDatePatternComboBox().getSelectedItem());        
        colorIncome = dialogOptions.getColorIncome();
        colorExpense = dialogOptions.getColorExpense();
        colorBalance = dialogOptions.getColorBalance();
        indexSelectLanguage = dialogOptions.getI18NComboBox().getSelectedIndex();
        indexSelectDatePattern = dialogOptions.getDatePatternComboBox().getSelectedIndex();
        initLabelColor();
        graphMakerGeneral.setColorIncome(colorIncome);
        graphMakerGeneral.setColorExpense(colorExpense);
        if (null != graphMakerDayStatistic) {
            graphMakerDayStatistic.setColorIncome(colorIncome);
            graphMakerDayStatistic.setColorExpense(colorExpense);
        }
        if (null != graphMakerWeekStatistic) {
            graphMakerWeekStatistic.setColorIncome(colorIncome);
            graphMakerWeekStatistic.setColorExpense(colorExpense);
        }
        if (null != graphMakerMonthStatistic) {
            graphMakerMonthStatistic.setColorIncome(colorIncome);
            graphMakerMonthStatistic.setColorExpense(colorExpense);
        }
        if (null != graphMakerBalance) {
            graphMakerBalance.setColorBalance(colorBalance);
        }
        repaint();
    }
}//GEN-LAST:event_jMenuItemOptionsActionPerformed

private void jTabbedPaneStatisticMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPaneStatisticMouseClicked
    jButtonToolBarStatisticActionPerformed(null);
}//GEN-LAST:event_jTabbedPaneStatisticMouseClicked

private void jSliderBalanceStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderBalanceStateChanged
    if (!jSliderBalance.isShowing()) {
        return;
    }
    changeTextBalanceSlieder();
    if (prevTimeBalanceIndex == jSliderBalance.getValue()) {
        return;
    }
    prevTimeBalanceIndex = jSliderBalance.getValue();
    if (null != graphMakerBalance) {
        updateBalanceStatisticGraph();
    }
}//GEN-LAST:event_jSliderBalanceStateChanged

private void jComboBoxMonthAverageBalanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMonthAverageBalanceActionPerformed
    
    int month = Integer.parseInt(jComboBoxMonthAverageBalance.getSelectedItem().toString());
    double expense = getAverageValue(month, Transaction.TYPE_EXPENSE);
    double income = getAverageValue(month, Transaction.TYPE_INCOME);
    jTextFieldMonthAverageBalance.setText(Options.toCurrency(income - expense));    
    
}//GEN-LAST:event_jComboBoxMonthAverageBalanceActionPerformed

private void jRadioButtonIncomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonIncomeActionPerformed
    
    if (null != tableSummary && null != graphMakerSummary) {
        tableSummary.setIncomeSum(true);
        tableSummary.updateData();
        jScrollPaneSum.setViewportView(tableSummary);
        graphMakerSummary.setGraphData(tableSummary.getData());
        graphMakerSummary.updateUI();
    }
    
}//GEN-LAST:event_jRadioButtonIncomeActionPerformed

private void jRadioButtonExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonExpenseActionPerformed
    
    if (null != tableSummary && null != graphMakerSummary) {
        tableSummary.setIncomeSum(false);
        tableSummary.updateData();
        jScrollPaneSum.setViewportView(tableSummary);
        graphMakerSummary.setGraphData(tableSummary.getData());
        graphMakerSummary.updateUI();
    }
    
}//GEN-LAST:event_jRadioButtonExpenseActionPerformed

private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
    
    if (jPanelSumGraph.isShowing()) {
        updateSummaryStatistics();
    }
    
}//GEN-LAST:event_formWindowActivated

private void jComboBoxSumPeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSumPeriodActionPerformed

    if (!jPanelSumGraph.isShowing()) {
        return;
    }
    Object obj = jComboBoxSumPeriod.getSelectedItem();
    if (obj instanceof ComboBoxItemTime) {
        ComboBoxItemTime tmpTime = (ComboBoxItemTime) obj;
        jCCFieldFrom.setDate(tmpTime.getStartDate());
        jCCFieldTo.setDate(tmpTime.getFinishDate());
        updateSummaryStatistics();
    }
    if (0 < jComboBoxSumPeriod.getItemCount()) {
        jComboBoxSumPeriod.setSelectedIndex(0);
    }

}//GEN-LAST:event_jComboBoxSumPeriodActionPerformed

    private void jMenuItemExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportActionPerformed

        jExportFileChooser.setSelectedFile(new File("export.csv"));
        int res = jExportFileChooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = jExportFileChooser.getSelectedFile();
            if (file != null) {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(file));
                    List<Transaction> list = transactionManager.getListTransactionWithTags();
                    for (Transaction t : list) {
                        double value = t.getType() == Transaction.TYPE_INCOME
                                ? t.getValue()
                                : -t.getValue();
                        String desc = t.getDescription().replaceAll(";", "");
                        String tags = "";
                        for (Object g : t.getTags()) {
                            tags += String.format("%s;", ((Tag) g).getName());
                        }
                        writer.write(String.format(
                            "%tF;%s;%f;%s\n",
                            t.getDate(), desc, value, tags));
                    }
                    JOptionPane.showMessageDialog(this, "Export Complete.", "Export", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex, "Fatal Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(this, ex, "Fatal Error", JOptionPane.ERROR_MESSAGE);
                            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }


    }//GEN-LAST:event_jMenuItemExportActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
    // <editor-fold defaultstate="collapsed" desc="Gui Variables">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupIncome;
    private javax.swing.ButtonGroup buttonGroupStatistic;
    private javax.swing.ButtonGroup buttonGroupSummary;
    private javax.swing.JButton jButtonExpenseDelete;
    private javax.swing.JButton jButtonExpenseEdit;
    private javax.swing.JButton jButtonExpenseNew;
    private javax.swing.JButton jButtonIncomeDelete;
    private javax.swing.JButton jButtonIncomeEdit;
    private javax.swing.JButton jButtonIncomeNew;
    private javax.swing.JButton jButtonNewExpressExpense;
    private javax.swing.JButton jButtonNewExpressIncome;
    private javax.swing.JButton jButtonTagListDelete;
    private javax.swing.JButton jButtonTagListEdit;
    private javax.swing.JButton jButtonTagListNew;
    private javax.swing.JButton jButtonToolBarExpense;
    private javax.swing.JButton jButtonToolBarGeneral;
    private javax.swing.JButton jButtonToolBarIncome;
    private javax.swing.JButton jButtonToolBarStatistic;
    private com.artcodesys.jcc.JCCField jCCFieldFrom;
    private com.artcodesys.jcc.JCCField jCCFieldTo;
    private javax.swing.JComboBox jComboBoxExpenseAdditionMovement;
    private javax.swing.JComboBox jComboBoxExpensePeriod;
    private javax.swing.JComboBox jComboBoxIncomeAdditionMovement;
    private javax.swing.JComboBox jComboBoxIncomePeriod;
    private javax.swing.JComboBox jComboBoxMonthAverageBalance;
    private javax.swing.JComboBox jComboBoxMonthAverageExpense;
    private javax.swing.JComboBox jComboBoxMonthAverageIncome;
    private javax.swing.JComboBox jComboBoxStatisticDiagramType;
    private javax.swing.JComboBox jComboBoxStatisticTimeTick;
    private javax.swing.JComboBox jComboBoxSumPeriod;
    private javax.swing.JDialog jDialogTagsList;
    private javax.swing.JFileChooser jExportFileChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelExpenseSelectedSum;
    private javax.swing.JLabel jLabelExpenseSelectedSumLabel;
    private javax.swing.JLabel jLabelExpenseSum;
    private javax.swing.JLabel jLabelExpenseSumLabel;
    private javax.swing.JLabel jLabelIncomeSelectedSum;
    private javax.swing.JLabel jLabelIncomeSelectedSumLabel;
    private javax.swing.JLabel jLabelIncomeSum;
    private javax.swing.JLabel jLabelIncomeSumLabel;
    private javax.swing.JLabel jLabelSliderBalance;
    private javax.swing.JLabel jLabelSliderTime;
    private javax.swing.JLabel jLabelSliderTime1;
    private javax.swing.JLabel jLabelStatisticDay;
    private javax.swing.JLabel jLabelStatisticMonth;
    private javax.swing.JLabel jLabelStatisticWeek;
    private javax.swing.JLabel jLabelStatisticYear;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuData;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemExpense;
    private javax.swing.JMenuItem jMenuItemExport;
    private javax.swing.JMenuItem jMenuItemGeneral;
    private javax.swing.JMenuItem jMenuItemHelpContents;
    private javax.swing.JMenuItem jMenuItemIncome;
    private javax.swing.JMenuItem jMenuItemNewExpense;
    private javax.swing.JMenuItem jMenuItemNewIncome;
    private javax.swing.JMenuItem jMenuItemOptions;
    private javax.swing.JMenuItem jMenuItemStatistic;
    private javax.swing.JMenuItem jMenuItemTagsList;
    private javax.swing.JMenu jMenuNavigate;
    private javax.swing.JMenu jMenuTools;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelDateInterval;
    private javax.swing.JPanel jPanelExpense;
    private javax.swing.JPanel jPanelExpenseBottom;
    private javax.swing.JPanel jPanelExpenseCenter;
    private javax.swing.JPanel jPanelExpenseTop;
    private javax.swing.JPanel jPanelGeneral;
    private javax.swing.JPanel jPanelGeneral1;
    private javax.swing.JPanel jPanelGeneral2;
    private javax.swing.JPanel jPanelIncome;
    private javax.swing.JPanel jPanelIncomeBottom;
    private javax.swing.JPanel jPanelIncomeCenter;
    private javax.swing.JPanel jPanelIncomeTop;
    private javax.swing.JPanel jPanelMainBottom;
    private javax.swing.JPanel jPanelMainCenter;
    private javax.swing.JPanel jPanelSGraphDay;
    private javax.swing.JPanel jPanelSGraphMonth;
    private javax.swing.JPanel jPanelSGraphWeek;
    private javax.swing.JPanel jPanelSGraphYear;
    private javax.swing.JPanel jPanelStatistic;
    private javax.swing.JPanel jPanelStatisticBalance;
    private javax.swing.JPanel jPanelStatisticBalanceGraph;
    private javax.swing.JPanel jPanelStatisticGraph;
    private javax.swing.JPanel jPanelStatisticGraphParam;
    private javax.swing.JPanel jPanelStatisticIncomeExpense;
    private javax.swing.JPanel jPanelStatisticSliderDay;
    private javax.swing.JPanel jPanelStatisticSliderMonth;
    private javax.swing.JPanel jPanelStatisticSliderWeek;
    private javax.swing.JPanel jPanelStatisticSliderYear;
    private javax.swing.JPanel jPanelStatisticSliders;
    private javax.swing.JPanel jPanelStatisticSummary;
    private javax.swing.JPanel jPanelSumGraph;
    private javax.swing.JPanel jPanelTagsListCenter;
    private javax.swing.JPanel jPanelTagsListTop;
    private javax.swing.JRadioButton jRadioButtonExpense;
    private javax.swing.JRadioButton jRadioButtonIncome;
    private javax.swing.JScrollPane jScrollPaneExpense;
    private javax.swing.JScrollPane jScrollPaneIncome;
    private javax.swing.JScrollPane jScrollPaneSum;
    private javax.swing.JScrollPane jScrollPaneTagsList;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSlider jSliderBalance;
    private javax.swing.JSlider jSliderStatisticDay;
    private javax.swing.JSlider jSliderStatisticMonth;
    private javax.swing.JSlider jSliderStatisticWeek;
    private javax.swing.JSlider jSliderStatisticYear;
    private javax.swing.JSlider jSliderTime;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPaneExpense;
    private javax.swing.JSplitPane jSplitPaneIncome;
    private javax.swing.JTabbedPane jTabbedPaneStatistic;
    private javax.swing.JTextField jTextFieldBalance;
    private javax.swing.JTextField jTextFieldCurrentMonthBalance;
    private javax.swing.JTextField jTextFieldCurrentMonthExpense;
    private javax.swing.JTextField jTextFieldCurrentMonthIncome;
    private javax.swing.JTextField jTextFieldExpenseTotal;
    private javax.swing.JTextField jTextFieldIncomeTotal;
    private javax.swing.JTextField jTextFieldLastMonthBalance;
    private javax.swing.JTextField jTextFieldLastMonthExpense;
    private javax.swing.JTextField jTextFieldLastMonthIncome;
    private javax.swing.JTextField jTextFieldMonthAverageBalance;
    private javax.swing.JTextField jTextFieldMonthAverageExpense;
    private javax.swing.JTextField jTextFieldMonthAverageIncome;
    private javax.swing.JToolBar jToolBar;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
}
