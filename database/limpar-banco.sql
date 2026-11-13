-- =============================================================
-- GUARDIÃO - apaga as tabelas do projeto
-- Use só se precisar rodar o guardiao.sql de novo.
-- (Na primeira vez as tabelas não existem e o Oracle avisa ORA-00942: pode ignorar.)
-- =============================================================
DROP TABLE T_SOLICITACAO CASCADE CONSTRAINTS;
DROP TABLE T_VISITA CASCADE CONSTRAINTS;
DROP TABLE T_PRESTADOR CASCADE CONSTRAINTS;
DROP TABLE T_EMPRESA CASCADE CONSTRAINTS;
DROP TABLE T_VINCULO CASCADE CONSTRAINTS;
DROP TABLE T_GUARDIAO CASCADE CONSTRAINTS;
DROP TABLE T_IDOSO CASCADE CONSTRAINTS;
