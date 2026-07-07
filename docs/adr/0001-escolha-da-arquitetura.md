# ADR 0001: Escolha da Arquitetura do Sistema

## Status
Aprovado

## Contexto
Precisávamos definir a estrutura de pacotes e o padrão arquitetural para a Event Manager API, considerando o escopo de gerenciamento de eventos, usuários e inscrições, além dos requisitos rígidos do desafio técnico.

## Decisão
Optamos por seguir a **Arquitetura em Camadas Tradicional** (Controller, Service, Repository, Entity), empacotada por componentes técnicos.

## Consequências
* **Prós:** Alinhamento estrito com os requisitos do avaliador, curva de aprendizado baixa, separação clara de responsabilidades e facilidade para leitura rápida do código.
* **Contras:** Acoplamento ligeiramente maior com o framework (Spring) se comparado à Arquitetura Hexagonal, o que é aceitável dado o escopo delimitado do projeto.
