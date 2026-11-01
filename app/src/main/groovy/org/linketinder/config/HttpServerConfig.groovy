package org.linketinder.config

import com.sun.net.httpserver.HttpServer

import java.util.concurrent.Executors

class HttpServerConfig {
    final String host
    final int port
    final int poolDeThreads

    HttpServerConfig() {
        this.host = System.getenv("HOST") ?: "localhost"
        this.port = (System.getenv("PORT") ?: "8080").toInteger()
        this.poolDeThreads = 10
    }

    HttpServer criarServidor() {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(host, port),
                0
        )

        server.executor = Executors.newFixedThreadPool(poolDeThreads)

        return server
    }
}