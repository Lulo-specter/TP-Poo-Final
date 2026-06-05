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
}