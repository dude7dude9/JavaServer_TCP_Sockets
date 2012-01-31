package threadedServer;

import gui.ClassRoomServer;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Consumer implements Runnable {
	
	private Buffer<Socket> fBuffer;
	private List<Socket> fItemsConsumed;
	private ClassRoomServer CRS;
	private String ClientID;
	

	public Consumer(Buffer<Socket> buffer, ClassRoomServer crs) {
		fBuffer = buffer;
		CRS = crs;
		fItemsConsumed = new ArrayList<Socket>();
	}
		
	public void run() {
		boolean finished = false;
		int buffFilled, fileSize;
		
		OutputStream outImage;
		BufferedImage screenShot;
		String remoteCommand;
		DataOutputStream dataOut;

		// Get next item of work from the buffer, blocking if necessary.
		Socket clientConnection;
		try {
			clientConnection = fBuffer.get();
			//Set blocking read timeouts to 1000ms
//			clientConnection.setSoTimeout(1000);
			
			while(!finished) {
			
				try{

					InputStream is = clientConnection.getInputStream();
					BufferedReader buffRead = new BufferedReader(new InputStreamReader(is));

					String header = buffRead.readLine();

					try{
						int imageSize = Integer.parseInt(header);
						System.out.println("GET IMAGE");

						buffFilled=0;
						fileSize=0;
//						is.skip(header.length()+1);
						outImage = new FileOutputStream(new File("./tempGrabbedScreen.jpg"));
						while (fileSize < imageSize){
							byte[] data = new byte[imageSize-fileSize];
							buffFilled = is.read(data);
							//					for(int i=0; i < buffFilled; i++){
							//						System.out.println("Position "+ Integer.toString(i) + " --> "+ Byte.toString(data[i]));
							//					}
							fileSize += buffFilled;
							if(data[header.length()]==10){
								buffFilled -= (header.length()+1);
								fileSize -= (header.length()+1);
								outImage.write(data, header.length()+1, buffFilled);
							}else{
								outImage.write(data, 0, buffFilled);
							}
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

				}catch(SocketTimeoutException timeOut){
					//Timed out during read, continue with write then loop back to read
					System.err.println(timeOut.getMessage());
				}
	
				BufferedOutputStream buffOut = new BufferedOutputStream( clientConnection.getOutputStream(), 100 );
				remoteCommand = CRS.getRemoteCommand();
	
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
			}/*End While Loop*/
		}catch (SocketException sex){
			System.err.println(sex.getCause());
			
		}catch(IOException ioe){
			System.err.println(ioe.getMessage());
			System.err.println(ioe.getCause());
			ioe.printStackTrace();
		}catch (InterruptedException ie) {
			System.err.println(ie.getMessage());
			System.err.println(ie.getCause());
			ie.printStackTrace();
		}
	}
	
	public List<Socket> itemsConsumed() {
		return fItemsConsumed;
	}

}
