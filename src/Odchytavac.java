import com.mb3364.http.AsyncHttpClient;
import com.mb3364.http.StringHttpResponseHandler;
import com.mb3364.http.SyncHttpClient;
import com.notification.NotificationFactory;
import com.notification.NotificationManager;
import com.notification.manager.SimpleManager;
import com.notification.types.TextNotification;
import com.theme.ThemePackagePresets;
import com.utils.Time;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by patrikdendis on 27.4.17.
 */
public class Odchytavac {

    private RoutesView routesView;
    private Connection con;

    public Odchytavac() throws SQLException {
        createDatabase();
        createUser();

        User user = ConnectionManager.getUser();
        if(user.getLoaded() != 1) {
            try {
                downloadStations();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        routesView = new RoutesView("Hladanie trasy");
        routesView.createView(this);
    }

    public void createDatabase() {

        String sqlStation = "CREATE TABLE IF NOT EXISTS station (\n"
                + "	id VARCHAR PRIMARY KEY,\n"
                + "	title VARCHAR,\n"
                + "	priority INTEGER,\n"
                + "	latitude VARCHAR,\n"
                + "	longitude VARCHAR,\n"
                + "	countryCode VARCHAR\n"
                + ");";

        String sqlUser = "CREATE TABLE IF NOT EXISTS user (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	token VARCHAR,\n"
                + "	loaded INTEGER\n"
                + ");";

        String sqlUserRoutes = "CREATE TABLE IF NOT EXISTS user_routes (\n"
                + "	id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "	stationFrom VARCHAR,\n"
                + "	stationTo VARCHAR,\n"
                + "	tariff VARCHAR,\n"
                + "	dateS DATETIME,\n"
                + "	currency VARCHAR,\n"
                + "	price VARCHAR,\n"
                + "	departure VARCHAR,\n"
                + "	arrival VARCHAR,\n"
                + "	last_checked DATETIME DEFAULT CURRENT_TIMESTAMP,\n"
                + "	seats INTEGER\n"
                + ");";

        con = ConnectionManager.getConnection();
        try  {
            Statement stmt = con.createStatement();
            stmt.execute(sqlStation);
            stmt.execute(sqlUser);
            stmt.execute(sqlUserRoutes);
            stmt.close();
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

    public void createUser() {

        if(ConnectionManager.getUser() == null) {
            String uuid = UUID.randomUUID().toString();
            String sql = "INSERT INTO user (id,token,loaded) " +
                    "VALUES (1,'"+uuid+"',0);";
            con = ConnectionManager.getConnection();
            try  {
                Statement stmt = con.createStatement();
                stmt.execute(sql);
                stmt.close();
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
    }

    public void downloadStations() throws SQLException {
        String url = "http://46.101.220.81:5000/api/stations";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, null, new StringHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Map<String, List<String>> headers, String content) {
                JSONArray stations = new JSONArray(content);
                try {
                    con = ConnectionManager.getConnection();
                    Statement stmt = con.createStatement();
                    for (int i = 0; i < stations.length(); i++) {
                        JSONObject station = stations.getJSONObject(i);
                        String sql = "INSERT INTO station (id,title,priority,latitude,longitude,countryCode) " +
                                "VALUES ("+station.getString("id")+",'"+station.getString("title")+"',"+station.get("priority")+",'"+station.getString("latitude")+"', '"+station.getString("longitude")+"', '"+station.getString("countryCode")+"');";
                        System.out.format("%d - %s\n",i,sql);
                        if(i != 228) {
                            stmt.addBatch(sql);
                        }
                    }

                    String sqlUpdate = "UPDATE user " + "SET loaded = 1 WHERE id = 1";
                    stmt.executeUpdate(sqlUpdate);
                    int[] updateCounts = stmt.executeBatch();
                    System.out.println(stations.length());
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

            @Override
            public void onFailure(int statusCode, Map<String, List<String>> headers, String content) {
                System.out.println("Chyba pri stahovani stanic.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println(throwable.getLocalizedMessage());
            }
        });
    }

    public static void main(String[] args) throws SQLException {
        new Odchytavac();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        CheckSeatsRunnable R1 = new CheckSeatsRunnable( "Check seats");
        executor.scheduleAtFixedRate(R1,0,30, TimeUnit.SECONDS);
    }
}

class CheckSeatsRunnable implements Runnable {

    private String threadName;
    private Connection con;

    CheckSeatsRunnable( String name) {
        threadName = name;
        System.out.println("Creating " +  threadName );
    }

    public void run() {
        System.out.println("Running " +  threadName );
        con = ConnectionManager.getConnection();
        try {
            Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery("SELECT * FROM user_routes WHERE dateS > datetime('now') ORDER BY last_checked LIMIT 1");
            if (rs.isBeforeFirst() ) {

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
                            if(!routeObject.getString("seats").equals(rs.getString("seats"))){
                                NotificationFactory factory = new NotificationFactory(ThemePackagePresets.cleanLight());
                                NotificationManager plain = new SimpleManager(NotificationFactory.Location.NORTHEAST);
                                TextNotification notification = factory.buildTextNotification("Zmenil sa počet miest","Aktualizujte si spojenia.");
                                notification.setCloseOnClick(true);
                                plain.addNotification(notification, Time.infinite());
                            }

                            Statement updateStatement = con.createStatement();
                            String sql = "UPDATE user_routes " + "SET seats = "+routeObject.getString("seats")+", last_checked = DATETIME('now') WHERE id = "+rs.getInt("id");
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
            e.printStackTrace();
        } finally{
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
        System.out.println("Thread " +  threadName + " exiting.");
    }

}
