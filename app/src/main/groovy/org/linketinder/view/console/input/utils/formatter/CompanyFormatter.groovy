package org.linketinder.view.console.input.utils.formatter

class CompanyFormatter {
    static String stringEmpresa(Map empresa) {
        String empresaString =
        """
            ID: ${empresa.id}
            Nome: ${empresa.nome}
            CNPJ: ${empresa.cnpj}
            Descrição: ${empresa.descricao}
            E-mail: ${empresa.email}
            País: ${empresa.pais}
            CEP: ${empresa.cep}
        """.stripIndent()

        return empresaString
    }
}
