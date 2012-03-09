package threadedServer;

import gui.ClassRoomServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Producer implements Runnable {
	
	private Buffer<IndexedSocket> fBuffer;
	private ClassRoomServer CRS;
	
	public Producer(Buffer<IndexedSocket> buffer, ClassRoomServer crs) {
		fBuffer = buffer;
		CRS = crs;
	}
	
	public void run() {
		try {
			ServerSocket socket = new ServerSocket(10012);
			InetAddress serverHost = InetAddress.getLocalHost();
			System.out.println("http://" + serverHost.getHostAddress() + ":"+ socket.getLocalPort() + "/");
			
			while(true){
				Socket clientConnection = socket.accept();
				try {
					IndexedSocket socketIndexStruct = new IndexedSocket(clientConnection, CRS.addScreen("./hide_Sit.bmp"));
					fBuffer.put(socketIndexStruct);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}		
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
