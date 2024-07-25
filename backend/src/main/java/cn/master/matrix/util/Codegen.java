package cn.master.matrix.util;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.EntityConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.time.LocalDateTime;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
public class Codegen {

    public static void main(String[] args) {
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/matrix?characterEncoding=UTF-8&useInformationSchema=true");
        dataSource.setUsername("root");
        dataSource.setPassword("admin");

        GlobalConfig globalConfig = createGlobalConfigUseStyle2();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }


    public static GlobalConfig createGlobalConfigUseStyle2() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("cn.master.matrix")
                .setSourceDir(System.getProperty("user.dir") + "/backend/src/main/java");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setTablePrefix("tb_")
                .setGenerateTable("test_plan_module");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(17)
                .setWithSwagger(true)
                .setSwaggerVersion(EntityConfig.SwaggerVersion.DOC)
                //.setSuperClass(BaseEntity.class)
        ;

        //设置生成 mapper
        //globalConfig.enableController();
        //globalConfig.enableService();
        //globalConfig.enableServiceImpl();
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        //可以单独配置某个列
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setColumnName("create_time");
        columnConfig.setOnInsertValue("now()");
        ColumnConfig columnConfig2 = new ColumnConfig();
        columnConfig2.setColumnName("update_time");
        columnConfig2.setOnInsertValue("now()");
        columnConfig2.setOnUpdateValue("now()");
        //globalConfig.getStrategyConfig().setColumnConfig("test_plan_case_execute_history", columnConfig);
        //globalConfig.getStrategyConfig().setColumnConfig("api_scenario", columnConfig2);
        globalConfig.getJavadocConfig().setAuthor("11's papa").setSince("1.0.0 " + LocalDateTime.now());
        return globalConfig;
    }
}
