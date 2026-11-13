package br.com.fiap;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.io.IOException;
import java.net.URI;

/** Sobe a API do Guardião no servidor embutido Grizzly. */
public class Main {

    public static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        ResourceConfig rc = new ResourceConfig()
                .packages("br.com.fiap.resource", "br.com.fiap.exception.mapper", "br.com.fiap.filter")
                // sem WADL: as requisições OPTIONS (pré-verificação do CORS) respondem direto
                .property(ServerProperties.WADL_FEATURE_DISABLE, true);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = startServer();
        System.out.println("Guardião API rodando em " + BASE_URI);
        System.out.println("Teste: " + BASE_URI + "status");
        System.out.println("Pressione Enter para encerrar...");
        System.in.read();
        server.shutdownNow();
    }
}
