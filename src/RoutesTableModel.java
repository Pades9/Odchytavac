import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Created by patrikdendis on 27.4.17.
 */
public class RoutesTableModel extends AbstractTableModel {

    private ArrayList<Route> routes;
    private Object columnNames[] = { "Odkiaľ", "Kam", "Dátum", "Počet volných miest", "Cena od" };

    public RoutesTableModel() {
        routes = new ArrayList<Route>();
    }

    public boolean isCellEditable(int row, int column){
        return false;
    }

    public int getRowCount() {
        return routes.size();
    }

    public String getColumnName(int columnIndex) {
        return (String)columnNames[columnIndex];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        Route route = routes.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return ConnectionManager.getStationByID(route.getStationFrom())+" "+route.getDeparture();
            case 1:
                return ConnectionManager.getStationByID(route.getStationTo())+" "+route.getArrival();
            case 2:
                return route.getDate();
            case 3:
                return route.getSeats();
            case 4:
                return route.getPrice()+" "+route.getCurrency();
            default:
                break;
        }
        return null;
    }

    public Route getRouteByIndex(int rowIndex) {
        return routes.get(rowIndex);
    }

    public boolean addRoute(Route paRoute) {
        return routes.add(paRoute);
    }

    public void deleteData() {
        routes.clear();
    }

    public void removeRow(Integer rowIndex) {
        System.out.println("Teraz");
    }

}
