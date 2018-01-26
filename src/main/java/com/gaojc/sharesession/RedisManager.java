package com.gaojc.sharesession;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * RedisManager
 * author:gaojccn
 * email:jcgaogs@163.com
 */
@Service
public class RedisManager {
    private static Logger logger = LoggerFactory.getLogger(RedisManager.class);
    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    private RedisSerializer<String> serializer = new StringRedisSerializer();

    /**
     * 添加缓存数据（给定key已存在，进行覆盖）
     *
     * @param key
     * @param obj
     * @throws DataAccessException
     */
    public <T> void set(String key, T obj) throws DataAccessException {
        final byte[] bkey = serializer.serialize(key);
        final byte[] bvalue = serializer.serialize(obj.toString());

        logger.info("set key {} value {}", key, obj);
        redisTemplate.execute(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(bkey, bvalue);
                return null;
            }
        });
    }

    /**
     * 添加缓存数据（给定key已存在，不进行覆盖，直接返回false）
     *
     * @param key
     * @param obj
     * @return 操作成功返回true，否则返回false
     * @throws DataAccessException
     */
    public <T> boolean setNX(String key, T obj) throws DataAccessException {
        final byte[] bkey = serializer.serialize(key);
        final byte[] bvalue = serializer.serialize(obj.toString());
        logger.info("setNX key {} value {}", key, obj);
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.setNX(bkey, bvalue);
            }
        });

        return result;
    }

    /**
     * 添加缓存数据，设定缓存失效时间
     *
     * @param key
     * @param obj
     * @param expireSeconds 过期时间，单位 秒
     * @throws DataAccessException
     */
    public <T> void setEx(String key, T obj, final long expireSeconds) throws DataAccessException {
        final byte[] bkey = serializer.serialize(key);
        final byte[] bvalue = serializer.serialize(obj.toString());
        logger.info("setEx key {} value {}", key, obj);
        redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                connection.setEx(bkey, expireSeconds, bvalue);
                return true;
            }
        });
    }

    /**
     * 获取key对应value
     *
     * @param key
     * @return
     * @throws DataAccessException
     */
    public <T> T get(final String key) throws DataAccessException {
        final byte[] keyStr = serializer.serialize(key);
        return get(keyStr);
    }

    /**
     * 根据 key字节数组 获取value
     *
     * @param keyStr
     * @param <T>
     * @return
     */
    public <T> T get(final byte[] keyStr) {
        T result = redisTemplate.execute(new RedisCallback<T>() {
            public T doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] value = connection.get(keyStr);
                return deseriaValueByte(value);
            }
        });
        return result;
    }

    /**
     * 反序列化value字节数组
     *
     * @param value
     * @param <T>
     * @return
     */
    private <T> T deseriaValueByte(byte[] value) {
        if (value == null) {
            return null;
        }
        String valueStr = serializer.deserialize(value);
        T retStr;
        try {
            retStr = SerializableUtils.deserialize(valueStr);
        } catch (Exception e) {
            logger.error("deseriaValueByte {} happen RuntimeException {}", value, e.getMessage());
            return null;
        }
        return retStr;
    }

    /**
     * 删除指定key数据
     *
     * @param key
     * @return 返回操作影响记录数
     */
    public Long delete(final String key) throws DataAccessException {
        logger.info("delete key {} from redis", key);
        if (StringUtils.isEmpty(key)) {
            return 0l;
        }
        Long delNum = redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keys = serializer.serialize(key);
                return connection.del(keys);
            }
        });
        return delNum;
    }

    /**
     * 根据key模糊查询value set集合
     *
     * @param key
     * @param <T>
     * @return
     * @throws DataAccessException
     */
    public <T> Set<T> keys(final String key) throws DataAccessException {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        Set<T> res = redisTemplate.execute(new RedisCallback<Set<T>>() {
            public Set<T> doInRedis(RedisConnection connection)
                    throws DataAccessException {
                Set<T> tSet = new HashSet<>();
                byte[] keys = serializer.serialize(key);
                Set<byte[]> keysByteSet = connection.keys(keys);
                if (keysByteSet != null && keysByteSet.size() > 0)
                    for (byte[] key : keysByteSet) {
                        byte[] valueByte = connection.get(key);
                        T value = deseriaValueByte(valueByte);
                        tSet.add(value);
                    }
                return tSet;
            }
        });
        return res;
    }

    /**
     * 清空缓存
     *
     * @return
     */
    public boolean flushDB() throws DataAccessException {
        logger.info("flushDB in redis...");
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.flushDb();
                return true;
            }
        });
        return result;
    }

}
