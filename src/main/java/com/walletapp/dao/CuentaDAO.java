package com.walletapp.dao;

import com.walletapp.model.Cuenta;
import com.walletapp.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CuentaDAO {

    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    public boolean insertar(Cuenta c) {
        String sql = "INSERT INTO cuentas (nombre, saldo, id_usuario) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setDouble(2, c.getSaldo());
            ps.setInt(3, c.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar cuenta: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Cuenta c) {
        String sql = "UPDATE cuentas SET nombre = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cuenta: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM cuentas WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cuenta: " + e.getMessage());
            return false;
        }
    }

    public List<Cuenta> listarPorUsuario(int idUsuario) {
        List<Cuenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM cuentas WHERE id_usuario = ? ORDER BY nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Cuenta(rs.getInt("id"), rs.getString("nombre"),
                    rs.getDouble("saldo"), rs.getInt("id_usuario")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar cuentas: " + e.getMessage());
        }
        return lista;
    }

    public Cuenta obtenerPorId(int id) {
        String sql = "SELECT * FROM cuentas WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Cuenta(rs.getInt("id"), rs.getString("nombre"),
                    rs.getDouble("saldo"), rs.getInt("id_usuario"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cuenta: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizarSaldo(int idCuenta, double nuevoSaldo) {
        String sql = "UPDATE cuentas SET saldo = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, nuevoSaldo);
            ps.setInt(2, idCuenta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar saldo: " + e.getMessage());
            return false;
        }
    }
}
