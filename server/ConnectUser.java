package server;

public class ConnectUser {

    static String connecterUtilisateur(String[] parts) {
        // Format reçu : "Connect,User,password"

        if (parts.length != 3) {
            return "400,CONNECT,Requête invalide";
        }

        String user = parts[1];
        String mdp  = parts[2];

        int indexUser = trouverUtilisateur(user);

        // 1. Vérifier si l'utilisateur existe
        if (indexUser == -1) {
            return "404,CONNECT,Utilisateur inexistant";
        }

        // 2. Vérifier le mot de passe
        if (!Server.password[indexUser].equals(mdp)) {
            return "401,CONNECT,Mot de passe incorrect";
        }

        // 3. Si tout est bon
        return "200,CONNECT,OK";
    }

    static int trouverUtilisateur(String user) {
        for (int i = 0; i < Server.nbusers; i++) {

            if (Server.username[i].equals(user) && Server.is_deleted[i] == false) {
                return i;
            }
        }

        return -1;
    }
}