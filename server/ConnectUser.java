package server;

public class ConnectUser {

    static String connectUser(String[] parts) {
    // Format reçu : "Login,User,password"
    
    if (parts.length != 3) {
        return "400,LOGIN,Requête invalide";
    }

    String user = parts[1];
    String mdp = parts[2];
    
    // Valide les entree
    if (user == null || mdp == null || user.isEmpty() || mdp.isEmpty()) {
        return "400,LOGIN,Identifiants vides";
    }

    int indexUser = trouverUtilisateur(user);

    // 1. verifie si l'utilisateur existe
    if (indexUser == -1) {
        return "404,LOGIN,Utilisateur inexistant";
    }

    // 2. verifie le mot de passe
    if (!Server.password[indexUser].equals(mdp)) {
        return "401,LOGIN,Mot de passe incorrect";
    }

    // 3. crée la session
    String token = java.util.UUID.randomUUID().toString();
    Server.isConnected[indexUser] = true;
    Server.sessionToken[indexUser] = token;  
    
    return "200,LOGIN," + token;
}

static int trouverUtilisateur(String user) {
    for (int i = 0; i < Server.nbusers; i++) {
        if (Server.username[i] != null && 
            Server.username[i].equals(user) && 
            !Server.is_deleted[i]) {
            return i;
        }
    }
    return -1;
}
}