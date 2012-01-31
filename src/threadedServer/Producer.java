package threadedServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Producer implements Runnable {
	
	private Buffer<Socket> fBuffer;
	
	public Producer(Buffer<Socket> buffer) {
		fBuffer = buffer;
	}
	
	public void run() {
		try {
			ServerSocket socket = new ServerSocket(10012);
			InetAddress serverHost = InetAddress.getLocalHost();
			System.out.println("http://" + serverHost.getHostAddress() + ":"+ socket.getLocalPort() + "/");
			
			while(true){
				Socket clientConnection = socket.accept();
				
				try {
					fBuffer.put(clientConnection);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}		
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
