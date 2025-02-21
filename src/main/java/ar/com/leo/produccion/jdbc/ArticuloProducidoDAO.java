package ar.com.leo.produccion.jdbc;

import ar.com.leo.produccion.model.ArticuloProducido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ar.com.leo.produccion.jdbc.DataSourceConfig.dataSource;

// * @author Leo
public class ArticuloProducidoDAO {

    public final static DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static List<ArticuloProducido> obtenerProduccion(String roomCode, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean actual, String articulo) throws SQLException {

        final double docena = roomCode.equals("SEAMLESS") ? 12 : 24;
        final List<ArticuloProducido> articulosProducidos = new ArrayList<>();

        String query;
        if (actual) {
            query = "SELECT COALESCE(pm.StyleCode, m.StyleCode) AS 'StyleCode'," +
                    "    COALESCE(SUM(pm.Pieces), 0) + COALESCE(MAX(m.LastpiecesSum), 0) AS 'Unidades'," +
                    "       'Produciendo' = CASE" +
                    "            WHEN EXISTS (" +
                    "                SELECT 1" +
                    "                FROM Machines m2" +
                    "                WHERE m2.StyleCode = COALESCE(pm.StyleCode, m.StyleCode)" +
                    "                    AND m2.state IN (0, 2, 3, 4, 5, 7)" +
                    "            ) " +
                    "THEN 'SI: ' + (" +
                    "            SELECT STUFF((" +
                    "                SELECT DISTINCT '-' + CONVERT(VARCHAR, m2.MachCode)" +
                    "                FROM Machines m2" +
                    "                WHERE m2.StyleCode = COALESCE(pm.StyleCode, m.StyleCode)" +
                    "                  AND m2.state IN (0, 2, 3, 4, 5, 7)" +
                    "                FOR XML PATH(''), TYPE" +
                    "            ).value('.', 'NVARCHAR(MAX)'), 1, 1, '')" +
                    "        ) " +
                    "ELSE 'NO'" +
                    "       END" +
                    " FROM PRODUCTIONS_MONITOR pm" +
                    " FULL JOIN (" + // LEFT JOIN ?
                    "   SELECT StyleCode, SUM(Lastpieces) AS LastpiecesSum" +
                    "   FROM MACHINES" +
                    "   WHERE RoomCode = '" + roomCode + "'" +
                    "   GROUP BY StyleCode" +
                    ") m ON pm.StyleCode = m.StyleCode" +
                    " WHERE ( " +
                    "   (pm.RoomCode = '" + roomCode + "'" + " AND pm.DateRec BETWEEN '" + SQL_DATE_TIME_FORMATTER.format(fechaInicio) + "' AND '" + SQL_DATE_TIME_FORMATTER.format(fechaFin) + "')" +
                    "   OR pm.StyleCode IS NULL" +
                    ")" +
                    (articulo.isBlank() ? "" : " AND pm.StyleCode LIKE '%" + articulo + "%'") +
                    " GROUP BY COALESCE(pm.StyleCode, m.StyleCode)" +
                    " ORDER BY StyleCode";
        } else {
            query = "SELECT pm.StyleCode," +
                    "    SUM(pm.Pieces) AS 'Unidades'," +
                    "    'Produciendo' = CASE" +
                    "            WHEN EXISTS (" +
                    "                SELECT 1" +
                    "                FROM Machines m2" +
                    "                WHERE m2.StyleCode = pm.StyleCode" +
                    "                    AND m2.state IN (0, 2, 3, 4, 5, 7)" +
                    "            ) THEN 'SI: ' + (" +
                    "                SELECT CONVERT(varchar, m2.MachCode) + '-' AS [text()]" +
                    "                FROM Machines m2" +
                    "                WHERE m2.StyleCode = pm.StyleCode AND m2.state IN (0, 2, 3, 4, 5, 7)" +
                    "                FOR XML PATH (''), TYPE" +
                    "            ).value('text()[1]', 'nvarchar(max)')" +
                    "            ELSE 'NO'" +
                    "       END" +
                    " FROM PRODUCTIONS_MONITOR pm" +
                    " LEFT JOIN MACHINES m ON pm.StyleCode = m.StyleCode AND pm.MachCode = m.MachCode" +
                    " WHERE pm.RoomCode = '" + roomCode + "'" +
                    " AND DateRec BETWEEN '" + SQL_DATE_TIME_FORMATTER.format(fechaInicio) + "' AND '" + SQL_DATE_TIME_FORMATTER.format(fechaFin) + "'" +
                    (articulo.isBlank() ? "" : " AND pm.StyleCode LIKE '%" + articulo + "%'") +
                    " GROUP BY pm.StyleCode" +
                    " ORDER BY pm.StyleCode";
        }

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getInt("Unidades") > 0) {
                        final ArticuloProducido articuloProducido = new ArticuloProducido();
                        final String styleCode = rs.getString("StyleCode").trim();
                        if (styleCode.length() > 6) {
                            String art = styleCode.substring(0, 5);
                            String talle;
                            if (styleCode.charAt(5) == '9') {
                                talle = "PARCHE";
                            } else {
                                talle = "T." + styleCode.charAt(5);
                            }
                            String color = styleCode.substring(6, 8);
                            if (styleCode.length() > 8 && styleCode.startsWith("02", 14)) // .2
                                articuloProducido.setStyleCode(art + " " + talle + " " + color + " (.2)");
                            else if (styleCode.length() > 8 && styleCode.startsWith("06", 14)) // .6
                                articuloProducido.setStyleCode(art + " " + talle + " " + color + " (.6)");
                            else if (styleCode.length() > 8 && styleCode.startsWith("08", 14)) // .8
                                articuloProducido.setStyleCode(art + " " + talle + " " + color + " (.8)");
                            else
                                articuloProducido.setStyleCode(art + " " + talle + " " + color);
                            int unidades = rs.getInt("Unidades");
                            if (styleCode.contains("#")) {
                                unidades *= 2;
                            } else if (styleCode.contains("%") || styleCode.contains("$")) {
                                unidades /= 2;
                            }
                            articuloProducido.setUnidades(unidades);
                            articuloProducido.setDocenas((BigDecimal.valueOf(unidades / docena).setScale(1, RoundingMode.HALF_UP)).doubleValue());
                            articuloProducido.setProduciendo(rs.getString("Produciendo").replaceAll("-$", ""));
                            articulosProducidos.add(articuloProducido);
                        }
                    }
                }
            } catch (SQLException e) {
                throw e;
//                e.printStackTrace();
            }
        } catch (SQLException e) {
            throw e;
//            e.printStackTrace();
        }

        return articulosProducidos;
    }

}


