/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.batch;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.ApplicationPlugin;
import net.codjo.plugin.common.CommandLineArguments;
/**
 *
 */
public interface BatchPlugin extends ApplicationPlugin {
    public String getType();


    public void execute(UserId userId, CommandLineArguments arguments)
          throws ContainerFailureException, BatchException;
}
