package org.linketinder.view.console.input.utils.formatter

class VagaFormatter {
    static String stringVagaFeed(Map vaga) {
        return """
        Título: ${vaga.titulo}
        Descrição: ${vaga.descricao}
        Local: ${vaga.localVaga}
        Competências: ${vaga.competencias.join(', ')}
    """.stripIndent()
    }
}
