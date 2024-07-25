package cn.master.matrix.payload.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleCountDTO {
    private String moduleId;
    private int dataCount;
}
