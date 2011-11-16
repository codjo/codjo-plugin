package net.codjo.plugin.server;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

public class AdministrationWebShutdownerTest extends TestCase {
    private static final int PORT = 8082;


    public void test_execute_ok() throws Exception {

        Server jetty = new Server(PORT);
        Context context = new Context(jetty, "/", Context.SESSIONS);

        final boolean[] done = new boolean[1];

        ServletHolder holder = new ServletHolder(new DefaultServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response)
                  throws ServletException, IOException {
                done[0] = true;
            }
        });
        context.addServlet(holder, "/mywebapp/stop");
        jetty.start();

        AdministrationWebShutdowner.main(new String[]{"localhost", String.valueOf(PORT), "mywebapp"});

        for (int i = 0; i < 1000; i++) {
            if (done[0]) {
                break;
            }
            Thread.sleep(10);
        }

        jetty.stop();
        assertTrue(done[0]);
    }

    public void test_execute_wrong_param_nb() throws Exception {
        try{
            AdministrationWebShutdowner.main(new String[]{"localhost", String.valueOf(PORT)});
            fail("Should fail because APP_NAME is not given");
        }
        catch (IllegalArgumentException e){
        }
    }

    public void test_execute_wrong_param() throws Exception {

        Server jetty = new Server(PORT);
        Context context = new Context(jetty, "/", Context.SESSIONS);

        final boolean[] done = new boolean[1];

        ServletHolder holder = new ServletHolder(new DefaultServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response)
                  throws ServletException, IOException {
                done[0] = true;
            }
        });
        context.addServlet(holder, "/mywebapp/stop");
        jetty.start();

        AdministrationWebShutdowner.main(new String[]{"localhost", String.valueOf(PORT), "mywebappwrongname"});

        for (int i = 0; i < 1000; i++) {
            if (done[0]) {
                break;
            }
            Thread.sleep(10);
        }

        jetty.stop();
        assertFalse(done[0]);
    }



}
