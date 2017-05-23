import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by patrikdendis on 27.4.17.
 */

public class GuiHelper extends JFrame {

    private Container aPanel;
    private JFrame aFrame;

    // Create GUI and
    public GuiHelper(String paName) {
        this.createGUI(paName);
    }

    public void createGUI(String paName) {
        //Create and set up the window.
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        aFrame = new JFrame(paName);
//        aFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
            }
        });
        //Set up the content pane.
        aPanel = aFrame.getContentPane();
        //Display the window.
        aFrame.setResizable(false);
    }

    // show GUI window
    public void showGUI() {
        aFrame.pack();
        aFrame.setVisible(true);
    }

    public Container getaPanel() {
        return aPanel;
    }

    // add jPanel to window
    public void addJPanel(JPanel paControlPanel) {
        aPanel.add(paControlPanel, BorderLayout.NORTH);
    }

    public JFrame getaFrame() {
        return aFrame;
    }

    public void closeFrame() {
        aFrame.setVisible(false);
        aFrame.dispose();
    }
}
