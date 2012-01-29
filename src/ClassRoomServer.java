import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;



@SuppressWarnings("serial")
public class ClassRoomServer extends JFrame implements ActionListener {

	public static String RemoteCommand = null;
	public BufferedImage screenShot = null;
	public JLabel screenLabel = null;
	public Container img = null;
	public Container content = null;
	public JFrame frame;
	public ClassRoomServer CRS;

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ClassRoomServer app = new ClassRoomServer();
				app.setVisible(true);
			}
		});
	}


	private ClassRoomServer(){
		super("Tablet Server");	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		content = getContentPane();
		content.setBackground(Color.white);
		content.setLayout(new FlowLayout());

		JButton Lock = new JButton("Lock Tablet");
		Lock.addActionListener(this);
		content.add(Lock);
		JButton Unlock = new JButton("Unlock Tablet");
		Unlock.addActionListener(this);
		content.add(Unlock);

		try {
			screenShot = ImageIO.read(new File("./hide_Sit.bmp"));
			//        	screenShot = ImageIO.read(new File("./tempGrabbedScreen.jpg"));
			screenLabel = new JLabel(new ImageIcon( screenShot ));
			add( screenLabel );    
		} catch (IOException e) {e.printStackTrace();}     

		//		    ImageIcon cup = new ImageIcon("images/cup.gif");
		//		    JButton button2 = new JButton(cup);
		//		    content.add(button2);
		pack();
		setVisible(true);
		content.repaint();
		// this centers the frame on the screen
		//setLocationRelativeTo(null);

		//start the server SwingWorker in a background thread
		CRS = this;
		tcpServer.execute();
	}

	//public class TcpServer extends SwingWorker<Void, Void>{
	private SwingWorker<Void, Void> tcpServer = new SwingWorker<Void, Void>(){
		@Override
		protected synchronized Void doInBackground() throws Exception {
			try 
			{ 
				Image image = null;
				//Initialize TCP Connection
				String currentline = null, header = null;
				ServerSocket socket = new ServerSocket( 10012 );		
				System.out.println("Before Host OBTAIN");
				InetAddress serverHost = InetAddress.getLocalHost( ); 
				System.out.println( "TCPServer destination: " + serverHost.getHostAddress( ) + ", "+ socket.getLocalPort( ) );



//				ServerSocket socket2 = new ServerSocket( 10011 );		
//				System.out.println("Before Host OBTAIN"); 
//				System.out.println( "TCPServer destination: " + serverHost.getHostAddress( ) + ", "+ socket2.getLocalPort( ) );
				InetAddress groupAddr = InetAddress.getByName("225.4.5.6");
				MulticastSocket mSocket = new MulticastSocket();
				//mSocket.joinGroup(groupAddr);
				
								

				try{
					//Run multicasting thread
//					System.out.println("Before Accept");
//					Socket clientConnection2 = socket2.accept();
//					System.out.println("After Accept");
					//Create new thread for connection2
//					ProcessServerInterrupt_Thread client2 = new ProcessServerInterrupt_Thread(mSocket, RemoteCommand, CRS, groupAddr, 10011);
//					Thread multicastThread = new Thread(client2);
//					multicastThread.setDaemon(true);
//					multicastThread.start();
//					client2.run();
					
					/* Repeatedly handle requests for processing. */ 
//					while( true ) {						

						System.out.println("Before Accept");
//						Socket clientConnection = socket.accept();
//						System.out.println("After Accept");
						//Create new thread for connection1
//						ProcessClientRequest_Thread client1 = new ProcessClientRequest_Thread(clientConnection, CRS);
						ProcessClientRequest_Thread client1 = new ProcessClientRequest_Thread(socket, CRS);
						Thread clientRequestThread = new Thread(client1);
						clientRequestThread.setDaemon(true);
						clientRequestThread.start();
//						client1.run();
						
//						this.wait();
						
//					}
//						InputStream is = clientConnection.getInputStream();
//						DataOutputStream out = new DataOutputStream( clientConnection2.getOutputStream( ) );
//
//						System.out.println("Before Readline");
//						System.out.println("INPUT STREAM " + String.valueOf(is.available()));
//
//						System.out.println("GET IMAGE");
//
//
//						int nRead=0;
//						byte[] data = new byte[1024];
//
//						OutputStream outImage = new FileOutputStream(new File("./tempGrabbedScreen.jpg"));
//
//						while ((nRead = is.read(data)) != -1) {
//							outImage.write(data, 0, nRead);
//						}
//
//						outImage.flush();
//
//						//If image retrieved is not null then repaint GUI image
//						screenShot = ImageIO.read(new File("./tempGrabbedScreen.jpg"));
//						if(screenShot==null){
//							System.out.println("NO SOURCE IMAGE");
//						}else{
//							repaint();
//						}
//
//						if( RemoteCommand != null ){
//							out.writeBytes(RemoteCommand);
//							out.writeChar('\n');
//							RemoteCommand = null;
//						}else{
//							out.writeBytes("No Action");
//							out.writeChar('\n');
//						}
//						out.flush();
//
//						clientConnection.close( );	
//						clientConnection2.close( );	
//					}
				}finally {	
					socket.close();
					mSocket.close();
				}
			}catch( IOException e ) {
				e.printStackTrace();
			}
			return null;
		}
	};

	@Override
	public void paint(Graphics g) {
		//			screenShot = ImageIO.read(new File("./hide_Sit.bmp"));
		//			System.out.println("TRY RELOAD");
		//screenLabel = new JLabel(new ImageIcon( screenShot ));
		if(screenShot != null){ screenLabel.setIcon(new ImageIcon( screenShot )); }
		//		System.out.println("REDRAW");
		//		System.out.println("ScreenLabelComps: "+screenLabel.getComponentCount() );
		//		System.out.println("ContentComps: "+content.getComponentCount() );
		//		System.out.println(content.getComponent(2).toString() );
		super.paintComponents(g);
	};

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		System.err.println( e.getSource().toString() );
		System.err.println( e.getID() );
		System.err.println( e.getActionCommand() );

		if( e.getActionCommand().equals("Lock Tablet") ){
			System.out.println("Lock Tablet Pressed");		
			RemoteCommand = "LOCK";
		}
		if( e.getActionCommand().equals("Unlock Tablet") ){
			System.out.println("Unlock Tablet Pressed");
			RemoteCommand = "UNLOCK";
		}

		System.out.println("");
	}

	public void updatePaint(BufferedImage screenShot){
		this.screenShot = screenShot;
		repaint();
	}
	
	public void setRemoteCommand(String set){
		RemoteCommand = set;
	}
	
	public String getRemoteCommand(){
		return RemoteCommand;
	}
}
