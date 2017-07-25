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
	ArrayList al;// 记录秒杀产品页面
	float price = 0.0f;// 商品价格
	float discount = 0.0f;// 商品折扣
	// 用于保存线程信息，在这个项目里用处不大
	private static List<Thread> runningThreads = new ArrayList<Thread>();

	// 这个是一个计数器（不太会用，线程这方面一直感觉是比较复杂的）
	public ThreadOne360buy(java.util.concurrent.CountDownLatch c) {
		this.c = c;
	}

	@Override
	public void run() {
		regist(this);// 线程开始时注册
		// 打印开始标记
		System.out.println(Thread.currentThread().getName() + "开始...");
		try {
			// 抓取京东手机秒杀页面
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
		unRegist(this);// 线程结束时取消注册
		// 打印结束标记
		System.out.println(Thread.currentThread().getName() + "结束.");
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
		// 通过判断runningThreads是否为空就能知道是否还有线程未执行完
		return (runningThreads.size() > 0);
	}

	/**
	 * 从手机秒杀页面获取 prodcut链接,product skuid,skuidkey,price,store信息
	 * 
	 * @param url
	 *            ：手机秒杀页面
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
			// map中保存的是产品name,price,折扣信息
			Util.println("Al(" + i + ") down:" + sb.toString());
			HashMap<String, String> hm = parseProduct(down(sb.toString()));
			// 用来匹配价格信息。匹配库存信息
			filter(hm, openUrl.toString());// 过滤价格,如果条件符合就打开浏览器
		}
	}

	/**
	 * 一个验证方法
	 * 
	 * @param hm
	 *            保存着价格信息
	 * @param url
	 *            产品页面
	 */
	public void filter(HashMap<String, String> hm, String url) {// url既是产品页面
	// view.Util.oenCMD.openWinExe(null,url);
	// 是不是应该先查看库存？
		String skuidkey = parseSkuidkey(url);
		if (!hasStore(skuidkey)) {
			Util.println("-------------------------------------");
			Util.println("没有库存了！");
			Util.println("-------------------------------------");
			// 减掉计数，以便主线程判断
			c.countDown();
			// 应该结束子线程哦？
			return;
		}

		if (hm.get("skuid").equals("201602")) {// 判断//摩托罗拉skuid=201602
			// 这里的价格是写死了，运行前得改过来才行。
			this.setPrice(499.0f);
			// 是不是应该打开控制台？
			if (Float.parseFloat(hm.get("price")) <= this.getPrice()) {
				view.util.oenCMD.openWinExe(null, url);
			}
		} else if (hm.get("skuid").equals("675647")) {// 天语skuid=675647
		// //这里的价格是写死了，运行前得改过来才行。
		// this.setPrice(699.0f);
		// //是不是应该打开控制台？
		// if(Float.parseFloat(hm.get("price"))<=this.getPrice()){
		// view.Util.oenCMD.openWinExe(null,url);
		// }
		}

	}

	/**
	 * 解析了产品页面中name,skuid，price信息
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
	 * 处理unicode字符，转换成显示字符（汉字），不太通用
	 * 
	 * @param it
	 *            : \u6a5d
	 * @return
	 */
	public static String unicodeDecode(String it) {// 有个缺点，就是前面的字符无法去掉
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
	 * 返回文档对象（下载内容）
	 * 
	 * @param url
	 *            下载页面
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
		// 释放资源
		EntityUtils.consume(entity);
		// 关闭连接
		httpClient.getConnectionManager().shutdown();
		return doc;
	}

	/**
	 * 加入了编码控制信息
	 * 
	 * @param url
	 *            待下载页面
	 * @param code
	 *            编码
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
		// 释放资源
		EntityUtils.consume(entity);
		// 关闭连接
		httpClient.getConnectionManager().shutdown();
		return doc;
	}

	/**
	 * 用来解析 秒杀页面中的产品（收集）链接
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
		 * 开始解析
		 */
		Elements elements = doc.select(rule);
		for (Element e : elements) {
			// Util.println(e.absUrl("abs:href"));
			urls.add(e.absUrl("abs:href"));
		}
		return urls;
	}

	/**
	 * 获取skuidkey,用于查询商品库存信息
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
	 * 查看库存信息
	 * 
	 * @param skuidkey
	 * @return
	 */
	public static boolean hasStore(String skuidkey) {// 这个地方没有处理，直接提取浏览器中的信息
		String address = null;
		boolean hasStore = false;
		if (skuidkey != null && !"".equals(skuidkey))
			address = "http://price.360buy.com/stocksoa/StockHandler.ashx?callback=getProvinceStockCallback&type=pcastock&skuid="
					+ skuidkey + "&provinceid=1&cityid=2800&areaid=2850";
		else {
			Util.println("解析skuidkey错误");
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
	 * djdarea.stockInfoDom.html("<strong class='store-over'>无货</strong>"); }
	 * else if(array[1]=="0"){ changeCart(false);
	 * djdarea.stockInfoDom.html("<strong class='store-over'>无货</strong>"); }
	 * else if(array[2]=="0"&&array[4]!="2"){ changeCart(false);
	 * djdarea.stockInfoDom.html("很抱歉，该商品无法送至您选择的区域"); } else
	 * if(array[1]=="33"||array[1]=="5"){ changeCart(true);
	 * djdarea.stockInfoDom.
	 * html("<strong>现货</strong>"+(array[4]=="1"?"，该区域"+(array
	 * [3]=="0"?"不":"")+"支持货到付款":"")+cashdesc); } else if(array[1]=="36"){
	 * changeCart(true);
	 * djdarea.stockInfoDom.html("<strong>预订</strong>"+(array[4
	 * ]=="1"?"，该区域"+(array[3]=="0"?"不":"")+"支持货到付款":"")+cashdesc); } else
	 * if(array[1]=="39"){ changeCart(true);
	 * djdarea.stockInfoDom.html("<strong>在途</strong>"
	 * +(array[4]=="1"?"，该区域"+(array[3]=="0"?"不":"")+"支持货到付款":"")+cashdesc); }
	 * else if(array[1]=="40"){ changeCart(true);
	 * djdarea.stockInfoDom.html("<strong>可配货</strong>"
	 * +(array[4]=="1"?"，该区域"+(array[3]=="0"?"不":"")+"支持货到付款":"")+cashdesc); }
	 */
	/**
	 * 解析库存信息
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
			// 无货
			Util.println("此地无货");
			return false;
		} else if (store[1].equals("33") || store[1].equals("5")) {
			// 现货
			Util.println("此地现货");
			return true;
		}
		Util.println(store[1]);
		return false;
	}

	// 几个bean方法
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