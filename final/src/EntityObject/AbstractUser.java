package EntityObject;

import HelperObject.PasswordStorage;
import ValueObject.Gender;
import ValueObject.Nationality;
import ValueObject.School;
import ValueObject.UserType;

import java.io.Serializable;
import java.time.Year;
import java.util.Random;

/**
 * class for implementing a user
 *
 */
public abstract class AbstractUser implements Serializable {
    private final String username;
    private final String hash;
    private final String name;
    private final Gender gender;
    private final Nationality nationality;
    private School school;
    private final UserType userType;
    private final String email;
    private static final long serialVersionUID = 1L;

    /**
     * Creates user containing specific information.
     *
     * @param name A String that represents user name
     * @param school An Enum value of the user's school
     * @param gender An Enum value of the user's gender
     * @param nationality An Enum value of the user's nationality
     * @param userType An Enum value of user's type
     * @throws PasswordStorage.CannotPerformOperationException unsafe to hash password
     * @see ValueObject.School
     * @see ValueObject.Gender
     * @see ValueObject.Nationality
     * @see ValueObject.UserType
     */
    public AbstractUser(String name, School school, Gender gender, Nationality nationality, UserType userType) throws PasswordStorage.CannotPerformOperationException {
        int randNum = new Random().nextInt(1000);
        this.hash = PasswordStorage.createHash(name.strip().toLowerCase() +
                Year.now().toString());
        this.username = name.contains(" ") ? name.toLowerCase().split(" ")[0] : name.toLowerCase() +
                school.toString().toLowerCase() +
                String.format("%04d", randNum);
        this.gender = gender;
        this.nationality = nationality;
        this.name = name;
        this.school = school;
        this.userType = userType;
        this.email = name.strip().toLowerCase() +
                randNum +
                school.toString().toLowerCase() +
                '.' +
                userType.toString().toLowerCase() +
                "@ntu.edu.sg";
        System.out.println(username + ": " + name.strip().toLowerCase() +
                Year.now().toString() + ": " + email);
    }

    /**
     * Gets user type
     * @return userType An enum that represents user type
     * @see ValueObject.UserType
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Gets user's email.
     * @return String that represents user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets user's gender.
     * @return gender that represent user's gender
     * @see ValueObject.Gender
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Gets user's nationality
     * @return nationality An enum representing user's nationality
     * @see ValueObject.Nationality
     */
    public Nationality getNationality() {
        return nationality;
    }

    /**
     * Gets user's username
     * @return username A String representing user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets hashed password
     * @return hash A String representing hashed password
     */
    public String getHash() {
        return hash;
    }

    /**
     * Gets name of user
     * @return name A String which represents user's name
     */
    public String getName() {
        return name;
    }
}
