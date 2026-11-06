package org.linketinder.http

class HttpLogger {
    private HttpLogger() {
    }

    static long iniciar() {
        return System.nanoTime()
    }

    static void registrar(
            String method,
            String path,
            int status,
            long inicio,
            String detalhe = null
    ) {
        long duracaoMs = (long) ((System.nanoTime() - inicio) / 1_000_000)
        String mensagem = detalhe
                ? "[HTTP] ${method} ${path} -> ${status} (${detalhe}, ${duracaoMs} ms)"
                : "[HTTP] ${method} ${path} -> ${status} (${duracaoMs} ms)"

        println(mensagem)
    }
}