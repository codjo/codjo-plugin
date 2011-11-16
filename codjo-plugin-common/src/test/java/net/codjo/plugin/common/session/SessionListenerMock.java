package net.codjo.plugin.common.session;
import net.codjo.agent.UserId;
import net.codjo.test.common.LogString;
/**
 * Mock de {@link net.codjo.plugin.common.session.SessionListener}.
 */
public class SessionListenerMock implements SessionListener {
    private LogString log;
    private SessionRefusedException startRefusedException;
    private RuntimeException handleError;


    public SessionListenerMock(LogString log) {
        this.log = log;
    }


    public void handleSessionStart(UserId userId) throws SessionRefusedException {
        if (startRefusedException != null) {
            throw startRefusedException;
        }
        if (handleError != null) {
            throw handleError;
        }
        log.call("handleSessionStart", userId.getLogin());
    }


    public void mockHandleSessionStartRefused(SessionRefusedException error) {
        this.startRefusedException = error;
    }


    public void mockError(RuntimeException error) {
        this.handleError = error;
    }


    public void handleSessionStop(UserId userId) {
        if (handleError != null) {
            throw handleError;
        }
        log.call("handleSessionStop", userId.getLogin());
    }
}
