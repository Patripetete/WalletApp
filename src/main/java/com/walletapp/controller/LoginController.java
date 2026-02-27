package com.walletapp.controller;

import com.walletapp.App;
import com.walletapp.dao.UsuarioDAO;
import com.walletapp.model.Usuario;
import com.walletapp.util.PasswordUtil;
import com.walletapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void login() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor, rellene todos los campos.");
            return;
        }

        Usuario usuario = usuarioDAO.buscarPorUsername(username);
        if (usuario == null || !PasswordUtil.checkPassword(password, usuario.getPasswordHash())) {
            lblError.setText("Credenciales incorrectas.");
            return;
        }

        SessionManager.getInstance().setUsuarioActual(usuario);
        try {
            App.setRoot("dashboard");
        } catch (IOException e) {
            lblError.setText("Error al cargar el dashboard.");
        }
    }

    @FXML
    private void irARegistro() {
        try {
            App.setRoot("registro");
        } catch (IOException e) {
            lblError.setText("Error al cargar el registro.");
        }
    }
}
