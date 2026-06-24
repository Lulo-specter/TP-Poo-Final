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
        String sqlRegistros = """
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
        String sqlProductores = """
                CREATE TABLE IF NOT EXISTS Productores (
                    Id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    Nombre    TEXT,
                    Telefono  TEXT,
                    Direccion TEXT
                )
                """;
        try (Statement st = conn.createStatement()) {
            st.execute(sqlRegistros);
            st.execute(sqlProductores);
            // Migración: agrega columnas si la tabla ya existía sin ellas
            try { st.execute("ALTER TABLE Productores ADD COLUMN Telefono  TEXT DEFAULT ''"); } catch (SQLException ignored) {}
            try { st.execute("ALTER TABLE Productores ADD COLUMN Direccion TEXT DEFAULT ''"); } catch (SQLException ignored) {}
        } catch (SQLException e) {
            System.err.println("Error al crear tablas: " + e.getMessage());
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