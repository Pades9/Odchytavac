import com.mb3364.http.AsyncHttpClient;
import com.mb3364.http.StringHttpResponseHandler;
import com.mb3364.http.SyncHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by patrikdendis on 27.4.17.
 */
public class RoutesView extends GuiHelper implements ActionListener {

    private JTable table;
    private RoutesTableModel routesTableModel;
    private Connection con;
    private User user;
    private JLabel titleLabel;
    private JButton deleteButton;
    private JButton backButton;
    private JButton refreshButton;

    public RoutesView(String paName) {
        super(paName);
        con = ConnectionManager.getConnection();
        user = ConnectionManager.getUser();
        System.out.println(user.getToken());
    }

    public void createView(Odchytavac odchytavac) {

        final JPanel controlPanel = new JPanel(null);
        controlPanel.setPreferredSize(new Dimension(700, 500));

        titleLabel = new JLabel("Uložené spojenia", JLabel.CENTER);
        titleLabel.setBounds(200,20,300,25);

        JButton addButton = new JButton("Hladať spojenie");
        addButton.addActionListener(this);
        addButton.setBounds(550,20,150,30);
        addButton.putClientProperty("id","search");

        routesTableModel = new RoutesTableModel();
        table = new JTable(routesTableModel);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBounds(0,70,700,370);

        JButton buyButton = new JButton("Kúpiť");
        buyButton.setBounds(220, 450, 100, 40);
        buyButton.addActionListener(this);
        buyButton.putClientProperty("id","buy");

        deleteButton = new JButton("Zmazať");
        deleteButton.setBounds(370, 450, 100, 40);
        deleteButton.addActionListener(this);
        deleteButton.putClientProperty("id","delete");

        backButton = new JButton("Spať");
        backButton.setBounds(0, 20, 80, 30);
        backButton.addActionListener(this);
        backButton.putClientProperty("id","back");
        backButton.setVisible(false);

        refreshButton = new JButton("Obnoviť");
        refreshButton.setBounds(0, 20, 80, 30);
        refreshButton.addActionListener(this);
        refreshButton.putClientProperty("id","refresh");

        controlPanel.add(titleLabel);
        controlPanel.add(addButton);
        controlPanel.add(scrollTable);
        controlPanel.add(deleteButton);
        controlPanel.add(buyButton);
        controlPanel.add(backButton);
        controlPanel.add(refreshButton);

        super.addJPanel(controlPanel);
        super.showGUI();

        this.loadSavedRoutes();
    }

    public void actionPerformed(ActionEvent e){
        JButton selectedButton = (JButton)e.getSource();
        if( selectedButton.getClientProperty("id").equals("search")) {

            SearchView searchView = new SearchView("Vyhladať cestu", this);
            searchView.createView(this);
        } else if (selectedButton.getClientProperty("id").equals("buy")) {
            buyRoute();
        } else if (selectedButton.getClientProperty("id").equals("delete")) {
            if(selectedButton.getText().equals("Uložiť")) {
                saveRoute();
            } else {
                deleteRoute();
            }
        } else if (selectedButton.getClientProperty("id").equals("back")) {
            deleteButton.setText("Zmazať");
            backButton.setVisible(false);
            refreshButton.setVisible(true);
            loadSavedRoutes();
        } else if (selectedButton.getClientProperty("id").equals("refresh")) {
            refreshRoutes();
        }
    }

    public void loadRoutes(String stationFrom, String stationTo, final String tariff, final String currency, final String dateField) {

        routesTableModel.deleteData();
        routesTableModel.fireTableDataChanged();
        titleLabel.setText("Načítavam...");
        deleteButton.setText("Uložiť");
        backButton.setVisible(true);
        refreshButton.setVisible(false);

        String url = "http://46.101.220.81:5000/api/routes/from/"+stationFrom+"/to/"+stationTo+"/tarif/"+tariff+"/date/"+dateField+"/currency/"+currency+"/user_token/"+user.getToken();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, null, new StringHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Map<String, java.util.List<String>> headers, String content) {
                JSONArray routes = new JSONArray(content);
                if(routes.length() > 0) {
                    titleLabel.setText("Počet nájdených spojení " + routes.length());

                    for (int i = 0; i < routes.length(); i++) {

                        JSONObject routeObject = routes.getJSONObject(i);
                        Route route = new Route();
                        route.setStationFrom(routeObject.getString("stationFrom"));
                        route.setStationTo(routeObject.getString("stationTo"));
                        route.setSeats(routeObject.getString("seats"));
                        route.setArrival(routeObject.getString("arrival"));
                        route.setDeparture(routeObject.getString("departure"));
                        route.setPrice(routeObject.getString("price"));
                        route.setTariff(tariff);
                        route.setDate(dateField);
                        route.setCurrency(currency);
                        routesTableModel.addRoute(route);
                    }

                    routesTableModel.fireTableDataChanged();
                } else {
                    titleLabel.setText("Neboli nájdené žiadne spojenia.");
                }
            }

