package com.github.dersolopes.eventmanagement.repository;

import com.github.dersolopes.eventmanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    /* Lê o nome do metodo existsByNameIgnoreCase e faz parsing
      * - existsBy: O Spring entende que você quer uma query de verificação de existência que retorna um boolean
      *   (SELECT CASE WHEN COUNT(...) > 0 ...).
      * - Name: Ele procura o atributo name dentro da entidade Category associada ao repositório.
      * - IgnoreCase: Ele entende que deve aplicar uma função para ignorar maiúsculas/minúsculas
      *   (como LOWER() ou UPPER() no SQL).
     * Gera a Query JPQL/SQL: O Spring traduz esse nome em uma consulta no banco de dados automaticamente:
     * SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(?1)
     * Cria um Proxy Dinâmico: Utilizando reflection e Proxies dinâmicos do Java, o Spring injeta uma classe gerada
     * na memória contendo a implementação pronta do JDBC/EntityManager que executa essa SQL no banco.
    */
    boolean existsByNameIgnoreCase(String name);
}