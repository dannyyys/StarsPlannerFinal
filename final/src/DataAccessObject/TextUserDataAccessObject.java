package DataAccessObject;

import EntityObject.Staff;
import Exception.ExistingUserException;
import Exception.NonExistentUserException;
import HelperObject.PasswordStorage;
import EntityObject.AbstractUser;
import EntityObject.Student;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * text version of user data access object
 */
public class TextUserDataAccessObject implements Serializable, IReadWriteUserDataAccessObject {
    /**
     * TreeMap of login information of users in STARS
     * Contains username in String and AbstractUser object
     */
    private final TreeMap<String, AbstractUser> loginInformation = new TreeMap<>();
    /**
     * A null instance of TextUserDataAccess
     */
    private static TextUserDataAccessObject instance = null;

    /**
     * A private Constructor that prevents any other class from instantiating
     *
     */
    private TextUserDataAccessObject() {
        super();
    }

    /**
     * Static 'instance' method
     * @return An instance of this class
     * @throws IOException cannot find file
     * @throws ClassNotFoundException Class does not exist
     */
    public static TextUserDataAccessObject getInstance() throws IOException, ClassNotFoundException{
        initialize();
        if(instance == null){
            instance = new TextUserDataAccessObject();
        }
        return instance;
    }

    /**
     * Reads in serialized Users database file
     * @throws IOException cannot find file
     * @throws ClassNotFoundException Class does not exist
     */
    private static void initialize() throws IOException, ClassNotFoundException {
        InputStream file = new FileInputStream("./data/Users.ser");
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        instance = (TextUserDataAccessObject) input.readObject();
    }

    /**
     * Writes/update into serialized Users database file
     *
     */
    private static void persist(){
        FileOutputStream fos;
        ObjectOutputStream out = null;
        try{
            fos = new FileOutputStream("./data/Users.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(instance);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Overrides existing Student object
     * @param student Student object
     * @throws NonExistentUserException Student does not exist
     */
    @Override
    public void updateStudent(Student student) throws NonExistentUserException {
        HashMap<String, Student> students = getAllStudents();

        if (!students.containsKey(student.getMatricNumber())) {
            throw new NonExistentUserException();
        } else {
            loginInformation.put(student.getUsername(), student);
            persist();
        }
    }

    /**
     * Add a new student
     * @param student Student object
     */
    @Override
    public void addStudent(Student student) throws ExistingUserException {
        HashMap<String, Student> students = getAllStudents();

        if (students.containsKey(student.getMatricNumber())) {
            throw new ExistingUserException();
        } else {
            loginInformation.put(student.getUsername(), student);
            persist();
        }
    }

    /**
     * Add a new staff
     * @param staff Staff object
     */
    @Override
    public void addAdmin(Staff staff) throws ExistingUserException {
        if (loginInformation.containsKey(staff.getUsername())) {
            throw new ExistingUserException();
        } else {
            loginInformation.put(staff.getUsername(), staff);
            persist();
        }
    }

    /**
     * Authenticate user
     * @param username A String that represents username
     * @param password A String that presents password
     * @return null if authentication fail, AbstractUser object to successful
     */
    @Override
    public AbstractUser authenticate(String username, String password) throws PasswordStorage.InvalidHashException, PasswordStorage.CannotPerformOperationException {
        AbstractUser user = loginInformation.get(username);

        if (user != null) {
            if (PasswordStorage.verifyPassword(password, user.getHash())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Gets Student object
     * @param matricNumber A String that represents student matric number
     * @return Student object
     */
    @Override
    public Student getStudent(String matricNumber) {
        HashMap<String, Student> students = getAllStudents();
        return students.get(matricNumber);
    }

    /**
     * get all student matric numbers as a list
     * @return list of student matric numbers
     */
    @Override
    public List<String> getAllStudentMatricNumbers() {
        HashMap<String, Student> students = getAllStudents();
        return new ArrayList<>(students.keySet());
    }

    /**
     * construct student info string
     * @return string containing all student's info
     */
    @Override
    public String studentsInfoToString() {
        StringBuilder str = new StringBuilder();
        HashMap<String, Student> students = getAllStudents();
        for (Student existingStudent : students.values()) {
            str.append(existingStudent.toString()).append('\n');
        }
        return str.toString();
    }

    /**
     * Gets HashMap of all existing students, contains student matric number in String and Student object
     * @return HashMap as described above
     */
    private HashMap<String, Student> getAllStudents() {
        HashMap<String, Student> students = new HashMap<>();
        for (AbstractUser user : loginInformation.values()) {
            if (user instanceof Student) {
                Student s = (Student) user;
                students.put(s.getMatricNumber(), s);
            }
        }
        return students;
    }
}
