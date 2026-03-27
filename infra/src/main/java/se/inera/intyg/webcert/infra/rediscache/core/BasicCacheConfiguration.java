/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.infra.rediscache.core;

import com.google.common.base.Strings;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import se.inera.intyg.webcert.infra.rediscache.core.util.ConnectionStringUtil;

@Configuration
@EnableCaching
public class BasicCacheConfiguration {

  @Value("${redis.host}")
  String redisHost;

  @Value("${redis.port}")
  String redisPort;

  @Value("${redis.password}")
  String redisPassword;

  @Value("${redis.cache.default_entry_expiry_time_in_seconds}")
  long defaultEntryExpiry;

  @Value("${redis.sentinel.master.name}")
  String redisSentinelMasterName;

  @Value("${redis.read.timeout:PT1M}")
  String redisReadTimeout;

  @Value("${redis.cluster.nodes:}")
  String redisClusterNodes;

  @Value("${redis.cluster.password:}")
  String redisClusterPassword;

  @Value("${redis.cluster.max.redirects:3}")
  Integer redisClusterMaxRedirects;

  @Value("${redis.cluster.read.timeout:PT1M}")
  String redisClusterReadTimeout;

  @Resource private Environment environment;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    final var activeProfiles = List.of(environment.getActiveProfiles());
    if (activeProfiles.contains("redis-cluster")) {
      return clusterConnectionFactory();
    }
    if (activeProfiles.contains("redis-sentinel")) {
      return sentinelConnectionFactory();
    }

    return standAloneConnectionFactory();
  }

  @Bean
  @DependsOn("cacheManager")
  public RedisCacheOptionsSetter redisCacheOptionsSetter() {
    return new RedisCacheOptionsSetter();
  }

  @Bean(name = "rediscache")
  RedisTemplate<Object, Object> redisTemplate() {
    RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(jedisConnectionFactory());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    return redisTemplate;
  }

  @Bean
  public RedisCacheManager cacheManager() {
    return new CacheFactory(
        RedisCacheWriter.nonLockingRedisCacheWriter(jedisConnectionFactory()),
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(defaultEntryExpiry)));
  }

  private JedisConnectionFactory standAloneConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(redisHost);
    redisStandaloneConfiguration.setPort(Integer.parseInt(redisPort));
    if (StringUtils.hasLength(redisPassword)) {
      redisStandaloneConfiguration.setPassword(redisPassword);
    }
    return new JedisConnectionFactory(
        redisStandaloneConfiguration, JedisClientConfiguration.builder().usePooling().build());
  }

  private JedisConnectionFactory sentinelConnectionFactory() {
    RedisSentinelConfiguration sentinelConfig =
        new RedisSentinelConfiguration().master(redisSentinelMasterName);
    sentinelConfig.setPassword(redisPassword);
    sentinelConfig.setSentinelPassword(redisPassword);

    if (Strings.isNullOrEmpty(redisHost) || Strings.isNullOrEmpty(redisPort)) {
      throw new IllegalStateException(
          "Cannot bootstrap RedisSentinelConfiguration, redis.host or redis.port is null or empty");
    }
    final var hosts = ConnectionStringUtil.parsePropertyString(redisHost);
    final var ports = ConnectionStringUtil.parsePropertyString(redisPort);

    if (hosts.isEmpty() || ports.isEmpty() || hosts.size() != ports.size()) {
      throw new IllegalStateException(
          "Cannot bootstrap RedisSentinelConfiguration, number of redis.host and/or redis.port was zero or not equal.");
    }

    for (int a = 0; a < hosts.size(); a++) {
      sentinelConfig = sentinelConfig.sentinel(hosts.get(a), Integer.parseInt(ports.get(a)));
    }

    final var clientConfig =
        JedisClientConfiguration.builder().readTimeout(Duration.parse(redisReadTimeout)).build();

    return new JedisConnectionFactory(sentinelConfig, clientConfig);
  }

  private JedisConnectionFactory clusterConnectionFactory() {
    final var clusterConfig =
        new RedisClusterConfiguration(ConnectionStringUtil.parsePropertyString(redisClusterNodes));
    clusterConfig.setMaxRedirects(redisClusterMaxRedirects);
    clusterConfig.setPassword(redisClusterPassword);

    final var clientConfig =
        JedisClientConfiguration.builder()
            .readTimeout(Duration.parse(redisClusterReadTimeout))
            .build();

    return new JedisConnectionFactory(clusterConfig, clientConfig);
  }
}
