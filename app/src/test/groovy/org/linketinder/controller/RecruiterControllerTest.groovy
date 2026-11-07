package org.linketinder.controller

import org.linketinder.dao.user.RecruiterDAO
import spock.lang.Specification

class RecruiterControllerTest extends Specification {
    def dao = Mock(RecruiterDAO)
    def controller = new RecruiterController(dao)

    def "valida CPF, data real adulta e senha antes de persistir"() {
        when:
        controller.criar(payload, 4)

        then:
        thrown(IllegalArgumentException)
        0 * dao._

        where:
        payload << [
                valid().tap { cpf = "11111111111" },
                valid().tap { cpf = "12345678900" },
                valid().tap { dataNascimento = "2024-02-30" },
                valid().tap { dataNascimento = "2010-01-01" },
                valid().tap { dataNascimento = "2999-01-01" },
                valid().tap { senha = "é" * 36 + "A1" },
                valid().tap { senha = "abcdefgh" }
        ]
    }

    def "aceita senha UTF-8 no limite de 72 bytes com letra e número"() {
        given:
        dao.criar(_, 4) >> { Map data, int company ->
            [id: 9, nome: data.nome, email: data.email, area: data.area,
             descricao: data.descricao, idEmpresa: company, ativo: true]
        }

        expect:
        controller.criar(valid().tap { senha = "é" * 35 + "A1" }, 4) != null
    }

    def "aplica limites obrigatórios de nome, área, descrição e e-mail"() {
        when:
        controller.criar(payload, 4)

        then:
        thrown(IllegalArgumentException)
        0 * dao._

        where:
        payload << [
                valid().tap { remove("nome") },
                valid().tap { nome = "" },
                valid().tap { nome = "a" },
                valid().tap { nome = "n" * 121 },
                valid().tap { remove("area") },
                valid().tap { area = "" },
                valid().tap { area = "a" },
                valid().tap { area = "a" * 101 },
                valid().tap { remove("descricao") },
                valid().tap { descricao = "" },
                valid().tap { descricao = "d" * 2001 },
                valid().tap { remove("email") },
                valid().tap { email = "" },
                valid().tap { email = "invalido" }
        ]
    }

    def "normaliza dados permitidos e devolve projeção sem PII sensível"() {
        given:
        dao.criar(_, 4) >> { Map data, int company ->
            assert data.email == "recrutador@example.test"
            assert data.cpf == "52998224725"
            [id: 9, nome: data.nome, email: data.email, area: data.area,
             descricao: data.descricao, idEmpresa: company, ativo: true]
        }

        when:
        Map result = controller.criar(valid().tap {
            nome = "  Recrutador  "
            email = " RECRUTADOR@EXAMPLE.TEST "
            cpf = "529.982.247-25"
        }, 4)

        then:
        result == [id: 9, nome: "Recrutador", email: "recrutador@example.test",
                   area: "Tech", descricao: "Sintetico", idEmpresa: 4, ativo: true]
        !result.containsKey("cpf")
        !result.containsKey("dataNascimento")
        !result.containsKey("senha")
    }

    private static Map valid() {
        [nome: "Recrutador", email: "recrutador@example.test", senha: "Senha123",
         area: "Tech", descricao: "Sintetico", cpf: "52998224725",
         dataNascimento: "1990-01-01"]
    }
}
