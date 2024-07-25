package cn.master.matrix.entity;

import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 测试集 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T10:32:20.267143100
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试集")
@Table("test_plan_collection")
public class TestPlanCollection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_collection.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_collection.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description = "父级ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_collection.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(description = "测试集名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan_collection.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "测试集类型(功能/接口/场景)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan_collection.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "执行方式(串行/并行)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.execute_method.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_collection.execute_method.length_range}", groups = {Created.class, Updated.class})
    private String executeMethod;

    @Schema(description = "是否继承", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.extended.not_blank}", groups = {Created.class})
    private Boolean extended;

    @Schema(description = "是否使用环境组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.grouped.not_blank}", groups = {Created.class})
    private Boolean grouped;

    @Schema(description = "环境ID/环境组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.environment_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_collection.environment_id.length_range}", groups = {Created.class, Updated.class})
    private String environmentId;

    @Schema(description = "测试资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.test_resource_pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_collection.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String testResourcePoolId;

    @Schema(description = "是否失败重试", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.retry_on_fail.not_blank}", groups = {Created.class})
    private Boolean retryOnFail;

    /**
     * 失败重试次数
     */
    @Schema(description = "失败重试次数")
    private Integer retryTimes;

    /**
     * 失败重试间隔(单位: ms)
     */
    @Schema(description = "失败重试间隔(单位: ms)")
    private Integer retryInterval;

    /**
     * 是否失败停止
     */
    @Schema(description = "是否失败停止")
    @NotNull(message = "{test_plan_collection.stop_on_fail.not_blank}", groups = {Created.class})
    private Boolean stopOnFail;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 自定义排序，间隔为4096
     */
    @Schema(description = "自定义排序，间隔为4096")
    @NotNull(message = "{test_plan_collection.pos.not_blank}", groups = {Created.class})
    private Long pos;

}
