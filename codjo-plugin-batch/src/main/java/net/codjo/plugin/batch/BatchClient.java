/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.batch;
import net.codjo.plugin.common.CommandLineArguments;
import java.io.IOException;
/**
 * @deprecated use BatchCore
 */
@Deprecated
public class BatchClient extends BatchCore {
    public BatchClient(CommandLineArguments arguments) throws IOException {
        super(arguments);
    }
}
