package cn.master.matrix.payload.dto.excel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 07/17/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class UserExcelRowDTO extends UserExcel {
    public int dataIndex;
    public String errorMessage;
    public String userRoleId;
}
