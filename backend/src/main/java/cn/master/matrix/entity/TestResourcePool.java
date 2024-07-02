package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 测试资源池 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:49:24.097299400
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试资源池")
@Table("test_resource_pool")
public class TestResourcePool implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资源池ID
     */
    @Id
    @Schema(description = "资源池ID")
    private String id;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 类型
     */
    @Schema(description = "类型")
    private String type;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean enable;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 是否用于接口测试
     */
    @Schema(description = "是否用于接口测试")
    private Boolean apiTest;

    /**
     * 是否用于性能测试
     */
    @Schema(description = "是否用于性能测试")
    private Boolean loadTest;

    /**
     * 是否用于ui测试
     */
    @Schema(description = "是否用于ui测试")
    private Boolean uiTest;

    /**
     * ms部署地址
     */
    @Schema(description = "ms部署地址")
    private String serverUrl;

    /**
     * 资源池应用类型（组织/全部）
     */
    @Schema(description = "资源池应用类型（组织/全部）")
    private Boolean allOrg;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    private Boolean deleted;

}
