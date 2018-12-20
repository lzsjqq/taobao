package snow.taobao.crawler.utils.downloader;

import com.datatub.rhino.core.base.RhinoCoreConfig;
import com.datatub.rhino.core.proxy.IProxyManager;
import com.datatub.rhino.core.proxy.IpObject;
import com.datatub.rhino.core.proxy.IpServiceException;
import com.datatub.rhino.core.proxy.ProxyManagerFactory;
import com.datatub.rhino.core.utils.NetworkUtil;
import com.datatub.rhino.core.utils.http.HttpUtil;
import com.datatub.rhino.core.utils.http.WebClientDevWrapper;
import com.datatub.rhino.core.utils.tuples.Tuple3;
import com.yeezhao.commons.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;


/**
 * @author sugan
 * @since 2016-06-22.
 */
public class CommonDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(CommonDownloader.class);
    private static final int SOCK_TIMEOUT = 30000;
    private static final int CONNECT_TIMEOUT = 30000;
    private static final String DEFAULT_UA_PC_CHROME = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36";
    private DownloadSetting downloadSetting = null;
    //缓存的cookie 键值
    final private Map<String, String> cacheCookieMap = new HashMap<>();
    private HttpContext cacheContext;
    private HttpResponse cacheResponse;
    private IProxyManager proxyManager = null;

    public CommonDownloader(DownloadSetting downloadSetting) {
        this.downloadSetting = downloadSetting;
        //可以自定义proxy manager
        proxyManager = ProxyManagerFactory.newInstance(RhinoCoreConfig.getInstance());
    }

    /**
     * 根据header名获取全部header
     *
     * @param headers header array
     * @param name    headerName
     * @return
     */
    private Header[] getHeaders(Header[] headers, String name) {
        ArrayList<Header> headersFound = new ArrayList<>();
        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase(name)) {
                headersFound.add(header);
            }
        }
        return headersFound.toArray(new Header[headersFound.size()]);
    }

    /**
     * 通用方法
     *
     * @param url
     * @return
     */
    public String processUrl(String url) {
        return processUrl(url, null);
    }

    /**
     * post data为Map类型
     * 样例: curl "http://datatub5:20425/app/v2/start?appId=1&crawlers=3" -X POST -H "content-type: application/x-www-form-urlencoded; charset=UTF-8" --data-urlencode  'seeds=[{"keyword":"食品"}]'
     *
     * @param url
     * @param postData
     * @return
     */
    public String processUrl(String url, Map<String, String> postData) {
        String response = null;
        try {
            response = processUrlThrowable(url, postData);
        } catch (DownloadException e) {
            LOG.warn(String.format("download exception, code: %s, url: %s, post data: %s, msg: %s", e.getHttpStatus(), url, postData, e.getMessage()), e);
        }
        return response;
    }

    /**
     * post data为字符串类型
     * 样例: curl -X POST 'http://hmly1:9200/skynet_public_content/_search' -d' {"query":{"match":{"id":"059651c2b7c8dd56b4c708b9fff02e99"}}}'
     *
     * @param url
     * @param postData
     * @param postStr
     * @return
     */
    public String processUrl(String url, Map<String, String> postData, String postStr) {
        String response = null;
        try {
            if (postData == null && StringUtils.isNotEmpty(postStr)) {
                response = processUrlThrowable(url, postStr);
            } else if (postData != null) {
                response = processUrl(url, postData);
            } else {
                response = processUrl(url);
            }
        } catch (DownloadException e) {
            LOG.warn(String.format("download exception, code: %s, url: %s, msg: %s", e.getHttpStatus(), url, e.getMessage()), e);
        }
        return response;
    }

    public String processUrlThrowable(String url) throws DownloadException {
        return processUrlThrowable(url, null);
    }

    public String processUrlThrowable(String url, Object postData) throws DownloadException {
        cacheContext = null;
        cacheResponse = null;
        // 判断是否为合法URL
        if (StringUtils.isEmpty(url)) {
            throw new DownloadException("URL should not empty");
        }
        // 判断URL是否包含空字符串
        if (url.trim().contains(" ")) {
            url = url.replace(" ", "%20");
        }
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage() + ", url: " + url, e);
            throw new DownloadException("URL error");
        }
        // 开始请求
        int sendTimes = downloadSetting.getRetryTimes() + 1;
        String resultStr = null;
        Tuple3<String, Integer, Header[]> tuple3 = null;
        //第几次重试
        int i = 0;
        boolean useHttps = false;
        //重试
        while (sendTimes-- > 0) {
            try {
                tuple3 = fetchUrl(url, postData, useHttps);
                Integer statusCode = tuple3._2();
                //自动缓存cookie
                String ckStr = getCookieStr(getHeaders(tuple3._3(), "Set-Cookie"));
                if (ckStr != null) {
                    downloadSetting.getHeaders().put("Cookie", ckStr);
                }
                if (downloadSetting.getHeaders().get("Referer") == null) {
                    downloadSetting.getHeaders().put("Referer", url);
                }

                resultStr = tuple3._1();
                if (!StringUtil.isNullOrEmpty(resultStr)
                        && !StringUtil.isNullOrEmpty(downloadSetting.getForceRetryStr())
                        && resultStr.contains(downloadSetting.getForceRetryStr())) {
                    LOG.info("force retry url:" + url);
                } else {
                    if (downloadSetting.isOpenCodeRetry()) {
                        //状态码为200
                        if (statusCode == 200) {
                            break;
                        } else if (statusCode == 410) {
                            throw new DownloadException(statusCode, resultStr, "Status Code:410 Gone");
                        } else if (StringUtils.startsWithAny(String.valueOf(statusCode), new String[]{"40", "50"})) {
                            throw new DownloadException(statusCode, resultStr, "Status Code Illegal");
                        }
                    } else {
                        break;
                    }
                }
            } catch (SSLException e) {
                useHttps = true;
                LOG.error(String.format("get page %s fail, left retry times:%d", url, sendTimes), e);
            } catch (IOException e) {
                LOG.error(String.format("get page %s fail, left retry times:%d", url, sendTimes), e);
                if (downloadSetting.getRetryTimes() == i) {
                    throw new DownloadException(e);
                }
            } catch (IpServiceException e) {
                LOG.error(String.format("get Ip object fail, left retry times:%d", sendTimes), e);
            } catch (DownloadException e) {
                throw e;
            } catch (Exception e) {
                LOG.error("unknow error.", e);
            }
            ++i;

            //重试时间间隔
            int sleepMillseconds = downloadSetting.getRetryInterval();
            if (sleepMillseconds > 0 || tuple3 == null) {
                try {
                    LOG.info(String.format("retry interval %s ms, left retry times:%s， retry number:%s", sleepMillseconds * i, sendTimes, i));
                    TimeUnit.MILLISECONDS.sleep(sleepMillseconds * i);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return resultStr;
    }

    /**
     * @param url      请求URL
     * @param postData post data
     * @param useHttps 使用https协议
     * @return 获取成功返回 null字符串，注意响应内容可能Empty，获取失败返回null
     * @throws IOException
     * @throws IpServiceException
     */
    protected Tuple3<String, Integer, Header[]> fetchUrl(String url, Object postData, boolean useHttps)
            throws IOException, IpServiceException, DownloadException {
        // URL unsafe character escape
        String safeUrl = escapeUnsafe(url);

        if (postData != null) {
            downloadSetting.setMethod("POST");
        }
        String charset = downloadSetting.getCharset();
        Map<String, Object> clientParam = downloadSetting.getClientSetting();
        Map<String, String> headers = downloadSetting.getHeaders();
        IpObject ipObject = null;

        if (downloadSetting.isOpenProxy() && downloadSetting.getProxyList() != null) {
            int idx = RandomUtils.nextInt(0, downloadSetting.getProxyList().size());
            ipObject = downloadSetting.getProxyList().get(idx);
        }
        if (ipObject == null && downloadSetting.isOpenProxy()) {
            ipObject = proxyManager.getIpObject();
        }

        DefaultHttpClient httpclient = new DefaultHttpClient();

        InetAddress ipSelected = null;
        if (downloadSetting.getForceLocalIP() != null) {
            ipSelected = InetAddress.getByName(downloadSetting.getForceLocalIP());
        }
        if (downloadSetting.isRandomLocalIP()) {
            //开启本地多出口IP切换
            ipSelected = NetworkUtil.getRandomLocalIpv4Addr();
        }
        if (ipSelected != null) {
            httpclient.getParams().setParameter(ConnRoutePNames.LOCAL_ADDRESS, ipSelected);
        }


        if (safeUrl.startsWith("https://") || useHttps) {
            // HTTPS站点
            httpclient = (DefaultHttpClient) WebClientDevWrapper.wrapClient(httpclient);
        }

        httpclient.addRequestInterceptor((request, context) -> {
            if (!request.containsHeader("Accept-Encoding")) {
                request.addHeader("Accept-Encoding", "gzip");
            }
        });

        httpclient.addRequestInterceptor((request, context) -> {
            List<Cookie> cookies = ((CookieStore) context.getAttribute(ClientContext.COOKIE_STORE)).getCookies();
            Map<String, String> requestCookies = new HashMap<>(cacheCookieMap);
            for (Cookie cookie : cookies) {
                requestCookies.put(cookie.getName(), cookie.getValue());
            }
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : requestCookies.entrySet()) {
                builder.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
            String cookieStr = builder.toString();
            if (!StringUtils.isEmpty(cookieStr)) {
                request.removeHeaders("Cookie");
                request.removeHeaders("cookie");
                request.setHeader("Cookie", cookieStr);
            }
        });

        httpclient.addResponseInterceptor((response, context) -> {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                    HeaderElement[] codecs = ceheader.getElements();
                    for (HeaderElement codec : codecs) {
                        if ("gzip".equalsIgnoreCase(codec.getName())) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });

        String html = null;
        try {
            // 使用代理
            if (ipObject != null) {
                HttpHost proxy = new HttpHost(ipObject.getHost(), ipObject.getPort());
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                LOG.info(String.format("use proxy - %s:%s", ipObject.getHost(), ipObject.getPort()));
            }
            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);
            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SOCK_TIMEOUT);
            if (downloadSetting.getTimeout() > 0) {
                httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, downloadSetting.getTimeout());
                httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, downloadSetting.getTimeout());
            }
            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 50);

