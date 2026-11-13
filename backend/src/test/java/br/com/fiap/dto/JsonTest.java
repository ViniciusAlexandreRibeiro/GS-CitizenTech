package br.com.fiap.dto;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Garante que o JSON que a web envia é entendido pela API (mesma biblioteca JSON-B do Jersey). */
class JsonTest {

    private final Jsonb jsonb = JsonbBuilder.create();

    @Test
    void aceitaDataHoraSemSegundosVindaDoInputDatetimeLocal() {
        String json = """
                {"idIdoso": 1, "idPrestador": 2, "dataAgendada": "2026-11-20T14:00", "agendadoPor": "EMPRESA"}
                """;
        CadastroVisitaDto dto = jsonb.fromJson(json, CadastroVisitaDto.class);
        assertEquals(LocalDateTime.of(2026, 11, 20, 14, 0), dto.getDataAgendada());
        assertEquals(2L, dto.getIdPrestador());
    }

    @Test
    void recordDeErroViraJson() {
        String json = jsonb.toJson(new ErroDto(409, "Conflito", "Já existe um cadastro com este CPF.", "cpf"));
        assertTrue(json.contains("\"status\":409"));
        assertTrue(json.contains("\"campo\":\"cpf\""));
    }
}
