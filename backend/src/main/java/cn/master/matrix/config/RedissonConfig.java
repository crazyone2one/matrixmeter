package cn.master.matrix.config;

import lombok.val;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Created by 11's papa on 07/08/2024
 **/
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() throws IOException {
        val config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResourceAsStream("redisson.yaml"))
                .setCodec(new StringCodec());
        return Redisson.create(config);
    }
}
