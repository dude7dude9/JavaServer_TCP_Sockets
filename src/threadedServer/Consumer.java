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

	private Buffer<IndexedSocket> fBuffer;
	private List<Socket> fItemsConsumed;
	private ClassRoomServer CRS;
	private String ClientID; 

	public Consumer(Buffer<IndexedSocket> buffer, ClassRoomServer crs) {
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

		IndexedSocket indexSoc;
		try {
			while(true){
				// Get next item of work from the buffer, blocking if necessary.
				indexSoc = fBuffer.get();
				
				try{

				while(!finished) {

					try{
						InputStream is = indexSoc.clientConn.getInputStream();
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
								//TODO change to indirect update call as Swing is not ThreadSafe
								CRS.updatePaint(screenShot, indexSoc.index);
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
					}catch (SocketException SEx) {
						System.err.println(SEx.getMessage());
						System.err.println(SEx.getCause());
						SEx.printStackTrace();
						//Client connection has been lost so remove from GUI
						CRS.removeScreen(indexSoc.index);
					}

					BufferedOutputStream buffOut = new BufferedOutputStream( indexSoc.clientConn.getOutputStream(), 100 );
					remoteCommand = CRS.getRemoteCommand(indexSoc.index);

					if(remoteCommand != null ){
						System.out.println("Interrupt Remote");
						String str = remoteCommand + "\n";
						System.out.println(str); 
						buffOut.write(str.getBytes());
						CRS.resetRemoteCommand(indexSoc.index);
					}else{
						buffOut.write("NoAction\n".getBytes());
					}
					buffOut.flush();
				}/*End While Loop - Processing Client Communication*/
				
				}catch (SocketException SeX) {
					System.err.println(SeX.getMessage());
					System.err.println(SeX.getCause());
					SeX.printStackTrace();
					//Client connection has been lost so remove from GUI
					CRS.removeScreen(indexSoc.index);
				}
				
			}/*End While Loop - Establishing Client Communication*/
		}catch (SocketException sex){
			System.err.println(sex.getCause());
			System.err.println(sex.getMessage());
			System.err.println(sex.getCause());
			sex.printStackTrace();
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
