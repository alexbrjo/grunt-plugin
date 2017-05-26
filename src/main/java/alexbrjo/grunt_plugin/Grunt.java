package alexbrjo.grunt_plugin;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import jenkins.plugins.nodejs.tools.NodeJSInstallation;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Executes Grunt tasks. Before execute confirms Node, Grunt-cli and Grunt installations.
 *
 * TODO: use Jenkins Node installation instead of user
 * TODO: support for multiple Node installations
 * TODO: windows support
 *
 * @author Alex Johnson
 */
public class Grunt extends Builder {
    /** Grunt's command on UNIX arch */
    private static final String UNIX_GRUNT_COMMAND = "grunt";
    /** npm's command on UNIX arch */
    private static final String NPM_UNIX_COMMAND = "npm";

    /** The unparsed string of Grunt tasks to execute */
    private String tasks;
    /** The name of the user selected NodeJS installation */
    private String nodeName;

    /**
     * Creates a Grunt builder
     */
    @DataBoundConstructor
    public Grunt () {
        nodeName = null;
    }

    /**
     * Gets the name of the NodeJS installation
     * @return the name of the NodeJS installation
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the name of the NodeJS installation
     * @param name the name of the NodeJS installation
     */
    @DataBoundSetter
    public void setNodeName(String name) {
        nodeName = name;
    }

    /**
     * Gets the Grunt tasks of the build
     * @return the tasks
     */
    public String getTasks() {
        return tasks;
    }

    /**
     * Sets the tasks of the build
     * @param tasks the tasks to set
     */
    @DataBoundSetter
    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    /**
     * Gets the nodeJS installation used by the set nodeName
     * @return the first Node installation
     */
    public NodeJSInstallation getNodeJs () {
        for (NodeJSInstallation n : getDescriptor().getToolDescriptor().getInstallations()) {
            // TODO: check version or add installation selection in config
            if (n.getName().equals(nodeName))
                return n;
        }
        // return null if there are no Node installations
        return null;
    }

    /**
     * {JavaDoc.inherit}
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        GruntLogger log = new GruntLogger(listener);    // Logs to the build console
        EnvVars env = build.getEnvironment(listener);   // The build environment
        env.overrideAll(build.getBuildVariables());

        NodeJSInstallation node = getNodeJs();      // the NodeJS installation, null means no global installation setup
        String npmPath;
        if (node == null) {
            npmPath = NPM_UNIX_COMMAND;
            log.out(GruntPriority.WARN, "No Node.js installation selected, falling back to user installation");
        } else {
            node = getNodeJs().forEnvironment(env)
                    .forNode(build.getBuiltOn(), listener);
            log.out(GruntPriority.INFO, "Using Node.js installed at " + node.getHome());
            npmPath = node.getHome().concat("/bin/npm");
        }

        // execute commands, manual for now :(
        log.out(GruntPriority.INFO,"updating node package manager");
        launcher.launch().cmdAsSingleString(npmPath + " update").stdout(listener).pwd(build.getWorkspace()).join();
        log.out(GruntPriority.INFO,"install local Grunt (Must have local)");
        launcher.launch().cmdAsSingleString(npmPath + " install grunt").stdout(listener).pwd(build.getWorkspace()).join();

        // prime Grunt Tasks
        List<String> gruntTasks = new LinkedList<String>();
        for (String s : tasks.trim().split("\n")) {
            gruntTasks.add(UNIX_GRUNT_COMMAND + " --no-color " + s);
        }

        // run Grunt tasks, success means that every Grunt task exited with code 0
        boolean success = true;
        for (String cmd : gruntTasks) {
            if (cmd == null || cmd.trim().isEmpty()) continue;
            // TODO: stdout needs to take GruntLogger as a parameter
            success &= 0 == launcher.launch().cmdAsSingleString(cmd).stdout(listener).pwd(build.getWorkspace()).join();
        }

        // Sets the result based on all Grunt tasks return codes
        build.setResult(success ? Result.SUCCESS : Result.FAILURE);
        return build.getResult() == Result.SUCCESS;
    }

    /**
     * Gets the class' BuildStepDescriptor
     * @return the Grunt BuildStepDescriptor
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * The Descriptor for the Grunt builder
     */
    @Extension
    @Symbol("grunt")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /** The installations of NodeJS */
        @CopyOnWrite
        private volatile NodeJSInstallation[] installations = new NodeJSInstallation[0];

        /**
         * {JavaDoc.inherit}
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * {JavaDoc.inherit}
         */
        protected DescriptorImpl(Class<? extends Grunt> clazz) {
            super(clazz);
        }

        /**
         * Gets the NodeJSInstallations from the global Tool installations
         * @return the NodeJSInstallation
         */
        public NodeJSInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(NodeJSInstallation.DescriptorImpl.class);
        }

        /**
         * Gets the NodeJS installations, corresponds to the Jelly ${descriptor.installations}
         * @return the NodeJS installations
         */
        public NodeJSInstallation[] getInstallations() {
            return (installations = getToolDescriptor().getInstallations());
        }

        /**
         * Sets the NodeJS installations
         * @param installations the NodeJS installations configured globally
         */
        public void setInstallations(NodeJSInstallation... installations) {
            this.installations = installations;
            save();
        }

        /**
         * {JavaDoc.inherit}
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * {JavaDoc.inherit}
         */
        @Override
        public String getDisplayName() {
            return "Grunt";
        }
    }

}
