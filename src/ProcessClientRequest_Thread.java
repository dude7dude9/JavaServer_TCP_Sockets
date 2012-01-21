import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;


public class ProcessClientRequest_Thread implements Runnable {

	private Socket clientConnection;
	private ClassRoomServer CRS;

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
		InputStream is;
		while(true){
			try {
				is = clientConnection.getInputStream();
				DataInputStream DIS = new DataInputStream(is);
				String header = DIS.readLine();
				try{
					int imageSize = Integer.parseInt(header);

					System.out.println("Before Readline");
					System.out.println("INPUT STREAM " + String.valueOf(is.available()));
					System.out.println("GET IMAGE");


					int buffFilled=0;
					int fileSize=0;
					byte[] data = new byte[2048];
					OutputStream outImage = new FileOutputStream(new File("./tempGrabbedScreen.jpg"));
					//			while ((nRead = is.read(data)) != -1) {
					while (fileSize < imageSize){
						buffFilled = is.read(data);
						//					if (nRead == -1){break;}
						fileSize += buffFilled;
						outImage.write(data, 0, buffFilled);
					}
					outImage.flush();


					//If image retrieved is not null then repaint GUI image
					BufferedImage screenShot = ImageIO.read(new File("./tempGrabbedScreen.jpg"));
					//				int size = screenShot.getHeight()*screenShot.getWidth();
					//				System.out.println("SIZE = " + Integer.toString(size));
					if(screenShot==null){
						System.out.println("NO SOURCE IMAGE");
					}else{
						CRS.updatePaint(screenShot);
					}
				}catch(NumberFormatException nfe){
					//Incoming message did not contain a header with the file size
					//Therefore ignore message
				}

//				clientConnection.close( );	
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
}
