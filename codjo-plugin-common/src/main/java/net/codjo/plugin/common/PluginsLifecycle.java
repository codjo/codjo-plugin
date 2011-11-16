package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import java.util.List;
import org.apache.log4j.Logger;
/**
 *
 */
public abstract class PluginsLifecycle {
    private final Logger logger;


    protected PluginsLifecycle(Logger logger) {
        this.logger = logger;
    }


    public abstract void start(List<? extends ApplicationPlugin> plugins,
                               CoreWrapper coreWrapper,
                               List<? extends LifecycleListener> listenerList) throws Exception;


    public abstract void stop(List<? extends ApplicationPlugin> plugins,
                              List<? extends LifecycleListener> listenerList) throws Exception;


    protected Logger logger() {
        return logger;
    }


    public static interface CoreWrapper {
        CommandLineArguments getCommandLineArguments();


        ContainerConfiguration getContainerConfiguration();


        AgentContainer getAgentContainer();
    }
    public static abstract class LifecycleListener {
        public void beforeInitContainer(CommandLineArguments arguments) throws Exception {
        }


        public void beforeStart(ContainerConfiguration configuration) throws Exception {
        }


        public void beforeInitGui() throws Exception {
        }


        public void afterInitGui() throws Exception {
        }


        public void beforeStop() throws Exception {
        }


        public void afterStop() throws Exception {
        }
    }
}
