import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;


public class ProcessClientRequest_Thread implements Runnable {

	private Socket clientConnection;
	private ClassRoomServer CRS;
	private String ClientID;

	ProcessClientRequest_Thread(ServerSocket socket, ClassRoomServer crs){
		try {
			this.clientConnection = socket.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}
		CRS = crs;
	}

	@Override
	public void run() {
		try {
			int buffFilled, fileSize;
			
			OutputStream outImage;
			BufferedImage screenShot;
			String remoteCommand;
			DataOutputStream dataOut;
			
			while(true){
				InputStream is = clientConnection.getInputStream();
				BufferedReader buffRead = new BufferedReader(new InputStreamReader(is));
				
				String header = buffRead.readLine();
				try{
					int imageSize = Integer.parseInt(header);
					System.out.println("GET IMAGE");
					
					buffFilled=0;
					fileSize=0;
//					is.skip(header.length()+1);
					outImage = new FileOutputStream(new File("./tempGrabbedScreen.jpg"));
					while (fileSize < imageSize){
						byte[] data = new byte[imageSize-fileSize];
						buffFilled = is.read(data);
//						for(int i=0; i < buffFilled; i++){
//							System.out.println("Position "+ Integer.toString(i) + " --> "+ Byte.toString(data[i]));
//						}
						fileSize += buffFilled;
						outImage.write(data, 0, buffFilled);
					}
					outImage.flush();

					//If image retrieved is not null then repaint GUI image
					screenShot = ImageIO.read(new File("./tempGrabbedScreen.jpg"));
					if(screenShot==null){
						System.out.println("NO SOURCE IMAGE");
					}else{
						CRS.updatePaint(screenShot);
					}
				}catch(NumberFormatException nfe){
					//Incoming message did not contain a header with the file size
					//Therefore ignore message
					if(header!=null){
						this.ClientID = header;
					}
				}
				
				BufferedOutputStream buffOut = new BufferedOutputStream( clientConnection.getOutputStream(), 100 );
				
				remoteCommand = CRS.getRemoteCommand();
				// Sends the packet socket.send(packet); 

				if(remoteCommand != null ){
					System.out.println("Interrupt Remote");
					String str = remoteCommand + "\n";
					System.out.println(str); 
					buffOut.write(str.getBytes());
					CRS.setRemoteCommand(null);
				}else{
					buffOut.write("NoAction\n".getBytes());
				}
				buffOut.flush();

			}/*End of Loop*/
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			//			notifyAll();
			//				try {
			////					clientConnection.close( );
			//				} catch (IOException e) {
			//					e.printStackTrace();
			//				}
		}
	}	
}
