package net.codjo.plugin.server;
import org.apache.log4j.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import java.io.IOException;


public class AdministrationWebShutdowner {
    private static final Logger LOG = Logger.getLogger(AdministrationWebShutdowner.class);
    public static final String USAGE_MESSAGE = "Usage : AdministrationWebShutdowner [webServer] [webPort] [applicationName]";


    private AdministrationWebShutdowner() {
    }


    public static void main(String[] args) {
        if (!areArgumentsOk(args)) {
            throw new IllegalArgumentException(USAGE_MESSAGE);
        }
        HttpClient client = new HttpClient();
        String webHost = args[0];
        int webPort = Integer.parseInt(args[1]);
        String applicationName = args[2];
        
        String url = "http://" + webHost + ":" + webPort + "/" + applicationName + "/stop";
        HttpMethod method = new GetMethod(url);
        try {
            client.executeMethod(method);
        }
        catch (IOException e) {
            LOG.info("Impossible d'arreter le serveur web a l'adresse '" + url
                          + "' - il est peut-etre deja arrete", e);
        }
        finally {
            method.releaseConnection();
        }
    }


    private static boolean areArgumentsOk(String[] args) {
        if (args == null || args.length != 3) {
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
}
