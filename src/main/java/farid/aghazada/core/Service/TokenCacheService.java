package farid.aghazada.core.Service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class TokenCacheService {

    private static final String CACHE_NAME = "activeJwts";

    private final CacheManager cacheManager;

    public TokenCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void addToken(String token) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(token, true);
        }
    }

    public void removeToken(String token) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(token);
        }
    }

    public boolean isTokenActive(String token) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return false;
        }

        Cache.ValueWrapper cachedToken = cache.get(token);
        return cachedToken != null;
    }

    public boolean isCacheAvailable() {
        return cacheManager.getCache(CACHE_NAME) != null;
    }
    
}
