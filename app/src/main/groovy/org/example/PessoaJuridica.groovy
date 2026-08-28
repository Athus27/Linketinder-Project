package org.example

class PessoaJuridica extends Pessoa {
    String CNPJ
    String pais

    @Override
    public String toString() {
        return """
                Nome: ${nome}
                    Descricao: ${descricao}
                    Competencias: ${competencias.join(', ')}
                    CEP: ${cep}
                    Estado: ${estado}
                    País: ${pais}
                    CNPJ: ${CNPJ}
                    Email: ${email}
""".stripIndent()
    }
}
