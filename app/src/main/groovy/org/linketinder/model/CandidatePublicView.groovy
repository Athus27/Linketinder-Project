package org.linketinder.model

class CandidatePublicView {
    Integer id
    List<String> competencias = []
    String formacao
    String nome
    String sobrenome
    String email
    String descricao

    Map asMap() {
        Map result = [id: id, competencias: competencias, formacao: formacao]
        if (nome != null) {
            result.putAll(nome: nome, sobrenome: sobrenome, email: email, descricao: descricao)
        }
        result
    }
}
