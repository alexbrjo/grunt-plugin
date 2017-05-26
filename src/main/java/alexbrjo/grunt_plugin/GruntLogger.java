package alexbrjo.grunt_plugin;

import hudson.model.TaskListener;

import java.io.PrintStream;

/**
 * Logs and TODO: colors output
 * @author Alex Johnson
 */
public class GruntLogger {

    /** The PrintStream of the TaskListener */
    private PrintStream log;

    /**
     * Creates a GruntLogger for a build's TaskListener
     * @param listener the listener to log to
     */
    public GruntLogger (TaskListener listener) {
        log = listener.getLogger();
    }

    /**
     * Logs a normal info message
     * @param g the priority of the message
     * @param s the message to log
     */
    public void out (GruntPriority g, String s) {
        log.println("[Grunt-plugin] " + g + s);
    }
}
