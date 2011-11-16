/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.batch;
/**
 *
 */
public class TimeoutBatchException extends BatchException {
    public TimeoutBatchException() {
        super("timeout");
    }
}
