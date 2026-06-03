
public class Utilisateur {

    private int id;
    private String username;
    private String password;

    private boolean isConnected;
    private String sessionToken;

    private boolean isDeleted;

    public Utilisateur(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void delete() {
        this.isDeleted = true;
    }

    @Override
    public String toString() {
        return username;
    }
}