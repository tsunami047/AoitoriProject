package io.aoitori043.aoitoriproject.connect.data.access;

import com.google.common.base.Objects;
import io.aoitori043.aoitoriproject.database.point.DataType;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-10  07:38
 * @Description: ?
 */
@Data
public class ExpirableDataAccess extends DataAccess {

    public String varName;
    public String initValue;
    public ExpirableDateType expirableDateType;
    public int parameter;

    public long loadedTimestamp;


    public ExpirableDataAccess(String varName, String initValue, ExpirableDateType expirableDateType, int parameter) {
        this.varName = varName;
        this.initValue = initValue;
        this.expirableDateType = expirableDateType;
        this.parameter = parameter;
        this.generateExpiredTimestamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpirableDataAccess)) return false;
        ExpirableDataAccess that = (ExpirableDataAccess) o;
        return Objects.equal(varName, that.varName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(varName);
    }

    long lastUpdateTimestamp;

    public long getLoadedTimestamp() {
        if (lastUpdateTimestamp > System.currentTimeMillis()-10_000){
            lastUpdateTimestamp = System.currentTimeMillis();
            generateExpiredTimestamp();
        }
        return loadedTimestamp;
    }

    void generateExpiredTimestamp() {
        switch (this.expirableDateType) {
            case DAY: {
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                ZonedDateTime zonedDateTime = tomorrow.atStartOfDay(ZoneId.systemDefault()).plusHours(parameter);
                loadedTimestamp = zonedDateTime.toInstant().toEpochMilli();
                return;
            }
            case WEEK: {
                LocalDate today = LocalDate.now();
                int currentDayOfWeek = today.getDayOfWeek().getValue();
                int daysUntilNext = (parameter - currentDayOfWeek + 7) % 7;
                LocalDate nextWeekDay = today.plusDays(daysUntilNext);
                ZonedDateTime zonedDateTime = nextWeekDay.atStartOfDay(ZoneId.systemDefault());
                loadedTimestamp = zonedDateTime.toInstant().toEpochMilli();
                return;
            }
            case MONTH: {
                LocalDate today = LocalDate.now();
                LocalDate nextMonthDay = today.withDayOfMonth(1).plusMonths(1).withDayOfMonth(Math.min(parameter, today.withDayOfMonth(1).plusMonths(1).lengthOfMonth()));
                ZonedDateTime zonedDateTime = nextMonthDay.atStartOfDay(ZoneId.systemDefault());
                loadedTimestamp = zonedDateTime.toInstant().toEpochMilli();
                return;
            }
        }
    }

    public enum ExpirableDateType{
        DAY,
        WEEK,
        MONTH
    }
}
