package org.linketinder.view.console.input.utils.formatter

class CandidateFormatter {
    static String stringCandidato(Map candidato) {
        String candidatoString =
                """
            ID: ${candidato.id}
            Nome: ${candidato.nome}
            Sobrenome: ${candidato.sobrenome}
            Data de nascimento: ${candidato.dataNascimento}
            CPF: ${candidato.cpf}
            Descrição: ${candidato.descricao}
            E-mail: ${candidato.email}
            Formação: ${candidato.formacao}
            País: ${candidato.pais}
            CEP: ${candidato.cep}
            Competências: ${candidato.competencias.join(', ')}
        """.stripIndent()

        return candidatoString
    }

    static String stringCandidatoParaFeed(Map candidato) {
        return """
        Descrição: ${candidato.descricao}
        Formação: ${candidato.formacao}
        Competências: ${candidato.competencias.join(', ')}
    """.stripIndent()
    }
}