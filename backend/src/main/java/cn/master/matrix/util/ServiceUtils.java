package cn.master.matrix.util;

import cn.master.matrix.exception.CustomException;

import static cn.master.matrix.handler.result.MmHttpResultCode.NOT_FOUND;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
public class ServiceUtils {
    //用于排序的pos
    public static final int POS_STEP = 4096;

    /**
     * 保存资源名称，在处理 NOT_FOUND 异常时，拼接资源名称
     */
    private static final ThreadLocal<String> RESOURCE_NAME = new ThreadLocal<>();

    public static <T> T checkResourceExist(T resource, String name) {
        if (resource == null) {
            RESOURCE_NAME.set(name);
            throw new CustomException(NOT_FOUND);
        }
        return resource;
    }

    public static String getResourceName() {
        return RESOURCE_NAME.get();
    }

    public static void clearResourceName() {
        RESOURCE_NAME.remove();
    }
}
