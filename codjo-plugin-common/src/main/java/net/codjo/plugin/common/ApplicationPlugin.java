/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
/**
 * Interface définissant le cycle de vie d'un Plugin applicatif (Import, Mad...).
 */
public interface ApplicationPlugin {
    void initContainer(ContainerConfiguration containerConfiguration) throws Exception;


    void start(AgentContainer agentContainer) throws Exception;


    void stop() throws Exception;
}
