package com.github.dersolopes.eventmanagement.repository;

import com.github.dersolopes.eventmanagement.entity.Event;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class EventSpecification {

    public static Specification<Event> byCity(String city) {
        if (city == null || city.isBlank()) {
            return Specification.unrestricted(); // Se for nulo, não aplica filtro nenhum
        }
        // Faz um JOIN implicito com a tabela de endereços e busca por cidade
        // ignorando maiúsculas/minúsculas
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("address").get("city")),
                "%" + city.toLowerCase() + "%"
        );
    }

    public static Specification<Event> byCategory(Long categoryId) {
        if (categoryId == null) {
            return Specification.unrestricted();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                root.get("category").get("id"),
                categoryId
        );
    }

    public static Specification<Event> byStartDateFrom(LocalDateTime startDate) {
        if (startDate == null) {
            return Specification.unrestricted();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                root.get("startDate"),
                startDate
        );
    }
}
