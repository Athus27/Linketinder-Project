package org.linketinder.view.console.input.utils

import spock.lang.Specification

class ValidationConsoleTest extends Specification {
    def "validadeCPF should return true for valid CPF format"() {
        expect:
        ValidationConsole.validadeCPF("123.456.789-00")
    }

    def "validadeCPF should return false for invalid CPF format"() {
        expect:
        !ValidationConsole.validadeCPF("12345678900")
    }

    def "validadeCNPJ should return true for valid CNPJ format"() {
        expect:
        ValidationConsole.validadeCNPJ("12.345.678/0001-00")
    }

    def "validadeCNPJ should return false for invalid CNPJ format"() {
        expect:
        !ValidationConsole.validadeCNPJ("12345678000100")
    }

    def "validadeCEP should return true for valid CEP format"() {
        expect:
        ValidationConsole.validadeCEP("12345-678")
    }

    def "validadeCEP should return false for invalid CEP format"() {
        expect:
        !ValidationConsole.validadeCEP("12345678")
    }

    def "validadeSenha should return true for password length >= 6"() {
        expect:
        ValidationConsole.validadeSenha("abcdef")
    }

    def "validadeSenha should return false for password length < 6"() {
        expect:
        !ValidationConsole.validadeSenha("abc")
    }
}
