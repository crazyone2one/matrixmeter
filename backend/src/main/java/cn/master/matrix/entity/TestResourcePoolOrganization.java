package cn.master.matrix.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 测试资源池项目关系 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-03T08:48:48.779292100
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试资源池项目关系")
@Table("test_resource_pool_organization")
public class TestResourcePoolOrganization implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 测试资源池项目关系ID
     */
    @Id
    @Schema(description = "测试资源池项目关系ID")
    private String id;

    /**
     * 资源池ID
     */
    @Schema(description = "资源池ID")
    private String testResourcePoolId;

    /**
     * 组织ID
     */
    @Schema(description = "组织ID")
    private String orgId;

}
