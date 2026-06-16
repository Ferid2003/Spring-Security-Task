package farid.aghazada.core.Security;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistService {

    private final Map<String, Date> blacklistedJtis = new ConcurrentHashMap<>();

    public void blacklistToken(String jti, Date expiration) {
        blacklistedJtis.put(jti, expiration);
    }

    public boolean isTokenBlacklisted(String jti) {
        removeExpiredTokens();
        return blacklistedJtis.containsKey(jti);
    }

    private void removeExpiredTokens() {
        blacklistedJtis.entrySet().removeIf(entry -> entry.getValue().before(new Date()));
    }
}
