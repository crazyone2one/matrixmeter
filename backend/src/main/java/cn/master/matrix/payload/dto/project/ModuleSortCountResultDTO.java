package cn.master.matrix.payload.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Data
@AllArgsConstructor
public class ModuleSortCountResultDTO {
    private boolean isRefreshPos;
    private long pos;
}
