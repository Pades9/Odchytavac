import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by patrikdendis on 27.4.17.
 */


public class SearchView extends GuiHelper implements ActionListener {

    private JComboBox stationFrom;
    private JComboBox stationTo;
    private JComboBox currency;
    private JComboBox tariff;
    private MyTextField dateField;
    private RoutesView previousView;
    private Connection con;
    private final String[] tarifsValue = {"REGULAR", "CZECH_STUDENT_PASS_26", "CZECH_STUDENT_PASS_15", "ISIC", "CHILD", "SENIOR", "SENIOR_70", "YOUTH", "DISABLED", "DISABLED_ATTENDANCE", "EURO26"};
    private final String[] tarifsText = {"Dospelý", "Žiacky preukaz <26", "Žiacky preukaz <15", "ISIC", "Dieťa <15", "Senior >60", "Senior >70", "Mládežník <26", "ŤZP (ŤZP/S)", "Sprievodca ŤZP/S", "Euro 26/Alive"};
    private final String[] currencyValue = {"EUR", "CZK"};

    public SearchView(String paName, RoutesView view) {
        super(paName);
        this.previousView = view;
        con = ConnectionManager.getConnection();
    }

    public void createView(RoutesView routesView) {
        final JPanel controlPanel = new JPanel(null);
        controlPanel.setPreferredSize(new Dimension(300, 350));

        JLabel labelFrom = new JLabel("Odkial cestujete:", SwingConstants.CENTER);
        labelFrom.setBounds(20,20,260,25);

        this.stationFrom = new JComboBox();
        this.stationFrom.setBounds(20,45,260,25);
        this.stationFrom.setPrototypeDisplayValue("Udine Autostaz. Viale Europa Unita");

        JLabel labelTo = new JLabel("Odkial cestujete:", SwingConstants.CENTER);
        labelTo.setBounds(20,70,260,25);

        this.stationTo = new JComboBox();
        this.stationTo.setBounds(20,95,260,25);
        this.stationTo.setPrototypeDisplayValue("Udine Autostaz. Viale Europa Unita");

        this.insertStations();

        JLabel labelCurrency = new JLabel("Mena:", SwingConstants.CENTER);
        labelCurrency.setBounds(20,120,130,25);

        this.currency = new JComboBox(currencyValue);
        this.currency.setBounds(20,145,130,25);
        this.currency.setPrototypeDisplayValue("Udine Autostaz. Viale Europa Unita");

        JLabel labelTariff = new JLabel("Tarifa:", SwingConstants.CENTER);
        labelTariff.setBounds(150,120,130,25);

        this.tariff = new JComboBox(tarifsText);
        this.tariff.setBounds(150,145,130,25);
        this.tariff.setPrototypeDisplayValue("Udine Autostaz. Viale Europa Unita");

        JLabel labelDate = new JLabel("Dátum odjazdu:", SwingConstants.CENTER);
        labelDate.setBounds(20,175,260,25);

        this.dateField = new MyTextField();
        this.dateField.setPlaceholder("Formát yyyyMMdd");
        this.dateField.setBounds(20,200,260,25);

        JButton submitButton = new JButton("Hladať");
        submitButton.setBounds(100, 250, 100, 40);
        submitButton.addActionListener((ActionListener) this);

        super.closeFrame();
        controlPanel.add(labelFrom);
        controlPanel.add(stationFrom);
        controlPanel.add(labelTo);
        controlPanel.add(stationTo);
        controlPanel.add(labelCurrency);
        controlPanel.add(currency);
        controlPanel.add(labelTariff);
        controlPanel.add(tariff);
        controlPanel.add(labelDate);
        controlPanel.add(dateField);
        controlPanel.add(submitButton);

        super.addJPanel(controlPanel);
        super.showGUI();
    }

    public void insertStations() {
        con = ConnectionManager.getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM station ORDER BY priority DESC");
            while (rs.next()) {
                this.stationFrom.addItem(new ComboItem(rs.getString("id"), rs.getString("title")));
                this.stationTo.addItem(new ComboItem(rs.getString("id"), rs.getString("title")));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        } finally{
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String stationFromID = ((ComboItem)stationFrom.getSelectedItem()).getID();
        String stationToID = ((ComboItem)stationTo.getSelectedItem()).getID();
        String tarrifID = tarifsValue[tariff.getSelectedIndex()];
        String currencyName = currencyValue[currency.getSelectedIndex()];
        String dateString = dateField.getText();

        previousView.loadRoutes(stationFromID,stationToID,tarrifID,currencyName,dateString);

        super.closeFrame();
    }

}
