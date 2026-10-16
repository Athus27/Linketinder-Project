package org.linketinder.view.console.input.utils

class ValidationConsole {
    static boolean validadeCPF(String text) {
        def cpfFormatado = /^\d{3}\.\d{3}\.\d{3}-\d{2}$/
        return text ==~ cpfFormatado
    }
    static boolean validadeCNPJ(String text) {
        def cnpjFormatado = /^\d{2}\.\d{3}\.\d{3}\/\d{4}-\d{2}$/

        return text ==~ cnpjFormatado
    }

    static boolean validadeCEP(String text) {
        def cepFormatado = /^\d{5}-\d{3}$/
        return text ==~ cepFormatado
    }

    static boolean validadeSenha(String senha) {
        return senha.length() >= 6
    }

    static boolean confirmarRemocao(String registro, Scanner scanner) {
        println("Tem certeza que deseja remover ${registro}? Digite SIM para confirmar:")
        return scanner.nextLine().equalsIgnoreCase("SIM")
    }

}
