package models;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

public class UserForm {

  @Constraints.Required
  protected String username;
  protected String password;

  public List<ValidationError> validate() {
    ArrayList<ValidationError> errors = new ArrayList<>();
    if (username.length() == 0) {
      errors.add(new ValidationError("username", "Username cannot be empty"));
    }
    if (password.length() < 6) {
      errors.add(new ValidationError("password", "Password must be at least 6 characters long"));
    }
    return errors.size() == 0 ? null : errors;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}