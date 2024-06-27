package cn.master.matrix.handler;

import cn.master.matrix.handler.result.MmHttpResultCode;
import lombok.Data;

/**
 * @author Created by 11's papa on 06/27/2024
 **/
@Data
public class ResultHandler {
    private int code = MmHttpResultCode.SUCCESS.getCode();
    // 描述信息，一般是错误信息
    private String message;
    // 详细描述信息, 如有异常，这里是详细日志
    private Object messageDetail;
    // 返回数据
    private Object data = "";

    public ResultHandler() {
    }

    public ResultHandler(Object data) {
        this.data = data;
    }

    public ResultHandler(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public ResultHandler(int code, String msg, Object data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public ResultHandler(int code, String msg, Object messageDetail, Object data) {
        this.code = code;
        this.message = msg;
        this.messageDetail = messageDetail;
        this.data = data;
    }

    public static ResultHandler success(Object obj) {
        return new ResultHandler(obj);
    }

    public static ResultHandler error(int code, String message) {
        return new ResultHandler(code, message, null, null);
    }

    public static ResultHandler error(String message, String messageDetail) {
        return new ResultHandler(-1, message, messageDetail, null);
    }

    public static ResultHandler error(int code, String message, Object messageDetail) {
        return new ResultHandler(code, message, messageDetail, null);
    }

    /**
     * 用于特殊情况，比如接口可正常返回，http状态码200，但是需要页面提示错误信息的情况
     *
     * @param code    自定义 code
     * @param message 给前端返回的 message
     */
    public static ResultHandler successCodeErrorInfo(int code, String message) {
        return new ResultHandler(code, message, null, null);
    }
}
