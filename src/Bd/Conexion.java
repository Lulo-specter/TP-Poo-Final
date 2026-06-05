package Bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
            "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=BalanzaDB;encrypt=false";

    private static final String USER = "balanza_user";
    private static final String PASS = "balanza123";

    private static Connection instancia = null;

    //----------------------------------
    // SINGLETON: una sola conexión
    //----------------------------------
    public static Connection getInstancia() {
        if (instancia == null) {
            try {
                instancia = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Conexión establecida.");
            } catch (SQLException e) {
                System.err.println("Error al conectar: " + e.getMessage());
            }
        }
        return instancia;
    }

    //----------------------------------
    // CERRAR CONEXIÓN
    //----------------------------------
    public static void cerrar() {
        if (instancia != null) {
            try {
                instancia.close();
                instancia = null;
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar: " + e.getMessage());
            }
        }
    }
}