package org.linketinder.view.console.input.utils.formatter

abstract class UserFormatter {

    static Map<String, String> mensagemDaCurtidaString = [
            "mensagemDeErro"              : "Erro ao curtir: ",
            "mensagemCandidatoJaCurtiu"   : "O candidato já curtiu essa vaga.",
            "mensagemEmpresaJaCurtiu"     : "A empresa já curtiu esse candidato para essa vaga.",
            "mensagemMatch"               : "Deu match!",
            "mensagemSucessoParaCandidato": "Candidato curtiu a vaga com sucesso!",
            "mensagemSucessoParaEmpresa"  : "Empresa curtiu o candidato com sucesso!"
    ]

    static String mensagemDaCurtida(
            Map resultado,
            String mensagemJaCurtiu,
            String mensagemSucesso
    ) {
        if (!resultado.sucesso) {
            return "${mensagemDaCurtidaString.mensagemDeErro}${resultado.mensagem}"
        } else if (resultado.jaCurtiu) {
            return mensagemJaCurtiu
        } else if (resultado.deuMatch) {
            return mensagemDaCurtidaString.mensagemMatch
        } else {
            return mensagemSucesso
        }
    }

}
