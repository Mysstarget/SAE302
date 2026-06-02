package Server;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.UUID;

public class Server {

    private ArrayList<Utilisateur> utilisateurs;
    private ArrayList<Friend> friends;
    private ArrayList<Message> messages;
    private ArrayList<Groupe> groupes;
    private ArrayList<GroupMember> membres;

    static final int port = 6010;

    private DatagramSocket socket;
    private DatagramPacket recu;

    public Server() throws SocketException {

        utilisateurs = new ArrayList<>();
        friends = new ArrayList<>();
        messages = new ArrayList<>();
        groupes = new ArrayList<>();
        membres = new ArrayList<>();

        System.out.println("Serveur en cours sur le port " + port);

        socket = new DatagramSocket(port);

        byte[] buffer = new byte[1024];
        recu = new DatagramPacket(buffer, buffer.length);
    }

    // fonction pour trouver l'utilisateur
    private Utilisateur findUser(String username) {

    for (Utilisateur u : utilisateurs) {
        if (u.getUsername().equals(username) && !u.isDeleted()) {
            return u;
        }
    }
    return null;
    }

    // génère un token
    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    // Vérifie si un utilisateur a le bon format
    private boolean isValidUsername(String username) {
        return username.matches("[a-zA-Z0-9_]+");
    }

    // Méthode créer un user
    private String createUser(String username, String password) {
    if (!isValidUsername(username)) {
        return "400,CREATE_USER,Nom d'utilisateur invalide";
    }
    if (findUser(username)!=null) {
        return "409,CREATE_USER,Utilisateur déjà existant";
    }
    try {
        Utilisateur user = new Utilisateur(utilisateurs.size() + 1, username, password);
        utilisateurs.add(user);
        return "201,CREATE_USER,Utilisateur créé";
    } catch (Exception e) {
        return "405,CREATE_USER,Erreur lors de la création";
    }
    }

    // Méthode connect
    private String connectUser(String username, String password) {
    Utilisateur user = findUser(username);
    if (user == null) {
        return "404,CONNECT,Utilisateur inexistant";
    }
    if (!user.getPassword().equals(password)) {
        return "401,CONNECT,Mot de passe incorrect";
    }
    user.setConnected(true);
    String token = generateToken();
    user.setSessionToken(token);
    return "200,CONNECT,OK;" + token;
    }

    // Méthode Delete
    private String deleteUser(String username, String password) {
    Utilisateur user = findUser(username);
    if (user == null) {
        return "404,DELETE_USER,Utilisateur inexistant";
    }
    if (!user.getPassword().equals(password)) {
        return "401,DELETE_USER,Mot de passe incorrect";
    }
    try {
        user.delete();
        return "200,DELETE_USER,Utilisateur supprimé";
    } catch (Exception e) {
        return "405,DELETE_USER,Erreur suppression utilisateur";
    }
    }

private void envoi(String message) throws IOException {
        byte[] data = message.getBytes();
        DatagramPacket paquet = new DatagramPacket(data, data.length, recu.getAddress(), recu.getPort()); socket.send(paquet);
        System.out.println("envoye = " + message);
    }

public void start() throws IOException {
    while (true) {
        byte[] buffer = new byte[1024];
        recu = new DatagramPacket(buffer, buffer.length);
        socket.receive(recu);
        String s = new String(recu.getData(), 0, recu.getLength());
            System.out.println("recu = " + s);
            String[] t = s.split(",");

        // envoie pour la méthode create
        if(t[0].equals("Create")) {
            envoi(createUser(t[1],t[2]));
        }

        // envoie pour la méthode connect
        if(t[0].equals("Connect")) {
            envoi(connectUser(t[1],t[2]));
        }

        // envoie pour la méthode delete
        if(t[0].equals("Delete")) {
            envoi(deleteUser(t[1],t[2]));
        }
}
}
public static void main(String[] args) {
    try {
        Server serveur = new Server();
        serveur.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
