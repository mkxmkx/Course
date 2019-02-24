package newsspider.news.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class getDate {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public  Date convertToDate(String source)
    {
        if (source == null)
            return null;
        logger.debug("source detail : " + source);
        String value = source.trim();
        if ("".equals(value)) {
            return null;
        }
        String year = value.substring(0,4);
        String month = value.substring(5,7);
        String day = value.substring(8,10);

        String time = year + "-" + month + "-" + day;

        logger.debug("time : " + time);

        Date date = null;
        DateConverterConfig dateConverterConfig = new DateConverterConfig();
        date = dateConverterConfig.convert(time);
        return date;
    }
}
