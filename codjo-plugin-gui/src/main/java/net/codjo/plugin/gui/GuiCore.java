package net.codjo.plugin.gui;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.plugin.common.ApplicationCore;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
/**
 *
 */
public class GuiCore<T extends JFrame> extends ApplicationCore {
    private T window;


    @Override
    protected AgentContainer createAgentContainer(ContainerConfiguration containerConfiguration) {
        return AgentContainer.createContainer(containerConfiguration);
    }


    public void show(String[] arguments) {
        try {
            window = createMainWindow();

            executeMainBehaviour(arguments);

            if (getWindow() != null) {
                displayMainWindow();
            }
            else {
                getLogger().info("Affichage de la fenetre desactive (pas de fenetre disponible)");
            }
        }
        catch (Exception e) {
            getLogger().error("Impossible de démarrer l'application.", e);
            JOptionPane.showMessageDialog(null,
                                          e.getLocalizedMessage(),
                                          "Erreur Fatale",
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }


    protected T createMainWindow() {
        return null;
    }


    protected T getWindow() {
        return window;
    }


    public static interface MainBehaviour extends ApplicationCore.MainBehaviour {

    }


    private void displayMainWindow() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                getWindow().setVisible(true);
            }
        });
    }
}
