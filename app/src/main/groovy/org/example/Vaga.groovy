package org.example

class Vaga {
    String titulo
    String descricao
    ArrayList<String> competencias = new ArrayList<>()
    PessoaJuridica empresa

    Vaga(PessoaJuridica empresa, String titulo, String descricao, ArrayList<String> competencias) {
        this.empresa = empresa
        this.titulo = titulo
        this.descricao = descricao
        this.competencias = competencias
    }


    @Override
    public String toString() {
        return "Vaga{" +
                "titulo='" + titulo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", competencias=" + competencias +
                ", empresa=" + empresa +
                '}';
    }
    String formatarVaga() {
        return """
                Título: ${titulo}
                Descrição: ${descricao}
                Competências: ${competencias.join(', ')}
                ---------------------------------------
        """.stripIndent()
    }
}
