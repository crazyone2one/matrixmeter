package cn.master.matrix.configuration;

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by 11's papa on 06/21/2024
 **/

@Configuration
public class MyBatisFlexConfiguration implements MyBatisFlexCustomizer {

    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        FlexGlobalConfig.getDefaultConfig().setLogicDeleteColumn("deleted");
        //配置主键生产策略
        FlexGlobalConfig.KeyConfig keyConfig = new FlexGlobalConfig.KeyConfig();
        keyConfig.setKeyType(KeyType.Generator);
        keyConfig.setValue(KeyGenerators.flexId);
        FlexGlobalConfig.getDefaultConfig().setKeyConfig(keyConfig);

        //开启审计功能
        //AuditManager.setAuditEnable(true);
        //设置 SQL 审计收集器
        //MessageCollector collector = new ConsoleMessageCollector();
        //AuditManager.setMessageCollector(collector);
    }

}
