package com.sistemapecas.representantes.exception;

/**
 * Exceção lançada quando há uma tentativa de cadastrar um Representante com um
 * CPF que já existe no sistema (violação de unicidade).
 */
public class DuplicateCpfException extends RuntimeException {

    public DuplicateCpfException(String message) {
        super(message);
    }
}
