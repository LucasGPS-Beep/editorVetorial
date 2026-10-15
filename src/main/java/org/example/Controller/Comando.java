package org.example.Controller;

/** Command: operação do documento executada com registro automático no histórico. */
@FunctionalInterface
public interface Comando {
    void executar();
}
