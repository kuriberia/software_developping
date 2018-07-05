package reciever;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.*;

public class Connector extends Thread {

    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private RtpVoicePacketReceiver rvpr;
    private BufferedReader in;
    private PrintWriter out;
    private String destIP = "";
    private final int PORT = 21234;
    private boolean isOnline;

    private ObservableList<String> chatLog;

    public Connector(String ip) {
        this.destIP = ip;
        isOnline = false;
        chatLog = FXCollections.observableArrayList();
    }

    @Override
    public void run() {
        try {
            tcpSocket = new Socket(destIP, 8080);
            in = new BufferedReader(
                    new InputStreamReader(
                            tcpSocket.getInputStream()));
            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    tcpSocket.getOutputStream())), true);
            connectSender();
            isOnline = true;
            while (isOnline) {
                if(in.ready()) {
                    String msg = in.readLine();
                    System.out.println("*" + msg);
                    if(msg.equals("END")){
                        isOnline = false;
                        break;
                    }
                    Platform.runLater(() -> chatLog.add(msg));
                }
            }
            rvpr.stop();
            tcpSocket.close();
            udpSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getOwnIP() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void connectStop() {
        isOnline = false;
    }

    public void connectSender() {
        out.println("connect");
        try {
//            String str = in.readLine();
//            System.out.println(str);
//            PORT = Integer.parseInt(str);
            out.println(getOwnIP()); // Send own IP address to sender.
            udpSocket = new DatagramSocket(PORT);
            rvpr = new RtpVoicePacketReceiver(udpSocket);
            rvpr.start();
            /*  Get log from server
            while(true){
                String data = in.readLine();
                System.out.println(data);
                if(data.equals("END")) break;
                chat.addMessage(data);
            }
            */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getIsOnline(){
        return this.isOnline;
    }

    public void sendMessage(String msg){
        out.println(msg);
    }

    public ObservableList<String> getLog(){
        return chatLog;
    }
}
