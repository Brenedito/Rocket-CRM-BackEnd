package com.rocket.crm.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroDTO(
        @NotBlank(message = "O nome da empresa é obrigatório")
        String nomeEmpresa,

        @NotBlank(message = "O CNPJ é obrigatório")
        @Size(min = 14, max = 18, message = "CNPJ inválido")
        String cnpj,

        @NotBlank(message = "O nome do administrador é obrigatório")
        String nomeAdmin,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail com formato inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String senha
) {}