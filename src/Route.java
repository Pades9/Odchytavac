/**
 * Created by patrikdendis on 8.5.17.
 */
public class Route {

    private String stationFrom;
    private String stationTo;
    private String departure;
    private String arrival;
    private String seats;
    private String tariff;
    private String price;
    private String type;
    private String currency;
    private String date;
    private boolean saved;

    public Route() {

    }

    public String getStationFrom() {
        return stationFrom;
    }

    public String getStationTo() {
        return stationTo;
    }

    public String getDeparture() {
        return departure;
    }

    public String getArrival() {
        return arrival;
    }

    public String getSeats() {
        return seats;
    }

    public String getTariff() {
        return tariff;
    }

    public String getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public boolean isSaved() {
        return saved;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDate() {
        return date;
    }

    public void setStationFrom(String stationFrom) {
        this.stationFrom = stationFrom;
    }

    public void setStationTo(String stationTo) {
        this.stationTo = stationTo;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Route{" +
                "stationFrom='" + stationFrom + '\'' +
                ", stationTo='" + stationTo + '\'' +
                ", departure='" + departure + '\'' +
                ", arrival='" + arrival + '\'' +
                ", seats='" + seats + '\'' +
                ", tariff='" + tariff + '\'' +
                ", price='" + price + '\'' +
                ", type='" + type + '\'' +
                ", currency='" + currency + '\'' +
                ", date='" + date + '\'' +
                ", saved=" + saved +
                '}';
    }
}
