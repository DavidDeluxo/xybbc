package com.xingyun.bbc.mallpc.common.components;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.xingyun.bbc.mallpc.common.utils.DateUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import redis.clients.util.SafeEncoder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-17
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Component
public class RedisHolder {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private RedisHolder() {
    }

    public Boolean put(String key, Object value) {
        return put(key, value, 60 * 60);
    }

    public Boolean put(String key, Object value, long seconds) {
        return execute((connection) -> {
            String str;
            if (value instanceof String) {
                str = (String) value;
            } else if (value instanceof Character) {
                str = String.valueOf(value);
            } else {
                str = JSON.toJSONString(value);
            }
            return connection.setEx(SafeEncoder.encode(key), seconds, SafeEncoder.encode(str));
        });
    }

    public Long sAdd(String key, Object... values) {
        return execute((connection) -> {
            byte[] keyBytes = SafeEncoder.encode(key);
            String[] array = Arrays.stream(values).map(value -> JSON.toJSONString(value)).toArray(String[]::new);
            return connection.sAdd(keyBytes, SafeEncoder.encodeMany(array));
        });
    }

    public boolean sIsMember(String key, Object value) {
        return execute((connection) -> {
            Boolean result = connection.sIsMember(SafeEncoder.encode(key), SafeEncoder.encode(JSON.toJSONString(value)));
            return BooleanUtils.toBoolean(result);
        });
    }

    public Long sCard(String key) {
        return execute((connection) -> connection.sCard(SafeEncoder.encode(key)));
    }

    public Long sRem(String key, Object... values) {
        return execute((connection) -> {
            byte[] keyBytes = SafeEncoder.encode(key);
            String[] array = Arrays.stream(values).map(value -> JSON.toJSONString(value)).toArray(String[]::new);
            return connection.sRem(keyBytes, SafeEncoder.encodeMany(array));
        });
    }

    public <T> T get(String key, Class<T> clazz) {
        return execute((connection) -> {
            byte[] keyBytes = connection.get(SafeEncoder.encode(key));
            if (keyBytes != null && keyBytes.length > 0) {
                return JSONObject.parseObject(SafeEncoder.encode(keyBytes), clazz);
            } else {
                return null;
            }
        });
    }

    public String get(String key) {
        return execute((connection) -> {
            byte[] keyBytes = connection.get(SafeEncoder.encode(key));
            if (keyBytes != null && keyBytes.length > 0) {
                return SafeEncoder.encode(keyBytes);
            } else {
                return null;
            }
        });
    }


    public Long remove(String key) {
        return execute((connection) -> connection.del(new byte[][]{SafeEncoder.encode(key)}));
    }

    public Long ttl(String key) {
        return execute((connection) -> connection.ttl(SafeEncoder.encode(key)));
    }

    public Boolean setExpire(String key, long seconds) {
        return execute((connection) -> connection.expire(SafeEncoder.encode(key), seconds));
    }

    public Boolean setExpireAt(String key, Date date) {
        return setExpire(key, DateUtils.differSeconds(new Date(), date));
    }

    public Boolean hput(String key, String field, Object value) {
        return execute((connection) -> connection.hSet(SafeEncoder.encode(key), SafeEncoder.encode(field), SafeEncoder.encode(JSON.toJSONString(value))));
    }

    public void hMSet(String key, Map<String, Object> map) {
        execute((connection) -> {
            if (CollectionUtils.isEmpty(map)) {
                return null;
            }
            Map<byte[], byte[]> hashes = Maps.newHashMap();
            map.entrySet().forEach(entry -> {
                if (StringUtils.isNotBlank(entry.getKey()) && Objects.nonNull(entry.getValue())) {
                    hashes.put(SafeEncoder.encode(entry.getKey()), SafeEncoder.encode(JSON.toJSONString(entry.getValue())));
                }
            });
            connection.hMSet(SafeEncoder.encode(key), hashes);
            return null;
        });
    }

    public Boolean hExists(String key, String field) {
        return execute((connection) -> connection.hExists(SafeEncoder.encode(key), SafeEncoder.encode(field)));
    }

    public <T> T hget(String key, String field, Class<T> clazz) {
        return execute((connection) -> {
            byte[] value = connection.hGet(SafeEncoder.encode(key), SafeEncoder.encode(field));
            return value != null && value.length > 0 ? JSON.parseObject(value, clazz) : null;
        });
    }

