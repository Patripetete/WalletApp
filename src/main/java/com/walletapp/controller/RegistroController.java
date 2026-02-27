package com.walletapp.controller;

import com.walletapp.App;
import com.walletapp.dao.UsuarioDAO;
import com.walletapp.model.Usuario;
import com.walletapp.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblError;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void registrar() {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() ||
            username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            lblError.setText("Por favor, rellene todos los campos.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            lblError.setText("Las contraseñas no coinciden.");
            return;
        }

        if (password.length() < 4) {
            lblError.setText("La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        if (usuarioDAO.existeEmail(email)) {
            lblError.setText("El email ya está registrado.");
            return;
        }

        if (usuarioDAO.existeUsername(username)) {
            lblError.setText("El nombre de usuario ya existe.");
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(email);
        usuario.setUsername(username);
        usuario.setPasswordHash(PasswordUtil.hashPassword(password));

        if (usuarioDAO.insertar(usuario)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registro exitoso");
            alert.setHeaderText(null);
            alert.setContentText("Cuenta creada correctamente. Ya puede iniciar sesión.");
            alert.showAndWait();
            volverALogin();
        } else {
            lblError.setText("Error al crear la cuenta. Inténtelo de nuevo.");
        }
    }

    @FXML
    private void volverALogin() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            lblError.setText("Error al volver al login.");
        }
    }
}
