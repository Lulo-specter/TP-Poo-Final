package Bd;

import Models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistroDAO {

    //----------------------------------
    // INSERT
    //----------------------------------
    public static void insertar(
            String fecha,
            int nroCupon,
            String nroProductor,
            String nombre,
            double pesoBruto,
            double tara,
            double descuento,
            double pesoNeto,
            int remito
    ) {
        String sql = "INSERT INTO Registros " +
                "(Fecha, Nro_Cupon, Nro_Productor, Nombre, Peso_Bruto, Tara, Descuento, Peso_Neto, Remito) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setString(1, fecha);
            ps.setInt(2, nroCupon);
            ps.setString(3, nroProductor);
            ps.setString(4, nombre);
            ps.setDouble(5, pesoBruto);
            ps.setDouble(6, tara);
            ps.setDouble(7, descuento);
            ps.setDouble(8, pesoNeto);
            ps.setInt(9, remito);
            ps.executeUpdate();
            System.out.println("Registro insertado correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al insertar: " + e.getMessage());
        }
    }



    //----------------------------------
// UPDATE
//----------------------------------
    public static boolean actualizar(
            int nroCupon,
            String nroProductor,
            String nombre,
            double pesoBruto,
            double tara,
            double descuento,
            int remito,
            String fecha
    )
    {
        double neto = pesoBruto - tara;
        neto = Math.round((neto - (neto * descuento / 100)) * 100.0) / 100.0;

        String sql = "UPDATE Registros SET " +
                "Nro_Productor = ?, " +
                "Nombre = ?, " +
                "Peso_Bruto = ?, " +
                "Tara = ?, " +
                "Descuento = ?, " +
                "Peso_Neto = ?, " +
                "Remito = ?, " +
                "Fecha = ? " +
                "WHERE Nro_Cupon = ?";

        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setString(1, nroProductor);
            ps.setString(2, nombre);
            ps.setDouble(3, pesoBruto);
            ps.setDouble(4, tara);
            ps.setDouble(5, descuento);
            ps.setDouble(6, neto);
            ps.setInt(7, remito);
            ps.setString(8, fecha);
            ps.setInt(9, nroCupon);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0; // true si encontró y actualizó el registro

        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
            return false;
        }
    }

    //----------------------------------
    // BUSCAR POR CUPON (para cargar datos en el formulario)
    //----------------------------------
    public static Producto buscarPorCupon(int nroCupon) {
        String sql = "SELECT * FROM Registros WHERE Nro_Cupon = ?";

        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setInt(1, nroCupon);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("Id"),
                        rs.getInt("Nro_Cupon"),
                        rs.getString("Nro_Productor"),
                        rs.getString("Nombre"),
                        rs.getDouble("Peso_Bruto"),
                        rs.getDouble("Tara"),
                        rs.getDouble("Descuento"),
                        rs.getString("Fecha")
                );
                p.setRemito(rs.getInt("Remito"));
                return p;
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar: " + e.getMessage());
        }
        return null; // null significa que no encontró nada
    }


    //----------------------------------
    // SELECT ALL
    //----------------------------------
    public static List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM Registros";

        try (
                PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("Id"),
                        rs.getInt("Nro_Cupon"),
                        rs.getString("Nro_Productor"),
                        rs.getString("Nombre"),
                        rs.getDouble("Peso_Bruto"),
                        rs.getDouble("Tara"),
                        rs.getDouble("Descuento"),
                        rs.getString("Fecha")
                );
                p.setRemito(rs.getInt("Remito"));
                lista.add(p);

            }
        } catch (SQLException e) {
            System.err.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }

    //----------------------------------
    // ULTIMO CUPON: registra el ultimo cupon cargado
    //----------------------------------
    public static int getUltimoCupon() {
        String sql = "SELECT MAX(Nro_Cupon) FROM Registros";
        try (
                PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next() && rs.getObject(1) != null) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener último cupón: " + e.getMessage());
        }
        return 0;
    }
    //----------------------------------
    // DELETE
    //----------------------------------
    public static boolean eliminar(int id) {
        String sql = "DELETE FROM Registros WHERE Id = ?";
        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar registro: " + e.getMessage());
            return false;
        }
    }

    //----------------------------------
    // VERIFICAR SI REMITO YA EXISTE
    //----------------------------------
    public static boolean remitoExiste(int remito) {
        String sql = "SELECT COUNT(*) FROM Registros WHERE Remito = ?";
        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setInt(1, remito);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar remito: " + e.getMessage());
        }
        return false;
    }
}