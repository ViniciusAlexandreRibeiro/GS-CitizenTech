package br.com.fiap.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

/**
 * Libera o acesso da interface web à API.
 * O index.html é aberto direto do arquivo (origem "null"), então a origem é liberada com "*".
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requisicao, ContainerResponseContext resposta) {
        MultivaluedMap<String, Object> headers = resposta.getHeaders();
        headers.putSingle("Access-Control-Allow-Origin", "*");
        headers.putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        headers.putSingle("Access-Control-Allow-Headers", "Content-Type, Accept");
        headers.putSingle("Access-Control-Allow-Private-Network", "true");
        headers.putSingle("Access-Control-Max-Age", "3600");
    }
}
