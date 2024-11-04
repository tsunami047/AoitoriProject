package io.aoitori043.syncdistribute.rmi.data.access;

import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;
import io.aoitori043.syncdistribute.rmi.RMIClient;
import lombok.Builder;

public class InitDataAccess extends DataAccess {

    private String initValue;

    public InitDataAccess(String varName) {
        super(varName);
    }

    @Override
    public Object get(PersistentDataAccess persistentDataAccess, String originValue){
        try {
            if (originValue == null) {
                persistentDataAccess.set(varName,initValue);
                return initValue;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return originValue;
    }

    @Builder
    public InitDataAccess(String varName, String initValue) {
        super(varName);
        this.initValue = initValue;
    }

    @Override
    public void register() {
        super.register();
    }


    public static final class InitDataAccessBuilder {
        private String initValue;
        private String varName;

        private InitDataAccessBuilder() {
        }

        public static InitDataAccessBuilder anInitDataAccess() {
            return new InitDataAccessBuilder();
        }

        public InitDataAccessBuilder withInitValue(String initValue) {
            this.initValue = initValue;
            return this;
        }

        public InitDataAccessBuilder withVarName(String varName) {
            this.varName = varName;
            return this;
        }

        public InitDataAccess build() {
            InitDataAccess initDataAccess = new InitDataAccess(varName);
            initDataAccess.initValue = this.initValue;
            return initDataAccess;
        }
    }
}
