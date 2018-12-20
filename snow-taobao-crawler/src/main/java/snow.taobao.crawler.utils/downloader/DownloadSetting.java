package snow.taobao.crawler.utils.downloader;

import com.datatub.rhino.core.proxy.IpObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hangs on 2016/5/16-15:31.
 */
public class DownloadSetting {
    private String charset = "UTF-8";       // 字符编码，默认UTF-8
    private boolean forceCharset = false;   // 强制使用设定的字符编码，默认false
    private boolean openProxy = false;      // 是否开启代理
    private int retryTimes = 3;             // 重试次数
    private int timeout = 60000;            // 超时时间, 单位是毫秒
    private int retryInterval = 5000;       // 重试间隔时间, 单位毫秒
    private boolean useMobileUserAgent;     // 使用手机浏览器UA
    private String method = "GET";          // 默认请求方法
    private boolean openGzip = false;
    private Map<String, String> headers = new HashMap<>();  // 自定义header
    private Map<String, Object> clientSetting;              // http client setting params
    private String forceRetryStr = null;                    // 匹配到该字符串强制重试
    private boolean randomLocalIP = false;                  //随机使用本地外网IP
    private String forceLocalIP = null;                     //指定使用本地外网IP地址
    private boolean isCacheCookie = true;   // 是否开启自动缓存cookie
    private boolean openCodeRetry = true;   // 开启智能重试, 关闭后不判断状态码
    private List<IpObject> proxyList = null;   // 候选代理列表

    public DownloadSetting() {
    }

    public DownloadSetting(DownloadSetting setting) {
        setCharset(setting.getCharset());
        setForceCharset(setting.isForceCharset());
        setOpenProxy(setting.isOpenProxy());
        setRetryTimes(setting.getRetryTimes());
        setTimeout(setting.getTimeout());
        setUseMobileUserAgent(setting.isUseMobileUserAgent());
        setMethod(setting.getMethod());
        setOpenGzip(setting.isOpenGzip());
        setHeaders(setting.getHeaders());
        setClientSetting(setting.getClientSetting());
        setForceRetryStr(setting.getForceRetryStr());
        setCacheCookie(setting.isCacheCookie());
        setOpenCodeRetry(setting.isOpenCodeRetry());
    }

    public boolean isCacheCookie() {
        return isCacheCookie;
    }

    public void setCacheCookie(boolean cacheCookie) {
        isCacheCookie = cacheCookie;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isOpenProxy() {
        return openProxy;
    }

    public void setOpenProxy(boolean openProxy) {
        this.openProxy = openProxy;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public boolean isOpenGzip() {
        return openGzip;
    }

    public void setOpenGzip(boolean openGzip) {
        this.openGzip = openGzip;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isUseMobileUserAgent() {
        return useMobileUserAgent;
    }

    public void setUseMobileUserAgent(boolean useMobileUserAgent) {
        this.useMobileUserAgent = useMobileUserAgent;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getClientSetting() {
        return clientSetting;
    }

    public void setClientSetting(Map<String, Object> clientSetting) {
        this.clientSetting = clientSetting;
    }

    public boolean isForceCharset() {
        return forceCharset;
    }

    public void setForceCharset(boolean forceCharset) {
        this.forceCharset = forceCharset;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getForceRetryStr() {
        return forceRetryStr;
    }

    public void setForceRetryStr(String forceRetryStr) {
        this.forceRetryStr = forceRetryStr;
    }

    @Override
    public String toString() {
        return "DownloadSetting{" +
                "charset='" + charset + '\'' +
                ", forceCharset=" + forceCharset +
                ", openProxy=" + openProxy +
                ", retryTimes=" + retryTimes +
                ", timeout=" + timeout +
                ", retryInterval=" + retryInterval +
                ", useMobileUserAgent=" + useMobileUserAgent +
                ", method='" + method + '\'' +
                ", openGzip=" + openGzip +
                ", headers=" + headers +
                ", clientSetting=" + clientSetting +
                ", forceRetryStr='" + forceRetryStr + '\'' +
                ", randomLocalIP=" + randomLocalIP +
                ", forceLocalIP='" + forceLocalIP + '\'' +
                ", isCacheCookie=" + isCacheCookie +
                ", openCodeRetry=" + openCodeRetry +
                ", proxyList=" + proxyList +
                '}';
    }

    public boolean isRandomLocalIP() {
        return randomLocalIP;
    }

    public void setRandomLocalIP(boolean randomLocalIP) {
        this.randomLocalIP = randomLocalIP;
    }

    public String getForceLocalIP() {
        return forceLocalIP;
    }

    public void setForceLocalIP(String forceLocalIP) {
        this.forceLocalIP = forceLocalIP;
    }

    public boolean isOpenCodeRetry() {
        return openCodeRetry;
    }

    public void setOpenCodeRetry(boolean openCodeRetry) {
        this.openCodeRetry = openCodeRetry;
    }

    public List<IpObject> getProxyList() {
        return proxyList;
    }

    public void setProxyList(List<IpObject> proxyList) {
        this.proxyList = proxyList;
    }
}
