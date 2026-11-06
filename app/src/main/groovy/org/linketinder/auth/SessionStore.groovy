package org.linketinder.auth

import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.Base64
import java.util.Objects
import java.util.Optional

class SessionStore {
    static final Duration TTL = Duration.ofHours(4)
    private final Clock clock
    private final SecureRandom random
    private final Map<String, Session> sessions = new ConcurrentHashMap<>()

    SessionStore() {
        this(Clock.systemUTC())
    }

    SessionStore(Clock clock) {
        this(clock, new SecureRandom())
    }

    SessionStore(Clock clock, SecureRandom random) {
        this.clock = Objects.requireNonNull(clock)
        this.random = Objects.requireNonNull(random)
    }

    String issue(AuthPrincipal principal) {
        byte[] bytes = new byte[32]
        random.nextBytes(bytes)
        String token = Base64.encoder.encodeToString(bytes)
        sessions.put(token, new Session(principal, clock.instant().plus(TTL)))
        return token
    }

    Optional<AuthPrincipal> resolve(String token) {
        if (!token) {
            return Optional.empty()
        }
        Session session = sessions.get(token)
        if (session == null) {
            return Optional.empty()
        }
        if (!clock.instant().isBefore(session.expiresAt)) {
            sessions.remove(token, session)
            return Optional.empty()
        }
        return Optional.of(session.principal)
    }

    void revoke(String token) {
        if (token) {
            sessions.remove(token)
        }
    }
}
