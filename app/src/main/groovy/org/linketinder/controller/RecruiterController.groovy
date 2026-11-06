package org.linketinder.controller

import org.linketinder.dao.user.RecruiterDAO

import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

class RecruiterController {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)
    private final RecruiterDAO recruiterDAO
    private final Clock clock

    RecruiterController(RecruiterDAO recruiterDAO) {
        this(recruiterDAO, Clock.systemUTC())
    }

    RecruiterController(RecruiterDAO recruiterDAO, Clock clock) {
        this.recruiterDAO = recruiterDAO
        this.clock = clock
    }

    List<Map> listar(int idEmpresa) {
        recruiterDAO.listar(idEmpresa)
    }

    Map criar(Map dados, int idEmpresa) {
        Map normalized = validar(dados)
        recruiterDAO.criar(normalized, idEmpresa)
    }

    boolean desativar(int id, int idEmpresa) {
        recruiterDAO.desativar(id, idEmpresa)
    }

    private Map validar(Map dados) {
        if (!(dados.nome instanceof String) || !between(dados.nome, 2, 120) ||
                !(dados.area instanceof String) || !between(dados.area, 2, 100) ||
                !(dados.descricao instanceof String) || !between(dados.descricao, 1, 2000) ||
                !(dados.email instanceof String) || !email(dados.email) ||
                !(dados.cpf instanceof String) || !cpf(dados.cpf) ||
                !(dados.dataNascimento instanceof String) ||
                !(dados.senha instanceof String) || !password(dados.senha)) {
            throw new IllegalArgumentException("Dados de recrutador invalidos")
        }
        LocalDate birth
        try {
            birth = LocalDate.parse(dados.dataNascimento, DATE_FORMAT)
        } catch (DateTimeParseException ignored) {
            throw new IllegalArgumentException("Data de nascimento invalida")
        }
        LocalDate today = LocalDate.now(clock)
        if (birth.isAfter(today) || Period.between(birth, today).years < 18) {
            throw new IllegalArgumentException("Data de nascimento invalida")
        }
        [
                nome: dados.nome.trim(),
                email: dados.email.trim().toLowerCase(Locale.ROOT),
                senha: dados.senha,
                area: dados.area.trim(),
                descricao: dados.descricao.trim(),
                cpf: dados.cpf.replaceAll(/\D/, ''),
                dataNascimento: birth
        ]
    }

    private static boolean between(String value, int min, int max) {
        String trimmed = value.trim()
        trimmed.size() >= min && trimmed.size() <= max
    }

    private static boolean email(String value) {
        value.trim() ==~ /^[^@\s]+@[^@\s]+\.[^@\s]+$/
    }

    private static boolean cpf(String value) {
        String digits = value.replaceAll(/\D/, '')
        if (!(value ==~ /^[0-9 .()\-]+$/) || digits.size() != 11 ||
                digits ==~ /^(\d)\1{10}$/) {
            return false
        }
        int first = checkDigit(digits[0..8])
        int second = checkDigit(digits[0..8] + first)
        digits[9].toInteger() == first && digits[10].toInteger() == second
    }

    private static int checkDigit(String value) {
        int sum = 0
        for (int i = 0; i < value.size(); i++) {
            sum += value[i].toInteger() * (value.size() + 1 - i)
        }
        int remainder = (sum * 10) % 11
        remainder == 10 ? 0 : remainder
    }

    private static boolean password(String value) {
        int bytes = value.getBytes(StandardCharsets.UTF_8).length
        bytes >= 8 && bytes <= 72 &&
                value.any { Character.isLetter(it as char) } &&
                value.any { Character.isDigit(it as char) }
    }
}
