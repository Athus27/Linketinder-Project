package org.example

class Curtida {
    PessoaFisica candidato
    PessoaJuridica empresa
    Vaga vaga
    boolean candidatoCurtiu = false
    boolean empresaCurtiu = false

    boolean deuMatch() {
        return candidatoCurtiu && empresaCurtiu
    }


    @Override
    public String toString() {
        String curtida = ""

        if (deuMatch()) {
            curtida = "${candidato.nome}<->${empresa.nome} deu match na vaga ${vaga.titulo}."
        } else if (empresaCurtiu) {
            curtida = "${empresa.nome}->${candidato.nome}."
        } else {
            curtida = "${candidato.nome}->${vaga.titulo} de ${empresa.nome}."
        }
    }

}