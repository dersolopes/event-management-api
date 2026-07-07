package com.github.dersolopes.eventmanagement.mapper;


import com.github.dersolopes.eventmanagement.dto.EventRequestDTO;
import com.github.dersolopes.eventmanagement.dto.EventResponseDTO;
import com.github.dersolopes.eventmanagement.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    // Ignora os relacionamentos complexos no mapeamento simples do DTO, trataremos no Service
    @Mapping(target = "id", ignore = true)          //O DTO que vem da internet para criar um evento não possui um ID (quem gera o UUID é o nosso banco de dados). Ignoramos para o MapStruct saber que ele não deve tentar buscar um ID no DTO.
    @Mapping(target = "status", ignore = true)      //O usuário não pode definir se o evento nasce ativo ou cancelado enviando um texto no JSON. Quem define que o status inicial é EventStatus.ACTIVE é a nossa regra de negócio dentro do Service.
    @Mapping(target = "category", ignore = true)    //O DTO envia apenas o número categoryId (ex: 1). A entidade Event espera um objeto completo da classe Category. Ignoramos no mapeamento automático porque nós buscaremos a categoria real no banco via CategoryRepository dentro do Service antes de salvar.
    @Mapping(target = "organizers", ignore = true)  //Quem define quem é o organizador de um evento não é o payload que vem da internet. É o Spring Security que vai ler o token JWT do usuário logado no momento da requisição, descobrir o ID dele, vincularemos esse usuário ao evento manualmente na service.
    Event toEntity(EventRequestDTO dto);

    // Mapeia o atributo aninhado category.name direto para a String categoryName do DTO
    @Mapping(target = "categoryName", source = "category.name")
    EventResponseDTO toResponseDTO(Event event);

}
