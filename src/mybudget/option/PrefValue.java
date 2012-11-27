package mybudget.option;

/**
 * PrefValue 10.06.2008 (15:40:44)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public interface  PrefValue {
    
    String KEY_I18N = "key_i18n";
    String VAL_I18N = "";
    
    String KEY_GENERAL_SLIDER_TIME = "key_general_slider_time";
    int VAL_GENERAL_SLIDER_TIME = 3;
    String KEY_DAY_SLIDER_TIME = "key_day_slider_time";
    int VAL_DAY_SLIDER_TIME = 15;
    String KEY_WEEK_SLIDER_TIME = "key_week_slider_time";
    int VAL_WEEK_SLIDER_TIME = 50;
    String KEY_MONTH_SLIDER_TIME = "key_month_slider_time";
    int VAL_MONTH_SLIDER_TIME = 24;
    String KEY_YEAR_SLIDER_TIME = "key_year_slider_time";
    int VAL_YEAR_SLIDER_TIME = 2;
    String KEY_BALANCE_SLIDER_TIME = "key_balance_slider_time";
    int VAL_BALANCE_SLIDER_TIME = 20;

    String KEY_INDEX_AVERAGE_INCOME = "key_index_average_income";
    int VAL_INDEX_AVERAGE_INCOME = 0;
    String KEY_INDEX_AVERAGE_EXPENSE = "key_index_average_expense";
    int VAL_INDEX_AVERAGE_BALANCE = 0;
    String KEY_INDEX_AVERAGE_BALANCE = "key_index_average_balance";
    int VAL_INDEX_AVERAGE_EXPENSE = 0;    
    
    String KEY_INDEX_PERIOD_INCOME = "key_index_period_income";
    int VAL_INDEX_PERIOD_INCOME = 9;
    String KEY_INDEX_PERIOD_EXPENSE = "key_index_period_expense";
    int VAL_INDEX_PERIOD_EXPENSE = 9;

    String KEY_INDEX_GRAPH_TYPE = "key_index_graph_type";
    int VAL_INDEX_GRAPH_TYPE = 0;
    String KEY_INDEX_GRAPH_TICK_TIME = "key_index_graph_tick_time";
    int VAL_INDEX_GRAPH_TICK_TIME = 0;

    String KEY_DATE_FORMAT = "key_date_format";
    
    String KEY_INCOME_R = "key_income_r";
    int VAL_INCOME_R = 0;
    String KEY_INCOME_G = "key_income_g";
    int VAL_INCOME_G = 200;
    String KEY_INCOME_B = "key_income_b";
    int VAL_INCOME_B = 130;
    
    String KEY_EXPENSE_R = "key_expense_r";
    int VAL_EXPENSE_R = 255;
    String KEY_EXPENSE_G = "key_expense_g";
    int VAL_EXPENSE_G = 150;
    String KEY_EXPENSE_B = "key_expense_b";
    int VAL_EXPENSE_B = 0;

    String KEY_BALANCE_R = "key_balance_r";
    int VAL_BALANCE_R = 0;
    String KEY_BALANCE_G = "key_balance_g";
    int VAL_BALANCE_G = 80;
    String KEY_BALANCE_B = "key_balance_b";
    int VAL_BALANCE_B = 200;
    
    String KEY_HEADER_WIDTH_0 = "key_header_width_0";
    int VAL_HEADER_WIDTH_0 = 0;
    String KEY_HEADER_WIDTH_1 = "key_header_width_1";
    int VAL_HEADER_WIDTH_1 = 100;
    String KEY_HEADER_WIDTH_2 = "key_header_width_2";
    int VAL_HEADER_WIDTH_2 = 80;
    String KEY_HEADER_WIDTH_3 = "key_header_width_3";
    int VAL_HEADER_WIDTH_3 = 80;
    String KEY_HEADER_WIDTH_4 = "key_header_width_4";
    int VAL_HEADER_WIDTH_4 = 80;
    
    String KEY_MW_X = "key_mw_x";
    int VAL_MW_X = 100;    
    String KEY_MW_Y = "key_mw_y";
    int VAL_MW_Y = 50;
    
    String KEY_MW_WIDTH = "key_mw_width";
    int VAL_MW_WIDTH = 900;    
    String KEY_MW_HEIGHT = "key_mw_height";
    int VAL_MW_HEIGHT = 600;
    
    String KEY_DTL_X = "key_dtl_x";
    int VAL_DTL_X = 150;   
    String KEY_DTL_Y = "key_dtl_y";
    int VAL_DTL_Y = 100;
    
    String KEY_DTL_WIDTH = "key_dtl_width";
    int VAL_DTL_WIDTH = 600;    
    String KEY_DTL_HEIGHT = "key_dtl_height";
    int VAL_DTL_HEIGHT = 400;
    
    String KEY_DO_X = "key_do_x";
    int VAL_DO_X = 150;   
    String KEY_DO_Y = "key_do_y";
    int VAL_DO_Y = 100;

    String KEY_DTAG_X = "key_dtag_x";
    int VAL_DTAG_X = 200;   
    String KEY_DTAG_Y = "key_dtag_y";
    int VAL_DTAG_Y = 100;
    
    String KEY_DTAG_WIDTH = "key_dtag_width";
    int VAL_DTAG_WIDTH = 340;    
    String KEY_DTAG_HEIGHT = "key_dtag_height";
    int VAL_DTAG_HEIGHT = 100;
    
    String KEY_DTR_X = "key_dtr_x";
    int VAL_DTR_X = 200;   
    String KEY_DTR_Y = "key_dtr_y";
    int VAL_DTR_Y = 100;
    
    String KEY_DTR_WIDTH = "key_dtr_width";
    int VAL_DTR_WIDTH = 500;    
    String KEY_DTR_HEIGHT = "key_dtr_height";
    int VAL_DTR_HEIGHT = 400;

    // разделитель таблицы ярлыков на доходах и расходах
    String KEY_TAG_DEVISION_INCOME = "key_tag_def_income";
    int VAL_TAG_DEVISION_INCOME = 250;
    String KEY_TAG_DEVISION_EXPENSE = "key_tag_def_expense";
    int VAL_TAG_DEVISION_EXPENSE = 250;
}
