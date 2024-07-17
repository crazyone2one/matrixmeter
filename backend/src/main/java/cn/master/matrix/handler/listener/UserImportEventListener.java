package cn.master.matrix.handler.listener;

import cn.master.matrix.payload.dto.excel.ExcelParseDTO;
import cn.master.matrix.payload.dto.excel.UserExcel;
import cn.master.matrix.payload.dto.excel.UserExcelRowDTO;
import cn.master.matrix.payload.dto.excel.UserExcelValidateHelper;
import cn.master.matrix.util.Translator;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 * @author Created by 11's papa on 07/17/2024
 **/
@Getter
@Slf4j
public class UserImportEventListener extends AnalysisEventListener<UserExcel> {

    private final ExcelParseDTO<UserExcelRowDTO> excelParseDTO;

    public UserImportEventListener() {
        excelParseDTO = new ExcelParseDTO<>();
    }

    @Override
    public void invoke(UserExcel data, AnalysisContext context) {
        String errMsg;
        Integer rowIndex = context.readRowHolder().getRowIndex();
        //使用javax.validation校验excel数据
        errMsg = UserExcelValidateHelper.validateEntity(data);
        if (StringUtils.isEmpty(errMsg)) {
            errMsg = businessValidate(data);
        }
        UserExcelRowDTO userExcelRowDTO = new UserExcelRowDTO();
        BeanUtils.copyProperties(data, userExcelRowDTO);
        userExcelRowDTO.setDataIndex(rowIndex);
        if (StringUtils.isEmpty(errMsg)) {
            excelParseDTO.addRowData(userExcelRowDTO);
        } else {
            userExcelRowDTO.setErrorMessage(errMsg);
            excelParseDTO.addErrorRowData(rowIndex, userExcelRowDTO);
        }
    }

    private String businessValidate(UserExcel data) {
        if (CollectionUtils.isNotEmpty(excelParseDTO.getDataList())) {
            for (UserExcelRowDTO userExcelRowDTO : excelParseDTO.getDataList()) {
                if (StringUtils.equalsIgnoreCase(userExcelRowDTO.getEmail(), data.getEmail())) {
                    return Translator.get("user.email.repeat") + ":" + data.getEmail();
                }
            }
        }
        return "";
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
