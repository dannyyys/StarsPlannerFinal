package ValueObject;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a Registration Period
 */
public class RegistrationPeriod implements Serializable {
    /**
     * The start and end time of the RegistrationPeriod
     */
    private final LocalDateTime startDate, endDate;
    /**
     * The Serialised ID that the RegistrationPeriod object is tagged to
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a RegistrationPeriod object with a given start and end date.
     * Start and end dates are set by the administrative user.
     *
     *
     * @param startDate A LocalDateTime object that represents the start date of the Registration Period
     * @param endDate A LocalDateTime object that represents the end date of the Registration Period
     *
     */
    public RegistrationPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Determines if the current datetime falls within the Registration Period.
     *
     * @return A boolean that is false if the current datetime occurs before the start date
     * or if the current datetime occurs after the end date
     */
    public boolean notWithinRegistrationPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(startDate) || now.isAfter(endDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RegistrationPeriod) {
            RegistrationPeriod that = (RegistrationPeriod) obj;
            return that.startDate.isEqual(startDate) && that.endDate.isEqual(endDate);
        } else {
            return false;
        }
    }
}
