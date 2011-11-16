package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.StartFailureException;
import net.codjo.logging.LoggingUtil;
import net.codjo.plugin.common.PluginsLifecycle.CoreWrapper;
import net.codjo.plugin.common.PluginsLifecycle.LifecycleListener;
import net.codjo.plugin.common.session.SessionListener;
import net.codjo.plugin.common.session.SessionManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
/**
 *
 */
public abstract class ApplicationCore {
    public static final String CONFIGURATION = "configuration";
    public static final String LOG_CONFIGURATION = "log4j.configuration";
    private final Logger logger = Logger.getLogger(getClass().getName());
    private ContainerConfiguration containerConfiguration = new ContainerConfiguration();
    private List<ApplicationPlugin> plugins = new ArrayList<ApplicationPlugin>();
    private AgentContainer agentContainer;
    private boolean stopping;
    private MutablePicoContainer picoContainer = new DefaultPicoContainer();
    private SessionManager sessionManager = new SessionManager();
    private MainBehaviour mainBehaviour;
    private PluginsLifecycle lifecycle;
    private List<LifecycleListener> lifecycleListeners = new ArrayList<LifecycleListener>();


    protected ApplicationCore() {
        picoContainer.registerComponentInstance(ApplicationCore.class, this);
        addGlobalComponent(picoContainer);
        addGlobalComponent(containerConfiguration);
        addGlobalComponent(sessionManager);
        initializeLogger(LoggingUtil.getDefaultLoggerConfiguration());

        setPluginsLifecycle(new DefaultPluginsLifecycle(getLogger()));
        addLifecycleListener(new DefaultLifecycleTasks());
    }


    protected abstract AgentContainer createAgentContainer(ContainerConfiguration configuration);


    public void start(final CommandLineArguments arguments) throws Exception {
        try {
            lifecycle.start(plugins, new DefaultCoreWrapper(arguments), lifecycleListeners);
        }
        catch (Exception exception) {
            logger.error("Erreur dans ApplicationCore.start.", exception);
            try {
                stop();
            }
            catch (Exception e) {
                logger.fatal("Erreur durant un 'stop' d'urgence", e);
            }
            throw exception;
        }
    }


    protected void configureContainer() throws Exception {
    }


    protected void startContainer() throws ContainerFailureException {
        agentContainer.start();
    }


    public void stop() throws Exception {
        if (!isStopping() && agentContainer != null) {
            stopping = true;
            lifecycle.stop(plugins, lifecycleListeners);
        }
    }


    public MainBehaviour getMainBehaviour() {
        return mainBehaviour;
    }


    public void setMainBehaviour(MainBehaviour mainBehaviour) {
        this.mainBehaviour = mainBehaviour;
    }


    public void executeMainBehaviour(String... args) throws Exception {
        if (getMainBehaviour() == null) {
            throw new IllegalArgumentException("MainBehaviour is not set !!!");
        }
        getMainBehaviour().execute(args);
    }


    public boolean isStopping() {
        return stopping;
    }


    public void addPlugin(Class<? extends ApplicationPlugin> aPluginClass) {
        picoContainer.registerComponentImplementation(aPluginClass);
        addPlugin((ApplicationPlugin)picoContainer.getComponentInstanceOfType(aPluginClass));
    }


    public void addPlugin(ApplicationPlugin plugin) {
        if (picoContainer.getComponentInstanceOfType(plugin.getClass()) == null) {
            picoContainer.registerComponentInstance(plugin);
        }
        if (plugin instanceof SessionListener) {
            sessionManager.addListener((SessionListener)plugin);
        }
        plugins.add(plugin);
    }


    public ContainerConfiguration getContainerConfig() {
        return containerConfiguration;
    }


    public AgentContainer getAgentContainer() {
        return agentContainer;
    }


    private void loadContainerConfig(CommandLineArguments arguments)
          throws IOException, StartFailureException {
        if (arguments.contains(CONFIGURATION)) {
            String configurationFile = arguments.getArgument(CONFIGURATION);
            logger.info("Utilisation de la configuration " + configurationFile);
            if (!new File(configurationFile).exists()) {
                logger.fatal("Le fichier de configuration est introuvable.");
                throw new StartFailureException(
                      "Fichier de configuration est introuvable : " + configurationFile);
            }
            containerConfiguration.loadConfig(configurationFile);
        }
        else if (arguments.contains("host") && arguments.contains("port")) {
            String host = arguments.getArgument("host");
            String port = arguments.getArgument("port");
            String name = arguments.getArgument("container-name");
            logger.info("Utilisation de la configuration définie dans les arguments " + host + ":" + port);
            containerConfiguration.setHost(host);
            containerConfiguration.setPort(Integer.parseInt(port));
            containerConfiguration.setContainerName(name);

            if (arguments.contains("local-port")) {
                String localPort = arguments.getArgument("local-port");
                containerConfiguration.setLocalPort(Integer.parseInt(localPort));
                logger.info("Utilisation du local-port définie dans les arguments :" + localPort);
            }
        }
    }


