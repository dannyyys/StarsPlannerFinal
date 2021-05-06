package DataAccessObject;

import HelperObject.PasswordStorage;
import EntityObject.AbstractUser;
import EntityObject.Student;

/**
 * Interface of read only CourseDataAccessObject
 */
public interface IReadUserDataAccessObject {
    /**
     * Checks if username and password are correct and returns corresponding AbstractUser object
     * @param username String that represents username
     * @param password  String that represents password
     * @return AbstractUser object
     * @throws PasswordStorage.InvalidHashException when hash is corrupted.
     * @throws PasswordStorage.CannotPerformOperationException when unsafe to verify password on platform.
     */
    AbstractUser authenticate(String username, String password) throws PasswordStorage.InvalidHashException, PasswordStorage.CannotPerformOperationException;

    /**
     * Gets Student object
     * @param matricNumber String that represents student matric number
     * @return Student object
     */
    Student getStudent(String matricNumber);
}
