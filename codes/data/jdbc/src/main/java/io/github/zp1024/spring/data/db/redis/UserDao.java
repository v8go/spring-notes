/**
 * The Apache License 2.0
 * Copyright (c) 2016 Zhang Peng
 */
package io.github.zp1024.spring.data.db.redis;

import io.github.zp1024.spring.data.db.redis.bean.UserDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhang Peng
 * @date 2017/4/12.
 */
public class UserDao extends AbstractBaseRedisDao<String, UserDTO> implements IUserDao {

    /**
     * 新增
     */
    @Override
    public boolean add(final UserDTO UserDTO) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(UserDTO.getId());
                byte[] name = serializer.serialize(UserDTO.getName());
                return connection.setNX(key, name);
            }
        });
        return result;
    }

    /**
     * 批量新增 使用pipeline方式
     */
    @Override
    public boolean add(final List<UserDTO> list) {
        Assert.notEmpty(list);
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                for (UserDTO UserDTO : list) {
                    byte[] key = serializer.serialize(UserDTO.getId());
                    byte[] name = serializer.serialize(UserDTO.getName());
                    connection.setNX(key, name);
                }
                return true;
            }
        }, false, true);
        return result;
    }

    /**
     * 删除
     */
    @Override
    public void delete(String key) {
        List<String> list = new ArrayList<String>();
        list.add(key);
        delete(list);
    }

    /**
     * 删除多个
     */
    @Override
    public void delete(List<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 修改
     */
    @Override
    public boolean update(final UserDTO UserDTO) {
        String key = UserDTO.getId();
        if (get(key) == null) {
            throw new NullPointerException("数据行不存在, key = " + key);
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(UserDTO.getId());
                byte[] name = serializer.serialize(UserDTO.getName());
                connection.set(key, name);
                return true;
            }
        });
        return result;
    }

    /**
     * 通过key获取
     */
    @Override
    public UserDTO get(final String keyId) {
        UserDTO result = redisTemplate.execute(new RedisCallback<UserDTO>() {
            public UserDTO doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(keyId);
                byte[] value = connection.get(key);
                if (value == null) {
                    return null;
                }
                String name = serializer.deserialize(value);
                return new UserDTO(keyId, name, null);
            }
        });
        return result;
    }
}
