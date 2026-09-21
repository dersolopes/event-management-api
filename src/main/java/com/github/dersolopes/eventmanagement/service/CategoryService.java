package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.dto.CategoryRequestDTO;
import com.github.dersolopes.eventmanagement.entity.Category;
import com.github.dersolopes.eventmanagement.exception.BusinessException;
import com.github.dersolopes.eventmanagement.exception.ResourceNotFoundException;
import com.github.dersolopes.eventmanagement.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public Category criar(CategoryRequestDTO dto) {
        log.info("Iniciando criação de categoria com nome: '{}'", dto.name());

        if (repository.existsByNameIgnoreCase(dto.name())) {
            log.warn("Tentativa de criação de categoria duplicada com nome: '{}'", dto.name());
            throw new BusinessException("Já existe uma categoria cadastrada com o nome '" + dto.name() + "'");
        }

        Category category = new Category();
        category.setName(dto.name());

        return repository.save(category);
    }

    public Category buscarPorId(Long id) {
        log.info("Buscando categoria por ID: {}", id);

        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Categoria não encontrada para o ID: {}", id);
                    return new ResourceNotFoundException("Categoria não encontrada com o ID: " + id);
                });
    }

    public List<Category> listarTodas() {
        return repository.findAll();
    }

    public Category atualizar(Long id, CategoryRequestDTO dto) {
        log.info("Iniciando atualização da categoria ID: {} com novo nome: '{}'", id, dto.name());

        // 1. Busca a categoria ou lança 404 se não existir
        Category category = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao atualizar: Categoria ID {} não encontrada", id);
                    return new ResourceNotFoundException("Categoria não encontrada com o ID: " + id);
                });

        // 2. Se o nome mudou, verifica se já existe outra categoria com o mesmo nome
        if (!category.getName().equalsIgnoreCase(dto.name())
                && repository.existsByNameIgnoreCase(dto.name())) {
            log.warn("Tentativa de atualizar categoria ID {} para um nome já existente: '{}'", id, dto.name());
            throw new BusinessException("Já existe uma categoria cadastrada com o nome '" + dto.name() + "'");
        }

        // 3. Atualiza os dados e salva
        category.setName(dto.name());
        return repository.save(category);
    }

    public void deletar(Long id) {
        log.info("Solicitada exclusão da categoria ID: {}", id);

        if (!repository.existsById(id)) {
            log.warn("Falha ao deletar: Categoria ID: {} não encontrada", id);
            throw new ResourceNotFoundException("Categoria não encontrada com o ID: " + id);
        }

        repository.deleteById(id);
    }
}