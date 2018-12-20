package snow.taobao.crawler.utils.downloader;

/**
 * @author zebin
 * @since 2018-05-21.
 */
public interface IDownloader {

    /**
     * 发出请求
     *
     * @param url      请求地址
     * @param postData 可以为 String/Map
     * @return 响应结果
     * @throws DownloadException
     */
    String request(String url, Object postData) throws DownloadException;

    DownloadSetting getDownloadSetting();

    void setDownloadSetting(DownloadSetting setting);
}
