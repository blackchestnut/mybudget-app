package mybudget;

import javax.swing.JOptionPane;
import mybudget.gui.MainWindow;
import mybudget.gui.splash.SplashForm;

/**
 *
 * @author Alexander Kalinichev / alexander.kalinichev@gmail.com
 */
public class Main {
    
    public static SplashForm splashForm = new SplashForm();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String vers = System.getProperty("java.version");
        if (vers.compareTo("1.5.0") < 0) {
            JOptionPane.showMessageDialog(null, "myBudget must be run with 1.5.0 or higher version VM.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }          
        splashForm.setVisible(true);
        MainWindow.main(args);
    }
    
}
