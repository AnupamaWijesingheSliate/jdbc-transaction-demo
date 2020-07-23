package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import db.DBConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import util.EmployeeTM;

import java.math.BigDecimal;
import java.sql.*;

public class ManageEmployeeController {
    public AnchorPane root;
    public TableView<EmployeeTM> tblEmployee;
    public JFXTextField txtId;
    public JFXTextField txtName;
    public JFXTextField txtSalary;
    public JFXTextField txtETF;
    public JFXButton btnNewEmployee;
    public JFXButton btnSave;

    public void initialize() {
        txtId.setEditable(false);
        txtETF.setEditable(false);
        reset(true, true);
        btnSave.setDisable(true);

        tblEmployee.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblEmployee.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblEmployee.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("salary"));
        tblEmployee.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("etf"));
        tblEmployee.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("btnDelete"));
        tblEmployee.getColumns().get(4).setStyle("-fx-alignment: center");

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

        tblEmployee.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EmployeeTM>() {

            @Override
            public void changed(ObservableValue<? extends EmployeeTM> observable, EmployeeTM oldValue, EmployeeTM emp) {
                if (emp == null) {
                    btnSave.setText("Save");
                    return;
                }

                btnSave.setDisable(false); // In case if it has been already disabled
                btnSave.setText("Update");
                txtId.setDisable(false);
                txtName.setDisable(false);
                txtSalary.setDisable(false);
                txtName.setEditable(false);
                txtSalary.setEditable(false);

                txtId.setText(emp.getId());
                txtName.setText(emp.getName());
                txtSalary.setText(emp.getSalary().toPlainString());
                txtETF.setText(emp.getEtf().toPlainString());
            }
        });
    }

    private void loadAllEmployees() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        Statement stm = connection.createStatement();
        ResultSet rst = stm.executeQuery("SELECT E.id, E.name, ES.salary, EE.eft FROM Employee E INNER JOIN Employee_Salary ES on E.id = ES.id\n" +
                "INNER JOIN Employee_ETF EE on E.id = EE.id");

        while (rst.next()) {
            Button btnDelete = new Button("Delete");
            EmployeeTM emp = new EmployeeTM(rst.getString(1),
                    rst.getString(2),
                    rst.getBigDecimal(3),
                    rst.getBigDecimal(4),
                    btnDelete);
            btnDelete.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    tblEmployee.getItems().remove(emp);
                    reset(true, true);
                    btnSave.setDisable(true);
                    // Todo: Delete an employee
                }
            });
            tblEmployee.getItems().add(emp);
        }
    }

    private void reset(boolean clearId, boolean focusToNewEmployee) {
        btnSave.setDisable(false);
        txtName.setDisable(false);
        txtName.setEditable(true);
        txtSalary.setDisable(false);
        txtSalary.setEditable(true);
        if (clearId) {
            txtId.clear();
            txtName.setDisable(true);
            txtSalary.setDisable(true);
        }
        txtName.clear();
        txtSalary.clear();
        txtETF.clear();
        tblEmployee.getSelectionModel().clearSelection();
        btnSave.setText("Save");
        txtSalary.setPromptText("Employee Salary"); // In case if it has changed to "Invalid salary"
        if (focusToNewEmployee) {
            btnNewEmployee.requestFocus();
        }
    }

    public void btnNewEmployee_OnAction(ActionEvent actionEvent) {
        try {
            generateNewEmployeeID();
            reset(false, false);
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

            /* If you are curious about this line, just go and check this link
             * https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html
             * Do you self-study part here, because I won't be behind you always.
             * */
            newEmployeeId = String.format("E%03d", (Integer.parseInt(lastEmployeeId.substring(1)) + 1));
        }
        txtId.setText(newEmployeeId);
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        if (!validate()) {
            return;
        }

        BigDecimal salary = new BigDecimal(txtSalary.getText());
        BigDecimal etf = new BigDecimal(txtETF.getText().substring(0, txtETF.getText().length() - 1));
        if (btnSave.getText().equals("Save")) {

            /* Let's save */
            Connection connection = DBConnection.getInstance().getConnection();

            try {

                /* Let's start the transaction */
                connection.setAutoCommit(false);

                /* Saving the Employee */
                PreparedStatement pstm = connection.prepareStatement("INSERT INTO Employee VALUES (?,?)");
                pstm.setObject(1, txtId.getText());
                pstm.setObject(2, txtName.getText());
                int affectedRows = pstm.executeUpdate();
                if (affectedRows == 0) {

                    /* For some weird reason if it doesn't work, let's remove everything from the buffer */
                    connection.rollback();
                    return;
                }

                /* Play with these */
//                if (true) {
//                    throw new Error("Pissu hadei");
//                }
//                if (true) {
//                    throw new Exception("Pissu hadei");
//                }

                /* Saving the Employee's Salary */
                pstm = connection.prepareStatement("INSERT INTO Employee_Salary VALUES (?,?)");
                pstm.setObject(1, txtId.getText());
                pstm.setObject(2, salary);
                affectedRows = pstm.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return;
                }

                /* Saving the Employee'e ETF */
                pstm = connection.prepareStatement("INSERT INTO Employee_ETF VALUES (?,?)");
                pstm.setObject(1, txtId.getText());
                pstm.setObject(2, etf);
                affectedRows = pstm.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return;
                }

                /* If everything works perfectly, time to commit everything */
                connection.commit();

                /* This is not a good practice, but I am too lazy you know */
                loadAllEmployees();
            } catch (Throwable ex) {

                /* Try to find answer for the following questions?
                 * Why have we used Throwable here?
                 * What is the purpose of this catch block? */

                /* Don't forget to add the printStackTrace() method, it is really useful in
                 * the development mode */
                ex.printStackTrace();
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            } finally {
                try {
                    /* Time to reset the auto commit mode to it's default state */
                    connection.setAutoCommit(true);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }

        } else {
            // Todo: Update an employee details
        }
    }

    private boolean validate() {
        if (!txtName.getText().matches("[A-Za-z ]+")) {
            new Alert(Alert.AlertType.ERROR, "Invalid employee name", ButtonType.OK).show();
            txtName.requestFocus();
            return false;
        } else if (!txtSalary.getText().matches("[0-9]+[.]?[0-9]*")) {
            new Alert(Alert.AlertType.ERROR, "Invalid employee salary", ButtonType.OK).show();
            txtSalary.requestFocus();
            return false;
        }
        return true;
    }

}
