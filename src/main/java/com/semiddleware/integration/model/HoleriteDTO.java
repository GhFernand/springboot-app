package com.semiddleware.integration.model;

import jakarta.validation.constraints.NotBlank;

public class HoleriteDTO {

    @NotBlank(message = "Filial não pode estar vazia")
    private String filial;

    @NotBlank(message = "Matrícula não pode estar vazia")
    private String matricula;

    @NotBlank(message = "Período não pode estar vazio")
    private String periodo;

    @NotBlank(message = "CPF não pode estar vazio")
    private String cpf;
    
    private String ano;
    
    public String getAno() { return ano; }
    public void setAno(String ano) { this.ano = ano; }

    public String getFilial() {
        return filial;
    }

    public void setFilial(String filial) {
        this.filial = filial;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}