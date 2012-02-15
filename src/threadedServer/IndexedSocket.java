package threadedServer;

import java.net.Socket;

public class IndexedSocket{
	public int index;
	public Socket clientConn;
	
	public IndexedSocket(Socket clientConnection, int i){
		index = i;
		clientConn = clientConnection;
	}
}
