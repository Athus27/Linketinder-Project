package org.example

class PessoaFisica extends Pessoa {
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
}
