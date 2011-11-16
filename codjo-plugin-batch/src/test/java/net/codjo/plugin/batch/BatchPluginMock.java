/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.batch;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.ApplicationPluginMock;
import net.codjo.plugin.common.CommandLineArguments;
import net.codjo.test.common.LogString;
/**
 *
 */
public class BatchPluginMock extends ApplicationPluginMock implements BatchPlugin {
    private String type;
    private BatchException executeFailure;
    private ContainerFailureException executeContainerFailure;


    public BatchPluginMock(String type, LogString log) {
        super(log);
        this.type = type;
    }


    public BatchPluginMock(String type) {
        this(type, new LogString());
    }


    public String getType() {
        return type;
    }


    public void execute(UserId userId, CommandLineArguments arguments)
          throws ContainerFailureException, BatchException {
        log.call("execute",
                 "userId(" + userId.getLogin() + "/" + userId.getPassword() + ")",
                 "CommandLineArguments(" + arguments.getArgument(BatchCore.BATCH_ARGUMENT)
                 + ")");
        if (executeFailure != null) {
            throw executeFailure;
        }
        if (executeContainerFailure != null) {
            throw executeContainerFailure;
        }
    }


    public void mockExecuteFailure(BatchException executeFailureMock) {
        executeFailure = executeFailureMock;
    }


    public void mockExecuteFailure(ContainerFailureException executeFailureMock) {
        executeContainerFailure = executeFailureMock;
    }
}
