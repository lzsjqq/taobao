import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: xyc
 * @Date: 2018/12/28 10:59
 * @Version 1.0
 */
public class CrawlIpProcessor implements PageProcessor {
    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);


    public static void main(String[] args) {

        List<Pipeline> list = new ArrayList<>();
        FilePipeline filePipeline = new FilePipeline();
        filePipeline.setPath("C:\\Users\\Rzxuser\\Desktop\\hosts");
        list.add(filePipeline);
        Spider.create(new CrawlIpProcessor()) // 实例化spider
                //从"https://github.com/code4craft"开始抓
                .addUrl("http://spys.one/free-proxy-list/")
                //开启5个线程抓取
                .thread(5)
                .setPipelines(list)
                .setDownloader(getProxyDownload("127.0.0.1", "8118"))
                //启动爬虫
                .run();
    }

    public static HttpClientDownloader getProxyDownload(String host, String port) {
        HttpClientDownloader clientDownloader = new HttpClientDownloader();
        Proxy proxy = new Proxy(host, Integer.valueOf(port));
        List<Proxy> list = new ArrayList<>();
        list.add(proxy);
        SimpleProxyProvider provider = new SimpleProxyProvider(list);
        clientDownloader.setProxyProvider(provider);
        return clientDownloader;
    }

    @Override
    public void process(Page page) {
        // 部分二：定义如何抽取页面信息，并保存下来
        List<Selectable> ips = page.getHtml().xpath("//table/tbody/tr/td[1]").nodes();
//        List<Selectable> ports = page.getHtml().xpath("//table/tbody/tr/td[3]/text()").nodes();
        List<String> ipAndPort = new LinkedList<>();
        for (int i = 0; i < ips.size(); i++) {
            String host = ips.get(i).get();
//            String port = ports.get(i).get();
//            if (TestProxyUtil.testProxy(host, port)) {
//                ipAndPort.add(host + ":" + port);
//            }
        }
        page.putField("proxy", ipAndPort);
//        if (page.getResultItems().get("name") == null) {
//            //skip this page
//            page.setSkip(true);
//        }
//        page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));
//        // 部分三： 从页面发现后续的url地址来抓取
//        page.addTargetRequests(page.getHtml().links().regex("/proxy/\\d.html").all());
    }

    @Override
    public Site getSite() {
        return site;
    }
}
