package com.esg.coleta.bdd;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Contexto compartilhado entre todas as classes de steps do Cucumber.
 * Anotado com @ScenarioScope para que uma nova instância seja criada por cenário,
 * garantindo isolamento entre testes.
 */
@Component
@ScenarioScope
@Data
public class SharedTestContext {
    private Response response;
    private Map<String, Object> requestBody = new HashMap<>();
    private Long pontoColetaId;
    private Long coletaId;
}
