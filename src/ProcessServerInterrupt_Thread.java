import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class ProcessServerInterrupt_Thread implements Runnable{

	private MulticastSocket mSock;
	private String RemoteCommand;
	private ClassRoomServer CRS;
	private InetAddress address;
	private int PORT;

	public ProcessServerInterrupt_Thread(MulticastSocket mSock, String cmd, ClassRoomServer crs, InetAddress addr, int port) {

		this.mSock = mSock;
		RemoteCommand = cmd;
		CRS = crs;
		PORT = port;
		address = addr;

	}

	@Override
	public void run() {
		byte[] data = new byte[32];
		String str;
		DatagramPacket packet;
		try {
			while(true){
				try {
					Thread.sleep(250);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				RemoteCommand = CRS.getRemoteCommand();
				// Sends the packet socket.send(packet); 

				if( RemoteCommand != null ){
					System.out.println("Interrupt Remote");
					str = RemoteCommand + "\n";
					System.out.println(str);
					data = str.getBytes(); 
					//				out.writeBytes(RemoteCommand);
					//				out.writeChar('\n');
					CRS.setRemoteCommand(null);
					//RemoteCommand = null;
				}else{
					continue;
//					str = "No Action\n";
//					System.out.println(str);
//					data = str.getBytes();
					
					//				out.writeBytes("No Action");
					//				out.writeChar('\n');
				}
				packet = new DatagramPacket( data, data.length, address, PORT );
//				System.out.println(address.getHostAddress());
				try {
					mSock.send(packet);
					//System.out.println("before close");
					//mSock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//			out.flush();
			}
		}finally{}
	}
}
