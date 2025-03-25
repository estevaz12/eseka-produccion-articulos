package ar.com.leo.produccion.jdbc;

import ar.com.leo.produccion.model.ArticuloColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static ar.com.leo.produccion.jdbc.DataSourceConfig.dataSource;

// * @author Leo
public class ColorDAO {

    public static List<ArticuloColor> obtenerArticulosColores() {

        List<ArticuloColor> articulos = new ArrayList<>();

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT *" +
                    "  FROM ARTICULOS_HOMBRE");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ArticuloColor articuloColor = new ArticuloColor();
                    articuloColor.setNumero(rs.getString("Articulo").trim());
                    articuloColor.setColor(rs.getString("Color").trim());
                    articuloColor.setPunto(rs.getString("Punto").trim());
                    articulos.add(articuloColor);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return articulos;
    }

}
