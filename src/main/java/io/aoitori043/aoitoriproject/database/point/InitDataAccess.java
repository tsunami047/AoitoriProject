package io.aoitori043.aoitoriproject.database.point;

import lombok.Data;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-10  12:54
 * @Description: ?
 */
@Data
public class InitDataAccess implements DataAccess{

    DataType varType = DataType.INIT_DATA;
    String varName;
    String initValue;

    public InitDataAccess(String varName, String initValue) {
        this.varName = varName;
        this.initValue = initValue;
    }
}
