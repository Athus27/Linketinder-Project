package org.example

class PessoaJuridica extends Pessoa {
    String CNPJ
    String pais
    ArrayList<Vaga> vagas = new ArrayList<>()

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
                    Vagas: ${vagas.collect { it.titulo }.join(', ')}
""".stripIndent()
    }

    @Override
    String exibirInformacoesFeed() {
        if (vagas) {
            return vagas.collect { it.formatarVaga() }.join('\n')
        }
        return null
    }

    void exibirInformacoesVagas(){
        if(vagas){
            vagas.eachWithIndex{ Vaga vaga, int i ->
                printf """
            Vaga: ${vaga.titulo}
                Descrição: ${vaga.descricao}
                Competencias: ${competencias.join(", ")}
                ----------------------------------------\n
""".stripIndent()
            }
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (o == null || getClass() != o.class) return false

        PessoaJuridica that = (PessoaJuridica) o

        if (CNPJ != that.CNPJ) return false
        if (pais != that.pais) return false


        return true
    }

    int hashCode() {
        int result
        result = (CNPJ != null ? CNPJ.hashCode() : 0)
        result = 31 * result + (pais != null ? pais.hashCode() : 0)
        return result
    }
}
