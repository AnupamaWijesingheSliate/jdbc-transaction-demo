package controller;

import com.jfoenix.controls.JFXTextField;
import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.sql.*;

public class ManageEmployeeController {
    public AnchorPane root;
    public TableView tblEmployee;
    public JFXTextField txtId;
    public JFXTextField txtName;
    public JFXTextField txtSalary;
    public JFXTextField txtETF;

    public void initialize(){
        txtId.setEditable(false);
        txtETF.setEditable(false);
    }

    public void btnNewEmployee_OnAction(ActionEvent actionEvent) {
        try {
            generateNewEmployeeID();
            txtName.clear();
            txtSalary.clear();
            txtName.requestFocus();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void generateNewEmployeeID() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        Statement stm = connection.createStatement();
        ResultSet rst = stm.executeQuery("SELECT * FROM Employee ORDER BY id DESC LIMIT 1");
        String newEmployeeId = "E001";
        if (rst.next()) {
            String lastEmployeeId = rst.getString(1);
            newEmployeeId = "E" + (Integer.parseInt(lastEmployeeId.substring(1)) + 1);
        }
        txtId.setText(newEmployeeId);
        connection.close();
    }

    public void txtId(ActionEvent actionEvent) {
    }

    public void txtName(ActionEvent actionEvent) {
    }

    public void txtSalary(ActionEvent actionEvent) {
    }

    public void txtETF(ActionEvent actionEvent) {
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
    }
}
