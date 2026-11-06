package org.linketinder.auth

import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class SessionStoreTest extends Specification {
    def "emite tokens opacos distintos e expira exatamente em quatro horas"() {
        given:
        def clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"))
        def store = new SessionStore(clock)
        def principal = new AuthPrincipal(7, "Recrutador", "recruiter", 3)

        when:
        String first = store.issue(principal)
        String second = store.issue(principal)

        then:
        first != second
        first.size() >= 43
        store.resolve(first).get() == principal

        when:
        clock.current = Instant.parse("2026-01-01T04:00:00Z")

        then:
        store.resolve(first).isEmpty()
    }

    def "revoga token de forma idempotente e não expõe o token"() {
        given:
        def store = new SessionStore(Clock.systemUTC())
        String token = store.issue(new AuthPrincipal(1, "Nome", "company", 1))

        expect:
        store.revoke(token)
        store.revoke(token)
        store.resolve(token).isEmpty()
    }

    private static class MutableClock extends Clock {
        Instant current

        MutableClock(Instant current) {
            this.current = current
        }

        @Override ZoneId getZone() { ZoneOffset.UTC }
        @Override Clock withZone(ZoneId zone) { this }
        @Override Instant instant() { current }
    }
}
