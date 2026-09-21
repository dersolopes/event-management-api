package com.github.dersolopes.eventmanagement.mapper;

import com.github.dersolopes.eventmanagement.dto.EventRequestDTO;
import com.github.dersolopes.eventmanagement.dto.EventResponseDTO;
import com.github.dersolopes.eventmanagement.entity.Event;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {


    // Ignora os relacionamentos complexos no mapeamento simples do DTO, trataremos no Service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "organizers", ignore = true)
    Event toEntity(EventRequestDTO dto);

    // Mapeia o atributo aninhado category.name direto para a String categoryName do DTO
    @Mapping(target = "categoryName", source = "category.name")
    EventResponseDTO toResponseDTO(Event event);

    // Atualiza a entidade existente com os dados do DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "organizers", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(EventRequestDTO dto, @MappingTarget Event entity);
}