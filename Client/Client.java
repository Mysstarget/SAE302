package com.mycompany.serveurudp;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
public class ServeurUDP {
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
s = new String(buffer, 0, 0, packet.getLength()) ;
System.out.println(s) ;
}
}
}