package br.com.fiap.models;

/**
 * Tudo que passa por verificação no Guardião: uma visita na porta ou um pedido
 * de ajuda (solicitação). Ambos têm um status e podem estar aguardando decisão.
 */
public interface Verificavel {

    /** true enquanto ainda depende de confirmação ou de resposta. */
    boolean estaPendente();

    /** Texto curto e amigável do status, para exibir ao idoso. */
    String getDescricaoStatus();
}
