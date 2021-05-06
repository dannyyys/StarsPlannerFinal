package ControlObject;

import EntityObject.AbstractUser;

/**
 * Interface to create class that handles user logins
 */
public interface ILoginControl {
/**
 * get username and password and return the corresponding user
 * @return AbstractUser object
 */
    AbstractUser login();
}
