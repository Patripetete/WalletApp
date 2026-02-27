package com.walletapp.dao;

import com.walletapp.model.Categoria;
import com.walletapp.model.TipoTransaccion;
import com.walletapp.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    public boolean insertar(Categoria c) {
        String sql = "INSERT INTO categorias (nombre, tipo, id_usuario) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getTipo().name());
            if (c.getIdUsuario() != null) {
                ps.setInt(3, c.getIdUsuario());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar categoría: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }

    public List<Categoria> listarPorUsuario(int idUsuario) {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias WHERE id_usuario IS NULL OR id_usuario = ? ORDER BY tipo, nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Integer idUsr = rs.getObject("id_usuario") != null ? rs.getInt("id_usuario") : null;
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre"),
                    TipoTransaccion.valueOf(rs.getString("tipo")), idUsr));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
        }
        return lista;
    }

    public List<Categoria> listarPorUsuarioYTipo(int idUsuario, TipoTransaccion tipo) {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias WHERE (id_usuario IS NULL OR id_usuario = ?) AND tipo = ? ORDER BY nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Integer idUsr = rs.getObject("id_usuario") != null ? rs.getInt("id_usuario") : null;
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre"),
                    TipoTransaccion.valueOf(rs.getString("tipo")), idUsr));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar categorías por tipo: " + e.getMessage());
        }
        return lista;
    }
}
