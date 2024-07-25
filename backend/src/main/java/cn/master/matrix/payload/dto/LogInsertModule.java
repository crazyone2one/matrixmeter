package cn.master.matrix.payload.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogInsertModule {
    @NotBlank
    private String operator;
    @NotBlank
    private String requestUrl;
    @NotBlank
    private String requestMethod;
}