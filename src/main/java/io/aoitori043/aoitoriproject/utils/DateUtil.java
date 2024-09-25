package io.aoitori043.aoitoriproject.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-11  18:12
 * @Description: ?
 */
public class DateUtil {

    public static boolean isAfterNextDayHours(Date date, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date currentDate = new Date();
        return currentDate.after(cal.getTime());
    }

}
