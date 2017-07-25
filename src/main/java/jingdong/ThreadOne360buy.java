package jingdong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import view.util.Util;

public class ThreadOne360buy extends Thread {
	java.util.concurrent.CountDownLatch c;
	ArrayList al;// ��¼��ɱ��Ʒҳ��
	float price = 0.0f;// ��Ʒ�۸�
	float discount = 0.0f;// ��Ʒ�ۿ�
	// ���ڱ����߳���Ϣ���������Ŀ���ô�����
	private static List<Thread> runningThreads = new ArrayList<Thread>();

	// �����һ������������̫���ã��߳��ⷽ��һֱ�о��ǱȽϸ��ӵģ�
	public ThreadOne360buy(java.util.concurrent.CountDownLatch c) {
		this.c = c;
	}

	@Override
	public void run() {
		regist(this);// �߳̿�ʼʱע��
		// ��ӡ��ʼ���
		System.out.println(Thread.currentThread().getName() + "��ʼ...");
		try {
			// ץȡ�����ֻ���ɱҳ��
			this.getMessage("http://sale.360buy.com/act/8VTHFGr10CjMDyZ.html#01");

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			c.countDown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			c.countDown();
		}
		c.countDown();
		unRegist(this);// �߳̽���ʱȡ��ע��
		// ��ӡ�������
		System.out.println(Thread.currentThread().getName() + "����.");
	}

	public void regist(Thread t) {
		synchronized (runningThreads) {
			runningThreads.add(t);
		}
	}

	public void unRegist(Thread t) {
		synchronized (runningThreads) {
			runningThreads.remove(t);
		}
	}

	public static boolean hasThreadRunning() {
		// ͨ���ж�runningThreads�Ƿ�Ϊ�վ���֪���Ƿ����߳�δִ����
		return (runningThreads.size() > 0);
	}

	/**
	 * ���ֻ���ɱҳ���ȡ prodcut����,product skuid,skuidkey,price,store��Ϣ
	 * 
	 * @param url
	 *            ���ֻ���ɱҳ��
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void getMessage(String url) throws ClientProtocolException,
			IOException {
		al = getMainUrl(down(url));

		Util.println(al);
		if (al.size() == 0) {
			c.countDown();
			System.exit(0);
			return;
		}

		for (int i = 0; i < al.size(); i++) {
			StringBuffer sb = new StringBuffer();
			StringBuffer openUrl = new StringBuffer();
			openUrl.append("http://www.360buy.com/product/");
			openUrl.append(al
					.get(i)
					.toString()
					.subSequence(al.get(i).toString().lastIndexOf('/') + 1,
							al.get(i).toString().lastIndexOf('.')));
			openUrl.append(".html");
			// 557673
			sb.append("http://d.360buy.com/fittingInfo/get?skuId=");
			sb.append(al
					.get(i)
					.toString()
					.subSequence(al.get(i).toString().lastIndexOf('/') + 1,
							al.get(i).toString().lastIndexOf('.')));
			sb.append("&callback=Recommend.cbRecoFittings");
			Util.println(sb.toString());
			// map�б�����ǲ�Ʒname,price,�ۿ���Ϣ
			Util.println("Al(" + i + ") down:" + sb.toString());
			HashMap<String, String> hm = parseProduct(down(sb.toString()));
			// ����ƥ��۸���Ϣ��ƥ������Ϣ
			filter(hm, openUrl.toString());// ���˼۸�,����������Ͼʹ������
		}
	}

	/**
	 * һ����֤����
	 * 
	 * @param hm
	 *            �����ż۸���Ϣ
	 * @param url
	 *            ��Ʒҳ��
	 */
	public void filter(HashMap<String, String> hm, String url) {// url���ǲ�Ʒҳ��
	// view.Util.oenCMD.openWinExe(null,url);
	// �ǲ���Ӧ���Ȳ鿴��棿
		String skuidkey = parseSkuidkey(url);
		if (!hasStore(skuidkey)) {
			Util.println("-------------------------------------");
			Util.println("û�п���ˣ�");
			Util.println("-------------------------------------");
			// �����������Ա����߳��ж�
			c.countDown();
			// Ӧ�ý������߳�Ŷ��
			return;
		}

		if (hm.get("skuid").equals("201602")) {// �ж�//Ħ������skuid=201602
			// ����ļ۸���д���ˣ�����ǰ�øĹ������С�
			this.setPrice(499.0f);
			// �ǲ���Ӧ�ô򿪿���̨��
			if (Float.parseFloat(hm.get("price")) <= this.getPrice()) {
				view.util.oenCMD.openWinExe(null, url);
			}
		} else if (hm.get("skuid").equals("675647")) {// ����skuid=675647
		// //����ļ۸���д���ˣ�����ǰ�øĹ������С�
		// this.setPrice(699.0f);
		// //�ǲ���Ӧ�ô򿪿���̨��
		// if(Float.parseFloat(hm.get("price"))<=this.getPrice()){
		// view.Util.oenCMD.openWinExe(null,url);
		// }
		}

	}

