/**
 * Created by patrikdendis on 27.4.17.
 */
public class User {
    private int id;
    private String token;
    private int loaded;

    public User(int id, String token, int loaded) {
        this.id = id;
        this.token = token;
        this.loaded = loaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLoaded() {
        return loaded;
    }

    public void setLoaded(int loaded) {
        this.loaded = loaded;
    }
}