    protected Logger getLogger() {
        return logger;
    }


    protected void setPluginsLifecycle(PluginsLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }


    public <T extends ApplicationPlugin> T getPlugin(Class<T> aClass) {
        return getGlobalComponent(aClass);
    }


    public List<ApplicationPlugin> getPlugins() {
        return plugins;
    }


    public void addGlobalComponent(Object object) {
        picoContainer.registerComponentInstance(object);
    }


    public void addGlobalComponentImplementation(Class clazz) {
        picoContainer.registerComponentImplementation(clazz);
    }


    public <T, U extends T> void addGlobalComponent(Class<T> classKey, U object) {
        picoContainer.registerComponentInstance(classKey, object);
    }


    public void removeGlobalComponent(Class aClass) {
        picoContainer.unregisterComponent(aClass);
    }


    public <T> T getGlobalComponent(Class<T> aClass) {
        //noinspection unchecked
        return (T)picoContainer.getComponentInstance(aClass);
    }


    public void initializeLogger(URL configLoggerUrl) {
        initializeLog4JLogger(configLoggerUrl);
        initializeJavaUtilLogger(configLoggerUrl);
    }


    private void initializeLog4JLogger(URL configLoggerUrl) {
        String property = System.getProperty(LOG_CONFIGURATION);
        if (property != null) {
            try {
                PropertyConfigurator.configure(new URL(property));
                logger.info("Utilisation de la configuration surcharge : " + property);
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            if (configLoggerUrl != null) {
                PropertyConfigurator.configure(configLoggerUrl);
                logger.info("Utilisation de la configuration des logs par defaut : "
                            + configLoggerUrl.toExternalForm());
            }
        }
    }


    private void initializeJavaUtilLogger(URL configLoggerUrl) {
        try {
            InputStream logStream;
            if (System.getProperty(LOG_CONFIGURATION) != null) {
                logStream = new URL(System.getProperty(LOG_CONFIGURATION)).openStream();
            }
            else {
                if (configLoggerUrl == null) {
                    return;
                }
                logStream = configLoggerUrl.openStream();
            }
            try {
                LogManager.getLogManager().readConfiguration(logStream);
            }
            finally {
                logStream.close();
            }
        }
        catch (IOException e) {
            logger.warn("Impossible d'initialiser les log java.util", e);
        }
    }


    MutablePicoContainer getPicoContainer() {
        return picoContainer;
    }


    public MutablePicoContainer createChildPicoContainer() {
        return new DefaultPicoContainer(picoContainer);
    }


    public void addLifecycleListener(LifecycleListener lifecycleListener) {
        lifecycleListeners.add(lifecycleListener);
    }


    public static interface MainBehaviour {
        void execute(String... args) throws Exception;
    }

    private class DefaultLifecycleTasks extends LifecycleListener {
        @Override
        public void beforeInitContainer(CommandLineArguments arguments) throws Exception {
            // TODO: faire quelque chose de plus propre !
            // on retire le picoContainer de lui-même sinon, le start boucle et plante
            // Solution : faire notre propre système de Startable sans passer par pico ?
            removeGlobalComponent(DefaultPicoContainer.class);
            picoContainer.start();
            addGlobalComponent(picoContainer);
            JadeWrapper.setPicoContainer(containerConfiguration, createChildPicoContainer());
            containerConfiguration.setLocalPort(AgentContainer.CONTAINER_PORT);
            loadContainerConfig(arguments);
            getContainerConfig().resolveHostDnsName();
            configureContainer();
        }


        @Override
        public void beforeStart(ContainerConfiguration configuration) throws Exception {
            logger.info("Demarrage de la plateforme agent");
            agentContainer = createAgentContainer(containerConfiguration);
            startContainer();
        }


        @Override
        public void beforeStop() throws Exception {
            picoContainer.stop();
            JadeWrapper.removePicoContainer(containerConfiguration);
        }


        @Override
        public void afterStop() throws Exception {
            try {
                agentContainer.stop();
            }
            finally {
                removeGlobalComponent(ContainerConfiguration.class);
                containerConfiguration = new ContainerConfiguration();
                addGlobalComponent(containerConfiguration);
                stopping = false;
                agentContainer = null;
            }
        }
    }
    private class DefaultCoreWrapper implements CoreWrapper {
        private final CommandLineArguments arguments;


        DefaultCoreWrapper(CommandLineArguments arguments) {
            this.arguments = arguments;
        }


        public CommandLineArguments getCommandLineArguments() {
            return arguments;
        }


        public ContainerConfiguration getContainerConfiguration() {
            return containerConfiguration;
        }


        public AgentContainer getAgentContainer() {
            return agentContainer;
        }
    }
}
