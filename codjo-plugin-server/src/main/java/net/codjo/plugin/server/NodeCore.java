package net.codjo.plugin.server;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.plugin.common.ApplicationCore;
/**
 *
 */
public class NodeCore extends ApplicationCore {
    @Override
    protected AgentContainer createAgentContainer(ContainerConfiguration configuration) {
        return AgentContainer.createContainer(configuration);
    }
}
