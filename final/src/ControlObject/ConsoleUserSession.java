package ControlObject;

import DataAccessObject.IReadCourseDataAccessObject;
import EntityObject.AbstractUser;
import EntityObject.Course;
import EntityObject.Index;
import EntityObject.Student;
import HelperObject.Factory;
import HelperObject.PasswordStorage;
import ValueObject.*;
import Exception.*;
import org.beryx.textio.*;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * User session
 */
public class ConsoleUserSession implements ISession{
    /**
     * TextIO console
     */
    private TextIO _textIO;
    /**
     * textIO terminal
     */
    private TextTerminal _terminal;
    /**
     * Boolean value that shows if student is logged in
     * Initialized as false
     */
    private boolean loggedIn = false;
    /**
     * Student object
     */
    private Student _user;
    /**
     * Boolean that shows if user session is within the registration period
     */
    private boolean withinRegistrationPeriod;
    /**
     * String that contains message used for dashboard
     */
    private final String user =
            "  _   _                      ___                _      _                            _ \n" +
                    " | | | |  ___  ___   _ _    |   \\   __ _   ___ | |_   | |__   ___   __ _   _ _   __| |\n" +
                    " | |_| | (_-< / -_) | '_|   | |) | / _` | (_-< | ' \\  | '_ \\ / _ \\ / _` | | '_| / _` |\n" +
                    "  \\___/  /__/ \\___| |_|     |___/  \\__,_| /__/ |_||_| |_.__/ \\___/ \\__,_| |_|   \\__,_|\n" +
                    "                                                                                      ";
    /**
     * String that contains menu of user session
     */
    private final String registrationPeriodUserOptions =
            "1. Add Course\n" +
                    "2. Drop Course\n" +
                    "3. Check/Print Courses Registered\n" +
                    "4. Check Vacancies Available\n" +
                    "5. Change Index Number of Course\n" +
                    "6. Swap Index Number with Another Student\n" +
                    "7. Log out\n" +
                    "8. Exit\n";

    /**
     * Initialize _textIO, _terminal and _user
     * @param textIO TextIO object
     * @param terminal object
     * @param user AbstractUser object (cast to student)
     *
     */
    public ConsoleUserSession(TextIO textIO, TextTerminal terminal, AbstractUser user) {
        _textIO = textIO;
        _terminal = terminal;
        _user = (Student) user;
    }

    /**
     * Log of out session
     * @return boolean loggedIn
     */
    @Override
    public boolean logout() {
        _terminal.resetToBookmark("clear");
        return loggedIn;
    }

    /**
     * Exit
     * @throws SecurityException security breached
     */
    @Override
    public void exit() {
        throw new SecurityException();
    }

    /**
     * Runs the session
     */
    @Override
    public void run() {
        String keyStrokeAbort = "alt Z";
        boolean registeredAbort =  _terminal.registerHandler(keyStrokeAbort,
                t -> new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT));

