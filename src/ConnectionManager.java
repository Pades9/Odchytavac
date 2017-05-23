import java.sql.*;

/**
 * Created by patrikdendis on 27.4.17.
 */
public class ConnectionManager {

    private static String url = "jdbc:sqlite:odchytavac.sqlite";
    private static String driverName = "org.sqlite.JDBC";
    private static Connection con;

    public static Connection getConnection() {
        try {
            Class.forName(driverName);
            try {
                con = DriverManager.getConnection(url);
            } catch (SQLException ex) {
                // log an exception. fro example:
                System.out.println("Failed to create the database connection.");
            }
        } catch (ClassNotFoundException ex) {
            // log an exception. for example:
            System.out.println("Driver not found.");
        }
        return con;
    }

    public static User getUser() {
        con = ConnectionManager.getConnection();
        try  {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM user WHERE id = 1;" );
            User user = new User(rs.getInt("id"),rs.getString("token"),rs.getInt("loaded"));
            stmt.close();
            return user;
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

        return null;
    }

    public static String getStationByID(String stationID) {
        con = ConnectionManager.getConnection();
        try  {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM station WHERE id = '"+stationID+"';" );
            if (!rs.isBeforeFirst() ) {
                return null;
            } else {
                return rs.getString("title");
            }
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

        return null;
    }
}
