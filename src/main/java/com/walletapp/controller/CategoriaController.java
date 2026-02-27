package com.walletapp.controller;

import com.walletapp.dao.CategoriaDAO;
import com.walletapp.model.Categoria;
import com.walletapp.model.TipoTransaccion;
import com.walletapp.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

public class CategoriaController {

    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colTipo;
    @FXML private TableColumn<Categoria, String> colOrigen;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private int idUsuario;

    @FXML
    public void initialize() {
        idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo().name()));
        colOrigen.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getIdUsuario() == null ? "Predefinida" : "Personalizada"));
        cargarCategorias();
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.listarPorUsuario(idUsuario);
        tablaCategorias.setItems(FXCollections.observableArrayList(categorias));
    }

    @FXML
    private void nuevaCategoria() {
        Dialog<Categoria> dialog = new Dialog<>();
        dialog.setTitle("Nueva Categoría");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre de la categoría");
        ComboBox<TipoTransaccion> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(TipoTransaccion.values()));
        cmbTipo.setValue(TipoTransaccion.GASTO);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(cmbTipo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && !txtNombre.getText().trim().isEmpty()) {
                return new Categoria(0, txtNombre.getText().trim(), cmbTipo.getValue(), idUsuario);
            }
            return null;
        });

        Optional<Categoria> resultado = dialog.showAndWait();
        resultado.ifPresent(cat -> {
            categoriaDAO.insertar(cat);
            cargarCategorias();
        });
    }

    @FXML
    private void eliminarCategoria() {
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alerta("Seleccione una categoría para eliminar.");
            return;
        }
        if (seleccionada.getIdUsuario() == null) {
            alerta("No se pueden eliminar las categorías predefinidas del sistema.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("Se eliminará la categoría '" + seleccionada.getNombre() + "'.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            categoriaDAO.eliminar(seleccionada.getId());
            cargarCategorias();
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
