package threadedServer;

public class BufferImpl<T> implements Buffer<T> {
	
	private int fCapacity; 	// Number of elements that can be stored.
	private int fFront; 	// Index of element at the front of the buffer.
	private int fBack; 		// Index of next free slot within the buffer.
	private T[ ] fElements; // Array to store elements.
//	private int[] indices;	// Array of socket data's image display indices
	
	public BufferImpl(int capacity) {
		fCapacity = capacity;
		fElements = (T[]) new Object[fCapacity];
//		indices = 	new int[fCapacity];
		fFront = 0;
		fBack = 0;
	}
	
	@Override
	public synchronized T get() throws InterruptedException {
		// Wait if necessary for a slot to be filled.
		while(isEmpty()){
			wait();
		}

		T result = fElements[fFront];
//		index = index.replaceFirst("-1", Integer.toString(indices[fFront]));
//		index.index = indices[fFront];
		
		fElements[fFront] = null;
//		indices[fFront] = -1;
		
		fFront = ( fFront + 1) % fCapacity;

		notifyAll();		
		return result;
	}
	
	@Override
	public synchronized void put(T element) throws InterruptedException {
		// Wait if necessary for an empty slot.
		while(isFull()){
			wait();
		}
		
		fElements[fBack] = element;
//		indices[fBack] = index;
		fBack = (fBack + 1) % fCapacity;	
		
		notifyAll();
	}
	
	@Override
	public boolean isFull() {
		return fFront == fBack && fElements[fFront] != null;
	}
	
	@Override
	public boolean isEmpty() {
		return fFront == fBack && fElements[fFront] == null;
	}
}
