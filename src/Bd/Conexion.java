package Bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion {

    private static Connection instancia = null;

    private static String getUrl() {
        String dir = System.getProperty("user.dir");
        return "jdbc:sqlite:" + dir + "/BalanzaDB.db";
    }

    public static Connection getInstancia() {
        try {
            if (instancia == null || instancia.isClosed()) {
                instancia = DriverManager.getConnection(getUrl());
                instancia.setAutoCommit(true);
                crearTabla(instancia);
                System.out.println("Conexión establecida: " + getUrl());
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar: " + e.getMessage());
        }
        return instancia;
    }

    private static void crearTabla(Connection conn) {
        String sql = """
                CREATE TABLE IF NOT EXISTS Registros (
                    Id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    Fecha         TEXT,
                    Nro_Cupon     INTEGER,
                    Nro_Productor TEXT,
                    Nombre        TEXT,
                    Peso_Bruto    REAL,
                    Tara          REAL,
                    Descuento     REAL,
                    Peso_Neto     REAL,
                    Remito        INTEGER
                )
                """;
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear tabla: " + e.getMessage());
        }
    }

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