package com.semiddleware.integration.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.stereotype.Service;

import com.semiddleware.integration.client.DapSistemaHttpClient;
import com.semiddleware.integration.model.EnderecoViaCepDTO;
import com.semiddleware.integration.model.HoleriteDTO;
import org.springframework.beans.factory.annotation.Value;

@Service
public class DapExternoService {

    private final DapSistemaHttpClient client;
    

    public DapExternoService(DapSistemaHttpClient client) {
        this.client = client;
    }

    public EnderecoViaCepDTO obterEnderecoPorCep(String cep) {
        return client.buscarEnderecoPorCep(cep);
    }
    
    @Value("${app.host.url}")
    private String hostBaseUrl;

    public Map<String, Object> gerarHolerite(HoleriteDTO dto) {
        byte[] pdfBytes = client.baixarHolerite(dto.getFilial(), dto.getMatricula(), dto.getPeriodo());

        try {
            String filename = String.format("holerite-%s-%s-%s.pdf",
                dto.getPeriodo().replace("/", "-"),
                dto.getFilial(),
                dto.getMatricula()
            );

            File pastaUpload = new File("uploads");
            if (!pastaUpload.exists()) {
                pastaUpload.mkdirs();
            }

            File pdfFile = new File(pastaUpload, filename);
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                fos.write(pdfBytes);
            }

            PDDocument document = PDDocument.load(pdfFile);
            AccessPermission ap = new AccessPermission();
            StandardProtectionPolicy spp = new StandardProtectionPolicy(dto.getCpf(), dto.getCpf(), ap);
            spp.setEncryptionKeyLength(128);
            spp.setPermissions(ap);
            document.protect(spp);
            document.save(pdfFile);
            document.close();

            String urlPublica = hostBaseUrl + "/files/" + filename;

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("filename", filename);
            resultado.put("file_size", pdfBytes.length);
            resultado.put("url", urlPublica);
            return resultado;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar ou criptografar o PDF", e);
        }
    }
        
}
