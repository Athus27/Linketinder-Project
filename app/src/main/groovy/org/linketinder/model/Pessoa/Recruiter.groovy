package org.linketinder.model.Pessoa

import java.time.LocalDate

class Recruiter {
    Integer idRecrutador
    Integer idEmpresa
    String area
    String descricao
    private String cpf
    LocalDate dataNascimento
    boolean ativo = true

    Recruiter() {
    }

    Recruiter(Integer idRecrutador,
              Integer idEmpresa,
              String area,
              String descricao,
              String cpf,
              LocalDate dataNascimento,
              boolean ativo = true) {
        this.idRecrutador = idRecrutador
        this.idEmpresa = idEmpresa
        this.area = area
        this.descricao = descricao
        this.cpf = cpf
        this.dataNascimento = dataNascimento
        this.ativo = ativo
    }
}
