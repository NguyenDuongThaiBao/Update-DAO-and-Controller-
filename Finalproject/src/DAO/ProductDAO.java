package DAO;

import Model.Product;
import database.brotherconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM product";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("pro_id"),
                    rs.getString("pro_name"),
                    rs.getString("pro_type"),
                    rs.getDouble("pro_price"),
                    rs.getString("pro_status"),
                    rs.getString("pro_image"),
                    rs.getInt("pro_stock")
                );
                products.add(product);
            }
        }
        return products;
    }
    
    
    public boolean insertProduct(Product pro) throws SQLException {
        String query = "INSERT INTO product (pro_id, pro_name, pro_type, pro_price, pro_image, pro_stock, pro_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, pro.getId());
            ps.setString(2, pro.getName());
            ps.setString(3, pro.getType());
            ps.setDouble(4, pro.getPrice());
            ps.setString(5, pro.getImage());
            ps.setInt(6, pro.getStock());
            ps.setString(7, pro.getStatus());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    
    public boolean updateProduct(Product pro) throws SQLException {
        String query = "UPDATE product SET pro_name=?, pro_type=?, pro_price=?, pro_image=?, pro_stock=?, pro_status=? WHERE pro_id=?";
        
        try (Connection conn = brotherconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, pro.getName());
            ps.setString(2, pro.getType());
            ps.setDouble(3, pro.getPrice());
            ps.setString(4, pro.getImage());
            ps.setInt(5, pro.getStock());
            ps.setString(6, pro.getStatus());
            ps.setInt(7, pro.getId());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    
    public boolean updateProductStatus(int productId, String status) throws SQLException {
        String query = "UPDATE product SET pro_status = ? WHERE pro_id = ?";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, status);
            ps.setInt(2, productId);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    
    public boolean deleteProduct(int productId) throws SQLException {
        String query = "DELETE FROM product WHERE pro_id = ?";
        
        try (Connection conn = brotherconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    
    public Product getProductById(int productId) throws SQLException {
        String query = "SELECT * FROM product WHERE pro_id = ?";
        
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Product(
                    rs.getInt("pro_id"),
                    rs.getString("pro_name"),
                    rs.getString("pro_type"),
                    rs.getDouble("pro_price"),
                    rs.getString("pro_status"),
                    rs.getString("pro_image"),
                    rs.getInt("pro_stock")
                );
            }
        }
        return null;
    }
    
    public List<Product> getProductsByCategory(String category) throws SQLException {
    List<Product> products = new ArrayList<>();

    String sql = category.equalsIgnoreCase("All")
            ? "SELECT * FROM product"
            : "SELECT * FROM product WHERE pro_type = ?";

    try (Connection con = brotherconnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        if (!category.equalsIgnoreCase("All")) {
            ps.setString(1, category);
        }

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Product product = new Product(
                rs.getInt("pro_id"),
                rs.getString("pro_name"),
                rs.getString("pro_type"),
                rs.getDouble("pro_price"),
                rs.getString("pro_status"),
                rs.getString("pro_image"),
                rs.getInt("pro_stock")
            );
            products.add(product);
        }
    }

    return products;
    }

}
