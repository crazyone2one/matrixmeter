package cn.master.matrix.util;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Created by 11's papa on 07/08/2024
 **/
@Component
@RequiredArgsConstructor
public class RedisService {
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    public void setString(String key, String value) {
        val bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }
    public String getString(String key) {
        val bucket = redissonClient.getBucket(key);
       return (String) bucket.get();
    }

    public <T> void setObject(String key, T value) {
        val bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    public <T> boolean updateToHash(String key, Object field, T value) {
        RMap<Object, T> hash = redissonClient.getMap(key);
        return hash.fastReplace(field, value);
    }

    public <T> void addList(String key, List<T> value) {
        redissonClient.getList(key).addAll(value);
    }

    public List<Object> getList(String key) {
        return redissonClient.getList(key).readAll();
    }
}
