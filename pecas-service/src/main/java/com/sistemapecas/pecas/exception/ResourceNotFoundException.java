package com.sistemapecas.pecas.exception;

/**
 * Exceção lançada quando um recurso (Peça) solicitado não é encontrado no banco de dados.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
