package snow.taobao.crawler.utils.downloader;

/**
 * @author sugan
 * @since 2016-07-20.
 */

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class PostRedirectStrategy extends DefaultRedirectStrategy {

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
                    return (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD") || method.equalsIgnoreCase("POST")) && locationHeader != null;
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
