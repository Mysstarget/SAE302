package server;

public class ConnectUser {

    public static String getUsername(String request) {

        String[] parts = request.split(",");

        if (parts.length < 3) {
            return null;
        }

        if (!parts[0].equals("Connect")) {
            return null;
        }

        return parts[1];
    }
}