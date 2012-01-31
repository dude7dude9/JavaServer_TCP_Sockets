package threadedServer;

public class BufferMonitor implements Runnable {

	private Buffer fBuffer;
	
	public BufferMonitor(Buffer buffer) {
		fBuffer = buffer;
	}
	
	public void run() {
		try {
			while(!fBuffer.isEmpty()) {
				Thread.sleep(100);
			} 
		} catch(InterruptedException e) {
			// No action necessary.
			System.err.println(e);
		}	
	}
}