//            httpclient.setRedirectStrategy(new LaxRedirectStrategy());
            httpclient.setRedirectStrategy(new RedirectWithCookieStrategy());
            if (clientParam != null) {
                for (Map.Entry<String, Object> entry : clientParam.entrySet()) {
                    httpclient.getParams().setParameter(entry.getKey(), entry.getValue());
                }
            }

            HttpRequestBase httpMethod;
            switch (downloadSetting.getMethod().toUpperCase()) {
                case HttpGet.METHOD_NAME:
                    httpMethod = new HttpGet(safeUrl);
                    break;
                case HttpPost.METHOD_NAME:
                    httpMethod = new HttpPost(safeUrl);
                    break;
                case HttpHead.METHOD_NAME:
                    httpMethod = new HttpHead(safeUrl);
                    break;
                default:
                    httpMethod = new HttpGet(safeUrl);
            }
            httpMethod.setHeader("Accept", "*/*");
            if (postData != null && httpMethod instanceof HttpPost) {
                if (postData instanceof Map) {
                    List<NameValuePair> nvps2 = new ArrayList<>();
                    for (Map.Entry<String, String> entry : ((Map<String, String>) postData).entrySet()) {
                        nvps2.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                    }
                    ((HttpPost) httpMethod).setEntity(new UrlEncodedFormEntity(nvps2, "UTF-8"));
                } else if (postData instanceof String) {
                    ((HttpPost) httpMethod).setEntity(new StringEntity((String) postData, "UTF-8"));
                } else {
                    throw new RuntimeException("postData must be `Map` or `String`!");
                }
            }

            //自定义header
            httpMethod.setHeader("User-Agent", DEFAULT_UA_PC_CHROME);
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpMethod.setHeader(entry.getKey(), entry.getValue());
                }
            }

            cacheContext = new BasicHttpContext();
            cacheResponse = httpclient.execute(httpMethod, cacheContext);
            HttpEntity entity = cacheResponse.getEntity();
            if (httpMethod instanceof HttpHead) {
                html = "";
            } else {
                byte[] rawData = EntityUtils.toByteArray(entity);
                String encoding = downloadSetting.getCharset();
                if (!downloadSetting.isForceCharset()) {
                    encoding = EntityUtils.getContentCharSet(entity);
                    if (encoding == null) {
                        //编码检测
                        encoding = HttpUtil.detectHtmlEncode(rawData, charset);
                    }
                }
                html = new String(rawData, encoding);
            }
            return new Tuple3<>(html, cacheResponse.getStatusLine().getStatusCode(), cacheResponse.getAllHeaders());
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }


    /**
     * 从响应信息中获取cookie
     *
     * @param setCookieHeaders header array
     * @return cookie string
     */
    private String getCookieStr(Header setCookieHeaders[]) {
        if (!downloadSetting.isCacheCookie()) {
            LOG.info("does not cache cookie");
            return null;
        }

        //恢复上一次携带的cookie
        if (downloadSetting.getHeaders().get("Cookie") != null && cacheCookieMap.isEmpty()) {
            restoreCookieMap(downloadSetting.getHeaders().get("Cookie"));
        }

        //更新cookie
        return parseAndUpdateCookie(setCookieHeaders);
    }

    private String parseAndUpdateCookie(Header[] setCookieHeaders) {
        if (setCookieHeaders != null && setCookieHeaders.length > 0) {
            String cookie = "";
            for (int i = 0; i < setCookieHeaders.length; i++) {
                String cVal = setCookieHeaders[i].getValue().split(";", 2)[0];
                if (!StringUtil.isNullOrEmpty(cVal)) {
                    cookie += cVal;
                    if (i != setCookieHeaders.length - 1) {
                        cookie += ";";
                    }
                }
            }

            restoreCookieMap(cookie);
        }
        //返回已有的cookie
        String cookiesTmp = "";
        for (String key : cacheCookieMap.keySet()) {
            cookiesTmp += key + "=" + cacheCookieMap.get(key) + ";";
        }
        return cookiesTmp.length() > 0 ? cookiesTmp.substring(0, cookiesTmp.length() - 1) : null;
    }

    private void restoreCookieMap(String cookie) {
        String cookies[] = cookie.split(";");
        for (String c : cookies) {
            String[] segs = c.trim().split("=", 2);
            if(downloadSetting.isCacheCookie()) {
                cacheCookieMap.put(segs[0], segs.length == 1 ? "" : segs[1]);
            }
        }
    }

    public DownloadSetting getDownloadSetting() {
        return downloadSetting;
    }

    public void setDownloadSetting(DownloadSetting downloadSetting) {
        this.downloadSetting = downloadSetting;
    }

    private String escapeUnsafe(String url) {
        return url.replaceAll("\\|", "%7C").replaceAll("\\[", "%5B").replaceAll("\\]", "%5D");
    }

    private static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {

            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }


    public class RedirectWithCookieStrategy extends DefaultRedirectStrategy {

        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            if (response == null) {
                throw new IllegalArgumentException("HTTP response may not be null");
            } else {
                int statusCode = response.getStatusLine().getStatusCode();
                String method = request.getRequestLine().getMethod();
                Header locationHeader = response.getFirstHeader("location");
                switch (statusCode) {
                    case 301:
                    case 307:
                        return method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD");
                    case 302:
                        boolean flag = (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD") || method.equalsIgnoreCase("POST")) && locationHeader != null;
                        if (flag) {
                            Header[] setCookieHeaders = getHeaders(response.getAllHeaders(), "Set-Cookie");
                            String ck = parseAndUpdateCookie(setCookieHeaders);
                            if (!StringUtil.isNullOrEmpty(ck)) {
                                request.addHeader("Cookie", ck);
                            }
                        }
                        return flag;
                    case 303:
                        return true;
                    case 304:
                    case 305:
                    case 306:
                    default:
                        return false;
                }
            }
        }
    }

    public HttpResponse getCacheResponse() {
        return cacheResponse;
    }

    public HttpContext getCacheContext() {
        return cacheContext;
    }
}
