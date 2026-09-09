package com.sistemapecas.clientes.exception;

/**
 * Exceção lançada quando um recurso (Cliente) solicitado não é encontrado no banco de dados.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
