package mybudget.option;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import mybudget.gui.MainWindow;

/**
 * Options 01.05.2008 (19:51:26)
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class Options implements PrefValue {
     
    public final static String PATTERN_DATE_FORMAT_1 = "dd.MM.yy";
    public final static String PATTERN_DATE_FORMAT_2 = "dd.MM.yyyy";
    public final static String PATTERN_DATE_FORMAT_3 = "yy.MM.dd";
    public final static String PATTERN_DATE_FORMAT_4 = "yyyy.MM.dd";
    
    public final static String[] PATTERN_DATE_FORMATS = {
        PATTERN_DATE_FORMAT_1, PATTERN_DATE_FORMAT_2, 
        PATTERN_DATE_FORMAT_3, PATTERN_DATE_FORMAT_4
    };

    public final static long MLS_IN_DAY = 1000 * 3600 * 24;
    
    public final static GregorianCalendar CALENDAR = new GregorianCalendar();
    
    public final static ResourceBundle I18N_BUNDLE;
    
    public final static Preferences PREF;
    
    static {
        PREF = Preferences.userNodeForPackage(MainWindow.class);
        String i18n = PREF.get(PrefValue.KEY_I18N, PrefValue.VAL_I18N);
        if (i18n.equals("")) {
            I18N_BUNDLE = ResourceBundle.getBundle("mybudget/option/i18n");
        } else {
            I18N_BUNDLE = ResourceBundle.getBundle("mybudget/option/i18n", new Locale(i18n));
        }
    }    

    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(PREF.get(KEY_DATE_FORMAT, 
            PATTERN_DATE_FORMAT_1));

    public final static int[] HEADER_SIZE_TRANSACTION = new int[5];
    
    static {
        HEADER_SIZE_TRANSACTION[0] = PREF.getInt(KEY_HEADER_WIDTH_0, VAL_HEADER_WIDTH_0);
        HEADER_SIZE_TRANSACTION[1] = PREF.getInt(KEY_HEADER_WIDTH_1, VAL_HEADER_WIDTH_1);
        HEADER_SIZE_TRANSACTION[2] = PREF.getInt(KEY_HEADER_WIDTH_2, VAL_HEADER_WIDTH_2);
        HEADER_SIZE_TRANSACTION[3] = PREF.getInt(KEY_HEADER_WIDTH_3, VAL_HEADER_WIDTH_3);
        HEADER_SIZE_TRANSACTION[4] = PREF.getInt(KEY_HEADER_WIDTH_4, VAL_HEADER_WIDTH_4);
    }
    
    static void setDateFormat(String format) {
        Options.DATE_FORMAT.applyPattern(format);
    }
    
    public static String toCurrency(double val) {
        String str = Double.toString(val);
        if (9999999 > val) {
            try {
                String[] arr = str.split("\\.");
                if (2 == arr.length) {
                    if (1 == arr[1].length()) {
                        str += "0";
                    } else if (1 < arr[1].length()) {
                        str = arr[0] + "." + arr[1].substring(0, 2);
                    }                
                }
            } catch(Exception ex) { }
        }
        return str;
    }

}
