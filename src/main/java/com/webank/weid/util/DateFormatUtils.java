package com.webank.weid.util;

import java.text.SimpleDateFormat;
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
}
