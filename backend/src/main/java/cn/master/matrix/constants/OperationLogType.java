package cn.master.matrix.constants;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
public enum OperationLogType {
    ADD,
    DELETE,
    UPDATE,
    DEBUG,
    REVIEW,
    COPY,
    EXECUTE,
    SHARE,
    RESTORE,
    IMPORT,
    EXPORT,
    LOGIN,
    SELECT,
    RECOVER,
    LOGOUT,
    DISASSOCIATE,
    ASSOCIATE,
    QRCODE,
    ARCHIVED,
    STOP;

    public boolean contains(OperationLogType keyword) {
        return this.name().contains(keyword.name());
    }
}
