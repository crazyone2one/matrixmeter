package cn.master.matrix.exception;

import cn.master.matrix.util.Translator;

/**
 * API 接口状态码
 *
 * @author Created by 11's papa on 06/27/2024
 **/
public interface IResultCode {
    int getCode();

    String getMessage();

    default String getTranslationMessage(String message) {
        return Translator.get(message, message);
    }
}
