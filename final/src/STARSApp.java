import ControlObject.ConsoleAdminSession;
import ControlObject.ISession;
import ControlObject.ConsoleLoginControl;
import ControlObject.StudentCourseRegistrar;
import DataAccessObject.IReadWriteCourseDataAccessObject;
import DataAccessObject.IReadWriteUserDataAccessObject;
import EntityObject.*;
import HelperObject.Factory;
import ValueObject.*;
import org.beryx.textio.TextIoFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class STARSApp {

    public static void main(String[] args) {

        try {
            ISession session;
            do {
                ConsoleLoginControl loginControl = Factory.createLoginControl();
                AbstractUser user = loginControl.login();
                session = Factory.createSession(user);
                session.run();
            } while (!session.logout());
        } catch (SecurityException e) {
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
