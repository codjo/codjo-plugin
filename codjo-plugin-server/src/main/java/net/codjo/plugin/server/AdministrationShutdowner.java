package net.codjo.plugin.server;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.Aid;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.protocol.InitiatorHandler;
import net.codjo.agent.protocol.RequestInitiator;
import net.codjo.agent.protocol.RequestProtocol;
import org.apache.log4j.Logger;
/**
 *
 */
public class AdministrationShutdowner {
    private static final Logger LOG = Logger.getLogger(AdministrationShutdowner.class);
    public static final String USAGE_MESSAGE = "Usage : AdministrationShutdowner [server] [port]";


    private AdministrationShutdowner() {
    }


    public static void main(String[] args) {
        if (!areArgumentsOk(args)) {
            LOG.info(USAGE_MESSAGE);
            throw new IllegalArgumentException(USAGE_MESSAGE);
        }

        LOG.info("Calling AdministrationShutdowner with args " + args[0] + " " + args[1]);

        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setHost(args[0]);
        containerConfiguration.setPort(Integer.parseInt(args[1]));

        try {
            LOG.info("Creating container...");
            AgentContainer standardContainer = AgentContainer.createContainer(containerConfiguration);
            LOG.info("Starting container...");
            standardContainer.start();
            LOG.info("Starting ShutdownServerAgent...");
            standardContainer.acceptNewAgent("stop-server-agent", new ShutdownServerAgent()).start();
            LOG.info("Done");
        }
        catch (ContainerFailureException e) {
            LOG.error("Erreur lors du démarrage du container", e);
            System.exit(0);
        }
        catch (Throwable t) {
            LOG.error("Erreur inattendue !!!", t);
            System.exit(0);
        }
    }


    private static AclMessage createShutdownRequest() {
        AclMessage requestMessage = new AclMessage(AclMessage.Performative.REQUEST, RequestProtocol.REQUEST);
        requestMessage.addReceiver(new Aid(AdministrationServerPlugin.AGENT_NAME));
        requestMessage.setContentObject(AdministrationAgent.SHUTDOWN_REQUEST);
        return requestMessage;
    }


    private static boolean areArgumentsOk(String[] args) {
        if (args == null || args.length != 2) {
            return false;
        }
        try {
            Integer.parseInt(args[1]);
        }
        catch (NumberFormatException exception) {
            return false;
        }
        return true;
    }


    private static class ShutdownServerAgent extends Agent {

        @Override
        protected void setup() {
            addBehaviour(new RequestInitiator(this, new ShutdownInitiator(), createShutdownRequest()));
        }


        @Override
        protected void tearDown() {
        }
    }

    private static class ShutdownInitiator implements InitiatorHandler {

        public void handleAgree(AclMessage agree) {
            // Inutile
        }


        public void handleRefuse(AclMessage refuse) {
            exitWithFailure(refuse);
        }


        public void handleInform(AclMessage inform) {
            LOG.info("Call to System.exit()");
            System.exit(0);
        }


        public void handleFailure(AclMessage failure) {
            exitWithFailure(failure);
        }


        public void handleOutOfSequence(AclMessage outOfSequenceMessage) {
            exitWithFailure(outOfSequenceMessage);
        }


        public void handleNotUnderstood(AclMessage notUnderstoodMessage) {
            exitWithFailure(notUnderstoodMessage);
        }


        private void exitWithFailure(AclMessage message) {
            LOG.error("Received unexpected message: " + message.toFipaACLString());
            System.exit(-1);
        }
    }
}
