package Bd;

import Models.Productor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductorDAO {

    //----------------------------------
    // INSERT
    //----------------------------------
    public static boolean insertar(String nombre, String telefono, String direccion) {
        String sql = "INSERT INTO Productores (Nombre, Telefono, Direccion) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, direccion);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar productor: " + e.getMessage());
            return false;
        }
    }

    //----------------------------------
    // UPDATE
    //----------------------------------
    public static boolean actualizar(int id, String nombre, String telefono, String direccion) {
        String sql = "UPDATE Productores SET Nombre=?, Telefono=?, Direccion=? WHERE Id=?";
        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, direccion);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar productor: " + e.getMessage());
            return false;
        }
    }

    //----------------------------------
    // DELETE
    //----------------------------------
    public static boolean eliminar(int id) {
        String sql = "DELETE FROM Productores WHERE Id=?";
        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar productor: " + e.getMessage());
            return false;
        }
    }

    //----------------------------------
    // SELECT ALL
    //----------------------------------
    public static List<Productor> listar() {
        List<Productor> lista = new ArrayList<>();
        String sql = "SELECT Id, Nombre, Telefono, Direccion FROM Productores ORDER BY Nombre ASC";
        try (
            PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(new Productor(
                    rs.getInt("Id"),
                    rs.getString("Nombre"),
                    rs.getString("Telefono"),
                    rs.getString("Direccion")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar productores: " + e.getMessage());
        }
        return lista;
    }

    //----------------------------------
    // KG VENDIDOS EN EL MES ACTUAL
    // Registros.Nro_Productor almacena el Id del productor como texto ("1","2",...)
    //----------------------------------
    public static double getKgMesActual(int productorId) {
        String mes = java.time.LocalDate.now().toString().substring(0, 7); // "YYYY-MM"
        String sql = "SELECT COALESCE(SUM(Peso_Neto), 0) FROM Registros " +
                     "WHERE Nro_Productor = ? AND substr(Fecha, 1, 7) = ?";
        try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {
            ps.setString(1, String.valueOf(productorId));
            ps.setString(2, mes);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error al obtener kg del mes: " + e.getMessage());
        }
        return 0;
    }
}
