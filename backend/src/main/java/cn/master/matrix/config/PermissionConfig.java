package cn.master.matrix.config;

import cn.master.matrix.entity.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Created by 11's papa on 06/27/2024
 **/
//@Configuration
public class PermissionConfig {
    @Bean
    public PermissionCache permissionCache() throws IOException {
        List<PermissionDefinitionItem> permissionDefinition = null;
        Enumeration<URL> urls = this.getClass().getClassLoader().getResources("permission.json");
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String content = IOUtils.toString(url.openStream(), String.valueOf(StandardCharsets.UTF_8));
            if (StringUtils.isBlank(content)) {
                continue;
            }
            List<PermissionDefinitionItem> temp = JsonUtils.parseArray(content, PermissionDefinitionItem.class);
            if (permissionDefinition == null) {
                permissionDefinition = temp;
            } else {
                permissionDefinition.addAll(temp);
            }
        }
        PermissionCache permissionCache = new PermissionCache();
        permissionCache.setPermissionDefinition(permissionDefinition);
        return permissionCache;
    }
}
