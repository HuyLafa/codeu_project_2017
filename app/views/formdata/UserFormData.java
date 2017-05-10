package views.formdata;

import play.data.validation.ValidationError;
import java.util.List;
import java.util.ArrayList;

/**
 * Backing class for the User data form.
 */
public class UserFormData {
  public String username = "";
  public String password = "";

  /**
   * Default constructor. Required for form instantiation.
   */
  public UserFormData() {

  }

  public UserFormData(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Validates Form<UserFormData>.
   * Called automatically in the controller by bindFromRequest().
   *
   * Validation checks include:
   * <ul>
   * <li> Name must be non-empty.
   * <li> Password must be properly formatted (see <tt>isPasswordInvalid</tt>)
   * </ul>
   *
   * @return Null if valid, or a List[ValidationError] if problems found.
   */
  public List<ValidationError> validate() {
    List<ValidationError> errors = new ArrayList<>();

    if (username == null || username.length() == 0) {
      errors.add(new ValidationError("name", "Username cannot be empty."));
    }
    if (!isPasswordValid(password)) {
      errors.add(new ValidationError("password", "Password does not meet requirements."));
    }
    return (errors.size() > 0) ? errors : null;
  }


  /**
   * Check if the input password is properly formatted.
   * @param password the user's input password when creating an account.
   * @return <tt>true</tt> if the password has at least 5 characters and <tt>false</tt> otherwise.
   */
  public boolean isPasswordValid(String password) {
    if (password != null && password.length() > 5) return true;
    // todo. add more password requirements?
    return false;
  }
}
