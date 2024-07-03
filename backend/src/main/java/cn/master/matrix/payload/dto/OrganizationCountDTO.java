package cn.master.matrix.payload.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@Data
public class OrganizationCountDTO {

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 项目数量
     */
    private Integer projectCount;

    /**
     * 组织ID
     */
    private String id;
}
