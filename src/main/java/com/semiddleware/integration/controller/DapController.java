package com.semiddleware.integration.controller;

import com.semiddleware.integration.model.EnderecoViaCepDTO;
import com.semiddleware.integration.model.HoleriteDTO;
import com.semiddleware.integration.service.DapExternoService;
import jakarta.validation.Valid;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dap")
public class DapController {

    private final DapExternoService dapExternoService;

    // LOGGER DEDICADO POR CONTEXTO
    private static final Logger mostrarLogger = LoggerFactory.getLogger("com.semiddleware.integration.log.mostrardados");
    private static final Logger holeriteLogger = LoggerFactory.getLogger("com.semiddleware.integration.log.solicholerite");
    // LOGGER GERAL
    private static final Logger logger = LoggerFactory.getLogger(DapController.class);

    public DapController(DapExternoService dapExternoService) {
        this.dapExternoService = dapExternoService;
    }

    @GetMapping("/cep")
    public ResponseEntity<?> buscarEndereco(@RequestParam String valor) {
        EnderecoViaCepDTO endereco = dapExternoService.obterEnderecoPorCep(valor);
        if (endereco == null || endereco.getCep() == null) {
            return ResponseEntity.status(404).body("Endereço não encontrado para o CEP: " + valor);
        }
        return ResponseEntity.ok(endereco);
    }
    
    @PostMapping("/holerite")
    public ResponseEntity<Map<String, Object>> gerarHolerite(@RequestBody @Valid HoleriteDTO dto) {
        holeriteLogger.debug("### INICIO SOLICITAÇÃO DE HOLERITE ###");
        holeriteLogger.debug("Recebido para matrícula: {}", dto.getMatricula());

        try {
            var resultado = dapExternoService.gerarHolerite(dto);

            File file = new File("uploads", (String) resultado.get("filename"));
            if (!file.exists()) {
                holeriteLogger.error("Arquivo gerado não encontrado: {}", resultado.get("filename"));
                return ResponseEntity.status(404).body(null);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("title", resultado.get("filename"));              // {{holerite@filename}}
            response.put("url", resultado.get("url"));                     // {{HoleriteCorpo@url}}
            response.put("type", "application/pdf");                       // MIME type fixo
            response.put("size", resultado.get("file_size"));             // {{Holerite@file_size}}

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            holeriteLogger.error("Erro ao gerar holerite", e);
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/teste")
    public Map<String, String> teste() {
        return Map.of("mensagem", "teste ok");
    }

     
}
