/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.User;
import database.brotherconnection;
import java.sql.*;

/**
 *
 * @author thaibao
 */
public class UserDAO {
    
    public boolean isEmailTaken(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM login WHERE email = ?";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    
    public boolean updateUserWithoutPassword(String oldEmail, String newUsername, String newEmail) throws SQLException {
        String query = "UPDATE login SET username = ?, email = ? WHERE email = ?";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, newUsername);
            ps.setString(2, newEmail);
            ps.setString(3, oldEmail);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    
    public boolean updateUserWithPassword(String oldEmail, String newUsername, String newEmail, String newPassword) throws SQLException {
        String query = "UPDATE login SET username = ?, email = ?, password = ? WHERE email = ?";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, newUsername);
            ps.setString(2, newEmail);
            ps.setString(3, newPassword);
            ps.setString(4, oldEmail);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    
    public boolean insertUser(User user) throws SQLException {
        String query = "INSERT INTO login (email, username, password) VALUES (?, ?, ?)";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getUsername());
            ps.setString(3, new String(user.getPassword()));
            ps.executeUpdate();
            
            return true;
        }
    }
    
    
    public User findUserByCredentials(String email, String password) throws SQLException {
        String query = "SELECT * FROM login WHERE email = ? AND password = ?";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, email);
            ps.setString(2, password);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                return user;
            }
        }
        return null;
    }
}
