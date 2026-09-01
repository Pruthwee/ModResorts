package com.acme.modres.db;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer information store backed by Amazon ElastiCache (Redis) on EKS.
 *
 * <p>The former EJB {@code @Singleton}/{@code @Startup} pattern stored state
 * in a single JVM instance, which causes data inconsistencies when the
 * application is scaled horizontally across multiple EKS pod replicas.
 * This class externalises that state into a shared Redis cluster so every
 * replica reads from and writes to the same data store.
 *
 * <p>Connection parameters are supplied via environment variables:
 * <ul>
 *   <li>{@code REDIS_HOST}  – ElastiCache primary endpoint (default: {@code localhost})</li>
 *   <li>{@code REDIS_PORT}  – Redis port                   (default: {@code 6379})</li>
 * </ul>
 */
public class ModResortsCustomerInformation {

    private static final String REDIS_HOST =
            System.getenv("REDIS_HOST") != null ? System.getenv("REDIS_HOST") : "localhost";

    private static final int REDIS_PORT =
            System.getenv("REDIS_PORT") != null ? Integer.parseInt(System.getenv("REDIS_PORT")) : 6379;

    private static final String CUSTOMER_INFO_KEY = "customer:info";

    private final JedisPool jedisPool;

    public ModResortsCustomerInformation() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        this.jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
    }

    /**
     * Retrieves customer information from the shared Redis store.
     *
     * @return list of customer info strings stored under {@value #CUSTOMER_INFO_KEY}
     */
    public ArrayList<String> getCustomerInformation() {
        ArrayList<String> customerInfo = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> values = jedis.lrange(CUSTOMER_INFO_KEY, 0, -1);
            if (values != null) {
                customerInfo.addAll(values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customerInfo;
    }

    /**
     * Stores a customer info entry in the shared Redis store.
     *
     * @param info the customer information string to persist
     */
    public void addCustomerInformation(String info) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.rpush(CUSTOMER_INFO_KEY, info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the underlying Jedis connection pool.
     * Call this during application shutdown.
     */
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}
