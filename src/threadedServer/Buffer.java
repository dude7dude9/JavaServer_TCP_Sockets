package threadedServer;

public interface Buffer<T> {
	public void put(T element) throws InterruptedException;
	public T get() throws InterruptedException;
	public boolean isFull() throws InterruptedException;
	public boolean isEmpty() throws InterruptedException;
//	void put(T element, int index) throws InterruptedException;
//	T get(String index) throws InterruptedException;
//	T get(Indexer index) throws InterruptedException;
}
