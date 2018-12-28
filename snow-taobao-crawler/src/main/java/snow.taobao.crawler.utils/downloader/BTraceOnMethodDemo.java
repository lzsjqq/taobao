package snow.taobao.crawler.utils.downloader; /**
 * @Author: xyc
 * @Date: 2018/12/25 16:59
 * @Version 1.0
 */

import java.util.concurrent.TimeUnit;

public class BTraceOnMethodDemo {
    public static void main(String[] args) {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("start main method...");
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
