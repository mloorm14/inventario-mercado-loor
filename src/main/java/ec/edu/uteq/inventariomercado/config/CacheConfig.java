package ec.edu.uteq.inventariomercado.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.uteq.inventariomercado.dto.ProductoPageResult;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final String PRODUCTOS_CACHE = "productos";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(ObjectMapper objectMapper) {
        // Serializador atado al tipo concreto ProductoPageResult (no polimorfico/generico):
        // GenericJackson2JsonRedisSerializer con "default typing" no sirve aqui porque los DTOs
        // son records (implicitamente final) y Jackson omite la metadata de tipo ("@class") para
        // el valor raiz cuando su clase en tiempo de ejecucion es final, dejando un JSON que el
        // propio Jackson no puede releer (SerializationException al hacer GET desde Redis).
        // Como esta cache solo guarda un tipo conocido, basta un serializador tipado explicito.
        RedisCacheConfiguration productosCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(objectMapper, ProductoPageResult.class)
                        )
                );

        return builder -> builder.withCacheConfiguration(PRODUCTOS_CACHE, productosCacheConfig);
    }
}
