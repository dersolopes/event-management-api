# ADR 0002: Estratégia de Alta Concorrência para Inscrições

## Status
Aprovado (Evoluído após revisão técnica)

## Contexto
O sistema precisa garantir que, em eventos disputados, duas requisições simultâneas não causem *overbooking* (ultrapassar a capacidade máxima).

## Decisão
A estratégia inicial de Lock Pessimista (`PESSIMISTIC_WRITE`) foi descartada após análise de gargalos no pool de conexões (`HikariPool`) e riscos de *Deadlocks*. Optou-se pela **Atualização Atômica Não-Bloqueante com Fluxo Invertido**:
1. Execução de `UPDATE` atômico condicional no Postgres: `WHERE current_count < capacity`.
2. Avaliação de linhas afetadas antes de realizar o `INSERT` na tabela de inscrições.

## Consequências
* **Prós:** Performance massiva sob estresse, consumo mínimo do pool de conexões, eliminação completa de Deadlocks e integridade total dos dados.
