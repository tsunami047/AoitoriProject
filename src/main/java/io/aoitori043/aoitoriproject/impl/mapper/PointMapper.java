package io.aoitori043.aoitoriproject.impl.mapper;

import io.aoitori043.aoitoriproject.config.ConfigProperties;
import io.aoitori043.aoitoriproject.config.Run;
import io.aoitori043.syncdistribute.rmi.data.access.ExpirableDataAccess;
import io.aoitori043.syncdistribute.rmi.data.access.InitDataAccess;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-13  16:43
 * @Description: ?
 */
@ConfigProperties
public class PointMapper {

    List<String> init;
    List<String> expirable;

    @Run(after = "expirable")
    public void onload(){
        for (String label : this.init) {
            String[] split = label.split(" ");
            InitDataAccess.builder()
                    .withInitValue(split[1])
                    .varName(split[0])
                    .build()
                    .register();
        }
        for (String label : this.expirable) {
            String[] split = label.split(" ");
            ExpirableDataAccess.ExpirableDataAccessBuilder expirableDataAccessBuilder = ExpirableDataAccess.builder()
                    .varName(split[0])
                    .initValue(split[1]);
            ExpirableDataAccess.ExpirableMap.ExpirableMapBuilder expirableMapBuilder = ExpirableDataAccess.ExpirableMap.builder();
            for (int i = 2; i < split.length; i++) {
                String s = split[i];
                String[] split1 = s.split("-");
                switch (split1[0]) {
                    case "hour":{
                        expirableMapBuilder.hours(Integer.parseInt(split1[1]));
                        break;
                    }
                    case "week":{
                        expirableMapBuilder.week(Integer.parseInt(split1[1]));
                        break;
                    }
                    case "month":{
                        expirableMapBuilder.month(Integer.parseInt(split1[1]));
                        break;
                    }
                }
            }
            ExpirableDataAccess.ExpirableMap expirableMap = expirableMapBuilder.build();
            expirableDataAccessBuilder
                    .expirableMap(expirableMap)
                    .build()
                    .register();
        }
    }
}
