package net.codjo.plugin.batch;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.AbstractApplicationPlugin;
import net.codjo.plugin.common.CommandLineArguments;
/**
 * @see BatchPlugin
 */
public abstract class AbstractBatchPlugin extends AbstractApplicationPlugin implements BatchPlugin {
    private String type;


    protected AbstractBatchPlugin() {
    }


    protected AbstractBatchPlugin(String type) {
        this.type = type;
    }


    public String getType() {
        return type;
    }


    public void execute(UserId userId, CommandLineArguments arguments)
          throws ContainerFailureException, BatchException {
    }
}
