package org.iflytek.common.core.redis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * spring redis 工具类
 *
 * @author ruoyi
 **/
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    @Autowired(required = false)
    public RedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, Object> memory = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> memoryExpire = new ConcurrentHashMap<>();
    private boolean isExpired(String key) {
        Long t = memoryExpire.get(key);
        return t != null && System.currentTimeMillis() > t;
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        if (redisTemplate != null)
        {
            try
            {
                redisTemplate.opsForValue().set(key, value);
                return;
            }
            catch (Exception ignored)
            {
            }
        }
        memory.put(key, value);
        memoryExpire.remove(key);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        if (redisTemplate != null)
        {
            try
            {
                redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
                return;
            }
            catch (Exception ignored)
            {
            }
        }
        memory.put(key, value);
        long t = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        memoryExpire.put(key, t);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        if (redisTemplate != null)
        {
            try
            {
                return redisTemplate.expire(key, timeout, unit);
            }
            catch (Exception ignored)
            {
            }
        }
        if (!memory.containsKey(key))
        {
            return false;
        }
        long t = System.currentTimeMillis() + unit.toMillis(timeout);
        memoryExpire.put(key, t);
        return true;
    }

    /**
     * 获取有效时间
     *
     * @param key Redis键
     * @return 有效时间
     */
    public long getExpire(final String key)
    {
        if (redisTemplate != null)
        {
            try
            {
                return redisTemplate.getExpire(key);
            }
            catch (Exception ignored)
            {
            }
        }
        Long t = memoryExpire.get(key);
        if (t == null)
        {
            return 0L;
        }
        long left = t - System.currentTimeMillis();
        if (left <= 0)
        {
            memory.remove(key);
            memoryExpire.remove(key);
            return 0L;
        }
        return TimeUnit.MILLISECONDS.toSeconds(left);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key)
    {
        if (redisTemplate != null)
        {
            try
            {
                return redisTemplate.hasKey(key);
            }
            catch (Exception ignored)
            {
            }
        }
        if (!memory.containsKey(key))
        {
            return false;
        }
        if (isExpired(key))
        {
            memory.remove(key);
            memoryExpire.remove(key);
            return false;
        }
        return true;
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key)
    {
        if (redisTemplate != null)
        {
            try
            {
                ValueOperations<String, T> operation = redisTemplate.opsForValue();
                return operation.get(key);
            }
            catch (Exception ignored)
            {
            }
        }
        if (isExpired(key))
        {
            memory.remove(key);
            memoryExpire.remove(key);
            return null;
        }
        Object v = memory.get(key);
        try
        {
            return (T) v;
        }
        catch (ClassCastException e)
        {
            return null;
        }
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key)
    {
        if (redisTemplate != null)
        {
            try
            {
                return redisTemplate.delete(key);
            }
            catch (Exception ignored)
            {
            }
        }
        boolean existed = memory.remove(key) != null;
        memoryExpire.remove(key);
        return existed;
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public boolean deleteObject(final Collection collection)
    {
        if (redisTemplate != null)
        {
            try
            {
                return redisTemplate.delete(collection) > 0;
            }
            catch (Exception ignored)
            {
            }
        }
        boolean any = false;
        for (Object o : collection)
        {
            if (o == null) continue;
            String k = String.valueOf(o);
            any = deleteObject(k) || any;
        }
        return any;
    }

    /**
     * 缓存List数据
     *
     * @param key 缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key)
    {
        if (redisTemplate != null) {
            return redisTemplate.opsForList().range(key, 0, -1);
        } else {
            Object v = getCacheObject(key);
            if (v instanceof List) {
                try {
                    return (List<T>) v;
                } catch (ClassCastException e) {
                    return java.util.Collections.emptyList();
                }
            }
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 缓存Set
     *
     * @param key 缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        if (redisTemplate == null) {
            return null;
        }
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext())
        {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        if (redisTemplate != null) {
            return redisTemplate.opsForSet().members(key);
        } else {
            Object v = getCacheObject(key);
            if (v instanceof Set) {
                try {
                    return (Set<T>) v;
                } catch (ClassCastException e) {
                    return java.util.Collections.emptySet();
                }
            }
            return java.util.Collections.emptySet();
        }
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (redisTemplate != null && dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        if (redisTemplate != null) {
            return redisTemplate.opsForHash().entries(key);
        } else {
            Object v = getCacheObject(key);
            if (v instanceof Map) {
                try {
                    return (Map<String, T>) v;
                } catch (ClassCastException e) {
                    return java.util.Collections.emptyMap();
                }
            }
            return java.util.Collections.emptyMap();
        }
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        if (redisTemplate == null) {
            return null;
        }
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        if (redisTemplate != null) {
            return redisTemplate.opsForHash().multiGet(key, hKeys);
        } else {
            Map<String, T> m = getCacheMap(key);
            if (m.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            List<T> list = new java.util.ArrayList<>();
            for (Object hk : hKeys) {
                if (hk == null) {
                    list.add(null);
                } else {
                    list.add(m.get(String.valueOf(hk)));
                }
            }
            return list;
        }
    }

    /**
     * 删除Hash中的某条数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return 是否成功
     */
    public boolean deleteCacheMapValue(final String key, final String hKey)
    {
        if (redisTemplate != null) {
            return redisTemplate.opsForHash().delete(key, hKey) > 0;
        } else {
            Map<String, Object> m = getCacheMap(key);
            if (m.isEmpty()) {
                return false;
            }
            boolean existed = m.remove(hKey) != null;
            setCacheObject(key, m);
            return existed;
        }
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        if (redisTemplate != null)
        {
            try
            {
                return redisTemplate.keys(pattern);
            }
            catch (Exception ignored)
            {
            }
        }
        if (pattern == null || pattern.equals("*"))
        {
            return new java.util.ArrayList<>(memory.keySet());
        }
        if (pattern.endsWith("*"))
        {
            String prefix = pattern.substring(0, pattern.length() - 1);
            java.util.List<String> list = new java.util.ArrayList<>();
            for (String k : memory.keySet())
            {
                if (k.startsWith(prefix))
                {
                    list.add(k);
                }
            }
            return list;
        }
        if (memory.containsKey(pattern))
        {
            return java.util.Collections.singletonList(pattern);
        }
        return java.util.Collections.emptyList();
    }
}