	/**
	 * �����˲�Ʒҳ����name,skuid��price��Ϣ
	 * 
	 * @param doc
	 * @return
	 */
	public static HashMap<String, String> parseProduct(Document doc) {
		String text = doc.text();
		String docc = text.substring(text.indexOf("master") + 9,
				text.indexOf("fittings") - 3).replaceAll("[\\s]", "");
		String[] ss = docc.split(",");
		HashMap<String, String> hm = new HashMap<String, String>();
		for (String it : ss) {
			String string = it.replaceAll("\"", "");
			if (string.contains("\\u"))
				string = unicodeDecode(string);

			String[] str = string.split(":");
			hm.put(str[0], str[1]);
		}
		Util.println(hm);
		return hm;
	}

	/**
	 * ����unicode�ַ���ת������ʾ�ַ������֣�����̫ͨ��
	 * 
	 * @param it
	 *            : \u6a5d
	 * @return
	 */
	public static String unicodeDecode(String it) {// �и�ȱ�㣬����ǰ����ַ��޷�ȥ��
		Util.println(it);
		String regex = "(\\\\u[0-9a-f]{4})";
		Pattern pt = Pattern.compile(regex);
		Matcher mc;
		StringBuffer sb;
		StringBuffer sba = new StringBuffer();
		mc = pt.matcher(it);
		while (mc.find()) {
			sb = new StringBuffer();
			mc.appendReplacement(
					sba,
					sb.append(
							(char) Integer.parseInt((mc.group(1).substring(2)),
									16)).toString());
		}
		return sba.toString();
	}

	/**
	 * �����ĵ������������ݣ�
	 * 
	 * @param url
	 *            ����ҳ��
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Document down(String url) throws ClientProtocolException,
			IOException {
		Document doc = null;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		Util.println("DownLoad:" + url);
		HttpGet get = new HttpGet(url);
		HttpResponse response;
		response = httpClient.execute(get);
		HttpEntity entity = response.getEntity();
		doc = Jsoup.parse(entity.getContent(), "utf-8", "");
		// �ͷ���Դ
		EntityUtils.consume(entity);
		// �ر�����
		httpClient.getConnectionManager().shutdown();
		return doc;
	}

	/**
	 * �����˱��������Ϣ
	 * 
	 * @param url
	 *            ������ҳ��
	 * @param code
	 *            ����
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Document down(String url, String code)
			throws ClientProtocolException, IOException {
		Document doc = null;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		Util.println("DownLoad:" + url);
		HttpGet get = new HttpGet(url);
		HttpResponse response;
		response = httpClient.execute(get);
		HttpEntity entity = response.getEntity();
		doc = Jsoup.parse(entity.getContent(), code, "");
		// �ͷ���Դ
		EntityUtils.consume(entity);
		// �ر�����
		httpClient.getConnectionManager().shutdown();
		return doc;
	}

	/**
	 * �������� ��ɱҳ���еĲ�Ʒ���ռ�������
	 * 
	 * @param doc
	 * @return
	 */
	public static ArrayList<String> getMainUrl(Document doc) {
		if (doc.equals("") || doc == null)
			return null;
		try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<String> urls = new ArrayList<String>();
		String rule = "map[name=Map] >area[href~=product]";
		/**
		 * ��ʼ����
		 */
		Elements elements = doc.select(rule);
		for (Element e : elements) {
			// Util.println(e.absUrl("abs:href"));
			urls.add(e.absUrl("abs:href"));
		}
		return urls;
	}

