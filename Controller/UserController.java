/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import DAO.UserDAO;
import database.brotherconnection;
import java.sql.*;
import javax.swing.JOptionPane;
import Model.User;


/**
 *
 * @author ThinkBook
 */
public class UserController {
    private UserDAO userDAO;
    
    public UserController(){
        this.userDAO = new UserDAO();
    }
    
    private boolean validateForm(String email, String username, char[] password, boolean isSignup) {
        if (email.isEmpty() || password == null || (isSignup && username.isEmpty())) {
            JOptionPane.showMessageDialog(null, "Please fill all information!");
            return false;
        }

        if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            JOptionPane.showMessageDialog(null, "Invalid email!");
            return false;
        }

        if (isSignup && password.length < 6) {
            JOptionPane.showMessageDialog(null, "Password must be at least 6 characters!");
            return false;
        }

        return true;
    }

    public boolean signup(User user) {
        if (!validateForm(user.getEmail(), user.getUsername(), user.getPassword(), true)) {
            return false;
        }

        try {
            userDAO.insertUser(user);
            JOptionPane.showMessageDialog(null, "Sign up successful!");
            return true;
            
        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(null, "Email already exist!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }

        return false;
    }

    public User login(User user) {
        if (!validateForm(user.getEmail(), "", user.getPassword(), false)) {
            return null;
        }

        try {
            User loggedUser = userDAO.findUserByCredentials(user.getEmail(), new String(user.getPassword()));
            
            if (loggedUser != null) {
                JOptionPane.showMessageDialog(null, "Login successful!");
                return loggedUser;
            } else {
                JOptionPane.showMessageDialog(null, "Email or password wrong");
                return null;
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            return null;
        }
    }
    
    public User checkStaff(User user) {
        if (!validateForm(user.getEmail(), "", user.getPassword(), false)) {
            return null;
        }

        try {
            User loggedUser = userDAO.findUserByCredentials(user.getEmail(), new String(user.getPassword()));
            return loggedUser; // Returns null if not found, no message shown
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            return null;
        }
    }
}