package net.codjo.plugin.batch;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.common.ApplicationPlugin;
import net.codjo.plugin.common.CommandLineArguments;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.NDC;
/**
 */
public class BatchCore extends ApplicationCore {
    public static final String BATCH_TYPE = "type";
    public static final String BATCH_ARGUMENT = "argument";
    public static final String BATCH_DATE = "date";
    public static final String BATCH_INITIATOR = "initiator";
    public static final int EXIT_WITH_JOB_ERROR = 5;
    public static final int EXIT_DURING_START = 50;
    public static final int EXIT_WITH_TIMEOUT = 100;
    public static final int EXIT_WITH_INTERNAL_ERROR = 200;
    private Map<String, BatchPlugin> batchPlugins = new HashMap<String, BatchPlugin>();
    private CommandLineArguments commandArguments;


    BatchCore() {
    }


    public BatchCore(CommandLineArguments arguments) throws IOException {
        this.commandArguments = arguments;
        checkAndLoadArgumentsFromConfiguration(arguments);
        NDC.push("[" + arguments.getArgument(BatchCore.BATCH_TYPE) + "]"
                 + arguments.getArgument(BatchCore.BATCH_ARGUMENT));
    }


    @Override
    public void addPlugin(ApplicationPlugin plugin) {
        if (plugin instanceof BatchPlugin) {
            addPlugin((BatchPlugin)plugin);
        }
        else {
            super.addPlugin(plugin);
        }
    }


    public void addPlugin(BatchPlugin plugin) {
        assertPluginIsNotDefined(plugin.getType());
        batchPlugins.put(plugin.getType(), plugin);
        super.addPlugin(plugin);
    }


    protected void execute(UserId id, CommandLineArguments arguments)
          throws ContainerFailureException, BatchException {
        BatchPlugin plugin = getPlugin(arguments);
        plugin.execute(id, arguments);
    }


    public void executeAndExit() {
        executeAndExit(getGlobalComponent(UserId.class), commandArguments);
    }


    public void executeAndExit(UserId userId) {
        executeAndExit(userId, commandArguments);
    }


    void executeAndExit(UserId userId, CommandLineArguments arguments) {
        try {
            try {
                execute(userId, arguments);
            }
            finally {
                stop();
            }
        }
        catch (TimeoutBatchException exception) {
            getLogger().info("Arguments = " + arguments);
            getLogger().error("Temps d'attente depassé.", exception);
            exitProcess(EXIT_WITH_TIMEOUT);
        }
        catch (BatchException exception) {
            getLogger().info("Arguments = " + arguments);
            getLogger().error("Erreur pendant la répartition ou le traitement de la tâche.",
                              exception);
            exitProcess(EXIT_WITH_JOB_ERROR);
        }
        catch (Throwable error) {
            getLogger().info("Arguments = " + arguments);
            getLogger().error("Erreur interne", error);
            exitProcess(EXIT_WITH_INTERNAL_ERROR);
        }
        getLogger().info("Fin du traitement !");
        exitProcess(0);
    }


    protected void exitProcess(int code) {
        System.exit(code);
    }


    void checkAndLoadArgumentsFromConfiguration(CommandLineArguments arguments) throws IOException {
        ensureCompatibility(arguments);
        arguments.assertFileArgument(CONFIGURATION);
        arguments.assertArgumentExists(BATCH_TYPE);
        arguments.assertArgumentExists(BATCH_INITIATOR);
    }


    @Override
    protected AgentContainer createAgentContainer(ContainerConfiguration containerConfiguration) {
        return AgentContainer.createContainer(containerConfiguration);
    }


    private BatchPlugin getPlugin(CommandLineArguments arguments) {
        assertPluginIsDefined(arguments.getArgument(BATCH_TYPE));
        return batchPlugins.get(arguments.getArgument(BATCH_TYPE));
    }


    private void assertPluginIsNotDefined(String type) {
        if (batchPlugins.containsKey(type)) {
            throw new IllegalArgumentException("Le plugin de type '" + type + "' est déjà défini");
        }
    }


    private void assertPluginIsDefined(String type) {
        if (batchPlugins.get(type) == null) {
            throw new IllegalArgumentException("Le plugin de type '" + type + "' est inconnu");
        }
    }


    private void ensureCompatibility(CommandLineArguments arguments) {
        copyIfNotDefined(arguments, "configFile", BatchCore.CONFIGURATION);
        copyIfNotDefined(arguments, "batchType", BatchCore.BATCH_TYPE);
        copyIfNotDefined(arguments, "initiator", BatchCore.BATCH_INITIATOR);
        copyIfNotDefined(arguments, "arg", BatchCore.BATCH_ARGUMENT);
    }


    private void copyIfNotDefined(CommandLineArguments arguments, String old, String next) {
        if (!arguments.contains(next) && arguments.contains(old)) {
            arguments.setArgument(next, arguments.getArgument(old));
        }
    }


    public void start() throws Exception {
        try {
            super.start(commandArguments);
        }
        catch (Exception e) {
            exitProcess(EXIT_DURING_START);
        }
    }


    /**
     * Use #start() instead.
     */
    @Override
    public void start(CommandLineArguments arguments) throws Exception {
        try {
            super.start(arguments);
        }
        catch (Exception e) {
            exitProcess(EXIT_DURING_START);
        }
    }
}
