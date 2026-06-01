package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
static final int port = 6010 ;
public static void main(String args[])
throws SocketException, IOException {
byte [] buffer = new byte [1024] ;
String s ;
DatagramSocket socket = new DatagramSocket(port) ;
for ( ; ; ) {
DatagramPacket packet =
new DatagramPacket(buffer, buffer.length);
socket.receive(packet) ;
s = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
System.out.println(s) ;

//
            String username = ConnectUser.getUsername(s);

            System.out.println("Utilisateur : " + username);

}
}
}