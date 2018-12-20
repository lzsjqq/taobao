package snow.taobao.crawler.utils.downloader;

/**
 * Created by hangs on 2016/11/8-11:53.
 */
public class DownloadException extends Exception {

    private int httpStatus;
    private String response;

    public DownloadException(int httpStatus, String response, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.response = response;
    }

    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownloadException(Throwable cause) {
        super(cause);
    }

    public DownloadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "; http status code - " + httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getResponse() {
        return response;
    }
}
