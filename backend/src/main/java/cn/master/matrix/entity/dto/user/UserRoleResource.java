package cn.master.matrix.entity.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 06/27/2024
 **/
@Data
public class UserRoleResource implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Boolean license = false;
}
