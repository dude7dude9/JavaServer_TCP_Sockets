package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import threadedServer.Buffer;
import threadedServer.BufferImpl;
import threadedServer.Consumer;
import threadedServer.IndexedSocket;
import threadedServer.Producer;

//TODO check bug with adding and removing clients of more than one, 2 ID's were same
//TODO look into how to screen capture more than one application
//TODO Allow client/server setting of user name, port, IP
//TODO Make more attractive, add default GUI size and Scroll bars
@SuppressWarnings("serial")
public class ClassRoomServer extends JFrame implements ActionListener {
	
//GUI Containers
	public Container img = null;
	public Container content = null;
	public JFrame frame;
	
//GUI Reference
	public ClassRoomServer CRS;
	
//GUI Objects for all clients
	private JButton LockAll;
	private static int clientID;
	
	//TODO change to threadsafe queue's
//GUI Objects for each client	
	public Vector<Container>		containers				= new Vector<Container>(16);
	public Vector<Consumer>			consumers	 			= new Vector<Consumer>(16);
	public Vector<BufferedImage> 	screenShots 			= new Vector<BufferedImage>(16);
	public Vector<JLabel> 			screenLabels 			= new Vector<JLabel>(16);
	public Vector<JButton> 			clientLocks 			= new Vector<JButton>(16);
	public Vector<String>			remoteClientCommands 	= new Vector<String>(16);
	public Vector<Integer> 			clientIDs 				= new Vector<Integer>(16);

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

		LockAll = new JButton("Lock All");
		LockAll.addActionListener(this);
		content.add(LockAll);

		pack();
		setVisible(true);
		content.repaint();
		// this centers the frame on the screen
		//setLocationRelativeTo(null);

		clientID = 0;
		CRS = this;

