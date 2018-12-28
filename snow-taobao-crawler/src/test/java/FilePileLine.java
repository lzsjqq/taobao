import org.apache.commons.io.FileUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @Author: xyc
 * @Date: 2018/12/28 17:12
 * @Version 1.0
 */
public class FilePileLine extends FilePersistentBase implements Pipeline {


    @Override
    public void process(ResultItems resultItems, Task task) {
        File file = new File("C:\\Users\\Rzxuser\\Desktop\\hosts.txt");
        Map<String, Object> all = resultItems.getAll();
        Set<Map.Entry<String, Object>> entries = all.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            Object value = entry.getValue();
            try {
                FileUtils.writeStringToFile(file, key + ">>" + value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
