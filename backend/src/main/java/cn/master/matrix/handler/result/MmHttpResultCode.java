package cn.master.matrix.handler.result;

import cn.master.matrix.exception.IResultCode;

/**
 * @author Created by 11's papa on 06/27/2024
 **/
public enum MmHttpResultCode implements IResultCode {
    SUCCESS(100200, "http_result_success"),
    FAILED(100500, "http_result_unknown_exception"),
    VALIDATE_FAILED(100400, "http_result_validate"),
    UNAUTHORIZED(100401, "http_result_unauthorized"),
    FORBIDDEN(100403, "http_result_forbidden"),
    NOT_FOUND(100404, "http_result_not_found");

    private final int code;
    private final String message;

    MmHttpResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return getTranslationMessage(this.message);
    }
}
