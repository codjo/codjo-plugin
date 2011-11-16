/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.test.common.LogString;
/**
 * Classe Mock de {@link net.codjo.plugin.common.ApplicationPlugin}.
 */
public class ApplicationPluginMock implements ApplicationPlugin {
    protected final LogString log;
    private Exception initFailure;
    private Exception startFailure;
    private Exception stopFailure;


    public ApplicationPluginMock() {
        this(new LogString());
    }


    public ApplicationPluginMock(LogString log) {
        this.log = log;
    }


    public void initContainer(ContainerConfiguration containerConfiguration)
          throws Exception {
        log.call("initContainer",
                 "containerConfiguration(" + containerConfiguration.getContainerName() + ")");
        if (initFailure != null) {
            throw initFailure;
        }
    }


    public void start(AgentContainer agentContainer)
          throws Exception {
        log.call("start", "agentContainer(" + agentContainer.getContainerName() + ")");
        if (startFailure != null) {
            throw startFailure;
        }
    }


    public void stop() throws Exception {
        log.call("stop");
        if (stopFailure != null) {
            stopFailure.fillInStackTrace();
            throw stopFailure;
        }
    }


    public void mockInitFailure(Exception exception) {
        initFailure = exception;
    }


    public void mockStartFailure(Exception exception) {
        startFailure = exception;
    }


    public void mockStopFailure(Exception exception) {
        stopFailure = exception;
    }
}
