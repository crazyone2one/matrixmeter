package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Data
public class BaseEntity implements Serializable {
    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 是否删除
     */
    private Boolean deleted;
}
