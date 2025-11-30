/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import DAO.UserDAO;
import Model.Session;
import Model.User;
import UI.StaffProfile;
import java.util.Arrays;
import javax.swing.JOptionPane;
import java.sql.*;
    

/**
 *
 * @author ThinkBook
 */
public class StaffProfileController {
    private String email;
    private String username;
    private char[] password;
    private StaffProfile staff_ui;
    private UserDAO userDAO;
    
    public StaffProfileController(StaffProfile view){
        this.staff_ui = view;
        this.userDAO = new UserDAO();
    }
    
    public StaffProfileController(String email, String username, char[] password){
        this.email = email;
        this.username = username;
        this.password = password;
        this.userDAO = new UserDAO();
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void wipePassword() {
        if (password != null) {
            Arrays.fill(password, '0');
        }
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }
    
    public void loadUserProfile(User currentUser) {
        if (this.staff_ui != null && currentUser != null) {
            staff_ui.setUsernameField(currentUser.getUsername());
            staff_ui.setEmailField(currentUser.getEmail());
        }
    }
    
    private boolean validateUpdateInput(String email, String username, char[] password) {
        if (email.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username and Email should not empty!");
            staff_ui.lockSystem();
            return false;
        }

        if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            JOptionPane.showMessageDialog(null, "Invalid email");
            staff_ui.lockSystem();
            return false;
        }

        if (password.length > 0 && password.length < 6) {
            JOptionPane.showMessageDialog(null, "Password at least 6 letters!");
            staff_ui.lockSystem();
            return false;
        }

        return true;
    }
    
    private boolean isEmailTaken(String email) {
        try {
            return userDAO.isEmailTaken(email);
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume taken on error for safety
        }
    }
    
    public boolean updateProfile() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return false;
        
        String oldEmail = currentUser.getEmail(); 

        String newUsername = staff_ui.getUsername();
        String newEmail = staff_ui.getEmail();
        char[] newPassChar = staff_ui.getPassword();

        if (!validateUpdateInput(newEmail, newUsername, newPassChar)) {
            return false;
        }

        if (!newEmail.equals(oldEmail) && isEmailTaken(newEmail)) {
            JOptionPane.showMessageDialog(null, "Email already exist!");
            staff_ui.lockSystem();
            return false;
        }

        boolean isSuccess = false;
        String newPassString = new String(newPassChar);

        if (newPassChar.length == 0) {
            isSuccess = updateInfoNoPass(oldEmail, newUsername, newEmail);
        } else {
            isSuccess = updateAllInfo(oldEmail, newUsername, newEmail, newPassString);
        }

        if (isSuccess) {
            JOptionPane.showMessageDialog(null, "Update successfully!");
            
            currentUser.setUsername(newUsername);
            currentUser.setEmail(newEmail);
            
            if (newPassChar != null && newPassChar.length > 0) {
                currentUser.setPassword(newPassChar.clone());
            }
            
            staff_ui.clearPasswordField();
            java.util.Arrays.fill(newPassChar, '0');
            return true;
        }
        return false;
    }

    private boolean updateInfoNoPass(String oldEmail, String newUsername, String newEmail) {
        try {
            return userDAO.updateUserWithoutPassword(oldEmail, newUsername, newEmail);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            return false;
        }
    }

    private boolean updateAllInfo(String oldEmail, String newUsername, String newEmail, String newPassword) {
        try {
            return userDAO.updateUserWithPassword(oldEmail, newUsername, newEmail, newPassword);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            return false;
        }
    }
}
