package com.walletapp.controller;

import com.walletapp.dao.CuentaDAO;
import com.walletapp.model.Cuenta;
import com.walletapp.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class CuentaController {

    @FXML private TableView<Cuenta> tablaCuentas;
    @FXML private TableColumn<Cuenta, String> colNombre;
    @FXML private TableColumn<Cuenta, String> colSaldo;

    private final CuentaDAO cuentaDAO = new CuentaDAO();
    private int idUsuario;

    @FXML
    public void initialize() {
        idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colSaldo.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f EUR", data.getValue().getSaldo())));
        cargarCuentas();
    }

    private void cargarCuentas() {
        List<Cuenta> cuentas = cuentaDAO.listarPorUsuario(idUsuario);
        tablaCuentas.setItems(FXCollections.observableArrayList(cuentas));
    }

    @FXML
    private void nuevaCuenta() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Cuenta");
        dialog.setHeaderText("Crear nueva cuenta bancaria");
        dialog.setContentText("Nombre de la cuenta:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(nombre -> {
            if (!nombre.trim().isEmpty()) {
                Cuenta cuenta = new Cuenta(0, nombre.trim(), 0.0, idUsuario);
                cuentaDAO.insertar(cuenta);
                cargarCuentas();
            }
        });
    }

    @FXML
    private void editarCuenta() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alerta("Seleccione una cuenta para editar.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(seleccionada.getNombre());
        dialog.setTitle("Editar Cuenta");
        dialog.setHeaderText("Modificar nombre de la cuenta");
        dialog.setContentText("Nombre:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(nombre -> {
            if (!nombre.trim().isEmpty()) {
                seleccionada.setNombre(nombre.trim());
                cuentaDAO.actualizar(seleccionada);
                cargarCuentas();
            }
        });
    }

    @FXML
    private void eliminarCuenta() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alerta("Seleccione una cuenta para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("Se eliminará la cuenta '" + seleccionada.getNombre() +
            "' y todas sus transacciones asociadas. Esta acción no se puede deshacer.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            cuentaDAO.eliminar(seleccionada.getId());
            cargarCuentas();
        }
    }

    private void alerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
