package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import db.DBConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import util.EmployeeTM;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ManageEmployeeController {
    public AnchorPane root;
    public TableView<EmployeeTM> tblEmployee;
    public JFXTextField txtId;
    public JFXTextField txtName;
    public JFXTextField txtSalary;
    public JFXTextField txtETF;
    public JFXButton btnNewEmployee;

    public void initialize() {
        txtId.setEditable(false);
        txtETF.setEditable(false);
        reset(true, true);

        tblEmployee.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblEmployee.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblEmployee.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("salary"));
        tblEmployee.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("etf"));
        tblEmployee.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("btnDelete"));

        try {
            loadAllEmployees();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        txtSalary.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.matches("[0-9]+[.]?[0-9]*")) {
                    txtETF.setText(new BigDecimal(newValue).multiply(new BigDecimal(10)).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN).toPlainString() + "%");
                    txtSalary.setPromptText("Employee Salary");
                } else {
                    txtETF.clear();
                    if (newValue.length() > 0) {
                        txtSalary.setPromptText("Invalid Salary");
                    }
                }
            }
        });
    }

    private void loadAllEmployees() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        Statement stm = connection.createStatement();
        ResultSet rst = stm.executeQuery("SELECT E.id, E.name, ES.salary, EE.eft FROM Employee E INNER JOIN Employee_Salary ES on E.id = ES.id\n" +
                "INNER JOIN Employee_ETF EE on E.id = EE.id");

        while (rst.next()){
            Button btnDelete = new Button("DEL");
            tblEmployee.getItems().add(new EmployeeTM(rst.getString(1),
                    rst.getString(2),
                    rst.getBigDecimal(3),
                    rst.getBigDecimal(4),
                    btnDelete));
        }
    }

    private void reset(boolean clearId, boolean focusToNewEmployee) {
        txtName.setDisable(false);
        txtSalary.setDisable(false);
        if (clearId) {
            txtId.clear();
            txtName.setDisable(true);
            txtSalary.setDisable(true);
        }
        txtName.clear();
        txtSalary.clear();
        txtETF.clear();
        tblEmployee.getSelectionModel().clearSelection();
        if (focusToNewEmployee) {
            btnNewEmployee.requestFocus();
        }
    }

    public void btnNewEmployee_OnAction(ActionEvent actionEvent) {
        try {
            generateNewEmployeeID();
            reset(false, false);
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
            newEmployeeId = String.format("E%03d", (Integer.parseInt(lastEmployeeId.substring(1)) + 1));
        }
        txtId.setText(newEmployeeId);
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
    }
}
