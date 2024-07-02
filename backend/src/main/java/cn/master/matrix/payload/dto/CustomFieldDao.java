package cn.master.matrix.payload.dto;

import cn.master.matrix.entity.CustomField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CustomFieldDao extends CustomField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean required;

    private String defaultValue;

    private Object value;
}

