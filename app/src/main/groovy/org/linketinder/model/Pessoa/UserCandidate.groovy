package org.linketinder.model.Pessoa

class UserCandidate extends User {
    String cpf
    int idade


    @Override
    public String toString() {
        return """
                Nome: ${nome}
                    Idade: ${idade} anos
                    Descricao: ${descricao}
                    Competencias: ${competencias.join(', ')}
                    CEP: ${cep}
                    Estado: ${estado}
                    CPF: ${cpf}
                    Email: ${email}
""".stripIndent()
    }

    @Override
    String exibirInformacoesFeed() {
        return """
                    Descricao: ${descricao}
                    Competencias: ${competencias.join(', ')}
                    CEP: ${cep}
                    Estado: ${estado}
""".stripIndent()
    }
}
