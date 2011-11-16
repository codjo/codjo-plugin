package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
/**
 *
 */
public class DefaultPluginsLifecycle extends PluginsLifecycle {
    public DefaultPluginsLifecycle(Logger logger) {
        super(logger);
    }


    @Override
    public void start(List<? extends ApplicationPlugin> list,
                      CoreWrapper coreWrapper,
                      List<? extends LifecycleListener> listenerList) throws Exception {
        PluginsDispatcher plugins = wrapp(list);
        ListenersDispatcher listeners = wrapp(listenerList);

        logger().info("##### Cycle de demarrage");

        logger().info("**** Initialisation");
        listeners.beforeInitContainer(coreWrapper.getCommandLineArguments());
        plugins.initContainer(coreWrapper.getContainerConfiguration());

        logger().info("**** Demarrage");
        listeners.beforeStart(coreWrapper.getContainerConfiguration());
        plugins.start(coreWrapper.getAgentContainer());

        startExtensionPoint(plugins, coreWrapper, listeners);

        logger().info("##### Fin du cycle de demarrage");
    }


    protected void startExtensionPoint(PluginsDispatcher plugins,
                                       CoreWrapper coreWrapper,
                                       ListenersDispatcher listeners) throws Exception {
    }


    @Override
    public void stop(List<? extends ApplicationPlugin> list,
                     List<? extends LifecycleListener> listenerList) throws Exception {
        PluginsDispatcher plugins = wrapp(list);
        ListenersDispatcher listeners = wrapp(listenerList);

        logger().info("##### Cycle d'arret");

        logger().info("***** Arret");
        listeners.beforeStop();
        try {
            plugins.stop();
        }
        finally {
            listeners.afterStop();
        }

        stopExtensionPoint(plugins, listeners);

        logger().info("##### Fin du cycle d'arret");
    }


    protected void stopExtensionPoint(PluginsDispatcher plugins, ListenersDispatcher listeners) {
    }


    private ListenersDispatcher wrapp(List<? extends LifecycleListener> list) {
        return new ListenersDispatcher(list);
    }


    private PluginsDispatcher wrapp(List<? extends ApplicationPlugin> list) {
        return new PluginsDispatcher(list);
    }


    protected class PluginsDispatcher implements ApplicationPlugin {
        private List<? extends ApplicationPlugin> plugins;


        private PluginsDispatcher(List<? extends ApplicationPlugin> plugins) {
            this.plugins = plugins;
        }


        public void initContainer(ContainerConfiguration containerConfiguration) throws Exception {
            for (ApplicationPlugin plugin : plugins) {
                logger().info("Plugin.initContainer " + plugin);
                plugin.initContainer(containerConfiguration);
            }
        }


        public void start(AgentContainer agentContainer) throws Exception {
            for (ApplicationPlugin plugin : plugins) {
                logger().info("Plugin.start " + plugin);
                try {
                    plugin.start(agentContainer);
                }
                catch (Exception e) {
                    logger().error("Plugin.start en échec : Plugin " + plugin, e);
                    throw e;
                }
            }
        }


        public void stop() throws Exception {
            Exception exception = null;
            List<ApplicationPlugin> reversedPluginList = new ArrayList<ApplicationPlugin>(plugins);
            Collections.reverse(reversedPluginList);
            for (ApplicationPlugin plugin : reversedPluginList) {
                logger().info("Plugin.stop " + plugin);
                try {
                    plugin.stop();
                }
                catch (Exception e) {
                    logger().error("Stop en echec : Plugin " + plugin, e);
                    exception = e;
                }
            }
            if (exception != null) {
                throw exception;
            }
        }


        public List<? extends ApplicationPlugin> getPlugins() {
            return plugins;
        }
    }
    protected static class ListenersDispatcher extends LifecycleListener {
        private List<? extends LifecycleListener> list;


        ListenersDispatcher(List<? extends LifecycleListener> list) {
            this.list = list;
        }


        @Override
        public void beforeInitContainer(CommandLineArguments arguments) throws Exception {
            for (LifecycleListener listener : list) {
                listener.beforeInitContainer(arguments);
            }
        }


        @Override
        public void beforeStart(ContainerConfiguration configuration) throws Exception {
            for (LifecycleListener listener : list) {
                listener.beforeStart(configuration);
            }
        }


        @Override
        public void beforeStop() throws Exception {
            for (LifecycleListener listener : list) {
                listener.beforeStop();
            }
        }


        @Override
        public void afterStop() throws Exception {
            for (LifecycleListener listener : list) {
                listener.afterStop();
            }
        }


        @Override
        public void beforeInitGui() throws Exception {
            for (LifecycleListener listener : list) {
                listener.beforeInitGui();
            }
        }


        @Override
        public void afterInitGui() throws Exception {
            for (LifecycleListener listener : list) {
                listener.afterInitGui();
            }
        }
    }
}
