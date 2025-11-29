package Controller;

import brothermanagement.Admistration;
import Model.Product;
import DAO.ProductDAO;  
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import javax.swing.*;
import java.io.File;
import UI.PlaceOrderPanel; 
import java.util.List;

public class AdminController {
    private JComboBox<String> productTypeList;
    private JComboBox<String> statusTypeList;
    private JTable table;
    private JLabel inventoryImageLabel;
    private JPanel placeorder_pane;
    private JPanel tab_menu;
    private String imagePath;
    
    // DAO instance
    private ProductDAO productDAO;
    
    public AdminController(){
        this.productDAO = new ProductDAO();
    };
    
    public AdminController(Admistration adminView, PlaceOrderPanel orderPanel) {
        this.productTypeList = adminView.getProductTypeList();
        this.statusTypeList = adminView.getStatusTypeList();
        this.inventoryImageLabel = adminView.getInventoryImageLabel();
        this.table = adminView.getTable();
        this.tab_menu = adminView.get_tabmenu();
        this.placeorder_pane = orderPanel.getPlaceorderpanel();
        this.productDAO = new ProductDAO();  // Initialize DAO
    }
    
    public JPanel getPanel(){
        return placeorder_pane;
    }
    
    public void loadProductTypes() {
        String[] types = {"Meal", "Drink", "Combo", "Dessert"};
        productTypeList.removeAllItems();
        for (String type : types) {
            productTypeList.addItem(type);
        }
    }
    
    public void loadStatusTypes() {
        String[] types = {"Available", "Unavailable"};
        statusTypeList.removeAllItems();
        for (String type : types) {
            statusTypeList.addItem(type);
        }
    }
    
    public void loadProductsFromDatabase() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        try {
            List<Product> products = productDAO.getAllProducts();  // Use DAO
            
            for (Product product : products) {
                String status = product.getStatus();
                
                // Business logic: Auto-update status if out of stock
                if (product.getStock() <= 0 && !"Unavailable".equalsIgnoreCase(status)) {
                    status = "Unavailable";
                    product.setStatus(status);
                    productDAO.updateProductStatus(product.getId(), status);  // Use DAO
                }
                
                model.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getType(),
                    String.format("%,.0f VND", product.getPrice()),
                    product.getStock(),
                    status,
                    product.getImage()
                });
            }
            
            model.fireTableDataChanged();
            table.revalidate();
            table.repaint();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading products: " + e.getMessage());
        }
    }
    
    public Product addProduct(Product pro){
        // Validation logic
        if (pro == null) {
            JOptionPane.showMessageDialog(null, "Invalid product data!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (pro.getName() == null || pro.getName().trim().isEmpty() ||
            pro.getType() == null || pro.getType().trim().isEmpty() ||
            pro.getStatus() == null || pro.getStatus().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all required fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // Business logic
        if (pro.getStock() <= 0) {
            pro.setStatus("Unavailable");
        }
        
        try {
            boolean success = productDAO.insertProduct(pro);  // Use DAO
            
            if (success) {
                JOptionPane.showMessageDialog(null, "Product added successfully!");
                loadProductsFromDatabase();
                return pro;
            }
            
        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(null, "Product ID or product's name already exists!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }

        return null;
    }
    
    public Product updateProduct(Product pro){
        // Validation logic
        if (pro == null) {
            JOptionPane.showMessageDialog(null, "Invalid product data!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (pro.getName() == null || pro.getName().trim().isEmpty() ||
            pro.getType() == null || pro.getType().trim().isEmpty() ||
            pro.getStatus() == null || pro.getStatus().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all required fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // Business logic
        if (pro.getStock() <= 0) {
            System.out.println(pro.getName() + " is out of stock");
            pro.setStatus("Unavailable");
        }
        
        try {
            boolean success = productDAO.updateProduct(pro);  // Use DAO
            
            if (success) {
                JOptionPane.showMessageDialog(null, "Product updated successfully!");
                loadProductsFromDatabase();
            } else {
                JOptionPane.showMessageDialog(null, "No product found with ID " + pro.getId());
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }

        return pro;
    } 
    
    public void deleteProduct(JTable table){
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a product to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to delete this product?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int productId = (int) model.getValueAt(selectedRow, 0);
            
            try {
                boolean deleted = productDAO.deleteProduct(productId);  // Use DAO
                
                if (deleted) {
                    model.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(null, "Product deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to delete product!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLIntegrityConstraintViolationException e) {
                JOptionPane.showMessageDialog(null, 
                    "Cannot delete this product because it already exists in the invoice history\n",
                    "Cannot delete", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            }
        }
    }
    
    public void importImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Image select");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "png", "jpg", "jpeg"));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            imagePath = file.getAbsolutePath();

            ImageIcon icon = new ImageIcon(new ImageIcon(imagePath)
                    .getImage().getScaledInstance(195, 175, java.awt.Image.SCALE_SMOOTH));

            if (inventoryImageLabel != null) {
                inventoryImageLabel.setIcon(icon);
                inventoryImageLabel.repaint();
            } else {
                JOptionPane.showMessageDialog(null, "Label not found!");
            }
        }
    }

    public String getImagePath() {
        return imagePath;
    }
}
