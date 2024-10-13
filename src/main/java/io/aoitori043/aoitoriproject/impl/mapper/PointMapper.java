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
            ExpirableDataAccess.builder()
                    .withVarName(split[0])
                    .withInitValue(split[1])
                    .withExpirableDateType(ExpirableDataAccess.ExpirableDateType.valueOf(split[2].toUpperCase()))
                    .withParameter(Integer.parseInt(split[3]));
        }
    }
}
