package com.walletapp.controller;

import com.walletapp.dao.*;
import com.walletapp.model.*;
import com.walletapp.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TransaccionController {

    @FXML private TableView<Transaccion> tablaTransacciones;
    @FXML private TableColumn<Transaccion, String> colFecha;
    @FXML private TableColumn<Transaccion, String> colDescripcion;
    @FXML private TableColumn<Transaccion, String> colCategoria;
    @FXML private TableColumn<Transaccion, String> colTipo;
    @FXML private TableColumn<Transaccion, String> colImporte;
    @FXML private TableColumn<Transaccion, String> colCuenta;

    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cmbTipoFiltro;
    @FXML private ComboBox<Categoria> cmbCategoriaFiltro;
    @FXML private ComboBox<Cuenta> cmbCuentaFiltro;

    private final TransaccionDAO transaccionDAO = new TransaccionDAO();
    private final CuentaDAO cuentaDAO = new CuentaDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final DateTimeFormatter fmtDisplay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int idUsuario;

    @FXML
    public void initialize() {
        idUsuario = SessionManager.getInstance().getUsuarioActual().getId();

        colFecha.setCellValueFactory(data -> {
            String fecha = data.getValue().getFecha();
            try {
                return new SimpleStringProperty(LocalDate.parse(fecha).format(fmtDisplay));
            } catch (Exception e) {
                return new SimpleStringProperty(fecha);
            }
        });
        colDescripcion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCategoria()));
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo().name()));
        colImporte.setCellValueFactory(data -> {
            Transaccion t = data.getValue();
            String signo = t.getTipo() == TipoTransaccion.INGRESO ? "+" : "-";
            return new SimpleStringProperty(signo + String.format("%.2f EUR", t.getImporte()));
        });
        colCuenta.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCuenta()));

        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle("INGRESO".equals(item) ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
                             "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            }
        });
        colImporte.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle(item.startsWith("+") ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
                             "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            }
        });

        cmbTipoFiltro.setItems(FXCollections.observableArrayList("Todos", "INGRESO", "GASTO"));
        cmbTipoFiltro.setValue("Todos");

        List<Categoria> categorias = categoriaDAO.listarPorUsuario(idUsuario);
        cmbCategoriaFiltro.getItems().add(null);
        cmbCategoriaFiltro.getItems().addAll(categorias);
        cmbCategoriaFiltro.setPromptText("Todas");
        cmbCategoriaFiltro.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "Todas" : item.getNombre());
            }
        });

        List<Cuenta> cuentas = cuentaDAO.listarPorUsuario(idUsuario);
        cmbCuentaFiltro.getItems().add(null);
        cmbCuentaFiltro.getItems().addAll(cuentas);
        cmbCuentaFiltro.setPromptText("Todas");
        cmbCuentaFiltro.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cuenta item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "Todas" : item.getNombre());
            }
        });

        cargarTransacciones();
    }

    private void cargarTransacciones() {
        List<Transaccion> lista = transaccionDAO.listarPorUsuario(idUsuario);
        tablaTransacciones.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    private void filtrar() {
        String desde = dpFechaDesde.getValue() != null ? dpFechaDesde.getValue().toString() : null;
        String hasta = dpFechaHasta.getValue() != null ? dpFechaHasta.getValue().toString() : null;
        String tipoStr = cmbTipoFiltro.getValue();
        TipoTransaccion tipo = "Todos".equals(tipoStr) ? null : TipoTransaccion.valueOf(tipoStr);
        Categoria cat = cmbCategoriaFiltro.getValue();
        Integer idCat = cat != null ? cat.getId() : null;
        Cuenta cta = cmbCuentaFiltro.getValue();
        Integer idCta = cta != null ? cta.getId() : null;

        List<Transaccion> lista = transaccionDAO.listarFiltradas(idUsuario, desde, hasta, tipo, idCat, idCta);
        tablaTransacciones.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    private void limpiarFiltros() {
        dpFechaDesde.setValue(null);
        dpFechaHasta.setValue(null);
        cmbTipoFiltro.setValue("Todos");
        cmbCategoriaFiltro.setValue(null);
        cmbCuentaFiltro.setValue(null);
        cargarTransacciones();
    }

    @FXML
    private void nuevaTransaccion() {
        mostrarDialogoTransaccion(null);
    }

    @FXML
    private void editarTransaccion() {
        Transaccion seleccionada = tablaTransacciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alerta("Seleccione una transacción para editar.");
            return;
        }
        mostrarDialogoTransaccion(seleccionada);
    }

    @FXML
    private void eliminarTransaccion() {
        Transaccion seleccionada = tablaTransacciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alerta("Seleccione una transacción para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("Se eliminará la transacción '" + seleccionada.getDescripcion() + "'. El saldo se revertirá.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Cuenta cuenta = cuentaDAO.obtenerPorId(seleccionada.getIdCuenta());
            if (cuenta != null) {
                double nuevoSaldo = seleccionada.getTipo() == TipoTransaccion.INGRESO ?
                    cuenta.getSaldo() - seleccionada.getImporte() :
                    cuenta.getSaldo() + seleccionada.getImporte();
                cuentaDAO.actualizarSaldo(cuenta.getId(), nuevoSaldo);
            }
            transaccionDAO.eliminar(seleccionada.getId());
            cargarTransacciones();
        }
    }

    private void mostrarDialogoTransaccion(Transaccion existente) {
        List<Cuenta> cuentas = cuentaDAO.listarPorUsuario(idUsuario);
        if (cuentas.isEmpty()) {
            alerta("Debe crear al menos una cuenta antes de registrar transacciones.");
            return;
        }

        Dialog<Transaccion> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nueva Transacción" : "Editar Transacción");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<TipoTransaccion> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(TipoTransaccion.values()));
        TextField txtDescripcion = new TextField();
        txtDescripcion.setPromptText("Descripción");
        TextField txtImporte = new TextField();
        txtImporte.setPromptText("Importe");
        DatePicker dpFecha = new DatePicker(LocalDate.now());
        ComboBox<Cuenta> cmbCuenta = new ComboBox<>(FXCollections.observableArrayList(cuentas));
        ComboBox<Categoria> cmbCategoria = new ComboBox<>();

        cmbTipo.setValue(TipoTransaccion.GASTO);
        actualizarCategorias(cmbCategoria, cmbTipo.getValue());

        cmbTipo.setOnAction(e -> actualizarCategorias(cmbCategoria, cmbTipo.getValue()));

        if (existente != null) {
            cmbTipo.setValue(existente.getTipo());
            txtDescripcion.setText(existente.getDescripcion());
            txtImporte.setText(String.valueOf(existente.getImporte()));
            dpFecha.setValue(LocalDate.parse(existente.getFecha()));
            cuentas.stream().filter(c -> c.getId() == existente.getIdCuenta()).findFirst().ifPresent(cmbCuenta::setValue);
            actualizarCategorias(cmbCategoria, existente.getTipo());
            List<Categoria> cats = cmbCategoria.getItems();
            cats.stream().filter(c -> c.getId() == existente.getIdCategoria()).findFirst().ifPresent(cmbCategoria::setValue);
        }

        grid.add(new Label("Tipo:"), 0, 0);      grid.add(cmbTipo, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1); grid.add(txtDescripcion, 1, 1);
        grid.add(new Label("Importe:"), 0, 2);    grid.add(txtImporte, 1, 2);
        grid.add(new Label("Fecha:"), 0, 3);      grid.add(dpFecha, 1, 3);
        grid.add(new Label("Cuenta:"), 0, 4);     grid.add(cmbCuenta, 1, 4);
        grid.add(new Label("Categoría:"), 0, 5);  grid.add(cmbCategoria, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    double importe = Double.parseDouble(txtImporte.getText().replace(",", "."));
                    if (importe <= 0) throw new NumberFormatException();
                    Transaccion t = existente != null ? existente : new Transaccion();
                    t.setTipo(cmbTipo.getValue());
                    t.setDescripcion(txtDescripcion.getText().trim());
                    t.setImporte(importe);
                    t.setFecha(dpFecha.getValue().toString());
                    t.setIdCuenta(cmbCuenta.getValue().getId());
                    t.setIdCategoria(cmbCategoria.getValue().getId());
                    return t;
                } catch (Exception e) {
                    alerta("Importe no válido. Introduzca un número positivo.");
                    return null;
                }
            }
            return null;
        });

        Optional<Transaccion> resultado = dialog.showAndWait();
        resultado.ifPresent(t -> {
            if (existente == null) {
                transaccionDAO.insertar(t);
                Cuenta cuenta = cuentaDAO.obtenerPorId(t.getIdCuenta());
                double nuevoSaldo = t.getTipo() == TipoTransaccion.INGRESO ?
                    cuenta.getSaldo() + t.getImporte() : cuenta.getSaldo() - t.getImporte();
                cuentaDAO.actualizarSaldo(cuenta.getId(), nuevoSaldo);
            } else {
                Transaccion original = transaccionDAO.obtenerPorId(existente.getId());
                if (original != null) {
                    Cuenta cuentaOriginal = cuentaDAO.obtenerPorId(original.getIdCuenta());
                    if (cuentaOriginal != null) {
                        double saldoRevertido = original.getTipo() == TipoTransaccion.INGRESO ?
                            cuentaOriginal.getSaldo() - original.getImporte() :
                            cuentaOriginal.getSaldo() + original.getImporte();
                        cuentaDAO.actualizarSaldo(cuentaOriginal.getId(), saldoRevertido);
                    }
                }
                transaccionDAO.actualizar(t);
                Cuenta cuentaNueva = cuentaDAO.obtenerPorId(t.getIdCuenta());
                if (cuentaNueva != null) {
                    double nuevoSaldo = t.getTipo() == TipoTransaccion.INGRESO ?
                        cuentaNueva.getSaldo() + t.getImporte() : cuentaNueva.getSaldo() - t.getImporte();
                    cuentaDAO.actualizarSaldo(cuentaNueva.getId(), nuevoSaldo);
                }
            }
            cargarTransacciones();
        });
    }

    private void actualizarCategorias(ComboBox<Categoria> cmbCategoria, TipoTransaccion tipo) {
        List<Categoria> cats = categoriaDAO.listarPorUsuarioYTipo(idUsuario, tipo);
        cmbCategoria.setItems(FXCollections.observableArrayList(cats));
        if (!cats.isEmpty()) cmbCategoria.setValue(cats.get(0));
    }

    private void alerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
