package io.shulie.amdb.utils;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

public class RedisUtil {
    public static void hmset(RedisTemplate redisTemplate, String key, Map<String, String> map) {
        redisTemplate.<String, String>opsForHash().putAll(key, map);
    }

    public static Map<String, String> hmget(RedisTemplate redisTemplate, String key, List<String> fields) {
        List<String> result = redisTemplate.<String, String>opsForHash().multiGet(key, fields);
        Map<String, String> ans = new HashMap<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            ans.put(fields.get(i), result.get(i));
        }
        return ans;
    }
}
