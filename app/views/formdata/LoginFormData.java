package views.formdata;

import play.data.validation.ValidationError;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by HuyNguyen on 4/8/17.
 */
public class LoginFormData {

  public String username = "";

  public String password = "";

  /**
   * Default constructor. Required for form instantiation.
   */
  public LoginFormData() {

  }

  public LoginFormData(String username, String password) {
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
   * <li> Password must be at least five characters.
   * </ul>
   *
   * @return Null if valid, or a List[ValidationError] if problems found.
   */
  public List<ValidationError> validate() {
    List<ValidationError> errors = new ArrayList<>();

    if (username == null || username.length() == 0) {
      errors.add(new ValidationError("name", "Username cannot be empty."));
    }
    if (password == null || password.length() == 0) {
      errors.add(new ValidationError("password", "Password cannot be empty."));
    }
    return (errors.size() > 0) ? errors : null;
  }

}
