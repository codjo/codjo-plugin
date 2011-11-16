/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.batch;
/**
 * Exception lancée lors de l'execution d'un batch.
 *
 * @see BatchClient
 */
public class BatchException extends Exception {
    public BatchException(String message) {
        super(message);
    }


    public BatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
