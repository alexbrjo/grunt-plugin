package alexbrjo.grunt_plugin;

/**
 * Priority levels in the Logger
 */
enum GruntPriority {
    INFO(),
    WARN("WARN: "),
    ERR("ERROR: ");

    /** The message of the Grunt Priority */
    String message;
    GruntPriority () {
        this("");
    }
    GruntPriority (String m) {
        message = m;
    }

    /**
     * {JavaDoc.inherit}
     */
    @Override
    public String toString() {
        return message;
    }
}