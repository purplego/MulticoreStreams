package skyline.test;

public class ThreadTest {
	
	public static void main(String args[]) throws InterruptedException
	{
		//ExecutorService exec = Executors.newCachedThreadPool();
		Thread []t = new Thread[3];
		
		for (int i = 0; i<3; i++)
		{
			t[i]= new Thread(new MyThread(i));
			t[i].start();
		}
		
		for(int i = 0 ; i< 3;i++)
			t[i].join();
		
		System.out.println("finish!!");
	}
}

class MyThread implements Runnable
{
	int i;
	public MyThread(int i ) {
		this.i = i ;
	}

	@Override
	public void run() {
		System.out.println("thread "+ i + " sleep" );
		try {
			Thread.currentThread().setName("test");
			System.err.println(Thread.currentThread().getName());
			System.err.println(Thread.currentThread().getId());
			Thread.sleep(1000*5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("thread "+ i + " finish!" );
		
		
	}
	
}
