package anonymized.sarp.utils;

import java.util.concurrent.atomic.AtomicLong;


public class SarFileUtils {
    private static AtomicLong baseId = new AtomicLong();

    public static String createID() {
        return String.valueOf(baseId.getAndIncrement());
    }

    public static String createID(String name) {
        return createID() + "_" + name;
    }

}
