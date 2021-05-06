package ControlObject;

import DataAccessObject.IReadUserDataAccessObject;
import HelperObject.Factory;
import HelperObject.PasswordStorage;
import EntityObject.AbstractUser;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;

import java.io.IOException;

/**
 * Class that handles user logins
 */
public class ConsoleLoginControl implements ILoginControl {
    /**
     * AbstractUser object
     */
    private AbstractUser _user;
    /**
     * TextIO object
     */
    private final TextIO _textIO;
    /**
     * TextTerminal object
     */
    private final TextTerminal _terminal;
    /**
     * String that contains message used for dashboard
     */
    private final String welcome =
            "   _____                                 _             _____   _                        \n" +
            "  / ____|                               | |           / ____| | |                       \n" +
            " | |        ___    _ __    ___    ___   | |   ___    | (___   | |_    __ _   _ __   ___ \n" +
            " | |       / _ \\  | '_ \\  / __|  / _ \\  | |  / _ \\    \\___ \\  | __|  / _` | | '__| / __|\n" +
            " | |____  | (_) | | | | | \\__ \\ | (_) | | | |  __/    ____) | | |_  | (_| | | |    \\__ \\\n" +
            "  \\_____|  \\___/  |_| |_| |___/  \\___/  |_|  \\___|   |_____/   \\__|  \\__,_| |_|    |___/\n" +
            "                                                                                        \n" +
            "                                                                                        ";

    /**
     * Initialize all attributes of this class
     * @param textIO TextIO console
     * @param terminal texIO terminal
     *
     * @see org.beryx.textio.TextTerminal
     */
    public ConsoleLoginControl(TextIO textIO, TextTerminal terminal) {
        _user = null;
        _textIO = textIO;
        _terminal = terminal;
        _terminal.getProperties().setPromptColor("white");
    }

    /**
     * Login user
     * @return AbstractUser object
     *
     * @see org.beryx.textio.TextTerminal
     * @see org.beryx.textio.TextIO
     *
     * @see HelperObject.Factory#getTextUserDataAccessObject(ConsoleAdminSession) (String, String)
     * @see DataAccessObject.IReadWriteUserDataAccessObject#authenticate(String, String)
     *
     */
    @Override
    public AbstractUser login() {
        _terminal.setBookmark("clear");
        try {
            IReadUserDataAccessObject userDataAccessObject = Factory.getTextUserDataAccessObject(this);
            _terminal.println(welcome);
            _terminal.setBookmark("prompt");
            do {
                String username = _textIO.newStringInputReader()
                        .read("Enter Username:");
                String password = _textIO.newStringInputReader()
                        .withMinLength(6)
                        .withInputMasking(true)
                        .read("Enter Password: ");
                _user = userDataAccessObject.authenticate(username, password);
                _terminal.resetToBookmark("prompt");
                if (_user == null) {
                    _terminal.getProperties().setPromptColor("red");
                    _terminal.println("wrong username/password");
                    _terminal.getProperties().setPromptColor("white");
                }
            } while (_user == null);
        } catch (PasswordStorage.CannotPerformOperationException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error validating password");
        } catch (PasswordStorage.InvalidHashException e) {
            _terminal.getProperties().setPromptColor("red");
            System.out.println("error unhashing password");
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("Error reading file");
        } finally {
            _terminal.getProperties().setPromptColor("white");
        }
        return _user;
    }
}