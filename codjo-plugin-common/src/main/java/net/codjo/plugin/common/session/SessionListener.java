package net.codjo.plugin.common.session;
import net.codjo.agent.UserId;
/**
 *
 */
public interface SessionListener {
    void handleSessionStart(UserId userId) throws SessionRefusedException;


    void handleSessionStop(UserId userId);
}
