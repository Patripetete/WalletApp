package com.walletapp.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    private static final String DB_DIR = System.getProperty("user.home") + File.separator + ".walletapp";
    private static final String DB_PATH = DB_DIR + File.separator + "walletapp.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    private DatabaseManager() {
        try {
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void crearTablas() {
        String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                apellidos TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                fecha_registro TEXT DEFAULT CURRENT_TIMESTAMP
            )""";

        String sqlCuentas = """
            CREATE TABLE IF NOT EXISTS cuentas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                saldo REAL DEFAULT 0.0,
                id_usuario INTEGER NOT NULL,
                FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE
            )""";

        String sqlCategorias = """
            CREATE TABLE IF NOT EXISTS categorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                tipo TEXT NOT NULL CHECK(tipo IN ('INGRESO','GASTO')),
                id_usuario INTEGER,
                FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE
            )""";

        String sqlTransacciones = """
            CREATE TABLE IF NOT EXISTS transacciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion TEXT,
                importe REAL NOT NULL,
                fecha TEXT NOT NULL,
                tipo TEXT NOT NULL CHECK(tipo IN ('INGRESO','GASTO')),
                id_cuenta INTEGER NOT NULL,
                id_categoria INTEGER NOT NULL,
                FOREIGN KEY (id_cuenta) REFERENCES cuentas(id) ON DELETE CASCADE,
                FOREIGN KEY (id_categoria) REFERENCES categorias(id)
            )""";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlCuentas);
            stmt.execute(sqlCategorias);
            stmt.execute(sqlTransacciones);
            insertarCategoriasPredefinidas(stmt);
        } catch (SQLException e) {
            System.err.println("Error al crear tablas: " + e.getMessage());
        }
    }

    private void insertarCategoriasPredefinidas(Statement stmt) throws SQLException {
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM categorias WHERE id_usuario IS NULL");
        if (rs.next() && rs.getInt(1) == 0) {
            String[][] categorias = {
                {"Alimentación", "GASTO"}, {"Transporte", "GASTO"}, {"Ocio", "GASTO"},
                {"Salud", "GASTO"}, {"Hogar", "GASTO"}, {"Educación", "GASTO"},
                {"Ropa", "GASTO"}, {"Servicios", "GASTO"},
                {"Nómina", "INGRESO"}, {"Freelance", "INGRESO"},
                {"Inversiones", "INGRESO"}, {"Otros ingresos", "INGRESO"}
            };
            for (String[] cat : categorias) {
                stmt.execute("INSERT INTO categorias (nombre, tipo, id_usuario) VALUES ('" +
                    cat[0] + "', '" + cat[1] + "', NULL)");
            }
        }
        rs.close();
    }
}
