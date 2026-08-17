package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.dto.CategoryRequestDTO;
import com.github.dersolopes.eventmanagement.entity.Category;
import com.github.dersolopes.eventmanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public Category criar(CategoryRequestDTO dto) {
        log.info("Iniciando criação de categoria com nome: '{}'", dto.name());

        if (repository.existsByNameIgnoreCase(dto.name())) {
            log.warn("Tentativa de criação de categoria duplicada com nome: '{}'", dto.name());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe uma categoria com este nome");
        }

        Category category = new Category();
        category.setName(dto.name());

        Category salva = repository.save(category);
        log.info("Categoria criada com sucesso. ID: '{}', Nome: '{}'", salva.getId(), salva.getName());
        return salva;
    }

    public List<Category> listarTodas() {
        log.info("Listando todas as categorias");
        List<Category> categorias = repository.findAll();
        log.debug("Total de categorias encontradas: {}", categorias.size());
        return categorias;
    }

    public Category buscarPorId(Long id) {
        log.info("Buscando categoria por ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Categoria não encontrada para o ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada");
                });
    }

    public Category atualizar(Long id, CategoryRequestDTO dto) {
        log.info("Iniciando atualização da categoria ID: {} com novo nome: '{}'", id, dto.name());

        Category category = buscarPorId(id);

        if (!category.getName().equalsIgnoreCase(dto.name()) && repository.existsByNameIgnoreCase(dto.name())) {
            log.warn("Falha ao atualizar ID {}: O novo nome '{}' já está em uso por outra categoria", id, dto.name());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe uma categoria com este nome");
        }

        category.setName(dto.name());
        Category atualizada = repository.save(category);
        log.info("Categoria ID: {} atualizada com sucesso para nome: '{}'", atualizada.getId(), atualizada.getName());
        return atualizada;
    }

    public void deletar(Long id) {
        log.info("Solicitada exclusão da categoria ID: {}", id);

        if (!repository.existsById(id)) {
            log.warn("Falha ao deletar: Categoria ID: {} não encontrada", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada para excluir");
        }

        repository.deleteById(id);
        log.info("Categoria ID: {} excluída com sucesso", id);
    }
}