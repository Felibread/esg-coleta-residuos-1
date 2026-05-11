package com.esg.coleta.bdd.steps;

import com.esg.coleta.bdd.SharedTestContext;
import io.cucumber.java.Before;
import io.cucumber.java.pt.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@RequiredArgsConstructor
public class PontoColetaSteps {

    @LocalServerPort
    private int port;

    private final SharedTestContext ctx;

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Dado("que a aplicação está em execução")
    public void queAAplicacaoEstaEmExecucao() {
        Response health = given().get("/api/pontos-coleta").then().extract().response();
        Integer status = health.statusCode();
        Assertions.assertThat(status).isIn(200, 404);
    }

    @Dado("que tenho os dados de um novo ponto de coleta:")
    public void queTenhoOsDadosDeUmNovoPontoDeColeta(Map<String, String> dados) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", dados.get("nome"));
        body.put("tipoResiduo", dados.get("tipoResiduo"));
        body.put("endereco", dados.get("endereco"));
        body.put("capacidadeKg", Double.parseDouble(dados.get("capacidadeKg")));
        ctx.setRequestBody(body);
    }

    @Dado("que tenho dados inválidos para um ponto de coleta:")
    public void queTenhoDadosInvalidosParaUmPontoDeColeta(Map<String, String> dados) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", dados.getOrDefault("nome", ""));
        body.put("tipoResiduo", dados.get("tipoResiduo"));
        body.put("endereco", dados.getOrDefault("endereco", ""));
        String cap = dados.get("capacidadeKg");
        if (cap != null && !cap.isBlank()) {
            body.put("capacidadeKg", Double.parseDouble(cap));
        }
        ctx.setRequestBody(body);
    }

    @Dado("que existe um ponto de coleta cadastrado com nome {string} e tipo {string}")
    public void queExisteUmPontoDeColetaCadastrado(String nome, String tipo) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("tipoResiduo", tipo);
        body.put("endereco", "Endereço de Teste, 123");
        body.put("capacidadeKg", 300.0);
        Response r = given().contentType(ContentType.JSON).body(body)
                .post("/api/pontos-coleta").then().statusCode(201).extract().response();
        ctx.setPontoColetaId(r.jsonPath().getLong("id"));
    }

    @Dado("que existem pontos de coleta cadastrados no sistema")
    public void queExistemPontosDeColetaCadastradosNoSistema() {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Ponto Teste Ativo");
        body.put("tipoResiduo", "METAL");
        body.put("endereco", "Av. Sustentável, 500");
        body.put("capacidadeKg", 200.0);
        given().contentType(ContentType.JSON).body(body)
                .post("/api/pontos-coleta").then().statusCode(201);
    }

    @Quando("envio uma requisição POST para {string}")
    public void envioUmaRequisicaoPOST(String endpoint) {
        ctx.setResponse(given().contentType(ContentType.JSON)
                .body(ctx.getRequestBody())
                .post(endpoint).then().extract().response());
    }

    @Quando("envio uma requisição GET para {string}")
    public void envioUmaRequisicaoGET(String endpoint) {
        String resolved = endpoint.replace("{id}",
                ctx.getPontoColetaId() != null ? ctx.getPontoColetaId().toString() : "0");
        ctx.setResponse(given().get(resolved).then().extract().response());
    }

    @Então("o status da resposta deve ser {int}")
    public void oStatusDaRespostaDeveSer(int expected) {
        Integer statusAtual = ctx.getResponse().statusCode();
        Assertions.assertThat(statusAtual)
                .as("Status HTTP esperado: %d, obtido: %d", expected, statusAtual)
                .isEqualTo(expected);
    }

    @Então("o corpo da resposta deve conter o campo {string} não nulo")
    public void oCorpoDaRespostaDeveConterCampoNaoNulo(String campo) {
        Object valor = ctx.getResponse().jsonPath().get(campo);
        Assertions.assertThat(valor)
                .as("Campo '%s' não pode ser nulo", campo)
                .isNotNull();
    }

    @Então("o corpo da resposta deve conter {string} com valor {string}")
    public void oCorpoDaRespostaDeveConterComValor(String campo, String valorEsperado) {
        String valor = ctx.getResponse().jsonPath().getString(campo);
        Assertions.assertThat(valor)
                .as("Campo '%s'", campo)
                .isEqualTo(valorEsperado);
    }

    @Então("o corpo da resposta deve conter o campo {string} com valor {string}")
    public void oCorpoDaRespostaDeveConterOCampoComValor(String campo, String valorEsperado) {
        String valor = ctx.getResponse().jsonPath().getString(campo);
        Assertions.assertThat(valor)
                .as("Campo '%s'", campo)
                .isEqualTo(valorEsperado);
    }

    @Então("o corpo da resposta deve conter o campo {string} com erros de validação")
    public void oCorpoDaRespostaDeveConterErrosDeValidacao(String campo) {
        List<?> mensagens = ctx.getResponse().jsonPath().getList(campo);
        Assertions.assertThat(mensagens)
                .as("Erros de validação em '%s'", campo)
                .isNotNull()
                .isNotEmpty();
    }

    @Então("o corpo da resposta deve ser uma lista")
    public void oCorpoDaRespostaDeveSerUmaLista() {
        List<?> lista = ctx.getResponse().jsonPath().getList("$");
        Assertions.assertThat(lista).isNotNull();
    }

    @Então("todos os itens da lista devem ter {string} igual a {string}")
    public void todosOsItensDaListaDevemTer(String campo, String valorEsperado) {
        if (valorEsperado.equalsIgnoreCase("true") || valorEsperado.equalsIgnoreCase("false")) {
            boolean boolEsperado = Boolean.parseBoolean(valorEsperado);
            List<Boolean> valores = ctx.getResponse().jsonPath().getList(campo, Boolean.class);
            if (valores != null && !valores.isEmpty()) {
                Assertions.assertThat(valores)
                        .allSatisfy(v -> Assertions.assertThat(v).isEqualTo(boolEsperado));
            }
        } else {
            List<String> valores = ctx.getResponse().jsonPath().getList(campo, String.class);
            if (valores != null && !valores.isEmpty()) {
                Assertions.assertThat(valores)
                        .allSatisfy(v -> Assertions.assertThat(v).isEqualTo(valorEsperado));
            }
        }
    }
}