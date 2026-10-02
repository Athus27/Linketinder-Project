package org.linketinder.model

import org.linketinder.model.Pessoa.UserCompany

class Vaga {
    Integer id
    Integer idEmpresa
    String titulo
    String descricao
    String localVaga
    ArrayList<String> competencias = new ArrayList<>()
    UserCompany empresa

    Vaga(
            UserCompany empresa,
            String titulo,
            String descricao,
            ArrayList<String> competencias,
            String localVaga = null
    ) {
        this.empresa = empresa
        this.titulo = titulo
        this.descricao = descricao
        this.competencias = competencias
        this.localVaga = localVaga
    }


    @Override
    public String toString() {
        return "Vaga{" +
                "id=" + id +
                ", idEmpresa=" + idEmpresa +
                ", titulo='" + titulo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", localVaga='" + localVaga + '\'' +
                ", competencias=" + competencias +
                ", empresa=" + empresa +
                '}';
    }
    String formatarVaga() {
        return """
                Título: ${titulo}
                Descrição: ${descricao}
                Local: ${localVaga ?: 'Não informado'}
                Competências: ${competencias.join(', ')}
                ---------------------------------------
        """.stripIndent()
    }
}
