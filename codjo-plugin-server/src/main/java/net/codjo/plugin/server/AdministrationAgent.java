package net.codjo.plugin.server;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.protocol.AbstractRequestParticipantHandler;
import net.codjo.agent.protocol.FailureException;
import net.codjo.agent.protocol.RequestParticipant;
import net.codjo.agent.protocol.RequestProtocol;
import org.apache.log4j.Logger;
/**
 *
 */
class AdministrationAgent extends Agent {
    private static final Logger LOG = Logger.getLogger(AdministrationAgent.class);
    public static final String SHUTDOWN_REQUEST = "shutdown";
    private final ServerCore serverCore;
    private final int delayBeforeShutdown;


    AdministrationAgent(ServerCore serverCore, int delayBeforeShutdown) {
        this.serverCore = serverCore;
        this.delayBeforeShutdown = delayBeforeShutdown;
    }


    @Override
    protected void setup() {
        addBehaviour(new RequestParticipant(this, new ShutdownRequestHandler(), matchShutdownRequest()));
    }


    @Override
    protected void tearDown() {
    }


    public static MessageTemplate matchShutdownRequest() {
        MessageTemplate matchShutdown = MessageTemplate.matchWith(
              new MessageTemplate.MatchExpression() {
                  public boolean match(AclMessage aclMessage) {
                      return SHUTDOWN_REQUEST.equals(aclMessage.getContentObject());
                  }
              });

        MessageTemplate matchAdministrationMessage =
              MessageTemplate.and(MessageTemplate.matchProtocol(RequestProtocol.REQUEST),
                                  MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST));

        return MessageTemplate.and(matchAdministrationMessage, matchShutdown);
    }


    private class ShutdownRequestHandler extends AbstractRequestParticipantHandler {
        public AclMessage executeRequest(AclMessage request, AclMessage agreement) throws FailureException {
            new Thread(new WaitAndShutdown()).start();
            return new AclMessage(AclMessage.Performative.INFORM);
        }
    }
    private class WaitAndShutdown implements Runnable {
        public void run() {
            try {
                Thread.sleep(delayBeforeShutdown);
            }
            catch (InterruptedException e) {
                LOG.error("Interruption !!!", e);
            }
            doShutdown();
        }


        private void doShutdown() {
            try {
                serverCore.stop();
            }
            catch (Exception e) {
                LOG.warn("Impossible de stopper le serveur proprement", e);
            }
            try {
                System.exit(0);
            }
            catch (Exception e) {
                LOG.warn("Impossible de terminer le processus (JVM)", e);
            }
        }
    }
}
