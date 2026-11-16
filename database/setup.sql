-- Script de criação do banco de dados e do usuário utilizado pela aplicação.
-- Execute como um usuário MySQL com privilégios administrativos:
--   mysql -u root -p < database/setup.sql
--
-- O Hibernate cria/atualiza as tabelas automaticamente (ddl-auto=update).

CREATE DATABASE IF NOT EXISTS taskmanager_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'taskuser'@'localhost' IDENTIFIED BY 'taskpass';

GRANT ALL PRIVILEGES ON taskmanager_db.* TO 'taskuser'@'localhost';

FLUSH PRIVILEGES;
