package cn.master.matrix.handler;

import cn.master.matrix.exception.CustomException;
import cn.master.matrix.exception.IResultCode;
import cn.master.matrix.handler.result.MmHttpResultCode;
import cn.master.matrix.util.ServiceUtils;
import cn.master.matrix.util.Translator;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Throwables.getStackTraceAsString;

/**
 * @author Created by 11's papa on 06/27/2024
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultHandler handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResultHandler.error(MmHttpResultCode.VALIDATE_FAILED.getCode(), MmHttpResultCode.VALIDATE_FAILED.getMessage(), errors);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultHandler handleHttpRequestMethodNotSupportedException(HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        return ResultHandler.error(HttpStatus.METHOD_NOT_ALLOWED.value(), exception.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResultHandler> handleCustomException(CustomException e) {
        IResultCode errorCode = e.getErrorCode();
        if (errorCode == null) {
            // 如果抛出异常没有设置状态码，则返回错误 message
            return ResponseEntity.internalServerError()
                    .body(ResultHandler.error(MmHttpResultCode.FAILED.getCode(), e.getMessage()));
        }
        int code = errorCode.getCode();
        String message = errorCode.getMessage();
        message = Translator.get(message, message);

        if (errorCode instanceof MmHttpResultCode) {
            // 如果是 MsHttpResultCode，则设置响应的状态码，取状态码的后三位
            if (errorCode.equals(MmHttpResultCode.NOT_FOUND)) {
                message = getNotFoundMessage(message);
            }
            return ResponseEntity.status(code % 1000)
                    .body(ResultHandler.error(code, message, e.getMessage()));
        } else {
            // 响应码返回 500，设置业务状态码
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultHandler.error(code, Translator.get(message, message), e.getMessage()));
        }
    }

    private String getNotFoundMessage(String message) {
        String resourceName = ServiceUtils.getResourceName();
        if (StringUtils.isNotBlank(resourceName)) {
            message = String.format(message, Translator.get(resourceName, resourceName));
        } else {
            message = String.format(message, Translator.get("resource.name"));
        }
        ServiceUtils.clearResourceName();
        return message;
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResultHandler> handleException(Exception e) {
        if (e instanceof AccessDeniedException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultHandler.error(MmHttpResultCode.FORBIDDEN.getCode(), MmHttpResultCode.FORBIDDEN.getMessage(), getStackTraceAsString(e)));
            //.body(ResultHandler.error(MmHttpResultCode.FORBIDDEN.getCode(), e.getMessage(), getStackTraceAsString(e)));
        }
        return ResponseEntity.internalServerError()
                .body(ResultHandler.error(MmHttpResultCode.FAILED.getCode(), e.getMessage(), getStackTraceAsString(e)));
    }
}
