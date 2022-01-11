
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by hawkingfoo on 2017/6/29 0029.
 */
public class TestLogFile {
    private static final Logger logger = LogManager.getLogger(TestLogFile.class);

    public static void main(String[] args) {
        logger.error("2FFFFFFFFFFFFFFFFF",new NullPointerException());
    }
}
