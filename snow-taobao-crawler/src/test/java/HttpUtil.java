import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpUtil {
    public static boolean httpGet(String proxyHost, Integer proxyPort, String url) {
        StringBuffer sb = new StringBuffer();
        //创建HttpClient实例
        HttpClient client = getHttpClient(proxyHost, proxyPort);
        //创建httpGet
        HttpGet httpGet = new HttpGet(url);
        //执行
        try {
            HttpResponse response = client.execute(httpGet);
            HttpEntity entry = response.getEntity();
            if (entry != null) {
                InputStreamReader is = new InputStreamReader(entry.getContent());
                BufferedReader br = new BufferedReader(is);
                String str ;
                while ((str = br.readLine()) != null) {
                    sb.append(str.trim());
                }
                br.close();
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(sb.toString());

        return true;
    }


    /**
     * 设置代理
     *
     * @param proxyHost
     * @param proxyPort
     * @return
     */
    public static HttpClient getHttpClient(String proxyHost, Integer proxyPort) {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String userName = "";
        String password = "";
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(proxyHost, proxyPort),
                new UsernamePasswordCredentials(userName, password));
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
        return httpClient;
    }

} 