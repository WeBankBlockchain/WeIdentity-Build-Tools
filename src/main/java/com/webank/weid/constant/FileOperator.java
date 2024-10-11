


package com.webank.weid.constant;

/**
 * @author tonychen 2019/4/8
 */
public enum FileOperator {

    /**
     * write to file by "overwrite" way.
     */
    OVERWRITE("overwrite"),

    /**
     * write to file by "append" way.
     */
    APPEND("append");

    /**
     * overwrite or append.
     */
    private String action;

    FileOperator(String action) {
        this.action = action;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

}
