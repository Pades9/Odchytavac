/**
 * Created by patrikdendis on 8.5.17.
 */
public class ComboItem {

    private String id;
    private String value;

    public ComboItem(String id, String value) {
        this.value = value;
        this.id = id;
    }

    public String getValue() {
        return this.value;
    }

    public String getID() {
        return this.id;
    }

    @Override
    public String toString() {
        return value;
    }
}