    public Long hDel(String key, String... fields) {
        return execute((connection) -> {
            byte[] keyBytes = SafeEncoder.encode(key);
            return connection.hDel(keyBytes, SafeEncoder.encodeMany(fields));
        });
    }

    public <T> Collection<T> hgetAll(String key, Class<T> clazz) {
        return execute((connection) -> {
            List<T> values = new ArrayList();
            Map<byte[], byte[]> map = connection.hGetAll(SafeEncoder.encode(key));
            Iterator var4 = map.entrySet().iterator();

            while (var4.hasNext()) {
                Map.Entry<byte[], byte[]> entry = (Map.Entry) var4.next();
                if (entry.getKey() != null && entry.getKey().length > 0 && entry.getValue() != null && entry.getValue().length > 0) {
                    values.add(JSON.parseObject(entry.getValue(), clazz));
                }
            }
            return values;
        });
    }

    public Long lpush(String key, Object... values) {
        String[] array = Arrays.stream(values).map(value -> JSON.toJSONString(value)).toArray(String[]::new);
        return execute((connection) -> connection.lPush(SafeEncoder.encode(key), SafeEncoder.encodeMany(array)));
    }

    public Long rpush(String key, Object... values) {
        String[] array = Arrays.stream(values).map(value -> JSON.toJSONString(value)).toArray(String[]::new);
        return execute((connection) -> connection.rPush(SafeEncoder.encode(key), SafeEncoder.encodeMany(array)));
    }

    public <T> List<T> lrange(String key, Class<T> clazz, long start, long end) {
        return execute((connection) -> {
            List<byte[]> list = connection.lRange(SafeEncoder.encode(key), start, end);
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            return (List<T>) list.stream().filter(each -> each != null && each.length > 0).map(each -> JSON.parseObject(each, clazz)).collect(Collectors.toList());
        });
    }

    public <T> T lpop(String key, Class<T> clazz) {
        return execute((connection) -> {
            byte[] value = connection.lPop(SafeEncoder.encode(key));
            return value != null && value.length > 0 ? JSON.parseObject(value, clazz) : null;
        });
    }

    public Long lrem(String key, String value, long count) {
        return execute((connection) -> connection.lRem(SafeEncoder.encode(key), count, SafeEncoder.encode(value)));
    }

    public Boolean exists(String key) {
        return execute((connection) -> connection.exists(SafeEncoder.encode(key)));
    }

    public Long incr(String key) {
        return execute((connection) -> connection.incr(SafeEncoder.encode(key)));
    }

    public Long incrBy(String key, long num) {
        return execute((connection) -> connection.incrBy(SafeEncoder.encode(key), num));
    }

    public Double incrByFloat(String key, double num) {
        return execute((connection) -> connection.incrBy(SafeEncoder.encode(key), num));
    }

    public boolean setnx(String key, Object value, long seconds) {
        return setnx(key, value, seconds, true);
    }

    public boolean setnx(String key, Object value, long seconds, boolean refreshExpireTime) {
        return execute((connection) -> {
            String json = JSON.toJSONString(value);
            byte[] keyBytes = SafeEncoder.encode(key);
            Boolean ret = connection.setNX(keyBytes, SafeEncoder.encode(json));
            if (refreshExpireTime || BooleanUtils.toBoolean(ret)) {
                connection.expire(keyBytes, seconds);
            }
            return ret.booleanValue();
        });
    }


    public Long decr(String key) {
        return execute((connection) -> connection.decr(SafeEncoder.encode(key)));
    }

    public Long decrBy(String key, long num) {
        return execute((connection) -> connection.decrBy(SafeEncoder.encode(key), num));
    }

    public byte[] get(byte[] key) {
        return execute((connection) -> connection.get(key));
    }

    public byte[] set(byte[] key, byte[] value, long seconds) {
        execute((connection) -> {
            connection.setEx(key, seconds, value);
            return value;
        });
        return value;
    }

    public Long del(byte[]... keys) {
        return execute((connection) -> connection.del(keys));
    }

    public Set<byte[]> keys(String pattern) {
        return execute((connection) -> connection.keys(SafeEncoder.encode(pattern)));
    }

    public void flushDB() {
        execute((connection) -> {
            connection.flushDb();
            return null;
        });
    }

    public Long dbSize() {
        return execute(RedisServerCommands::dbSize);
    }

    private <T> T execute(RedisCallback<T> action) {
        return redisTemplate.execute(action);
    }

}
