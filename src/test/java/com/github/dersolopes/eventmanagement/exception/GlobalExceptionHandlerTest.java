package com.github.dersolopes.eventmanagement.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dersolopes.eventmanagement.controller.CategoryController;
import com.github.dersolopes.eventmanagement.dto.CategoryRequestDTO;
import com.github.dersolopes.eventmanagement.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyLong; // Ou any(Long.class)

@WebMvcTest(controllers = CategoryController.class)
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService service;

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o nome for nulo (regra de obrigatoriedade)")
    void handleValidationErrors_NomeNulo_DeveRetornarStatus400() throws Exception {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO(null);

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Requisição Inválida"))
                .andExpect(jsonPath("$.fieldErrors.name").value("O nome da categoria é obrigatório"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o nome for menor que o limite de caracteres")
    void handleValidationErrors_NomeCurto_DeveRetornarStatus400() throws Exception {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO("a");

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Requisição Inválida"))
                .andExpect(jsonPath("$.fieldErrors.name").value("O nome deve ter entre 3 e 50 caracteres"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 Not Found com ProblemDetailResponse quando o recurso não for encontrado")
    void handleResourceNotFound_DeveRetornarStatus404() throws Exception {
        Long idInexistente = 99L; // Utilizar Long em vez de UUID

        when(service.buscarPorId(idInexistente))
                .thenThrow(new ResourceNotFoundException("Categoria não encontrada com o ID: " + idInexistente));

        mockMvc.perform(get("/api/v1/categories/{id}", idInexistente)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                .andExpect(jsonPath("$.details").value("Categoria não encontrada com o ID: 99"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    @DisplayName("Deve retornar HTTP 409 Conflict com ProblemDetailResponse ao violar regra de negócio")
    void handleBusinessException_DeveRetornarStatus409() throws Exception {
        CategoryRequestDTO duplicateRequest = new CategoryRequestDTO("Tecnologia");

        when(service.criar(any(CategoryRequestDTO.class)))
                .thenThrow(new BusinessException("Já existe uma categoria cadastrada com o nome 'Tecnologia'"));

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Regra de Negócio Violada"))
                .andExpect(jsonPath("$.details").value("Já existe uma categoria cadastrada com o nome 'Tecnologia'"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }


    @Test
    @DisplayName("Deve retornar HTTP 409 Conflict quando ocorrer violação de integridade de dados no banco")
    void handleDataIntegrity_DeveRetornarStatus409() throws Exception {
        when(service.buscarPorId(anyLong())) // Usa anyLong() ou any(Long.class)
                .thenThrow(new DataIntegrityViolationException("Constraint violation", new RuntimeException("duplicate key")));

        mockMvc.perform(get("/api/v1/categories/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflito de Dados"))
                .andExpect(jsonPath("$.details").value("O registro já existe no sistema ou possui dependências ativas vinculadas."));
    }

    @Test
    @DisplayName("Deve retornar HTTP 401 Unauthorized quando as credenciais forem inválidas")
    void handleBadCredentials_DeveRetornarStatus401() throws Exception {
        when(service.buscarPorId(1L))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(get("/api/v1/categories/{id}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Não Autorizado"))
                .andExpect(jsonPath("$.details").value("Credenciais inválidas. Verifique seu e-mail e senha."));
    }

    @Test
    @DisplayName("Deve retornar HTTP 403 Forbidden quando o acesso for negado")
    void handleAccessDenied_DeveRetornarStatus403() throws Exception {
        when(service.buscarPorId(1L))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/v1/categories/{id}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Acesso Negado"))
                .andExpect(jsonPath("$.details").value("Você não possui permissão para acessar este recurso."));
    }

    @Test
    @DisplayName("Deve retornar HTTP com status dinâmico quando ocorrer ResponseStatusException")
    void handleResponseStatusException_DeveRetornarStatusCorrespondente() throws Exception {
        when(service.buscarPorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso indisponível"));

        mockMvc.perform(get("/api/v1/categories/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                .andExpect(jsonPath("$.details").value("Recurso indisponível"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 500 Internal Server Error quando ocorrer exceção não tratada")
    void handleUncaughtException_DeveRetornarStatus500() throws Exception {
        when(service.buscarPorId(1L))
                .thenThrow(new RuntimeException("Erro inesperado"));

        mockMvc.perform(get("/api/v1/categories/{id}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Erro Interno no Servidor"))
                .andExpect(jsonPath("$.details").value("Ocorreu um erro inesperado. Entre em contato com o suporte se o problema persistir."));
    }
}