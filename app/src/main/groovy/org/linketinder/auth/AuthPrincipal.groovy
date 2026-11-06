package org.linketinder.auth

import groovy.transform.Immutable

@Immutable
class AuthPrincipal {
    Integer id
    String nome
    String tipo
    Integer idEmpresa
}