		//start the server SwingWorker in a background thread
		ModelSwingWorker tcpServer = new ModelSwingWorker(this); 
		tcpServer.execute();
	}
	
	/**
	 * 
	 * @param imageLoad - String of default image, will probably make void in future
	 * @return int - index of screen or -1 if addScreen failed
	 */
	public synchronized int addScreen(String imageLoad){
		try {
//			int index = 0;
//			if(!clientIDs.isEmpty()){
//				for(int takenId : clientIDs){
//					if(takenId != index){
//						break;
//					}
//					index++;
//				}
//			}
//			
			Container perUserContent = new Container();
			perUserContent.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			//Add the screen for a client device
			BufferedImage screenShot = ImageIO.read(new File(imageLoad));
			screenShots.add(screenShot);
//			screenShots.insertElementAt(screenShot, index);
//			int index = screenShots.indexOf(screenShot);
			JLabel screenLabel = new JLabel(new ImageIcon( screenShot ));
			screenLabels.add(screenLabel);
//			screenLabels.insertElementAt(screenLabel, index);
			perUserContent.add( screenLabel, gbc );   
			
			gbc.gridy = 1;
			//Add the lock button for a client device
			JButton clientLock = new JButton("Lock Device " + clientID);
			Dimension buttonSize = new Dimension(100, 1);
			clientLock.setMaximumSize(buttonSize);
			clientLock.addActionListener(CRS);
			perUserContent.add(clientLock, gbc);
			clientLocks.add(clientLock);
//			clientLocks.insertElementAt(clientLock, index);
			containers.add(perUserContent);
//			containers.insertElementAt(perUserContent, index);
			
			remoteClientCommands.add("NOACTION");
			clientIDs.add(clientID);
//			clientIDs.insertElementAt(clientID, index);
			clientID++;

			content.add(containers.lastElement());
//			content.add(containers.elementAt(index), index);
			content.validate();
			content.repaint();
			return (clientID-1);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} 
	}
	
	public synchronized void removeScreen(int i){
		int index = clientIDs.indexOf(i);
		Container cont =  containers.elementAt(index);
//		containers.remove(button);
//		content.remove(button);
		content.remove(cont);
		
		screenShots.remove(index);
		screenLabels.remove(index);
		clientLocks.remove(index);
		remoteClientCommands.remove(index);
		clientIDs.remove(index);
		containers.remove(index);
		
		content.validate();
		content.repaint();
	}
	
	class ModelSwingWorker extends 
	SwingWorker<ClassRoomServer, Void>{
		private final ClassRoomServer crsModel;
		
		public ModelSwingWorker(ClassRoomServer crsObj){
			this.crsModel = crsObj;
		}
		
		@Override
		protected synchronized ClassRoomServer doInBackground() throws Exception {
			try 
			{ 	
				System.out.println("Before Host OBTAIN");

				try{
					final int CONSUMERS = 50;
					final int BUFFER_SIZE = 100;
					
					Buffer<IndexedSocket> buffer = new BufferImpl<IndexedSocket>(BUFFER_SIZE);
					
					Thread producerThread = new Thread(new Producer(buffer, CRS));
					producerThread.setName("Producer");
					producerThread.start();
					
//					publish();
					
					ArrayList<Thread> consumerThreads = new ArrayList<Thread>();
					for(int i=0;i<CONSUMERS;i++){
						Thread thread = new Thread(new Consumer(buffer, CRS));
						consumerThreads.add(thread);
						thread.setName("Consumer-" + i);
						thread.start();
					}
				}finally {	
				}
			}finally{}
			return null;
		}
		
		@Override
		protected void process(java.util.List<Void> chunks) {
			addScreen("./hide_Sit.bmp");
			crsModel.addScreen("./hide_Sit.bmp");
		};
	};

	@Override
	public void paint(Graphics g) {
		//screenShot = ImageIO.read(new File("./hide_Sit.bmp"));
		//		System.out.println("TRY RELOAD");
		//screenLabel = new JLabel(new ImageIcon( screenShot ));
//		if(screenShot != null){ screenLabel.setIcon(new ImageIcon( screenShot )); }
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

		//Lock All
		if( e.getActionCommand().equals("Lock All") ){
			System.out.println("Lock All Tablets Pressed");		
//			RemoteCommand = "LOCK";
			for(int i=0; i < remoteClientCommands.size(); i++){
				remoteClientCommands.setElementAt("LOCK", i);
			}
			LockAll.setText("Unlock All");
		}
		//Unlock All
		if( e.getActionCommand().equals("Unlock All") ){
			System.out.println("Unlock Tablet Pressed");
//			RemoteCommand = "UNLOCK";
			for(int i=0; i < remoteClientCommands.size(); i++){
				remoteClientCommands.setElementAt("UNLOCK", i);
			}
			LockAll.setText("Lock All");
		}
		
		System.out.println(screenShots.size());
		System.out.println(screenShots.capacity());
		//Lock Selected Client
		for(int i=0; i < screenShots.size(); i++){
			if( e.getActionCommand().equals("Lock Device " + clientIDs.elementAt(i)) ){
				System.out.println("Lock Device " + i);
				
				remoteClientCommands.set(i, "LOCK");
				clientLocks.elementAt(i).setText("Unlock Device " + clientIDs.elementAt(i));
//				int x =0;
//				for(JButton jBut : clientLocks){
//					x++;
//					if(jBut.equals(e.getSource())){
//						
//					}
//				}
				
//				RemoteCommand = "LOCK"+i;
			}
		}
		//Unlock Selected Client
		for(int i=0; i < screenShots.size(); i++){
			if( e.getActionCommand().equals("Unlock Device " + clientIDs.elementAt(i)) ){
				System.out.println("Unlock Device " + i);
				remoteClientCommands.set(i, "UNLOCK");
				clientLocks.elementAt(i).setText("Lock Device " + clientIDs.elementAt(i));
			}
		}
		System.out.println("");
	}

	public void updatePaint(BufferedImage screenShot, int i){
		int index = clientIDs.indexOf(i);
		if(index != -1){
			screenShots.setElementAt(screenShot, index);

			if(screenShots.get(index) != null){
				JLabel screenLabel = screenLabels.get(index);
				screenLabel.setIcon(new ImageIcon( screenShot ));
				screenLabels.setElementAt(screenLabel, index); 
			}

			repaint();
		}
	}
	
	public void resetRemoteCommand(int i){
		int index = clientIDs.indexOf(i);
		remoteClientCommands.set(index, "NOACTION");
	}
	
	public String getRemoteCommand(int i){
		int index = clientIDs.indexOf(i);
		return remoteClientCommands.elementAt(index);
	}
}
