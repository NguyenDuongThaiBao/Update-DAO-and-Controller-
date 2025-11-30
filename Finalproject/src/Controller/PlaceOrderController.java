/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;


import DAO.BillDAO;
import DAO.ProductDAO;
import Model.BillItem;
import Model.Product;
import UI.BillFrame;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import UI.PlaceOrderPanel;
import UI.ProductItemPanel;
import database.brotherconnection;
import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ThinkBook
 */
public class PlaceOrderController {
    private PlaceOrderPanel take_btn_panel;
    private JPanel productContainer;
    private JScrollPane scrollPane;
    private JPanel placeorder_pane;
    private JButton all_btn;
    private AdminController adminController;
    
    private int batchSize = 12;
    private int loadedCount = 0;
    private ArrayList<Product> list = new ArrayList<>();

    private JTable place_table; 
    private JTextField Total; 
    private PlaceOrderPanel orderPanel; 
    
    private ProductDAO productDAO;
    private BillDAO billDAO;
    
    public PlaceOrderController(PlaceOrderPanel panel) {
        this.take_btn_panel = panel;
        this.productContainer = panel.getProductContainer();
        this.productContainer = panel.getProductContainer();
        this.scrollPane = panel.getScrollPane();
        this.placeorder_pane = panel.getPlaceorderpanel();
        this.all_btn = panel.getAllbutton();
        
        this.orderPanel = panel;
        this.place_table = orderPanel.getPlaceTable();
        this.Total = orderPanel.getTotalTextField();
        
        this.productDAO = new ProductDAO();
        this.billDAO = new BillDAO();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Number) {
                    value = currencyFormat.format(value);
                }
                super.setValue(value);
            }
        };
    
   
        currencyRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

    
        try {
            place_table.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
            place_table.getColumnModel().getColumn(5).setCellRenderer(currencyRenderer);
        } catch (Exception e) {
            System.out.println("Error");
        }

        setupDeleteButton();
        setupResetButton();
    }
    
    public void setAdminController(AdminController adminCtrl) {
        this.adminController = adminCtrl;
    }
    
    public void click_default(){
        List<Product> prods = getProductsByCategory("All");
        highlightButtonDefault();
        showProducts(prods);
    }
    
    public List<JButton> getButtonsInPanel(JPanel panel) {
        List<JButton> list = new ArrayList<>();
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) {
                list.add((JButton) comp);
            }
        }
        return list;
    }
    
    
    
    public void registerButtonEvents() {
        List<JButton> btns = getButtonsInPanel(take_btn_panel.getButtonPanel());

        for (JButton b : btns) {
            b.addActionListener(e -> {
                highlightSelectedButton(b, btns);
                List<Product> prods = getProductsByCategory(b.getText());
                showProducts(prods);
            });
        }
    }

    
    private void highlightButtonDefault(){
        List<JButton> btns = getButtonsInPanel(take_btn_panel.getButtonPanel());
        for (JButton b : btns) {
            if (b == all_btn) {
                b.setBackground(new Color(204, 0, 0));
                b.setForeground(new Color(204,204,204));
            } else {
                b.setBackground(new Color(255, 51, 51));
                b.setForeground(Color.WHITE);
            }
        }
    }
    
    private void highlightSelectedButton(JButton selected, List<JButton> all) {
        
        for (JButton b : all) {
            if (b == selected) {
                b.setBackground(new Color(204, 0, 0));
                b.setForeground(new Color(204,204,204));
            } else {
                b.setBackground(new Color(255, 51, 51));
                b.setForeground(new Color(255,255,255));
            }
        }
    }
    
    public List<Product> getProductsByCategory(String category) {
    try {
        return productDAO.getProductsByCategory(category);
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        return new ArrayList<>();
    }
}
        
    public void showProducts() {
    productContainer.removeAll();
    loadedCount = 0;

    try {
        List<Product> products = productDAO.getAllProducts();

        for (Product product : products) {
            ProductItemPanel productPanel = new ProductItemPanel(product);
            productPanel.setAdminController(this);
            productContainer.add(productPanel.getitems_panel());
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
    }

    productContainer.revalidate();
    productContainer.repaint();
}

    
    public void showProducts(List<Product> products) {
        productContainer.removeAll();

        for (Product p : products) {
            ProductItemPanel item = new ProductItemPanel(p);
            item.setAdminController(this);
            productContainer.add(item.getitems_panel());
        }

        productContainer.revalidate();
        productContainer.repaint();
    }
    
    public void loadMoreProducts(JPanel productContainer) {
        int end = Math.min(loadedCount + batchSize, list.size());

        for (int i = loadedCount; i < end; i++) {
            ProductItemPanel item = new ProductItemPanel(list.get(i));
            item.setAdminController(this);
            productContainer.add(item);
        }

        loadedCount = end;
        productContainer.revalidate();
        productContainer.repaint();
    }

    
    public void enableAutoLoad() {
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            int extent = bar.getModel().getExtent();
            int value = bar.getValue();
            int max = bar.getMaximum();

            if (value + extent >= max - 50) {
                loadMoreProducts(productContainer);
            }
        });
    }
    
    public void addProductToTable(int productId, int quantity) {
    System.out.print("add product");
    
    try {
        
        Product product = productDAO.getProductById(productId);
        
        if (product == null) {
            JOptionPane.showMessageDialog(null, "Product not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String name = product.getName();
        String type = product.getType();
        double price = product.getPrice();
        int stock = product.getStock();

        if (quantity > stock) {
            JOptionPane.showMessageDialog(null, 
                "Not enough stock for product: " + name + "\nAvailable: " + stock, 
                "Stock Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) place_table.getModel();
        boolean found = false;

        for (int i = 0; i < model.getRowCount(); i++) {
            int currentId = Integer.parseInt(model.getValueAt(i, 0).toString());
            
            if (currentId == productId) {
                int currentQty = Integer.parseInt(model.getValueAt(i, 3).toString());
                int newQty = currentQty + quantity;

                if (newQty > stock) {
                    JOptionPane.showMessageDialog(null, 
                        "Some products is out of stock",
                        "Out of stock (OOS)",
                        JOptionPane.WARNING_MESSAGE);
                    return; 
                }
            }
        }
        
        for (int i = 0; i < model.getRowCount(); i++) {
            int currentId = Integer.parseInt(model.getValueAt(i, 0).toString());
            if (currentId == productId) {
                int currentQty = Integer.parseInt(model.getValueAt(i, 3).toString());
                int newQty = currentQty + quantity;
                double newSubtotal = newQty * price;

                model.setValueAt(newQty, i, 3);
                model.setValueAt(newSubtotal, i, 5);
                found = true;
                break;
            }
        }

        if (!found) {
            double subtotal = price * quantity;
            model.addRow(new Object[]{
                productId,
                name,
                type,
                quantity,
                price,
                subtotal
            });
        }

        updateTotal();

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error adding product: " + e.getMessage());
    }
    }

    
    private void updateTotal() {
        DefaultTableModel model = (DefaultTableModel) place_table.getModel();
        double total = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object obj = model.getValueAt(i, 5); // subtotal
            double subtotal = obj instanceof Number ? ((Number) obj).doubleValue() : 0;
            total += subtotal;
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        Total.setText(currencyFormat.format(total));
    }

    private void setupDeleteButton() {
        if (orderPanel != null && orderPanel.getDeleteButton() != null) {
            orderPanel.getDeleteButton().addActionListener(e -> deleteSelectedRow());
        }
    }
    
        
        private void setupResetButton() {
        if (orderPanel != null && orderPanel.getResetButton() != null) {
            orderPanel.getResetButton().addActionListener(e -> ResetTable());
        }
    }

    
    private void deleteSelectedRow() {
        JTable placeTable = orderPanel.getPlaceTable();
        DefaultTableModel model = (DefaultTableModel) placeTable.getModel();
        int selectedRow = placeTable.getSelectedRow();

        if (selectedRow >= 0) {
            model.removeRow(selectedRow);   
            updateTotal();                  
        }     
    }
    
    private void ResetTable(){
        JTable placeTable = orderPanel.getPlaceTable();
        DefaultTableModel model = (DefaultTableModel) placeTable.getModel();
        
        model.setRowCount(0); 
        updateTotal();  
    }

    
    public void handleOrderAndPrint() {
        DefaultTableModel model = (DefaultTableModel) place_table.getModel();

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "No items in the order!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("************ Brother Chicken ***********\n");
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("%-20s %-8s %s\n", "Item", "Quantity", "Price"));
        receipt.append("----------------------------------------\n");

        double total = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            String item = model.getValueAt(i, 1).toString();  // Name
            int qty = (int) model.getValueAt(i, 3);           // Quantity
            double subtotal = (double) model.getValueAt(i, 5); // Subtotal as double

            receipt.append(String.format("%-20s %-8d %,.0f\n", item, qty, subtotal));
            total += subtotal;
        }

        receipt.append("----------------------------------------\n");
        receipt.append(String.format("TOTAL: %,.0f VND\n", total));
        receipt.append("----------------------------------------\n");
        receipt.append("Thanks for choosing us!\n");

        new BillFrame(receipt.toString(), orderPanel) {{
            setLocationRelativeTo(null);
            setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            setVisible(true);
        }};
    }

    public void saveBillToDatabase(DefaultTableModel model, double total) {
    Connection conn = null;

    try {
        java.util.List<BillItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            int productId = Integer.parseInt(model.getValueAt(i, 0).toString());
            String productName = model.getValueAt(i, 1).toString();
            int qty = Integer.parseInt(model.getValueAt(i, 3).toString());
            double subtotal = Double.parseDouble(model.getValueAt(i, 5).toString());

            items.add(new BillItem(0, productId, productName, qty, subtotal));
        }

        
        conn = brotherconnection.getConnection();
        conn.setAutoCommit(false);

        
        int billId = billDAO.insertBill(total);

        
        billDAO.insertBillItems(conn, billId, items);
        billDAO.updateStocksAndStatus(conn, items);

        
        conn.commit();

    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
        JOptionPane.showMessageDialog(null, "Error saving bill: " + e.getMessage());
    } finally {
        if (conn != null) {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    model.setRowCount(0);
    updateTotal();

    if (this.adminController != null) {
        this.adminController.loadProductsFromDatabase();
    }
    click_default();
}
}

    