        int choice = 0;
        _terminal.getProperties().setPromptBold(true);
        _terminal.resetToBookmark("clear");
        _terminal.println(user);
        if (registeredAbort) {
            _terminal.println("--------------------------------------------------------------------------------");
            _terminal.println("Press " + keyStrokeAbort + " to go abort your current action");
            _terminal.println("You can use this key combinations at any moment during your session.");
            _terminal.println("--------------------------------------------------------------------------------");
        }
        _terminal.setBookmark("user");
        displayUserMainMenu();
        do {
            try {
                //A bookmark to get system to go back to
                _terminal.resetToBookmark("user");
                //displays main menu
                displayUserMainMenu();
                choice = _textIO.newIntInputReader()
                        .withMinVal(1).withMaxVal(8)
                        .read("Enter your choice: ");
                //Go back to bookmark named "user"
                _terminal.resetToBookmark("user");
                switch (choice) {
                    case 1 -> addCourseMenu();
                    case 2 -> dropCourseMenu();
                    case 3 -> {
                        printRegisteredCourses();
                        _textIO.newStringInputReader().withDefaultValue(" ")
                                .read("press enter to continue");
                    }
                    case 4 -> checkCourseVacanciesMenu();
                    case 5 -> changeIndexMenu();
                    case 6 -> swapIndexWithPeerMenu();
                    case 7 -> loggedIn = false;
                    case 8 -> exit();
                }
            } catch (ReadAbortedException ignored) {
            }
        } while (choice >= 0 && choice < 7);
    }

    /**
     * Menu for student to add a course
     */
    private void addCourseMenu() {
        _terminal.println("add course");
        printRegisteredCourses();
        try {
            //Access course data and store all enrolled matric numbers in an array list
            IReadCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
            List<String> coursesString = courseDataAccessObject.getAllCourseCodes();
            Set<String> coursesRegistered = _user.getRegisteredCourses().keySet();
            Set<String> waitinglistCourses = _user.getRegisteredCourses().keySet();
            ArrayList<String> allRegisteredCourses = new ArrayList<>();
            allRegisteredCourses.addAll(coursesRegistered);
            allRegisteredCourses.addAll(waitinglistCourses);
            if (allRegisteredCourses.containsAll(coursesString)) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("no available courses to register");
                return;
            }
            //Prompt user to enter selection to choose the course he/she wants to add and reads it afterwards
            String courseCodeInput = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(coursesString)
                    .read("Enter course code: ");
            Course course = courseDataAccessObject.getCourse(courseCodeInput);
            //If user have already registered for the course, print the following message
            if (allRegisteredCourses.contains(courseCodeInput)) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("course already registered");
                return;
            }
            //Prompt user to enter selection to choose the index he/she wants to add and reads it afterwards
            String indexInputStr = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(course.getListOfIndexNumbers())
                    .read("Enter index: ");
            String matricNumber = _user.getMatricNumber();
            try {
                StudentCourseRegistrar studentCourseRegistration = Factory.createStudentCourseRegistrar();
                studentCourseRegistration.addRegistration(matricNumber, courseCodeInput, Integer.parseInt(indexInputStr));
                _terminal.getProperties().setPromptColor(Color.GREEN);
                _terminal.println("successfully added course");
            } catch (IOException | ClassNotFoundException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("error reading file");
            } catch (InsufficientAUsException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("You exceed max AU limit.");
            } catch (MaxEnrolledStudentsException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("maximum class size reached, adding you to waiting list instead.");
            } catch (InvalidAccessPeriodException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("registration period have not started/over");
            } catch (ExistingCourseException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("course already registered");
            } catch (ClashingTimeTableException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("unable to add course, time table clashes");
            } catch (Exception e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("error adding course");
            } finally {
                _terminal.getProperties().setPromptColor("white");
            }
            printRegisteredCourses();
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error reading file");
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ")
                    .read("press enter to continue");
        }
    }

    /**
     * Menu for student to drop a course he/she have registered
     */
    private void dropCourseMenu() {
        _terminal.println("drop course");
        printRegisteredCourses();
        try {
            _user = Factory.getTextUserDataAccessObject(this).getStudent(_user.getMatricNumber());
            TreeMap<String, Integer> registered = _user.getRegisteredCourses();
            TreeMap<String, Integer> waitList = _user.getWaitingListCourses();
            List<String> allCoursesStr = new ArrayList<>(registered.keySet());
            allCoursesStr.addAll(waitList.keySet());
            //If no courses registered, print the following message
            if (allCoursesStr.size() == 0) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("no course to drop");
                return;
            }
            String courseCodeInput = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(allCoursesStr)
                    .read("Select course to drop: ");
            String matricNumber = _user.getMatricNumber();

            //get index number of course that student wants to drop, confirms course is already reg
            TreeMap<String, Integer> registeredCourses = _user.getRegisteredCourses();
            TreeMap<String, Integer> waitListCourses = _user.getWaitingListCourses();
            int indexNumber = 0;

            if (registeredCourses.containsKey(courseCodeInput)) {
                indexNumber = registeredCourses.get(courseCodeInput);
            } else if (waitListCourses.containsKey(courseCodeInput)) {
                indexNumber = waitListCourses.get(courseCodeInput);
            } else {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("Course has not been registered yet.");
            }
            try {
                StudentCourseRegistrar studentCourseRegistration = Factory.createStudentCourseRegistrar();
                studentCourseRegistration.deleteRegistration(matricNumber, courseCodeInput, indexNumber);
                _terminal.getProperties().setPromptColor(Color.GREEN);
                _terminal.println("Successfully dropped course");
                _terminal.getProperties().setPromptColor("white");
            } catch (IOException | ClassNotFoundException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("error reading file");
            } catch (NonExistentUserException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("no such student");
            } catch (InvalidAccessPeriodException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("registration period have not started/over");
            } catch (NonExistentRegistrationException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("non existent registration");
            } catch (MaxEnrolledStudentsException ignored) {
            } catch (RuntimeException e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("error sending email, course dropped nonetheless");
            } catch (Exception e) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("error dropping course");
            } finally {
                _terminal.getProperties().setPromptColor("white");
            }
            printRegisteredCourses();
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error reading file");
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ")
                    .read("press enter to continue");
        }

    }

    /**
     * Print student registered courses
     */
    private void printRegisteredCourses() {
        try {
            _terminal.getProperties().setPromptColor(Color.green);
            _user = Factory.getTextUserDataAccessObject(this).getStudent(_user.getMatricNumber());
            TreeMap<String, Integer> registered = _user.getRegisteredCourses();
            TreeMap<String, Integer> waitingList = _user.getWaitingListCourses();
            _terminal.println("_______Registered Courses_______");
            _terminal.println("Course code |\t Index");
            registered.forEach((key, value) -> _terminal.printf("%s \t:\t %d\n", key, value));
            _terminal.println("_______Waiting List Courses_______");
            _terminal.println("Course code |\t Index");
            waitingList.forEach((key, value) -> _terminal.printf("%s \t:\t %d\n", key, value));
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error reading file");
        } finally {
            _terminal.getProperties().setPromptColor("white");
        }
    }

    /**
     * Check course vacancy menu
     */
    private void checkCourseVacanciesMenu() {
        try {
            _terminal.println("Check course vacancies");
            IReadCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
            List<String> coursesString = courseDataAccessObject.getAllCourseCodes();
            String courseCode = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(coursesString)
                    .read("select course to check vacancies for: ");
            Course course = courseDataAccessObject.getCourse(courseCode);
            List<String> indexString = course.getListOfIndexNumbers();
            String indexNum = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(indexString)
                    .read("select index to check vacancies for: ");
            int vacancies = course.checkVacancies(Integer.parseInt(indexNum));
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("Successfully retrieved vacancies");
            //Print appropriate messages for each of the following cases
            if (vacancies <= 0) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.printf("There is 0/%d vacancy for %s of %s\n",
                        course.getIndex(Integer.parseInt(indexNum)).getMaxClassSize(),
                        indexNum, courseCode);
                if (vacancies >= -1) {
                    _terminal.println("There is " + -vacancies + " student in the waiting list");
                } else {
                    _terminal.println("There are " + -vacancies + " students in the waiting list");
                }
            } else {
                if (vacancies <= 1) {
                    _terminal.printf("There is %d/%d vacancy for %s of %s\n",
                            vacancies, course.getIndex(Integer.parseInt(indexNum)).getMaxClassSize(),
                            indexNum, courseCode);
                } else {
                    _terminal.printf("There are %d/%d vacancies for %s of %s\n",
                            vacancies, course.getIndex(Integer.parseInt(indexNum)).getMaxClassSize(),
                            indexNum, courseCode);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("file not found");
        } catch (NonExistentIndexException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("no such index");
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ").read("press enter to continue");
        }
    }

    /**
     * Allows student to change the index group of a course he/she is enrolled in.
     * Retrieves the user's registered courses to determine which index group he/she is currently enrolled in.
     * Prompts user to choose a course index to switch to.
     * Exits from function if the selected index is the same as his/her current one.
     * <p>
     * Otherwise, the class (tutorial/laboratory) timings for the selected index group will be
     * checked against the student's timetable.
     * Disallows student to change to the selected index group if there will be a clash in timetable.
     *
     */
    private void changeIndexMenu() {
        _terminal.println("Change index");
        try {
            _user = Factory.getTextUserDataAccessObject(this).getStudent(_user.getMatricNumber());
            TreeMap<String, Integer> registered = _user.getRegisteredCourses();
            //if no registered courses, print the error message
            List<String> registeredCoursesStr = new ArrayList<>(registered.keySet());
            if (registeredCoursesStr.size() == 0) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("no course registered");
                return;
            }
            String courseCodeInput = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(registeredCoursesStr)
                    .read("Select course to swap: ");
            int currIndexNumber = _user.getRegisteredCourses().get(courseCodeInput);
            _terminal.getProperties().setPromptColor(Color.GREEN);
            _terminal.println("User current index is: " + currIndexNumber);
            _terminal.getProperties().setPromptColor("white");
            IReadCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
            List<String> indexesStr = courseDataAccessObject.getCourse(courseCodeInput).getListOfIndexNumbers();
            String indexNumberInput = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(indexesStr)
                    .read("Select index to swap to: ");
            int newIndexNumber = Integer.parseInt(indexNumberInput);
            //Print following message if current index number is the same as new index number
            if (newIndexNumber == currIndexNumber) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("You are already in the index.");
                _terminal.getProperties().setPromptColor("white");
                return;
            }

            boolean clashingTimeTable = false;
            Course course = courseDataAccessObject.getCourse(courseCodeInput);
            Index index = course.getIndex(newIndexNumber);
            Set<String> registeredCourses = _user.getRegisteredCourses().keySet();
            Set<String> waitlist = _user.getWaitingListCourses().keySet();
            ArrayList<String> allRegisteredCourses = new ArrayList<>();
            allRegisteredCourses.addAll(registeredCourses);
            allRegisteredCourses.addAll(waitlist);
            //Checks if user had registered for the course
            for (String registeredCourseCode : allRegisteredCourses) {
                if (registeredCourseCode.equals(courseCodeInput)) {
                    continue;
                }
                int registeredCourseIndexNumber = _user.getRegisteredCourses().get(registeredCourseCode);
                Course registeredCourse = courseDataAccessObject.getCourse(registeredCourseCode);
                Index registeredIndex = registeredCourse.getIndex(registeredCourseIndexNumber);
                //Check if there is a clash of schedule of new index and user current schedule
                if (registeredCourse.isClashing(course) || registeredCourse.isClashing(index) ||
                        registeredIndex.isClashing(index)) {
                    _terminal.getProperties().setPromptColor("red");
                    _terminal.println("Unable to swap. There is clashing timeslot in your timetable");
                    _terminal.getProperties().setPromptColor("white");
                    clashingTimeTable = true;
                    break;
                }
            }

            if (!clashingTimeTable) {
                //get user matricNumber
                String matricNumber = _user.getMatricNumber();
                //delete old index
                StudentCourseRegistrar studentCourseRegistrar = Factory.createStudentCourseRegistrar();
                studentCourseRegistrar.deleteRegistration(matricNumber, courseCodeInput, currIndexNumber);

                //add new index
                studentCourseRegistrar.addRegistration(matricNumber, courseCodeInput, newIndexNumber);
                _terminal.getProperties().setPromptColor(Color.green);
                _terminal.printf("successfully swapped %s from %s to %s\n", courseCodeInput, currIndexNumber, newIndexNumber);
            }
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("file not found");
        } catch (InvalidAccessPeriodException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("registration period is over/haven't started");
        } catch (MaxEnrolledStudentsException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("maximum enrolled students, added to waiting list instead");
        } catch (ClashingTimeTableException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("unable to add course, time table clashes");
        } catch (Exception e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error saving file");
        } finally {
            _terminal.getProperties().setPromptColor("white");
            _textIO.newStringInputReader().withDefaultValue(" ")
                    .read("press enter to continue");
        }
    }

    /**
     * Allows student to swap index groups with a peer given the same course.
     * Validates the username and password provided by user.
     * If there is a timetable clash for either the user or the student, swapping is disallowed.
     */
    private void swapIndexWithPeerMenu() {
        int currIndexNumber;
        String courseCodeInput;

        //get student's registered courses first, checks if any course has been registered first
        try {
            _user = Factory.getTextUserDataAccessObject(this).getStudent(_user.getMatricNumber());
            TreeMap<String, Integer> registered = _user.getRegisteredCourses();
            List<String> registeredCoursesStr = new ArrayList<>(registered.keySet());
            if (registeredCoursesStr.size() == 0) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("no course registered");
                _textIO.newStringInputReader().withDefaultValue(" ")
                        .read("press enter to continue");
                return;
            }
            courseCodeInput = _textIO.newStringInputReader()
                    .withNumberedPossibleValues(registeredCoursesStr)
                    .read("Select course to swap: ");
            currIndexNumber = _user.getRegisteredCourses().get(courseCodeInput);
            _terminal.getProperties().setPromptColor(Color.green);
            _terminal.println("User current index is: " + currIndexNumber);
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error reading the file");
            return;
        } finally {
            _terminal.getProperties().setPromptColor("white");
        }

        //peer logs in for swapping
        AbstractUser absPeer = null;
        _terminal.setBookmark("Enter peer username:");
        do {
            String peerUsername = _textIO.newStringInputReader()
                    .read("Enter peer username: ");
            String peerPassword = _textIO.newStringInputReader()
                    .withMinLength(6)
                    .withInputMasking(true)
                    .read("Enter peer password: ");
            //authenticate peer
            try {
                absPeer = Factory.getTextUserDataAccessObject(this).authenticate(peerUsername, peerPassword);
                if (!(absPeer instanceof Student)) {
                    _terminal.resetToBookmark("Enter peer username:");
                    _terminal.getProperties().setPromptColor("red");
                    _terminal.println("Wrong username/password. Try again.");
                } else {
                    //downcast peer from AbstractUser to Student
                    Student studentPeer = (Student) absPeer;
                    //change to auto fetch peer old index.
                    if (studentPeer == _user) { //peer is same as current student
                        _terminal.resetToBookmark("Enter peer username:");
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("you cant swap index with yourself");
                        break;
                    }
                    //check if peer is registered for the selected course
                    boolean containsKey = studentPeer.getRegisteredCourses().containsKey(courseCodeInput);
                    if (!containsKey) {
                        _terminal.getProperties().setPromptColor("red");
                        _terminal.println("Peer is not registered for this course");
                        _terminal.getProperties().setPromptColor("white");
                    } else {
                        //check if peer and user are already in same index
                        int peerIndexNumber = studentPeer.getRegisteredCourses().get(courseCodeInput);
                        if (peerIndexNumber == currIndexNumber) {
                            //_terminal.println("You are already in the index.");
                            _terminal.getProperties().setPromptColor("red");
                            _terminal.println("Both of you are in the same index.");
                            _terminal.getProperties().setPromptColor("white");
                            break;
                        }

                        boolean clashingTimeTable = false;
                        IReadCourseDataAccessObject courseDataAccessObject = Factory.getTextCourseDataAccessObject(this);
                        Course course = courseDataAccessObject.getCourse(courseCodeInput);
                        Index peerIndex = course.getIndex(peerIndexNumber);
                        Index currIndex = course.getIndex(currIndexNumber);

                        Set<String> userRegisteredCourses = _user.getRegisteredCourses().keySet();
                        Set<String> userWaitlist = _user.getWaitingListCourses().keySet();
                        ArrayList<String> userAllRegisteredCourses = new ArrayList<>();
                        userAllRegisteredCourses.addAll(userRegisteredCourses);
                        userAllRegisteredCourses.addAll(userWaitlist);

                        //iterates through registered courses to check for any clashing
                        for (String registeredCourseCode : userAllRegisteredCourses) {
                            if (registeredCourseCode.equals(courseCodeInput)) {
                                continue;
                            }
                            int registeredCourseIndexNumber = _user.getRegisteredCourses().get(registeredCourseCode);
                            Course registeredCourse = courseDataAccessObject.getCourse(registeredCourseCode);
                            Index registeredIndex = registeredCourse.getIndex(registeredCourseIndexNumber);

                            //incoming index clashes with current timetable
                            if (registeredCourse.isClashing(course) || registeredCourse.isClashing(peerIndex) ||
                                    registeredIndex.isClashing(peerIndex)) {
                                _terminal.getProperties().setPromptColor("red");
                                _terminal.println("Unable to swap. There is clashing timeslot in your timetable");
                                _terminal.getProperties().setPromptColor("white");
                                clashingTimeTable = true;
                                break;
                            }
                        }
                        if (!clashingTimeTable) {
                            Set<String> peerRegisteredCourses = studentPeer.getRegisteredCourses().keySet();
                            Set<String> peerWaitlist = studentPeer.getWaitingListCourses().keySet();
                            ArrayList<String> peerAllRegisteredCourses = new ArrayList<>();
                            peerAllRegisteredCourses.addAll(peerRegisteredCourses);
                            peerAllRegisteredCourses.addAll(peerWaitlist);

                            //iterates through registered courses to check for any clashing
                            for (String registeredCourseCode : peerAllRegisteredCourses) {
                                if (registeredCourseCode.equals(courseCodeInput)) {
                                    continue;
                                }
                                int peerRegisteredCourseIndexNumber = studentPeer.getRegisteredCourses().get(registeredCourseCode);
                                Course peeRegisteredCourse = courseDataAccessObject.getCourse(registeredCourseCode);
                                Index peerRegisteredIndex = peeRegisteredCourse.getIndex(peerRegisteredCourseIndexNumber);

                              //outgoing index clashes with peer's current timetable
                                if (peeRegisteredCourse.isClashing(course) || peeRegisteredCourse.isClashing(currIndex) ||
                                        peerRegisteredIndex.isClashing(currIndex)) {
                                    _terminal.getProperties().setPromptColor("red");
                                    _terminal.println("Unable to swap. There is clashing timeslot in peer's timetable");
                                    _terminal.getProperties().setPromptColor("white");
                                    clashingTimeTable = true;
                                    break;
                                }
                            }
                        }

                        //swapping is performed by deleting and adding registrations for user and peer
                        if (!clashingTimeTable) {
                            StudentCourseRegistrar studentCourseRegistrar = Factory.createStudentCourseRegistrar();
                            //drop course for peer
                            studentCourseRegistrar.deleteRegistration(studentPeer.getMatricNumber(), courseCodeInput, peerIndexNumber);
                            //drop course for user
                            studentCourseRegistrar.deleteRegistration(_user.getMatricNumber(), courseCodeInput, currIndexNumber);
                            //add course for peer with user index
                            studentCourseRegistrar.addRegistration(studentPeer.getMatricNumber(), courseCodeInput, currIndexNumber);
                            //add course for user with peer index
                            studentCourseRegistrar.addRegistration(_user.getMatricNumber(), courseCodeInput, peerIndexNumber);
                            _terminal.getProperties().setPromptColor(Color.GREEN);
                            _terminal.println("Successfully swapped indexes");
                            _terminal.println("Your new index is: " + peerIndexNumber);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                _terminal.setBookmark("Enter peer username:");
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("error reading file");
            } catch (PasswordStorage.InvalidHashException | PasswordStorage.CannotPerformOperationException e) {
                _terminal.setBookmark("Enter peer username:");
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("error encountered when hashing");
            } catch (InvalidAccessPeriodException e) {
                _terminal.setBookmark("Enter peer username:");
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("registration period have not started/over");
            } catch (Exception e) {
                _terminal.setBookmark("Enter peer username:");
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("error encountered when saving");
            } finally {
                _terminal.getProperties().setPromptColor("white");
            }
        } while (absPeer == null);
        _textIO.newStringInputReader().withDefaultValue(" ")
                .read("press enter to continue");
    }

    /**
     * Prints the user main menu.
     * Informs the user of the functionalities that he/she will not be able to access
     * if registration period has not started/already ended.
     */
    private void displayUserMainMenu() {
        //checks if called session is within access period, displays message that functionalities are restricted outside the period
    	try {
            RegistrationPeriod registrationPeriod = Factory.getTextRegistrationDataAccessObject(this).getRegistrationPeriod();
            withinRegistrationPeriod = !registrationPeriod.notWithinRegistrationPeriod();
            if (!withinRegistrationPeriod) {
                _terminal.getProperties().setPromptColor("red");
                _terminal.println("It is not registration period, only checking registered courses/ course vacancies is functional");
            }
        } catch (IOException | ClassNotFoundException e) {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("error accessing files");
        } finally {
            _terminal.getProperties().setPromptColor("white");
        }
        if (withinRegistrationPeriod) {
            _terminal.println(registrationPeriodUserOptions);
        } else {
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("1. Add Course\n" +
                    "2. Drop Course");
            _terminal.getProperties().setPromptColor("white");
            _terminal.println("3. Check/Print Courses Registered\n" +
                    "4. Check Vacancies Available");
            _terminal.getProperties().setPromptColor("red");
            _terminal.println("5. Change Index Number of Course\n" +
                    "6. Swap Index Number with Another Student");
            _terminal.getProperties().setPromptColor("white");
            _terminal.println("7. Log out\n" +
                    "8. Exit");
            _terminal.getProperties().setPromptColor("white");
        }
        _terminal.println("\t\twelcome " + _user.getName());
    }
}