package cn.master.matrix.payload.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Data
public class OperationLogResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    //操作人
    private String createUser;
    private String userName;

    //操作范围
    private String projectId;
    private String projectName;

    private String organizationId;
    private String organizationName;

    //操作对象
    private String module;

    //操作类型
    private String type;

    //名称
    private String content;

    //创建时间
    private Long createTime;


    private String sourceId;

}