// QUERY VIEJA
//"SELECT pm.StyleCode," +
//        "    COALESCE(SUM(pm.Pieces), 0) + COALESCE(m.LastpiecesSum, 0) AS 'Unidades'," +
//        "       'Produciendo' = CASE" +
//        "            WHEN EXISTS (" +
//        "                SELECT 1" +
//        "                FROM Machines m2" +
//        "                WHERE m2.StyleCode = pm.StyleCode" +
//        "                    AND m2.state IN (0, 2, 3, 4, 5, 7)" +
//        "            ) THEN 'SI: ' + (" +
//        "                SELECT CONVERT(varchar, m2.MachCode) + '-' AS [text()]" +
//        "                FROM Machines m2" +
//        "                WHERE m2.StyleCode = pm.StyleCode AND m2.state IN (0, 2, 3, 4, 5, 7)" +
//        "                FOR XML PATH (''), TYPE" +
//        "            ).value('text()[1]', 'nvarchar(max)')" +
//        "            ELSE 'NO'" +
//        "       END" +
//        " FROM PRODUCTIONS_MONITOR pm" +
//        " FULL JOIN (" + // LEFT JOIN ?
//        "   SELECT StyleCode, SUM(Lastpieces) AS LastpiecesSum" +
//        "   FROM MACHINES" +
//        "   WHERE RoomCode = '" + roomCode + "'" +
//        "   GROUP BY StyleCode" +
//        ") m ON pm.StyleCode = m.StyleCode" +
//        " WHERE pm.RoomCode = '" + roomCode + "'" +
//        " AND DateRec BETWEEN '" + SQL_DATE_TIME_FORMATTER.format(fechaInicio) + "' AND '" + SQL_DATE_TIME_FORMATTER.format(fechaFin) + "'" +
//        (articulo.isBlank() ? "" : " AND pm.StyleCode LIKE '%" + articulo + "%'") +
//        " GROUP BY pm.StyleCode, m.LastpiecesSum" +
//        " ORDER BY pm.StyleCode"