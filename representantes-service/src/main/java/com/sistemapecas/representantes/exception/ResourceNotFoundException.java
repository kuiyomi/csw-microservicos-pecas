package com.sistemapecas.representantes.exception;

/**
 * Exceção lançada quando um recurso (Representante) solicitado não é encontrado no banco de dados.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
