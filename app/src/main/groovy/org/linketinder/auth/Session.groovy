package org.linketinder.auth

import java.time.Instant

class Session {
    final AuthPrincipal principal
    final Instant expiresAt

    Session(AuthPrincipal principal, Instant expiresAt) {
        this.principal = principal
        this.expiresAt = expiresAt
    }
}
