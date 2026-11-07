package org.linketinder.model

class CompanyPublicView {
    Integer id
    String nome
    String email

    Map asMap() {
        Map result = [id: id]
        if (nome != null) {
            result.putAll(nome: nome, email: email)
        }
        result
    }
}
