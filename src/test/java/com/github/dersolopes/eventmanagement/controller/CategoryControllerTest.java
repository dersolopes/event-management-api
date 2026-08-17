package com.github.dersolopes.eventmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dersolopes.eventmanagement.dto.CategoryRequestDTO;
import com.github.dersolopes.eventmanagement.entity.Category;
import com.github.dersolopes.eventmanagement.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class)
@WithMockUser
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService service;

    @Nested
    @DisplayName("POST /api/v1/categories - Criar Categoria")
    class CriarCategoriaTestes {

        @Test
        @DisplayName("Deve retornar HTTP 201 Created com a categoria criada no corpo da resposta")
        void criar_ComDadosValidos_DeveRetornarStatus201ECategoria() throws Exception {
            // ARRANGE
            CategoryRequestDTO request = new CategoryRequestDTO("Tecnologia");
            Category categorySalva = new Category();
            categorySalva.setId(1L);
            categorySalva.setName("Tecnologia");

            when(service.criar(any(CategoryRequestDTO.class))).thenReturn(categorySalva);

            // ACT & ASSERT
            mockMvc.perform(post("/api/v1/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Tecnologia"));

            verify(service, times(1)).criar(any(CategoryRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar HTTP 400 Bad Request ao tentar enviar payload inválido")
        void criar_ComNomeInvalido_DeveRetornarStatus400() throws Exception {
            // ARRANGE (Nome com menos de 3 caracteres viola o @Size)
            CategoryRequestDTO requestInvalido = new CategoryRequestDTO("AB");

            // ACT & ASSERT
            mockMvc.perform(post("/api/v1/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andExpect(status().isBadRequest());

            verify(service, never()).criar(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories - Listar Categoria")
    class ListarCategoriasTestes {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK com a lista de categorias")
        void listarTodas_DeveRetornarStatus200EListaDeCategorias() throws Exception {
            // ARRANGE
            Category c1 = new Category();
            c1.setId(1L);
            c1.setName("Tecnologia");

            Category c2 = new Category();
            c2.setId(2L);
            c2.setName("Workshop");

            when(service.listarTodas()).thenReturn(List.of(c1, c2));

            // ACT & ASSERT
            mockMvc.perform(get("/api/v1/categories")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("Tecnologia"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].name").value("Workshop"));

            verify(service, times(1)).listarTodas();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/{id} - Buscar por ID")
    class BuscarPorIdTestes {

        @Test
        @DisplayName("Deve retornar HTTP 200 OK e a categoria quando o ID for encontrado")
        void buscarPorId_ComIdExistente_DeveRetornarStatus200ECategoria() throws Exception {
            // ARRANGE
            Long id = 1L;
            Category category = new Category();
            category.setId(id);
            category.setName("Tecnologia");

            when(service.buscarPorId(id)).thenReturn(category);

            // ACT & ASSERT
            mockMvc.perform(get("/api/v1/categories/{id}", id)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Tecnologia"));

            verify(service, times(1)).buscarPorId(id);
        }

        @Test
        @DisplayName("Deve retornar HTTP 404 Not Found quando o ID não for encontrado no Service")
        void buscarPorId_ComIdInexistente_DeveRetornarStatus404() throws Exception {
            // ARRANGE
            Long idInexistente = 99L;
            when(service.buscarPorId(idInexistente))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

            // ACT & ASSERT
            mockMvc.perform(get("/api/v1/categories/{id}", idInexistente)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(service, times(1)).buscarPorId(idInexistente);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categories/{id} - Deletar Categoria")
    class DeletarCategoriaTestes {

        @Test
        @DisplayName("Deve retornar HTTP 204 No Content quando a exclusão for bem-sucedida")
        void deletar_ComIdExistente_DeveRetornarStatus204() throws Exception {
            // ARRANGE
            Long id = 1L;
            doNothing().when(service).deletar(id);

            // ACT & ASSERT
            mockMvc.perform(delete("/api/v1/categories/{id}", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).deletar(id);
        }

        @Test
        @DisplayName("Deve retornar HTTP 404 Not Found ao tentar deletar categoria inexistente")
        void deletar_ComIdInexistente_DeveRetornarStatus404() throws Exception {
            // ARRANGE
            Long idInexistente = 99L;
            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"))
                    .when(service).deletar(idInexistente);

            // ACT & ASSERT
            mockMvc.perform(delete("/api/v1/categories/{id}", idInexistente)
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(service, times(1)).deletar(idInexistente);
        }
    }
}