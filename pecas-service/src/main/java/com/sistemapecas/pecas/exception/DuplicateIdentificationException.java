package com.sistemapecas.pecas.exception;

/**
 * Exceção lançada quando há uma tentativa de cadastrar uma Peça com um
 * número de identificação que já existe no sistema (violação de unicidade).
 */
public class DuplicateIdentificationException extends RuntimeException {

    public DuplicateIdentificationException(String message) {
        super(message);
    }
}
