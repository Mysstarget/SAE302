package Client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
public class ClientUDP {
static final int port = 6010 ;
public static void main(String args[])
throws UnknownHostException, SocketException, IOException {
InetAddress address = InetAddress.getByName("192.168.27.66") ;
String msg="cest moi";
int msglen = msg.length() ;
byte [] message = new byte [msglen] ;
msg.getBytes(0, msglen, message, 0) ;
DatagramPacket packet =
new DatagramPacket(message, msglen, address, port) ;
DatagramSocket socket = new DatagramSocket() ;
socket.send(packet) ;
}
}