	/**
	 * ��ȡskuidkey,���ڲ�ѯ��Ʒ�����Ϣ
	 * 
	 * @param url
	 * @return
	 */
	public static String parseSkuidkey(String url) {
		Document doc = null;
		try {
			doc = down(url, "gb2312");
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Util.println(doc.select("script"));
		String text = null;
		for (Element e : doc.select("script")) {
			if (e.data().contains("skuidkey:")) {
				text = e.data();
				break;
			}
		}
		// skuidkey:'7D45919EA8242511DAA5CC7C6D7B351C'
		text = text.substring(text.indexOf("skuidkey:") + 10,
				text.indexOf("skuidkey:") + 42);
		Util.println("---------------------------------");
		Util.println(text);
		return text;
	}

	/**
	 * �鿴�����Ϣ
	 * 
	 * @param skuidkey
	 * @return
	 */
	public static boolean hasStore(String skuidkey) {// ����ط�û�д���ֱ����ȡ������е���Ϣ
		String address = null;
		boolean hasStore = false;
		if (skuidkey != null && !"".equals(skuidkey))
			address = "http://price.360buy.com/stocksoa/StockHandler.ashx?callback=getProvinceStockCallback&type=pcastock&skuid="
					+ skuidkey + "&provinceid=1&cityid=2800&areaid=2850";
		else {
			Util.println("����skuidkey����");
		}
		try {
			if (parseStore(down(address))) {
				hasStore = true;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hasStore;
	}

	/*
	 * if(array[1]=="34"||array[1]=="18"){ changeCart(false);
	 * djdarea.stockInfoDom.html("<strong class='store-over'>�޻�</strong>"); }
	 * else if(array[1]=="0"){ changeCart(false);
	 * djdarea.stockInfoDom.html("<strong class='store-over'>�޻�</strong>"); }
	 * else if(array[2]=="0"&&array[4]!="2"){ changeCart(false);
	 * djdarea.stockInfoDom.html("�ܱ�Ǹ������Ʒ�޷�������ѡ�������"); } else
	 * if(array[1]=="33"||array[1]=="5"){ changeCart(true);
	 * djdarea.stockInfoDom.
	 * html("<strong>�ֻ�</strong>"+(array[4]=="1"?"��������"+(array
	 * [3]=="0"?"��":"")+"֧�ֻ�������":"")+cashdesc); } else if(array[1]=="36"){
	 * changeCart(true);
	 * djdarea.stockInfoDom.html("<strong>Ԥ��</strong>"+(array[4
	 * ]=="1"?"��������"+(array[3]=="0"?"��":"")+"֧�ֻ�������":"")+cashdesc); } else
	 * if(array[1]=="39"){ changeCart(true);
	 * djdarea.stockInfoDom.html("<strong>��;</strong>"
	 * +(array[4]=="1"?"��������"+(array[3]=="0"?"��":"")+"֧�ֻ�������":"")+cashdesc); }
	 * else if(array[1]=="40"){ changeCart(true);
	 * djdarea.stockInfoDom.html("<strong>�����</strong>"
	 * +(array[4]=="1"?"��������"+(array[3]=="0"?"��":"")+"֧�ֻ�������":"")+cashdesc); }
	 */
	/**
	 * ���������Ϣ
	 * 
	 * @param doc
	 * @return
	 */
	public static boolean parseStore(Document doc) {
		String text = doc.text();
		String docc = text.substring(text.indexOf("-") - 1,
				text.lastIndexOf(",") - 1);
		Util.println(docc);
		String[] store = docc.split("-");
		if (store[1].equals("34") || store[1].equals("18")) {
			// �޻�
			Util.println("�˵��޻�");
			return false;
		} else if (store[1].equals("33") || store[1].equals("5")) {
			// �ֻ�
			Util.println("�˵��ֻ�");
			return true;
		}
		Util.println(store[1]);
		return false;
	}

	// ����bean����
	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public float getDiscount() {
		return discount;
	}

	public void setDiscount(float discount) {
		this.discount = discount;
	}

}