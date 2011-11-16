/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.server;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.common.CommandLineArguments;
/**
 * @see ServerPlugin
 */
public class ServerCore extends ApplicationCore {

    public ServerCore() {
        addPlugin(AdministrationServerPlugin.class);
    }


    @Override
    protected AgentContainer createAgentContainer(ContainerConfiguration containerConfiguration) {
        return AgentContainer.createMainContainer(containerConfiguration);
    }


    public void startAndExitIfFailure(CommandLineArguments commandLineArguments) {
        try {
            start(commandLineArguments);
        }
        catch (Exception e) {
            getLogger().error("Erreur pendant le démarrage du serveur.");
            System.exit(-1);
        }
    }
}
