package io.aoitori043.syncdistribute.rmi.data.access;

import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-10  07:38
 * @Description: ?
 */
@Getter
public class ExpirableDataAccess extends DataAccess {

    public String initValue;
    public ExpirableDateType expirableDateType;
    public int parameter;

    public void register(){
        super.register();
//        InitDataAccess initDataAccess = InitDataAccess
//                .InitDataAccessBuilder
//                .anInitDataAccess()
//                .withVarName("$expirable" + varName)
//                .withInitValue("0")
//                .build();
//        initDataAccess.register();
    }

    @Builder
    public ExpirableDataAccess(String varName, String initValue, ExpirableDateType expirableDateType, int parameter) {
        super(varName);
        this.initValue = initValue;
        this.expirableDateType = expirableDateType;
        this.parameter = parameter;
    }

    public ExpirableDataAccess(String varName) {
        super(varName);
    }


//    public long getLoadedTimestamp() {
//        if (lastUpdateTimestamp > System.currentTimeMillis()-10_000){
//            lastUpdateTimestamp = System.currentTimeMillis();
//            generateExpiredTimestamp();
//        }
//        return loadedTimestamp;
//    }

    long generateExpiredTimestamp() {
        switch (this.expirableDateType) {
            case DAY: {
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                ZonedDateTime zonedDateTime = tomorrow.atStartOfDay(ZoneId.systemDefault()).plusHours(parameter);
                return zonedDateTime.toInstant().toEpochMilli();

            }
            case WEEK: {
                LocalDate today = LocalDate.now();
                int currentDayOfWeek = today.getDayOfWeek().getValue();
                int daysUntilNext = (parameter - currentDayOfWeek + 7) % 7;
                LocalDate nextWeekDay = today.plusDays(daysUntilNext);
                ZonedDateTime zonedDateTime = nextWeekDay.atStartOfDay(ZoneId.systemDefault());
                return zonedDateTime.toInstant().toEpochMilli();
            }
            case MONTH: {
                LocalDate today = LocalDate.now();
                LocalDate nextMonthDay = today.withDayOfMonth(1).plusMonths(1).withDayOfMonth(Math.min(parameter, today.withDayOfMonth(1).plusMonths(1).lengthOfMonth()));
                ZonedDateTime zonedDateTime = nextMonthDay.atStartOfDay(ZoneId.systemDefault());
                return zonedDateTime.toInstant().toEpochMilli();
            }
        }
        return Long.MAX_VALUE;
    }

    @Override
    public String get(PersistentDataAccess persistentDataAccess, String originValue) {
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


    public static final class ExpirableDataAccessBuilder {
        private String initValue;
        private ExpirableDateType expirableDateType;
        private int parameter;
        private long loadedTimestamp;
        private long lastUpdateTimestamp;
        private String varName;

        private ExpirableDataAccessBuilder() {
        }

        public static ExpirableDataAccessBuilder anExpirableDataAccess() {
            return new ExpirableDataAccessBuilder();
        }

        public ExpirableDataAccessBuilder withInitValue(String initValue) {
            this.initValue = initValue;
            return this;
        }

        public ExpirableDataAccessBuilder withExpirableDateType(ExpirableDateType expirableDateType) {
            this.expirableDateType = expirableDateType;
            return this;
        }

        public ExpirableDataAccessBuilder withParameter(int parameter) {
            this.parameter = parameter;
            return this;
        }

        public ExpirableDataAccessBuilder withLoadedTimestamp(long loadedTimestamp) {
            this.loadedTimestamp = loadedTimestamp;
            return this;
        }

        public ExpirableDataAccessBuilder withLastUpdateTimestamp(long lastUpdateTimestamp) {
            this.lastUpdateTimestamp = lastUpdateTimestamp;
            return this;
        }

        public ExpirableDataAccessBuilder withVarName(String varName) {
            this.varName = varName;
            return this;
        }

        public ExpirableDataAccess build() {
            ExpirableDataAccess expirableDataAccess = new ExpirableDataAccess(varName);
            expirableDataAccess.initValue = this.initValue;
            expirableDataAccess.expirableDateType = this.expirableDateType;
            expirableDataAccess.parameter = this.parameter;
            return expirableDataAccess;
        }
    }
}
