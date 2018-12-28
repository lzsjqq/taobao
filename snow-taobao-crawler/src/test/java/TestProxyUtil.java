import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: xyc
 * @Date: 2018/12/28 16:23
 * @Version 1.0
 */
public class TestProxyUtil {

    private static Site site = Site.me().setRetryTimes(1).setSleepTime(1000);

    /**
     * 测试可行性
     *
     * @param host
     * @param port
     * @return
     */
    public static boolean testProxy(String host, String port) {
        HttpClientDownloader clientDownloader = new HttpClientDownloader();
        Proxy proxy = new Proxy(host, Integer.valueOf(port));
        List<Proxy> list = new ArrayList<>();
        list.add(proxy);
        SimpleProxyProvider provider = new SimpleProxyProvider(list);
        clientDownloader.setProxyProvider(provider);
        Request request = new Request("https://twitter.com/");
        Page page = clientDownloader.download(request, new Task() {
            @Override
            public String getUUID() {
                return null;
            }

            @Override
            public Site getSite() {
                return site;
            }
        });
        return page.isDownloadSuccess() && (page.getStatusCode() == 200);
    }

    public static void main(String[] args) {
        boolean b = testProxy("127.0.0.1", "8118");
        System.out.println(b);
    }

}