            @Override
            public void onFailure(int statusCode, Map<String, java.util.List<String>> headers, String content) {
                System.out.println("Chyba pri hladani ciest.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println(throwable.getLocalizedMessage());
            }
        });
    }

    public void loadSavedRoutes() {
        routesTableModel.deleteData();
        routesTableModel.fireTableDataChanged();
        titleLabel.setText("Uložené spojenia");
        con = ConnectionManager.getConnection();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM user_routes WHERE dateS > datetime('now') ORDER BY dateS");
            while (rs.next()) {

                String[] dateString = rs.getString("dateS").split(" ");

                Route route = new Route();
                route.setStationFrom(rs.getString("stationFrom"));
                route.setStationTo(rs.getString("stationTo"));
                route.setSeats(rs.getString("seats"));
                route.setArrival(rs.getString("arrival"));
                route.setDeparture(rs.getString("departure"));
                route.setPrice(rs.getString("price"));
                route.setTariff(rs.getString("tariff"));
                route.setCurrency(rs.getString("currency"));
                route.setDate(dateString[0]);

                routesTableModel.addRoute(route);
            }

            routesTableModel.fireTableDataChanged();

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

    public void saveRoute() {

        if(table.getSelectionModel().isSelectionEmpty()) {
            return;
        }

        Route selectedRoute = routesTableModel.getRouteByIndex(table.getSelectedRow());

        String dateStringFinish = null;
        try {
            String stringDateTime = selectedRoute.getDate()+" "+selectedRoute.getDeparture();
            DateFormat formatBefore = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.ENGLISH);
            Date date = formatBefore.parse(stringDateTime);

            DateFormat formatAfter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateStringFinish = formatAfter.format(date);
        } catch (ParseException e) {
            System.out.println(e.getLocalizedMessage());
        }

        String sql = "INSERT INTO user_routes (stationFrom, stationTo, tariff, dateS, currency, price, departure, arrival, seats) " +
                "VALUES ('"+selectedRoute.getStationFrom()+"','"+selectedRoute.getStationTo()+"','"+selectedRoute.getTariff()+"','"+dateStringFinish+"','"+selectedRoute.getCurrency()+"','"+selectedRoute.getPrice()+"','"+selectedRoute.getDeparture()+"','"+selectedRoute.getArrival()+"',"+selectedRoute.getSeats()+");";
        System.out.println(sql);
        con = ConnectionManager.getConnection();
        try  {
            Statement stmt = con.createStatement();
            stmt.execute(sql);
            stmt.close();
            JOptionPane.showMessageDialog(this, "Spojenie uložené.");
            con.commit();
        } catch (SQLException e) {
            System.out.println(e.getLocalizedMessage());
        } finally{
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public void deleteRoute() {

        if(table.getSelectionModel().isSelectionEmpty()) {
            return;
        }

        Route selectedRoute = routesTableModel.getRouteByIndex(table.getSelectedRow());

        String dateStringFinish = null;
        try {
            String stringDateTime = selectedRoute.getDate()+" "+selectedRoute.getDeparture();
            DateFormat formatBefore = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
            Date date = formatBefore.parse(stringDateTime);
            DateFormat formatAfter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateStringFinish = formatAfter.format(date);
        } catch (ParseException e) {
            System.out.println(e.getLocalizedMessage());
        }

        String sql = "DELETE FROM user_routes WHERE stationFrom = '"+selectedRoute.getStationFrom()+"' and stationTo = '"+selectedRoute.getStationTo()+"' and dateS = '"+dateStringFinish+"'";

        System.out.println(sql);

        con = ConnectionManager.getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            loadSavedRoutes();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally{
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public void buyRoute() {

        if(table.getSelectionModel().isSelectionEmpty()) {
            return;
        }

        Route selectedRoute = routesTableModel.getRouteByIndex(table.getSelectedRow());
        String dateString = selectedRoute.getDate().replace("-", "");
        String urlString = "http://jizdenky.studentagency.sk/m/Booking/from/"+selectedRoute.getStationFrom()+"/to/"+selectedRoute.getStationTo()+"/tarif/"+selectedRoute.getTariff()+"/departure/"+dateString+"/retdep/"+dateString+"/return/False/credit/True";
        try {
            java.awt.Desktop.getDesktop().browse(new URI(urlString));
        } catch (IOException e) {
            System.out.println("Open website problem");
        } catch (URISyntaxException e) {
            System.out.println("URI wrong format");
        }
    }

    public void refreshRoutes() {
        routesTableModel.deleteData();
        routesTableModel.fireTableDataChanged();
        titleLabel.setText("Obnovujem spojenia...");
        con = ConnectionManager.getConnection();
        try {
            Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery("SELECT * FROM user_routes WHERE dateS > datetime('now') ORDER BY dateS");
            while (rs.next()) {
                String[] dateArray = rs.getString("dateS").split(" ");
                String dateString = dateArray[0].replace("-", "");
                String timeString = dateArray[1].substring(0,5);
                String url = "http://46.101.220.81:5000/api/routeSpecific/from/"+rs.getString("stationFrom")+"/to/"+rs.getString("stationTo")+"/tarif/"+rs.getString("tariff")+"/date/"+dateString+"/time/"+timeString+"/currency/"+rs.getString("currency");
                SyncHttpClient client = new SyncHttpClient();
                client.get(url, null, new StringHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Map<String, java.util.List<String>> headers, String content) {
                        JSONObject routeObject = new JSONObject(content);

                        try {
                            Statement updateStatement = con.createStatement();
                            String sql = "UPDATE user_routes " +
                                    "SET seats = "+routeObject.getString("seats")+", price = '"+routeObject.getString("price")+"' WHERE id = "+rs.getInt("id");
                            System.out.println(sql);
                            updateStatement.executeUpdate(sql);
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                        } finally {
                            if (con != null) {
                                try {
                                    con.close();
                                } catch (SQLException e) {
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Map<String, java.util.List<String>> headers, String content) {
                        System.out.println("Chyba pri hladani ciest.");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getLocalizedMessage());
                    }
                });
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            loadSavedRoutes();
            titleLabel.setText("Uložené spojenia");
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }
}