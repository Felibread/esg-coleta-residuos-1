package com.esg.coleta.bdd.steps;

import com.esg.coleta.bdd.SharedTestContext;
import io.cucumber.java.Before;
import io.cucumber.java.pt.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@RequiredArgsConstructor
public class ColetaSteps {

    @LocalServerPort
    private int port;

    private final SharedTestContext ctx;

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Dado("que existe um ponto de coleta ativo com tipo {string}")
    public void queExisteUmPontoDeColetaAtivoComTipo(String tipo) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Ponto BDD Coleta " + tipo);
        body.put("tipoResiduo", tipo);
        body.put("endereco", "Rua BDD, 42");
        body.put("capacidadeKg", 1000.0);

        Response r = given().contentType(ContentType.JSON).body(body)
                .post("/api/pontos-coleta").then().statusCode(201).extract().response();

        ctx.setPontoColetaId(r.jsonPath().getLong("id"));
    }

    @Dado("que tenho os dados de uma nova coleta:")
    public void queTenhoOsDadosDeUmaNovaColeta(Map<String, String> dados) {
        Map<String, Object> body = new HashMap<>();
        body.put("pontoColetaId", ctx.getPontoColetaId());
        body.put("dataColeta", dados.get("dataColeta"));
        body.put("pesoColetadoKg", Double.parseDouble(dados.get("pesoColetadoKg")));
        body.put("responsavel", dados.get("responsavel"));
        if (dados.containsKey("status") && !dados.get("status").isBlank()) {
            body.put("status", dados.get("status"));
        }
        if (dados.containsKey("observacoes")) {
            body.put("observacoes", dados.get("observacoes"));
        }
        ctx.setRequestBody(body);
    }

    @Dado("que tenho os dados de uma coleta inválida:")
    public void queTenhoOsDadosDeUmaColetaInvalida(Map<String, String> dados) {
        Map<String, Object> body = new HashMap<>();
        body.put("pontoColetaId", ctx.getPontoColetaId());
        body.put("dataColeta", dados.get("dataColeta"));
        body.put("pesoColetadoKg", Double.parseDouble(dados.get("pesoColetadoKg")));
        body.put("responsavel", dados.get("responsavel"));
        ctx.setRequestBody(body);
    }

    @Dado("que existe uma coleta com status {string} cadastrada")
    public void queExisteUmaColetaComStatusCadastrada(String status) {
        Map<String, Object> body = new HashMap<>();
        body.put("pontoColetaId", ctx.getPontoColetaId());
        body.put("dataColeta", "2025-06-20");
        body.put("pesoColetadoKg", 80.0);
        body.put("responsavel", "Operador BDD");
        body.put("status", status);

        Response r = given().contentType(ContentType.JSON).body(body)
                .post("/api/coletas").then().statusCode(201).extract().response();

        ctx.setColetaId(r.jsonPath().getLong("id"));
    }

    @Dado("que uso o ID 99999 como ponto de coleta inexistente")
    public void queUsoOIdComoPontoDeColetaInexistente() {
        ctx.setPontoColetaId(99999L);
    }

    @Quando("envio uma requisição POST para {string} com ponto inexistente")
    public void envioUmaRequisicaoPOSTComPontoInexistente(String endpoint) {
        Map<String, Object> body = new HashMap<>();
        body.put("pontoColetaId", ctx.getPontoColetaId());
        body.put("dataColeta", "2025-07-01");
        body.put("pesoColetadoKg", 100.0);
        body.put("responsavel", "Teste");

        ctx.setResponse(given().contentType(ContentType.JSON).body(body)
                .post(endpoint).then().extract().response());
    }

    @Quando("envio uma requisição PATCH para atualizar o status para {string}")
    public void envioUmaRequisicaoPATCHParaAtualizarOStatus(String novoStatus) {
        ctx.setResponse(given()
                .patch("/api/coletas/" + ctx.getColetaId() + "/status?status=" + novoStatus)
                .then().extract().response());
    }
}
