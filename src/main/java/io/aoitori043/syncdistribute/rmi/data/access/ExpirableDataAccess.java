package io.aoitori043.syncdistribute.rmi.data.access;

import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-10  07:38
 * @Description: ?
 */
@Getter
@Setter
public class ExpirableDataAccess extends DataAccess {

    public String initValue;
    public ExpirableMap expirableMap;
//    public HashMap<ExpirableDateType,Integer> expirableMap;

    @Builder
    public ExpirableDataAccess(String varName, String initValue, ExpirableMap expirableMap) {
        super(varName);
        this.initValue = initValue;
        this.expirableMap = expirableMap;
    }

    @Getter
    public static class ExpirableMap{
        Integer hours;
        Integer week;
        Integer month;

        @Builder
        public ExpirableMap(Integer hours, Integer month, Integer week) {
            this.hours = hours;
            this.month = month;
            this.week = week;
        }
    }

    public ExpirableDataAccess(String varName) {
        super(varName);
    }

    public void register(){
        super.register();
    }


    public long generateExpiredTimestamp() {
        LocalDate now = LocalDate.now();
        ZonedDateTime zonedDateTime;

        if (expirableMap.getHours()!=null && expirableMap.getWeek()!=null) {
            int dayHour = expirableMap.getHours();
            int weekDay = expirableMap.getWeek();
            DayOfWeek targetDayOfWeek = DayOfWeek.of(weekDay);
            int daysUntilNext = (targetDayOfWeek.getValue() - now.getDayOfWeek().getValue() + 7) % 7;

            LocalDate nextWeekDay = now.plusDays(daysUntilNext);
            zonedDateTime = nextWeekDay.atStartOfDay(ZoneId.systemDefault()).plusHours(dayHour);
            return zonedDateTime.toInstant().toEpochMilli();
        }

        if (expirableMap.getMonth()!=null && expirableMap.getHours()!=null) {
            int monthDay = expirableMap.getMonth();
            int dayHour = expirableMap.getHours();

            LocalDate nextMonthDay = now.withDayOfMonth(1).plusMonths(1)
                    .withDayOfMonth(Math.min(monthDay, now.plusMonths(1).lengthOfMonth()));
            zonedDateTime = nextMonthDay.atStartOfDay(ZoneId.systemDefault()).plusHours(dayHour);
            return zonedDateTime.toInstant().toEpochMilli();
        }

        if (expirableMap.getHours()!=null) {
            int dayHour = expirableMap.getHours();
            zonedDateTime = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).plusHours(dayHour);
            return zonedDateTime.toInstant().toEpochMilli();
        }

        if (expirableMap.getWeek()!=null) {
            int weekDay = expirableMap.getWeek();
            DayOfWeek targetDayOfWeek = DayOfWeek.of(weekDay);
            int daysUntilNext = (targetDayOfWeek.getValue() - now.getDayOfWeek().getValue() + 7) % 7;

            LocalDate nextWeekDay = now.plusDays(daysUntilNext);
            zonedDateTime = nextWeekDay.atStartOfDay(ZoneId.systemDefault());
            return zonedDateTime.toInstant().toEpochMilli();
        }

        if (expirableMap.getMonth()!=null) {
            int monthDay = expirableMap.getMonth();
            LocalDate nextMonthDay = now.withDayOfMonth(1).plusMonths(1)
                    .withDayOfMonth(Math.min(monthDay, now.plusMonths(1).lengthOfMonth()));
            zonedDateTime = nextMonthDay.atStartOfDay(ZoneId.systemDefault());
            return zonedDateTime.toInstant().toEpochMilli();
        }

        return Long.MAX_VALUE;
    }


    @Override
    public Object get(PersistentDataAccess persistentDataAccess, String originValue) {
        String timestampIndex = "$expirable" + varName;
        long expiredTime = persistentDataAccess.getAsLong(timestampIndex);
        if (System.currentTimeMillis() > expiredTime) {
            persistentDataAccess.set(super.varName,initValue);
            persistentDataAccess.set(timestampIndex,String.valueOf(generateExpiredTimestamp()));
            return initValue;
        }
        return originValue;
    }

    public enum ExpirableDateType{
        DAY,
        WEEK,
        MONTH
    }

}
