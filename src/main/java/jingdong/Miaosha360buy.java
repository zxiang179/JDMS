package jingdong;

public class Miaosha360buy {
	
	java.util.concurrent.CountDownLatch t = new java.util.concurrent.CountDownLatch(1);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(Thread.currentThread().getName() + "开始");
		Miaosha360buy ms360 = new Miaosha360buy();
		new ThreadOne360buy(ms360.t).start();
		while (true) {
			try {
				ms360.t.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000 * 60);// 间隔1分钟调用一次？
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ms360.t = new java.util.concurrent.CountDownLatch(1);
			new ThreadOne360buy(ms360.t).start();
			System.out.println("New Tread in while..");
		}
	}
}