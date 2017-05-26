package alexbrjo.grunt_plugin;

import hudson.Plugin;

import java.util.logging.Logger;

/**
 * Loads the Grunt Plugin
 * @author Alex Johnson
 */
public class GruntPlugin extends Plugin {

    /** The logger of the Grunt-Plugin */
    private final static Logger LOG = Logger.getLogger(GruntPlugin.class.getName());

    /**
     * Starts the plugin
     */
    public void start() throws Exception {
        load();
    }
    
}
