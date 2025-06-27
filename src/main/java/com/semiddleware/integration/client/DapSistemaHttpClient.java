package com.semiddleware.integration.client;

import com.semiddleware.integration.model.EnderecoViaCepDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DapSistemaHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(DapSistemaHttpClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${api.rh.api-key}")
    private String apiKey;

    @Value("${api.rh.basic-user}")
    private String basicUser;

    @Value("${api.rh.basic-pass}")
    private String basicPass;
    
    public EnderecoViaCepDTO buscarEnderecoPorCep(String cep) {
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        try {
            ResponseEntity<EnderecoViaCepDTO> response = restTemplate.getForEntity(url, EnderecoViaCepDTO.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Erro ao buscar endereço no ViaCEP", e);
            return null;
        }
    }
    
    public byte[] baixarHolerite(String filial, String matricula, String periodo) {
        String dataInicio = calcularDataInicio(periodo);
        String dataFim = calcularDataFim(periodo);

        String url = String.format("http://192.168.0.251:8112/rest/downloadrhfile/api/v1/DownEspelhoPt" +
                "?cEmpGPE=01&cFilGPE=%s&cMatGPE=%s&cPIniGPE=%s&cPFimGPE=%s&Hash=1234",
                filial, matricula, dataInicio, dataFim);

        HttpHeaders headers = new HttpHeaders();

        headers.set("x-api-key", apiKey);

        String auth = basicUser + ":" + basicPass;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Accept", "application/json");

        // Requisição com headers
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Erro ao baixar o PDF: " + response.getStatusCode());
        }
    }
    
    private String calcularDataInicio(String periodo) {
    	String[] partes = periodo.split("[-/]");
    	int mes = Integer.parseInt(partes[0]);
    	int ano = Integer.parseInt(partes[1]);
    	
    	LocalDate dataInicio;
        if (mes == 1) {
            dataInicio = LocalDate.of(ano - 1, 12, 21);
        } else {
            dataInicio = LocalDate.of(ano, mes - 1, 21);
        }
        return dataInicio.format(DateTimeFormatter.BASIC_ISO_DATE);
    }
    
    private String calcularDataFim(String periodo) {
    	String[] partes = periodo.split("[-/]");
        int mes = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]);

        LocalDate dataFim;
        if (mes == 12) {
            dataFim = LocalDate.of(ano + 1, 1, 20);
        } else {
            dataFim = LocalDate.of(ano, mes, 20);
        }
        return dataFim.format(DateTimeFormatter.BASIC_ISO_DATE);
    }
}
