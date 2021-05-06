package ControlObject;

/**
 * Interface for sessions
 */
public interface ISession {
    /**
     * logout
     * @return whether session user logged out
     */
    boolean logout();

    /**
     * exit
     */
    void exit();

    /**
     * run
     */
    void run();
}

