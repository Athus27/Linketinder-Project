package org.linketinder.model.Pessoa

import org.linketinder.model.Vaga

class UserCompany extends User {
    String CNPJ
    String pais
    ArrayList<Vaga> vagas = new ArrayList<>()

    @Override
    String toString() {
        return """
                Perfil de empresa
                    Competencias: ${competencias.join(', ')}
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

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (o == null ||
            getClass() !=
            o.class) {
            return false
        }

        UserCompany that = (UserCompany) o

        if (CNPJ != that.CNPJ) {
            return false
        }
        if (pais != that.pais) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = (CNPJ != null ? CNPJ.hashCode() : 0)
        result = 31 * result +
                 (pais != null ? pais.hashCode() : 0)
        return result
    }
}
