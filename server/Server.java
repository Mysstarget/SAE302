package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.net.InetAddress;

// Définis les différents status
enum status { PENDING, ACCEPTED, REFUSED }

// Définis les différents roles
enum role { OWNER, ADMIN, MEMBER }

// Définis les différents types
enum TypeMsg { PRIVATE, GROUP }

public class Server {

// Tableaux users
static int[] id_user = new int[5];
static String[] username = new String[5];
static String[] password = new String[5];
static boolean[] is_deleted = new boolean[5]; // false par défaut
static int nbusers = 0;

// Tableaux Friend
static String[] F_src_user = new String[5];
static String[] F_dst_user = new String[5];
static status[] status = new status[25];
static boolean[] seen_src = new boolean[5];
static boolean[] seen_dst = new boolean[5];

// Tableaux Message
static ArrayList<Integer>  id_msg     = new ArrayList<>();
static ArrayList<Integer>  src_user   = new ArrayList<>();
static ArrayList<Integer>  dst_user   = new ArrayList<>();  // -1 si groupe
static ArrayList<Integer>  dst_group  = new ArrayList<>();  // -1 si privé
static ArrayList<TypeMsg>  type       = new ArrayList<>();
static ArrayList<String>   content    = new ArrayList<>();
static ArrayList<Boolean>  delivered  = new ArrayList<>();
static ArrayList<Boolean>  read       = new ArrayList<>();
static int id_msg_counter = 0;

// Tableaux Groupes
static int[] id_group = new int[50];
static String[] group_name = new String[50];
static int[] owner = new int[4]; // id_user du créateur

// Tableaux Group_Member
static int[] id_group_member = new int[50];
static int[] id_group_user = new int[5];
static role[] role = new role[25];
static boolean[] seen_join = new boolean[5];
static boolean[] last_seen_msg_id = new boolean[5];

static final int port = 6010 ;
public static void main(String args[])
throws SocketException, IOException {
byte [] buffer = new byte [1024] ;
String s ;
DatagramSocket socket = new DatagramSocket(port) ;
for ( ; ; ) {

DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
socket.receive(packet) ;

InetAddress clientAddr = packet.getAddress();
int clientPort         = packet.getPort();

String recu = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
String[] parts = recu.split(",");

// Switch pour les différentes méthodes
String reponse;
switch (parts[0]) {
    case "Create":  reponse = creerUtilisateur(parts); break;
    case "Login":   reponse = connecter(parts);        break;
    case "Message": reponse = envoyerMessage(parts);   break;
    case "Read":    reponse = lireMessages(parts);     break;
}
// Renvoyer au client
byte[] repBytes = reponse.getBytes("UTF-8");
socket.send(new DatagramPacket(repBytes, repBytes.length, clientAddr, clientPort));
}
}
}
