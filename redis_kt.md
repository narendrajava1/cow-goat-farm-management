# Redis Knowledge Transfer Document

## Overview

This document provides comprehensive knowledge about Redis usage in the farm animal service. The application uses Redis for caching and reactive data storage via Spring Data Redis with reactive programming.

## Configuration

### Application Properties (application.yml)

The application is configured to use Redis via the `spring.data.redis` namespace:

```yaml
spring:
  data:
    redis:
      reactive:
        host: ${REDIS_HOST:localhost}
        port: 6379
        password: ${REDIS_PASSWORD:}
        lettuce:
          pool:
            max-active: 8
```

### Redis Configuration Class

The `RedisConfig.java` defines a reactive Redis template using:

- **Key serialization**: `StringRedisSerializer.UTF_8`
- **Value serialization**: `GenericJackson2JsonRedisSerializer`

```java
@Configuration
public class RedisConfig {
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, Object> serializationContext =
                RedisSerializationContext.<String, Object>newSerializationContext()
                        .key(StringRedisSerializer.UTF_8)
                        .value(new GenericJackson2JsonRedisSerializer())
                        .hashKey(StringRedisSerializer.UTF_8)
                        .hashValue(new GenericJackson2JsonRedisSerializer())
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
```

## Key Usage Patterns

### 1. Reactive Template Usage

The service uses `ReactiveRedisTemplate<String, Object>` directly without using Spring's blocking cache manager.

```java
@Autowired
private ReactiveRedisTemplate<String, Object> redisTemplate;
```

### 2. Common Operations

- **Set operations**: Store/retrieve POJOs as JSON
- **Key pattern**: Use human-readable keys with UUIDs
- **Type safety**: Generic type parameters for compile-time checking

### 3. Data Structures

The system uses:
- **Strings**: For single values or serialized objects
- **Hashes**: When storing structured data related to entities

## Key Patterns in Code

1. **Caching**: The reactive services directly interact with Redis via Mono/Flux streams
2. **No Blocking Cache**: All Redis operations are non-blocking and reactive
3. **JSON Serialization**: Objects are serialized as JSON using Jackson for interoperability

## Connection Pool Configuration

- Max active connections: 8
- Default connection host: localhost
- Default port: 6379
- Password: Empty by default (can be configured via environment variable)

## Environment Variables

Available configuration:
```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=""
```

## Key Redis Commands Used

When inspecting Redis:
```bash
# List keys
redis-cli KEYS "*"

# Get specific key
redis-cli GET <key>

# Get hash keys
redis-cli HKEYS <hash-key>

# Get hash values
redis-cli HGETALL <hash-key>
```

## Monitoring and Health

The application exposes Redis health indicators:
- `/actuator/health` includes Redis status
- Metrics are available via Prometheus endpoint (`/actuator/prometheus`)

## Troubleshooting

### Connection Issues

1. Check Redis server is running: `redis-cli ping`
2. Verify network connectivity
3. Validate environment variables for host/port/password
4. Ensure firewall allows connections

### Serialization Issues

1. Verify that objects being cached implement Serializable or are Jackson-compatible
2. Ensure all fields are properly annotated if using custom serializers

### Performance Issues

1. Monitor connection pool usage
2. Check key expiration strategies
3. Validate TTL settings for cached items

## Best Practices

1. **Use meaningful key patterns**: Prefix with entity type and UUID
2. **Set appropriate TTLs**: Avoid infinite caching of mutable data
3. **Handle Redis failures gracefully**: Implement fallback mechanisms
4. **Monitor connection pool usage**: Watch for connection exhaustion
5. **Use reactive patterns**: Don't block on Redis operations

## Testing Considerations

### Test Setup

Tests should mock Redis interactions or use in-memory Redis containers.

### Example Test Pattern

```java
@ExtendWith(MockitoExtension.class)
class RedisServiceTest {
    @MockBean
    private ReactiveRedisTemplate<String, Object> redisTemplate;
    
    // Test redis operations...
}
```
