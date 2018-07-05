package sender;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.*;

public class Sender implements Runnable{

	private Socket tcpSocket;
	private DatagramSocket udpSocket;
	private RtpVoicePacketTransmitter rvpt;
	private BufferedReader in;
	private PrintWriter out;
	private static String destIP; //宛先IPアドレス
	private static String destPort; //宛先UDPポート番号
	private static boolean isOnline;

	private ObservableList<String> chatLog;

	Sender(){
		destIP = "192.168.48.152";
		destPort = "8080";
		chatLog = FXCollections.observableArrayList();
	}

	@Override
	public void run(){
		try{
			ServerSocket ss = new ServerSocket(8080);
			System.out.println("waiting...");
			tcpSocket = ss.accept();
			System.out.println("accept");
			//tcpSocket = new Socket("localhost", 8080);
			in = new BufferedReader(
							new InputStreamReader(
									tcpSocket.getInputStream()));
			out = new PrintWriter(
							new BufferedWriter(
									new OutputStreamWriter(
											tcpSocket.getOutputStream())), true);
			System.out.println("start");
			//destPort = in.readLine(); //ポート番号受け取り
			udpSocket = new DatagramSocket(Integer.parseInt(destPort));
			rvpt = new RtpVoicePacketTransmitter(udpSocket, destIP, destPort);
			rvpt.start();
			while(isOnline) {
				if(in.ready()) {
					String msg = in.readLine();
					Platform.runLater(() -> chatLog.add(msg));
					out.println(msg);
				}
			}
			System.out.println("comment stopped.");
		} catch(IOException ioe) {
			ioe.printStackTrace();
      	} finally {
			try {
				rvpt.stop();
				udpSocket.close();
				tcpSocket.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	void sendMessage(String msg) {
		out.println(msg);
		chatLog.add(msg);
	}

	static boolean isOnline(){
		return isOnline;
	}
	static void setOffline(){
		isOnline = false;
	}
	static void setOnline(){
		isOnline = true;
	}
	ObservableList<String> getChatLog(){
		return chatLog;
	}
}