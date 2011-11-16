package net.codjo.plugin.common.session;
/**
 *
 */
public class SessionRefusedException extends Exception {

    public SessionRefusedException(String message) {
        super(message);
    }


    public SessionRefusedException(String message, Throwable cause) {
        super(message, cause);
    }
}
