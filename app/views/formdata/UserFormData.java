package views.formdata;

import play.data.validation.ValidationError;
import java.util.List;
import java.util.ArrayList;

/**
 * Backing class for the User data form.
 */
public class UserFormData {

  public String name = "";

  public String password = "";

  /**
   * Default constructor. Required for form instantiation.
   */
  public UserFormData() {

  }

  public UserFormData(String name, String password) {
    this.name = name;
    this.password = password;
  }

  /**
   * Validates Form<UserFormData>.
   * Called automatically in the controller by bindFromRequest().
   *
   * Validation checks include:
   * <ul>
   * <li> Name must be non-empty.
   * <li> Password must be at least five characters.
   * </ul>
   *
   * @return Null if valid, or a List[ValidationError] if problems found.
   */
  public List<ValidationError> validate() {
    List<ValidationError> errors = new ArrayList<>();

    if (name == null || name.length() == 0) {
      errors.add(new ValidationError("name", "Username cannot be empty."));
    }
//    else if (isPasswordInValid(password)) {
//      errors.add(new ValidationError("password", "Password does not meet requirements."));
//    }
    return (errors.size() > 0) ? errors : null;
  }

  public boolean isPasswordInValid(String password) {
    if (password == null || password.length() < 5) return true;
    // todo. add more password requirements?
    return false;
  }
}
