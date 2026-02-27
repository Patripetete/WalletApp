package com.walletapp.dao;

import com.walletapp.model.TipoTransaccion;
import com.walletapp.model.Transaccion;
import com.walletapp.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransaccionDAO {

    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    public boolean insertar(Transaccion t) {
        String sql = "INSERT INTO transacciones (descripcion, importe, fecha, tipo, id_cuenta, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getDescripcion());
            ps.setDouble(2, t.getImporte());
            ps.setString(3, t.getFecha());
            ps.setString(4, t.getTipo().name());
            ps.setInt(5, t.getIdCuenta());
            ps.setInt(6, t.getIdCategoria());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar transacción: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Transaccion t) {
        String sql = "UPDATE transacciones SET descripcion=?, importe=?, fecha=?, tipo=?, id_cuenta=?, id_categoria=? WHERE id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getDescripcion());
            ps.setDouble(2, t.getImporte());
            ps.setString(3, t.getFecha());
            ps.setString(4, t.getTipo().name());
            ps.setInt(5, t.getIdCuenta());
            ps.setInt(6, t.getIdCategoria());
            ps.setInt(7, t.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar transacción: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM transacciones WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar transacción: " + e.getMessage());
            return false;
        }
    }

    public Transaccion obtenerPorId(int id) {
        String sql = "SELECT t.*, c.nombre AS nombre_cuenta, cat.nombre AS nombre_categoria " +
                     "FROM transacciones t JOIN cuentas c ON t.id_cuenta=c.id JOIN categorias cat ON t.id_categoria=cat.id WHERE t.id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapTransaccion(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener transacción: " + e.getMessage());
        }
        return null;
    }

    public List<Transaccion> listarPorUsuario(int idUsuario) {
        List<Transaccion> lista = new ArrayList<>();
        String sql = "SELECT t.*, c.nombre AS nombre_cuenta, cat.nombre AS nombre_categoria " +
                     "FROM transacciones t JOIN cuentas c ON t.id_cuenta=c.id JOIN categorias cat ON t.id_categoria=cat.id " +
                     "WHERE c.id_usuario=? ORDER BY t.fecha DESC, t.id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapTransaccion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar transacciones: " + e.getMessage());
        }
        return lista;
    }

    public List<Transaccion> listarFiltradas(int idUsuario, String fechaDesde, String fechaHasta,
                                              TipoTransaccion tipo, Integer idCategoria, Integer idCuenta) {
        List<Transaccion> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, c.nombre AS nombre_cuenta, cat.nombre AS nombre_categoria " +
            "FROM transacciones t JOIN cuentas c ON t.id_cuenta=c.id JOIN categorias cat ON t.id_categoria=cat.id " +
            "WHERE c.id_usuario=?");
        List<Object> params = new ArrayList<>();
        params.add(idUsuario);

        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            sql.append(" AND t.fecha >= ?");
            params.add(fechaDesde);
        }
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            sql.append(" AND t.fecha <= ?");
            params.add(fechaHasta);
        }
        if (tipo != null) {
            sql.append(" AND t.tipo = ?");
            params.add(tipo.name());
        }
        if (idCategoria != null) {
            sql.append(" AND t.id_categoria = ?");
            params.add(idCategoria);
        }
        if (idCuenta != null) {
            sql.append(" AND t.id_cuenta = ?");
            params.add(idCuenta);
        }
        sql.append(" ORDER BY t.fecha DESC, t.id DESC");

        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
                else ps.setString(i + 1, (String) p);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapTransaccion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar transacciones: " + e.getMessage());
        }
        return lista;
    }

    public double sumaPorTipoMesActual(int idUsuario, TipoTransaccion tipo) {
        YearMonth ym = YearMonth.now();
        String desde = ym.atDay(1).toString();
        String hasta = ym.atEndOfMonth().toString();
        String sql = "SELECT COALESCE(SUM(t.importe),0) FROM transacciones t JOIN cuentas c ON t.id_cuenta=c.id " +
                     "WHERE c.id_usuario=? AND t.tipo=? AND t.fecha>=? AND t.fecha<=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo.name());
            ps.setString(3, desde);
            ps.setString(4, hasta);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error al sumar por tipo: " + e.getMessage());
        }
        return 0;
    }

    public Map<String, Double> sumaPorCategoria(int idUsuario, TipoTransaccion tipo) {
        Map<String, Double> mapa = new LinkedHashMap<>();
        YearMonth ym = YearMonth.now();
        String desde = ym.atDay(1).toString();
        String hasta = ym.atEndOfMonth().toString();
        String sql = "SELECT cat.nombre, SUM(t.importe) as total FROM transacciones t " +
                     "JOIN cuentas c ON t.id_cuenta=c.id JOIN categorias cat ON t.id_categoria=cat.id " +
                     "WHERE c.id_usuario=? AND t.tipo=? AND t.fecha>=? AND t.fecha<=? GROUP BY cat.nombre ORDER BY total DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo.name());
            ps.setString(3, desde);
            ps.setString(4, hasta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mapa.put(rs.getString("nombre"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error al sumar por categoría: " + e.getMessage());
        }
        return mapa;
    }

    public Map<String, double[]> sumasMensuales(int idUsuario, int meses) {
        Map<String, double[]> mapa = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yy");
        YearMonth actual = YearMonth.now();

        for (int i = meses - 1; i >= 0; i--) {
            YearMonth ym = actual.minusMonths(i);
            String label = ym.format(fmt);
            mapa.put(label, new double[]{0, 0});
        }

        for (int i = meses - 1; i >= 0; i--) {
            YearMonth ym = actual.minusMonths(i);
            String label = ym.format(fmt);
            String desde = ym.atDay(1).toString();
            String hasta = ym.atEndOfMonth().toString();

            String sql = "SELECT t.tipo, SUM(t.importe) as total FROM transacciones t " +
                         "JOIN cuentas c ON t.id_cuenta=c.id WHERE c.id_usuario=? AND t.fecha>=? AND t.fecha<=? GROUP BY t.tipo";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, desde);
                ps.setString(3, hasta);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    double total = rs.getDouble("total");
                    double[] vals = mapa.get(label);
                    if ("INGRESO".equals(tipo)) vals[0] = total;
                    else vals[1] = total;
                }
            } catch (SQLException e) {
                System.err.println("Error al obtener sumas mensuales: " + e.getMessage());
            }
        }
        return mapa;
    }

    private Transaccion mapTransaccion(ResultSet rs) throws SQLException {
        Transaccion t = new Transaccion();
        t.setId(rs.getInt("id"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setImporte(rs.getDouble("importe"));
        t.setFecha(rs.getString("fecha"));
        t.setTipo(TipoTransaccion.valueOf(rs.getString("tipo")));
        t.setIdCuenta(rs.getInt("id_cuenta"));
        t.setIdCategoria(rs.getInt("id_categoria"));
        t.setNombreCuenta(rs.getString("nombre_cuenta"));
        t.setNombreCategoria(rs.getString("nombre_categoria"));
        return t;
    }
}
