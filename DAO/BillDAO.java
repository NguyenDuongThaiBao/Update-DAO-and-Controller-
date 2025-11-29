/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.Bill;
import Model.BillItem;
import database.brotherconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ThinkBook
 */
public class BillDAO {
     public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();

        try {
            Connection con = brotherconnection.getConnection();
            String sql = "SELECT * FROM bill";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Bill bill = new Bill(
                    rs.getInt("bill_id"),
                    rs.getTimestamp("bill_date"),
                    rs.getInt("total_amount")
                );
                list.add(bill);
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
     
     public int insertBill(double total) throws SQLException {
        String sql = "INSERT INTO bill (bill_date, total_amount) VALUES (NOW(), ?)";
        try (Connection conn = brotherconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, total);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to get bill id");
        }
    }

    public void insertBillItems(Connection conn, int billId, List<BillItem> items) throws SQLException {
        String sql = "INSERT INTO bill_items (bill_id, product_id, product_name, quantity, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (BillItem item : items) {
                ps.setInt(1, billId);
                ps.setInt(2, item.getProductId());
                ps.setString(3, item.getProductName());
                ps.setInt(4, item.getQuantity());
                ps.setDouble(5, item.getSubTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void updateStocksAndStatus(Connection conn, List<BillItem> items) throws SQLException {
        String sqlStock = "UPDATE product SET pro_stock = pro_stock - ? WHERE pro_id = ?";
        String sqlStatus = "UPDATE product SET pro_status = 'Unavailable' WHERE pro_id = ? AND pro_stock <= 0";

        try (PreparedStatement psStock = conn.prepareStatement(sqlStock);
             PreparedStatement psStatus = conn.prepareStatement(sqlStatus)) {

            for (BillItem item : items) {
                psStock.setInt(1, item.getQuantity());
                psStock.setInt(2, item.getProductId());
                psStock.addBatch();

                psStatus.setInt(1, item.getProductId());
                psStatus.addBatch();
            }

            psStock.executeBatch();
            psStatus.executeBatch();
        }
    }
    public void deleteBill(int billId) throws SQLException {
    String sql = "DELETE FROM bill WHERE bill_id = ?";
    
    try (Connection conn = brotherconnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setInt(1, billId);
        ps.executeUpdate();
    }
    
}

}
