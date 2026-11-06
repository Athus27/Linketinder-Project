package org.linketinder.model.Pessoa

class UserCandidate extends User {
    String cpf
    int idade

    @Override
    String toString() {
        return """
                Perfil de candidato
                    Competencias: ${competencias.join(', ')}
""".stripIndent()
    }

    @Override
    String exibirInformacoesFeed() {
        return """
                    Competencias: ${competencias.join(', ')}
""".stripIndent()
    }
}
