package org.linketinder.controller

import org.linketinder.auth.AuthPrincipal
import org.linketinder.dao.user.AuthDAO

class AuthController {
    private final AuthDAO authDAO

    AuthController(AuthDAO authDAO) {
        this.authDAO = authDAO
    }

    Map login(String email, String senha) {
        return authDAO.autenticar(email, senha)
    }

    AuthPrincipal validar(AuthPrincipal principal) {
        return authDAO.validarPrincipal(principal)
    }
}
