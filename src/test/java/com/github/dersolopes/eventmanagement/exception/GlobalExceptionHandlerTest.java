package com.github.dersolopes.eventmanagement.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dersolopes.eventmanagement.controller.CategoryController;
import com.github.dersolopes.eventmanagement.dto.CategoryRequestDTO;
import com.github.dersolopes.eventmanagement.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService service; // Corrigido para CategoryService

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
                .andExpect(jsonPath("$.fieldErrors.name").value("O nome deve ter entre 3 e 50 caracteres"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 Not Found com ProblemDetailResponse quando o recurso não for encontrado")
    void handleResourceNotFound_DeveRetornarStatus404() throws Exception {
        Long idInexistente = 99L;
        when(service.buscarPorId(idInexistente))
                .thenThrow(new ResourceNotFoundException("Categoria não encontrada com o ID: " + idInexistente));

        mockMvc.perform(get("/api/v1/categories/{id}", idInexistente)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                .andExpect(jsonPath("$.detail").value("Categoria não encontrada com o ID: 99"))
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
                .andExpect(jsonPath("$.detail").value("Já existe uma categoria cadastrada com o nome 'Tecnologia'"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }
}