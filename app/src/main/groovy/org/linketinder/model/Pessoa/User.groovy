package org.linketinder.model.Pessoa

abstract class User {
    String nome
    String descricao
    String email
    String estado
    String cep

    ArrayList<String> competencias = new ArrayList<>()

    abstract String exibirInformacoesFeed();

}
