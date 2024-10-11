

package com.webank.weid.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtils {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public static String dateToString(long time) {
        return sdf.format(new Date(time));
    }
    
    public static String dateToStringNoMin(long time) {
        return sdf1.format(new Date(time));
    }
    
    public static String getDateString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 获取当天凌晨
     * @return 返回当然凌晨时间
     */
    public static Date getTimesmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        return cal.getTime();
    }
}
