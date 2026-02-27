package com.walletapp.controller;

import com.walletapp.App;
import com.walletapp.dao.CuentaDAO;
import com.walletapp.dao.TransaccionDAO;
import com.walletapp.model.Cuenta;
import com.walletapp.model.TipoTransaccion;
import com.walletapp.model.Usuario;
import com.walletapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label lblUsuario;
    @FXML private StackPane contentPane;

    private final CuentaDAO cuentaDAO = new CuentaDAO();
    private final TransaccionDAO transaccionDAO = new TransaccionDAO();

    @FXML
    public void initialize() {
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        lblUsuario.setText(usuario.getNombre() + " " + usuario.getApellidos());
        mostrarDashboard();
    }

    @FXML
    public void mostrarDashboard() {
        contentPane.getChildren().clear();
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        int idUsuario = usuario.getId();

        VBox panel = new VBox(20);
        panel.setStyle("-fx-padding: 25;");

        // Tarjetas resumen
        List<Cuenta> cuentas = cuentaDAO.listarPorUsuario(idUsuario);
        double saldoTotal = cuentas.stream().mapToDouble(Cuenta::getSaldo).sum();
        double ingresosMes = transaccionDAO.sumaPorTipoMesActual(idUsuario, TipoTransaccion.INGRESO);
        double gastosMes = transaccionDAO.sumaPorTipoMesActual(idUsuario, TipoTransaccion.GASTO);

        HBox tarjetas = new HBox(20);
        tarjetas.getChildren().addAll(
            crearTarjeta("Saldo Total", String.format("%.2f EUR", saldoTotal), "#2c3e50"),
            crearTarjeta("Ingresos del Mes", String.format("+%.2f EUR", ingresosMes), "#27ae60"),
            crearTarjeta("Gastos del Mes", String.format("-%.2f EUR", gastosMes), "#e74c3c")
        );

        // Gráficos
        HBox graficos = new HBox(20);
        graficos.setStyle("-fx-padding: 0;");
        HBox.setHgrow(graficos, Priority.ALWAYS);

        // PieChart
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Gastos por Categoría");
        pieChart.setLabelsVisible(true);
        Map<String, Double> gastosPorCat = transaccionDAO.sumaPorCategoria(idUsuario, TipoTransaccion.GASTO);
        for (Map.Entry<String, Double> entry : gastosPorCat.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        if (gastosPorCat.isEmpty()) {
            pieChart.setTitle("Gastos por Categoría (sin datos)");
        }
        pieChart.setPrefSize(450, 350);

        // BarChart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes");
        yAxis.setLabel("Importe (EUR)");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Ingresos vs Gastos");

        XYChart.Series<String, Number> serieIngresos = new XYChart.Series<>();
        serieIngresos.setName("Ingresos");
        XYChart.Series<String, Number> serieGastos = new XYChart.Series<>();
        serieGastos.setName("Gastos");

        Map<String, double[]> mensuales = transaccionDAO.sumasMensuales(idUsuario, 6);
        for (Map.Entry<String, double[]> entry : mensuales.entrySet()) {
            serieIngresos.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()[0]));
            serieGastos.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()[1]));
        }
        barChart.getData().addAll(serieIngresos, serieGastos);
        barChart.setPrefSize(450, 350);

        graficos.getChildren().addAll(pieChart, barChart);

        panel.getChildren().addAll(tarjetas, graficos);
        VBox.setVgrow(graficos, Priority.ALWAYS);
        contentPane.getChildren().add(panel);
    }

    private VBox crearTarjeta(String titulo, String valor, String color) {
        VBox tarjeta = new VBox(8);
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        tarjeta.setPrefWidth(280);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");
        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        tarjeta.getChildren().addAll(lblTitulo, lblValor);
        HBox.setHgrow(tarjeta, Priority.ALWAYS);
        return tarjeta;
    }

    @FXML
    public void mostrarTransacciones() {
        cargarVista("transacciones");
    }

    @FXML
    public void mostrarCuentas() {
        cargarVista("cuentas");
    }

    @FXML
    public void mostrarCategorias() {
        cargarVista("categorias");
    }

    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = App.getLoader(fxml);
            Parent vista = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(vista);
        } catch (IOException e) {
            System.err.println("Error al cargar vista " + fxml + ": " + e.getMessage());
        }
    }

    @FXML
    private void cerrarSesion() {
        SessionManager.getInstance().cerrarSesion();
        try {
            App.setRoot("login");
        } catch (IOException e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
        }
    }
}
