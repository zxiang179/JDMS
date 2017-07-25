package test;
import java.io.FileOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;

/**
 * TODO(��һ�仰�������ļ�������)
 * 
 * @title: HttpClientDemo.java
 * @author Carl_Hugo
 * @date 2014-6-11 14:59:04
 */

public class HttpClientDemo
{

    /**
     * The main method.
     * 
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception
    {
        getResoucesByLoginCookies();
    }

    /**
     * ���ݵ�¼Cookie��ȡ��Դ
     * һ���쳣��δ������Ҫ�������쳣
     * 
     * @throws Exception
     */
    private static void getResoucesByLoginCookies() throws Exception
    {
        HttpClientDemo demo = new HttpClientDemo();
        String username = "965730955@qq.com";// ��¼�û�
        String password = "zxbzj123456";// ��¼����

        // ��Ҫ�ύ��¼����Ϣ
        String urlLogin = "http://www.iteye.com/login?name=" + username + "&password=" + password;

        // ��¼�ɹ�����Ҫ���ʵ�ҳ�� ������������Դ ��Ҫ�滻���Լ����ռ����ַ
        String urlAfter = "http://my.iteye.com/messages";

        DefaultHttpClient client = new DefaultHttpClient(new PoolingClientConnectionManager());

        /**
         * ��һ�������¼ҳ�� ���cookie
         * �൱���ڵ�¼ҳ������¼���˴���URL�� ���������
         * ��������б��൱��Ļ�����ʹ��HttpClient�ķ�ʽ�������
         * �˴���׸��
         */
        HttpPost post = new HttpPost(urlLogin);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        CookieStore cookieStore = client.getCookieStore();
        client.setCookieStore(cookieStore);

        /**
         * ���ŵ�¼����cookie������һ��ҳ�棬��������Ҫ��¼�������ص�url
         * �˴�ʹ�õ���iteye�Ĳ�����ҳ�������¼�ɹ�����ô��ҳ����ʾ����ӭXXXX��
         * 
         */
        HttpGet get = new HttpGet(urlAfter);
        response = client.execute(get);
        entity = response.getEntity();

        /**
         * ���������ŵ��ļ�ϵͳ�б���Ϊ myindex.html,����ʹ��������ڱ��ش� �鿴���
         */

        String pathName = "F:\\myindex.html";
        writeHTMLtoFile(entity, pathName);
    }

    /**
     * Write htmL to file.
     * ���������Զ�������ʽ�ŵ��ļ�ϵͳ�б���Ϊ.html�ļ�,����ʹ��������ڱ��ش� �鿴���
     * 
     * @param entity the entity
     * @param pathName the path name
     * @throws Exception the exception
     */
    public static void writeHTMLtoFile(HttpEntity entity, String pathName) throws Exception
    {

        byte[] bytes = new byte[(int) entity.getContentLength()];

        FileOutputStream fos = new FileOutputStream(pathName);

        bytes = EntityUtils.toByteArray(entity);

        fos.write(bytes);

        fos.flush();

        fos.close();
    }

}