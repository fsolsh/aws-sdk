package com.fsolsh.aws.config;

/**
 * attachment file type
 */
public enum FileType {
    WORD("application/msword; charset=UTF-8"),
    EXCEL("application/x-xls; charset=UTF-8"),
    PDF("application/pdf; charset=UTF-8"),
    ZIP("application/zip; charset=UTF-8"),
    DOWNLOAD("application/octet-stream; charset=UTF-8");

    String miniType;

    FileType(String miniType) {
        this.miniType = miniType;
    }

    public String getMiniType() {
        return miniType;
    }
}
