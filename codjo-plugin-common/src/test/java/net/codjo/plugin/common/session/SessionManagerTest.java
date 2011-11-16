package net.codjo.plugin.common.session;
import net.codjo.agent.UserId;
import net.codjo.test.common.LogString;
import java.util.List;
import junit.framework.TestCase;
/**
 *
 */
public class SessionManagerTest extends TestCase {
    private UserId userId = UserId.createId("jones", "secret");
    private LogString log = new LogString();
    private SessionManager sessionManager = new SessionManager();
    private SessionListenerMock first = new SessionListenerMock(new LogString("first", log));
    private SessionListenerMock second = new SessionListenerMock(new LogString("second", log));


    public void test_oneListener() throws Exception {
        sessionManager.addListener(new SessionListenerMock(log));

        sessionManager.startSession(userId);
        log.assertContent("handleSessionStart(jones)");

        sessionManager.stopSession(userId);
        log.assertContent("handleSessionStart(jones), handleSessionStop(jones)");
    }


    public void test_twoListeners() throws Exception {
        sessionManager.addListener(first);
        sessionManager.addListener(second);

        sessionManager.startSession(userId);
        log.assertContent("first.handleSessionStart(jones), second.handleSessionStart(jones)");
        log.clear();

        sessionManager.stopSession(userId);
        log.assertContent("second.handleSessionStop(jones), first.handleSessionStop(jones)");
    }


    public void test_refusedLogin() throws Exception {
        sessionManager.addListener(first);
        first.mockHandleSessionStartRefused(new SessionRefusedException("j'ai pas envie"));
        try {
            sessionManager.startSession(userId);
            fail();
        }
        catch (SessionRefusedException ex) {
            assertEquals("j'ai pas envie", ex.getLocalizedMessage());
        }

        log.assertContent("");
    }


    public void test_refusedLogin_twoListeners() throws Exception {
        sessionManager.addListener(first);
        sessionManager.addListener(second);

        second.mockHandleSessionStartRefused(new SessionRefusedException("j'ai pas envie"));

        try {
            sessionManager.startSession(userId);
            fail();
        }
        catch (SessionRefusedException ex) {
            assertEquals("j'ai pas envie", ex.getLocalizedMessage());
        }

        log.assertContent("first.handleSessionStart(jones), first.handleSessionStop(jones)");
    }


    public void test_removeListener() throws Exception {
        sessionManager.addListener(first);
        sessionManager.addListener(second);

        assertTrue(sessionManager.removeListener(second));

        sessionManager.startSession(userId);
        log.assertContent("first.handleSessionStart(jones)");

        assertFalse(sessionManager.removeListener(second));
    }


    public void test_getListeners() throws Exception {
        sessionManager.addListener(first);

        List<SessionListener> listenerList = sessionManager.getListenersBeforeStart();

        assertEquals(1, listenerList.size());
        assertSame(first, listenerList.get(0));
    }


    public void test_getListeners_unmodifiableList() throws Exception {
        try {
            sessionManager.getListenersBeforeStart().clear();
            fail();
        }
        catch (UnsupportedOperationException ex) {
            ; // Ok
        }
    }


    public void test_runtimeException() throws Exception {
        sessionManager.addListener(first);
        sessionManager.addListener(second);

        first.mockError(new NullPointerException("j'ai fait un bug"));

        sessionManager.startSession(userId);
        log.assertContent("second.handleSessionStart(jones)");
        log.clear();

        sessionManager.stopSession(userId);
        log.assertContent("second.handleSessionStop(jones)");
    }


    public void test_listenersRegisteredAfterStartAreRemovedAfterStop() throws Exception {
        sessionManager.addListener(first);
        sessionManager.start();
        sessionManager.addListener(second);

        sessionManager.startSession(userId);
        log.assertContent("first.handleSessionStart(jones), second.handleSessionStart(jones)");
        log.clear();

        sessionManager.stop();

        sessionManager.stopSession(userId);
        log.assertContent("first.handleSessionStop(jones)");
        log.clear();
    }
}
