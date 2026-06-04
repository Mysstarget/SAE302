
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

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
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    // fonction pour trouver groupe
    private Groupe findGroupe(String groupName) {
        for (Groupe g : groupes) {
            if (g.getGroupName().equals(groupName)) {
                return g;
            }
        }
        return null;
    }

    // fonction qui vérifie si un utilisateur a le bon format
    private boolean isValidUsername(String username) {
        return username.matches("[a-zA-Z0-9_]+");
    }

    // fonction qui vérifie si un groupe a le bon format
    private boolean isValidGroupname(String groupname) {
        return groupname.matches("[a-zA-Z0-9_]+");
    }

    // fonction pour limiter le nombre de message
    private void limiterMessages(String username) {
        while (true) {
            int nb = 0;
            for (Message m : messages) {
                if (username.equals(m.getDstUser()) && m.getType().equals("PRIVATE")) {
                    nb++;
                }
            }
            if (nb <= 4) {
                break;
            }
            for (int i = 0; i < messages.size(); i++) {
                Message m = messages.get(i);
                if (username.equals(m.getDstUser()) && m.getType().equals("PRIVATE")) {
                    messages.remove(i);
                    break;
                }
            }
        }
    }
 
    // Méthode pour créer un user
    private String createUser(String username, String password) {
        if (!isValidUsername(username)) {
            return "400,CREATE_USER,Nom d'utilisateur invalide";
        }
        if (findUser(username)!=null) {
            return "409,CREATE_USER,Utilisateur deja existant";
        }
        if (utilisateurs.size()>=4) {
            return "410,LIMITE,4 utilisateur maximum";
        }
        try {
            Utilisateur user = new Utilisateur(utilisateurs.size() + 1, username, password);
            utilisateurs.add(user);
            return "201,CREATE_USER,Utilisateur cree";
        } catch (Exception e) {
            return "405,CREATE_USER,Erreur lors de la creation";
        }
    }
    
    // fonction vérifie membre
    private boolean getMGroupe(String username, String groupName) {
        for (GroupMember gm : membres) {
            if (gm.getUsername().equals(username)) {
                for (Groupe g : groupes) {
                    if (g.getIdGroup() == gm.getIdGroup()
                            && g.getGroupName().equals(groupName)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
        try {
        String friendsList = "";
        String groupsList = "";
        String msgList = "";
        String msgList_G = "";

        // Amis
        for (Friend f : friends) {
            if (f.getStatus().equals("ACCEPTED")) {
                if (f.getSrcUser().equals(username)) {
                    friendsList += f.getDstUser() + ",";
                }
                if (f.getDstUser().equals(username)) {
                    friendsList += f.getSrcUser() + ",";
                }
            }
        }

        // Groupes
        for (GroupMember m : membres) {
            if (m.getUsername().equals(username)) {
                for (Groupe g : groupes) {
                    if (g.getIdGroup() == m.getIdGroup()) {
                        groupsList += g.getGroupName() + ",";
                    }
                }
            }
        }

        // Messages privés
        for (Message m : messages) {
            if (m.getType().equals("PRIVATE") && (username.equals(m.getDstUser()) || username.equals(m.getSrcUser()))) {
                msgList += m.getSrcUser() + ":" + m.getDstUser() + ":" + m.getContent() + ",";
                m.setDelivered(true);
            }
        }

        // Messages Group
        for (Message msg : messages) {
            if (msg.getType().equals("GROUP") && getMGroupe(username, msg.getDstGroup())) {
                msgList_G += msg.getSrcUser() + ":" + msg.getDstGroup() + ":" + msg.getContent() + ",";
            }
        }

        // Enlever la dernière virgule
        if (!friendsList.isEmpty()) {
            friendsList = friendsList.substring(0, friendsList.length() - 1);
        }
        if (!groupsList.isEmpty()) {
            groupsList = groupsList.substring(0, groupsList.length() - 1);
        }
        if (!msgList.isEmpty()) {
            msgList = msgList.substring(0, msgList.length() - 1);
        }
        if (!msgList_G.isEmpty()) {
            msgList_G = msgList_G.substring(0, msgList_G.length() - 1);
        }
        user.setConnected(true);
        return "200,CONNECT,OK" + ";FRIENDS=" + friendsList + ";GROUPS=" + groupsList + ";MSG=" + msgList + ";MSG_G=" + msgList_G;
    } catch (Exception e) {
        return "402,CONNECT,Erreur récupération données";
    }
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
            // Delete friend
            for (int i = friends.size() - 1; i >= 0; i--) {
                Friend f = friends.get(i);
                if (f.getSrcUser().equals(username) || f.getDstUser().equals(username)) {
                    friends.remove(i);
                }
            }
            // Delete message
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message m = messages.get(i);
                if (username.equals(m.getSrcUser()) || username.equals(m.getDstUser())) {
                    messages.remove(i);
                }
            }
            // Delete membre groupe
            for (int i = membres.size() - 1; i >= 0; i--) {
                GroupMember gm = membres.get(i);

                if (gm.getUsername().equals(username)) {
                    membres.remove(i);
                }
            }

            utilisateurs.remove(user);
            return "200,DELETE_USER,Utilisateur supprime";
        } catch (Exception e) {
            return "405,DELETE_USER,Erreur suppression utilisateur";
        }
    }

    // Méthode ajout d'ami
    private String F_add(String srcUser, String dstUser) {
        Utilisateur src = findUser(srcUser);
        if (src == null) {
            return "404,F_ADD,Utilisateur source inexistant";
        }
        Utilisateur dst = findUser(dstUser);
        if (dst == null) {
            return "404,F_add,Utilisateur destination inexistant";
        }
        if (srcUser.equals(dstUser)) {
            return "400,F_add,Impossible de s'ajouter soi-même";
        }
        // Vérifie si une relation existe déjà
        for (Friend f : friends) {
            boolean memeRelation = (f.getSrcUser().equals(srcUser) && f.getDstUser().equals(dstUser)) || (f.getSrcUser().equals(dstUser) && f.getDstUser().equals(srcUser));
            if (memeRelation) {
                return "409,F_Add,Relation deja existante";
            }
        }
        try {
            Friend f = new Friend(srcUser, dstUser);
            friends.add(f);
            return "200,F_ADD,Demande envoyee";
        } catch (Exception e) {
            return "405,F_ADD,Erreur creation demande ami";
        }
    }

    // Méthode demande amis
    private String F_acc(String srcUser, String dstUser, String value) {
        Utilisateur src = findUser(srcUser);
        Utilisateur dst = findUser(dstUser);
        if (src == null || dst == null) {
            return "404,F_ACC,Utilisateur inexistant";
        }
        Friend demande = null;
        for (Friend f : friends) {
            if (f.getSrcUser().equals(dstUser) && f.getDstUser().equals(srcUser) && f.getStatus().equals("PENDING")) {
                demande = f;
                break;
            }
        }
        if (demande == null) {
            return "404,F_ACC,Demande inexistante";
        }
        if (!value.equals("0") && !value.equals("1")) {
            return "400,F_ACC,Valeur invalide";
        }
        try {
            if (value.equals("1")) {
                demande.accept();
            } else {
                demande.refuse();
            }
            demande.setSeenSrc(false);
            demande.setSeenDst(true);
            return "200,F_ACC,Reponse enregistree";
        } catch (Exception e) {
            return "405,F_ACC,Erreur traitement demande";
        }
    }

    // Méthode ajout groupe
    private String G_add(String srcUser, String groupName) {
        if (findUser(srcUser) == null) {
            return "404,G_ADD,Utilisateur inexistant";
        }
        if (!isValidGroupname(groupName)) {
            return "400,G_ADD,Nom de groupe invalide";
        }
        if (findGroupe(groupName)!=null) {
            return "409, Conflit, ressource déjà existante";
        }
        try {
            int idGroup = groupes.size() + 1;
            Groupe g = new Groupe(idGroup, groupName, srcUser);
            groupes.add(g);
            GroupMember m = new GroupMember(idGroup, srcUser, "OWNER");
            membres.add(m);
            m.setSeenJoin(true);
            return "201,G_ADD,Groupe cree";
        } catch (Exception e) {
            return "405,G_ADD,Erreur creation groupe";
        }
    }

    // Méthode ajout Membre groupe
    private String G_add_M(String srcUser, String groupName, String userToAdd) {
        Utilisateur src = findUser(srcUser);
        if (src == null) {
            return "404,G_ADD_M,Utilisateur source inexistant";
        }
        Groupe groupe = null;
        for (Groupe g : groupes) {
            if (g.getGroupName().equals(groupName)) {
                groupe = g;
                break;
            }
        }
        if (groupe == null) {
            return "404,G_ADD_M,Groupe inexistant";
        }
        Utilisateur cible = findUser(userToAdd);
        if (cible == null) {
            return "404,G_ADD_M,Utilisateur a ajouter inexistant";
        }

        // Vérifie les droits
        boolean autorise = false;
        for (GroupMember m : membres) {
            if (m.getIdGroup() == groupe.getIdGroup() && m.getUsername().equals(srcUser) && (m.getRole().equals("OWNER") || m.getRole().equals("ADMIN"))) {
                autorise = true;
                break;
            }
        }
        if (!autorise) {
            return "401,G_ADD_M,Acces refuse";
        }

        // Vérifie si déjà membre
        for (GroupMember m : membres) {
            if (m.getIdGroup() == groupe.getIdGroup() && m.getUsername().equals(userToAdd)) {
                return "409,G_ADD_M,Utilisateur deja membre";
            }
        }
        try {
            GroupMember nouveau = new GroupMember(groupe.getIdGroup(), userToAdd, "MEMBER");
            nouveau.setSeenJoin(false);
            membres.add(nouveau);
            return "200,G_ADD_M,Membre ajoute";
        } catch (Exception e) {
            return "405,G_ADD_M,Erreur ajout membre";
        }
    }

    // Méthode envoie message privée
    private String Send_Msg(String srcUser, String dstUser, String msg) {
        if (findUser(srcUser) == null) {
            return "404,SEND_MSG,Utilisateur source inexistant";
        }
        if (msg == null || msg.trim().isEmpty()) {
            return "400,SEND_MSG,Message invalide";
        }
        try {
            // Dans le cas pour tous
            if (dstUser.equalsIgnoreCase("@everyone")) {
            for (Friend f : friends) {
                if (f.getStatus().equals("ACCEPTED")) {
                    String destinataire = null;
                    if (f.getSrcUser().equals(srcUser)) {
                        destinataire = f.getDstUser();
                    }
                    else if (f.getDstUser().equals(srcUser)) {
                        destinataire = f.getSrcUser();
                    }
                    if (destinataire != null) {
                        int idMsg = messages.size() + 1;
                        Message m = new Message(idMsg, srcUser, destinataire, null, "PRIVATE", msg);
                        messages.add(m);
                        limiterMessages(destinataire);
                    }
                }
            }
            return "200,SEND_MSG,Message envoye a tous les amis";
        }
        if (findUser(dstUser) == null) {
            return "404,SEND_MSG,Destinataire inexistant";
        }
        // Dans le cas d'un user destination classique
            int idMsg = messages.size() + 1;
            Message m = new Message(idMsg, srcUser, dstUser, null, "PRIVATE", msg);
            messages.add(m);
            limiterMessages(dstUser);
            return "200,SEND_MSG,Message envoye";
        } catch (Exception e) {
            return "405,SEND_MSG,Erreur stockage message";
        }
    }

    // Méthode envoie message groupe
    private String Send_G_Msg(String srcUser, String dstGroup, String msg) {
        if (findUser(srcUser) == null) {
            return "404,SEND_MSG,Utilisateur source inexistant";
        }
        Groupe dst = findGroupe(dstGroup);
        if (dst == null) {
            return "404,SEND_MSG,Destinataire inexistant";
        }
        if (msg == null || msg.trim().isEmpty()) {
            return "400,SEND_MSG,Message invalide";
        }
        try {
            int idMsg = messages.size() + 1;
            Message m = new Message(idMsg, srcUser, null, dstGroup, "GROUP", msg);
            messages.add(m);
            return "200,SEND_MSG,Message envoye";
        } catch (Exception e) {
            return "405,SEND_MSG,Erreur stockage message";
        }
    }

    // Méthode Update
    private String Update(String username) {
        if (findUser(username) == null) {
            return "404,UPDATE,Utilisateur inexistant";
        }
        String data = "";

        // Messages privés
        for (Message m : messages) {
            if (m.getType().equals("PRIVATE") && username.equals(m.getDstUser()) && !m.isDelivered()) {
                data += ";MSG=" + m.getSrcUser() + ":" + m.getDstUser() + ":" + m.getContent();
                m.setDelivered(true);
            }
        }

        // Demandes d'amis reçues
        for (Friend f : friends) {
            if (f.getDstUser().equals(username) && f.getStatus().equals("PENDING") && !f.isSeenDst()) {
                data += ";FRIEND_REQUEST=" + f.getSrcUser();
                f.setSeenDst(true);
            }
        }

        // Réponses aux demandes d'amis
        for (Friend f : friends) {
            if (f.getSrcUser().equals(username) && !f.isSeenSrc() && (f.getStatus().equals("ACCEPTED") || f.getStatus().equals("REFUSED"))) {
                data += ";FRIEND_RESPONSE=" + f.getDstUser() + ":" + f.getStatus();
                f.setSeenSrc(true);
            }
        }

        // Nouveaux groupes
        for (GroupMember m : membres) {
            if (m.getUsername().equals(username) && !m.isSeenJoin()) {
                for (Groupe g : groupes) {
                    if (g.getIdGroup() == m.getIdGroup()) {
                        data += ";GROUP=" + g.getGroupName();
                        m.setSeenJoin(true);
                    }
                }
            }
        }

        // Messages de groupe
        for (GroupMember gm : membres) {
            if (gm.getUsername().equals(username)) {
                int dernierMsg = gm.getLastSeenMsgId();
                for (Message msg : messages) {
                    if (msg.getType().equals("GROUP") && getMGroupe(username, msg.getDstGroup()) && msg.getIdMsg() > dernierMsg) {
                        data += ";GROUP_MSG=" + msg.getDstGroup() + ":" + msg.getSrcUser() + ":" + msg.getContent();
                        gm.setLastSeenMsgId(msg.getIdMsg());
                    }
                }
            }
        }

        if (data.isEmpty()) {
            return "200,UPDATE,NO_DATA";
        }
        return "200,UPDATE,DATA" + data;
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

            // envoie pour la méthode ajout amis
            if(t[0].equals("F_add")) {
                envoi(F_add(t[1],t[2]));
            }

            // envoie pour la méthode demande amis
            if(t[0].equals("F_acc")) {
                envoi(F_acc(t[1],t[2],t[3]));
            }

            // envoie pour la méthode ajout groupes
            if(t[0].equals("G_add")) {
                envoi(G_add(t[1],t[2]));
            }

            // envoie pour la méthode ajout membres groupes
            if(t[0].equals("G_add_M")) {
                envoi(G_add_M(t[1],t[2],t[3]));
            }

            // envoie pour la méthode messages users
            if(t[0].equals("Send_Msg")) {
                envoi(Send_Msg(t[1],t[2],t[3]));
            }

            // envoie pour la méthode messages users groupes
            if(t[0].equals("Send_G_Msg")) {
                envoi(Send_G_Msg(t[1],t[2],t[3]));
            }

            // Update
            if(t[0].equals("Update")) {
                envoi(Update(t[1]));
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
