package com.partion.trade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CurrentPriceCacheService {

    private static final String CURRENT_PRICE_KEY_FORMAT = "product:%d:current-price";

    private final StringRedisTemplate stringRedisTemplate;

    public void saveCurrentPrice(Long productId, BigDecimal price) {
        String key = currentPriceKey(productId);
        stringRedisTemplate.opsForValue().set(key, price.toPlainString());
    }

    public Optional<BigDecimal> getCurrentPrice(Long productId) {
        String key = currentPriceKey(productId);
        String value = stringRedisTemplate.opsForValue().get(key);

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new BigDecimal(value));
    }

    private String currentPriceKey(Long productId) {
        return String.format(CURRENT_PRICE_KEY_FORMAT, productId);
    }
}