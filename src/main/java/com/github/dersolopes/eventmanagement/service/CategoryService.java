package com.github.dersolopes.eventmanagement.service;

import com.github.dersolopes.eventmanagement.dto.CategoryRequestDTO;
import com.github.dersolopes.eventmanagement.entity.Category;
import com.github.dersolopes.eventmanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public Category criar(CategoryRequestDTO dto) {
        if (repository.existsByNameIgnoreCase(dto.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe uma categoria com este nome");
        }

        Category category = new Category();
        category.setName(dto.name());

        return repository.save(category);
    }

    public List<Category> listarTodas() {
        return repository.findAll();
    }

    public Category buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    public Category atualizar(Long id, CategoryRequestDTO dto) {
        Category category = buscarPorId(id);

        category.setName(dto.name());

        return repository.save(category);
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada para excluir");
        }
        repository.deleteById(id);
    }
}