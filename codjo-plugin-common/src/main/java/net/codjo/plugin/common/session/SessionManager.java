package net.codjo.plugin.common.session;
import net.codjo.agent.UserId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;
/**
 * Gestion des Session dans un socle.
 *
 * <p> Cette classe est multi-thread safe. </p>
 */
public class SessionManager implements Startable {
    private final static Logger LOGGER = Logger.getLogger(SessionManager.class);
    private List<SessionListener> listenersBeforeStart = new ArrayList<SessionListener>();
    private List<SessionListener> listenersAfterStart = new ArrayList<SessionListener>();
    private final Object lock = new Object();
    private boolean started;


    public void addListener(SessionListener sessionListener) {
        synchronized (lock) {
            if (started) {
                listenersAfterStart.add(sessionListener);
            }
            else {
                listenersBeforeStart.add(sessionListener);
            }
        }
    }


    public boolean removeListener(SessionListener listener) {
        synchronized (lock) {
            boolean removed = listenersBeforeStart.remove(listener);
            return removed || listenersAfterStart.remove(listener);
        }
    }


    public void start() {
        synchronized (lock) {
            started = true;
        }
    }


    public void stop() {
        synchronized (lock) {
            listenersAfterStart.clear();
            started = false;
        }
    }


    public void startSession(UserId userId) throws SessionRefusedException {
        List<SessionListener> copy = createListenerListCopy();

        for (int i = 0; i < copy.size(); i++) {
            SessionListener listener = copy.get(i);
            try {
                listener.handleSessionStart(userId);
            }
            catch (RuntimeException e) {
                logWarn("startSession", listener, e);
            }
            catch (SessionRefusedException e) {
                fireSessionStop(userId, copy.subList(0, i));
                throw e;
            }
        }
    }


    public void stopSession(UserId userId) {
        fireSessionStop(userId, createListenerListCopy());
    }


    List<SessionListener> getListenersBeforeStart() {
        return Collections.unmodifiableList(listenersBeforeStart);
    }


    List<SessionListener> getListenersAfterStart() {
        return Collections.unmodifiableList(listenersAfterStart);
    }


    private static void fireSessionStop(UserId userId, List<SessionListener> copy) {
        Collections.reverse(copy);
        for (SessionListener listener : copy) {
            try {
                listener.handleSessionStop(userId);
            }
            catch (RuntimeException e) {
                logWarn("stopSession", listener, e);
            }
        }
    }


    private List<SessionListener> createListenerListCopy() {
        synchronized (lock) {
            List<SessionListener> copy =
                  new ArrayList<SessionListener>(listenersBeforeStart.size() + listenersAfterStart.size());
            copy.addAll(listenersBeforeStart);
            copy.addAll(listenersAfterStart);
            return copy;
        }
    }


    private static void logWarn(String eventType, SessionListener listener, RuntimeException error) {
        LOGGER.warn("Le listener '" + listener.getClass()
                    + "' a declenché l'erreur '" + error.getLocalizedMessage()
                    + "' sur l'event "
                    + eventType
                    + ". Les autres listeners seront activés.", error);
    }
}
