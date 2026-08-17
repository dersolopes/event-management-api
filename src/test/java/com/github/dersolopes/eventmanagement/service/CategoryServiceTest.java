package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.dto.CategoryRequestDTO;
import com.github.dersolopes.eventmanagement.entity.Category;
import com.github.dersolopes.eventmanagement.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    @Nested
    @DisplayName("Testes do método criar()")
    class CriarTestes {

        @Test
        @DisplayName("Deve criar e retornar Category quando os dados forem válidos")
        void criar_DeveSalvarECategorizar_QuandoDadosValidos() {
            // ARRANGE
            CategoryRequestDTO dto = new CategoryRequestDTO("Tecnologia");
            Category categorySalva = new Category();
            categorySalva.setId(1L);
            categorySalva.setName("Tecnologia");

            when(repository.existsByNameIgnoreCase("Tecnologia")).thenReturn(false);
            when(repository.save(any(Category.class))).thenReturn(categorySalva);

            // ACT
            Category response = service.criar(dto);

            // ASSERT
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Tecnologia", response.getName());

            verify(repository, times(1)).existsByNameIgnoreCase("Tecnologia");
            verify(repository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Deve lançar ResponseStatusException (BAD_REQUEST) quando tentar cadastrar categoria com nome duplicado")
        void criar_DeveLancarExcecao_QuandoNomeJaExistir() {
            // ARRANGE
            CategoryRequestDTO dto = new CategoryRequestDTO("Tecnologia");
            when(repository.existsByNameIgnoreCase("Tecnologia")).thenReturn(true);

            // ACT & ASSERT
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.criar(dto)
            );

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertTrue(exception.getReason().contains("Já existe uma categoria com este nome"));

            verify(repository, times(1)).existsByNameIgnoreCase("Tecnologia");
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes do método buscarPorId()")
    class BuscarPorIdTestes {

        @Test
        @DisplayName("Deve retornar Category quando o ID existir")
        void buscarPorId_DeveRetornarCategoria_QuandoIdExistir() {
            // ARRANGE
            Long id = 1L;
            Category category = new Category();
            category.setId(id);
            category.setName("Tecnologia");

            when(repository.findById(id)).thenReturn(Optional.of(category));

            // ACT
            Category response = service.buscarPorId(id);

            // ASSERT
            assertNotNull(response);
            assertEquals(id, response.getId());
            assertEquals("Tecnologia", response.getName());
            verify(repository, times(1)).findById(id);
        }

        @Test
        @DisplayName("Deve lançar ResponseStatusException (NOT_FOUND) quando o ID não existir")
        void buscarPorId_DeveLancarExcecao_QuandoIdNaoExistir() {
            // ARRANGE
            Long idInexistente = 99L;
            when(repository.findById(idInexistente)).thenReturn(Optional.empty());

            // ACT & ASSERT
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.buscarPorId(idInexistente)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(repository, times(1)).findById(idInexistente);
        }
    }

    @Nested
    @DisplayName("Testes do método listarTodas()")
    class ListarTodasTestes {

        @Test
        @DisplayName("Deve retornar uma lista com todas as categorias")
        void listarTodas_DeveRetornarListaDeCategorias() {
            // ARRANGE
            Category c1 = new Category();
            c1.setId(1L);
            c1.setName("Tecnologia");

            Category c2 = new Category();
            c2.setId(2L);
            c2.setName("Música");

            List<Category> listaCategorias = List.of(c1, c2);

            when(repository.findAll()).thenReturn(listaCategorias);

            // ACT
            List<Category> response = service.listarTodas();

            // ASSERT
            assertNotNull(response);
            assertEquals(2, response.size());
            assertEquals("Tecnologia", response.get(0).getName());
            assertEquals("Música", response.get(1).getName());

            verify(repository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Testes do método deletar()")
    class DeletarTestes {

        @Test
        @DisplayName("Deve deletar categoria com sucesso quando o ID existir")
        void deletar_DeveRemoverCategoria_QuandoIdExistir() {
            // ARRANGE
            Long id = 1L;
            when(repository.existsById(id)).thenReturn(true);
            doNothing().when(repository).deleteById(id);

            // ACT & ASSERT
            assertDoesNotThrow(() -> service.deletar(id));

            verify(repository, times(1)).existsById(id);
            verify(repository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Deve lançar ResponseStatusException (NOT_FOUND) ao tentar deletar categoria inexistente")
        void deletar_DeveLancarExcecao_QuandoIdNaoExistir() {
            // ARRANGE
            Long idInexistente = 99L;
            when(repository.existsById(idInexistente)).thenReturn(false);

            // ACT & ASSERT
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.deletar(idInexistente)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(repository, times(1)).existsById(idInexistente);
            verify(repository, never()).deleteById(any());
        }
    }
}