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
public class ProgramadaArticuloProducidoDAO {

    public final static DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static List<ArticuloProducido> obtenerProduccion() throws SQLException {

        final List<ArticuloProducido> articulosProducidos = new ArrayList<>();
        final String fechaInicioMes = SQL_DATE_TIME_FORMATTER.format(LocalDateTime.now().withDayOfMonth(1).withHour(6).withMinute(0).withSecond(1));

        try (Connection con = dataSource.getConnection()) {
            try (final PreparedStatement ps = con.prepareStatement("SELECT " +
                    "    COALESCE(pm.StyleCode, m.StyleCode) AS 'StyleCode', " +
                    "    COALESCE(SUM(pm.Pieces), 0) + COALESCE(MAX(m.LastpiecesSum), 0) AS 'Unidades', " +
                    "    'Produciendo' = CASE " +
                    "        WHEN EXISTS ( " +
                    "            SELECT 1 " +
                    "            FROM Machines m2 " +
                    "            WHERE m2.StyleCode = COALESCE(pm.StyleCode, m.StyleCode) " +
                    "              AND m2.state IN (0, 2, 3, 4, 5, 7) " +
                    "        ) THEN 'SI: ' + ( " +
                    "            SELECT STUFF(( " +
                    "                SELECT DISTINCT '-' + CONVERT(VARCHAR, m2.MachCode) " +
                    "                FROM Machines m2 " +
                    "                WHERE m2.StyleCode = COALESCE(pm.StyleCode, m.StyleCode) " +
                    "                  AND m2.state IN (0, 2, 3, 4, 5, 7) " +
                    "                FOR XML PATH(''), TYPE " +
                    "            ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') " +
                    "        ) " +
                    "        ELSE 'NO' " +
                    "    END, " +
                    "    MAX(m.IdealCycle) AS 'IdealCycle' " +
                    "FROM " +
                    "    PRODUCTIONS_MONITOR pm " +
                    "FULL JOIN ( " +
                    "    SELECT " +
                    "        StyleCode, " +
                    "        SUM(Lastpieces) AS LastpiecesSum, " +
                    "        MAX(IdealCycle) AS IdealCycle " +
                    "    FROM MACHINES " +
                    "WHERE " +
                    "    RoomCode = 'HOMBRE' " +
                    "    GROUP BY StyleCode " +
                    ") m ON pm.StyleCode = m.StyleCode " +
                    "WHERE " +
                    "    (pm.RoomCode = 'HOMBRE' AND pm.DateRec >= '" + fechaInicioMes + "') " +
                    "    OR pm.StyleCode IS NULL " +
                    "GROUP BY " +
                    "    COALESCE(pm.StyleCode, m.StyleCode) " +
                    "ORDER BY " +
                    "    StyleCode");
                 final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getInt("Unidades") > 0) {
                        final ArticuloProducido articuloProducido = new ArticuloProducido();
                        final String styleCode = rs.getString("StyleCode").trim();
                        if (styleCode.length() > 6) {
                            final String numero = styleCode.substring(0, 5);
                            int talle;
                            if (styleCode.charAt(5) == '8') { // Talle UNICO
                                talle = 1;
                            } else if (styleCode.charAt(5) == '9') { // Talle 00
                                talle = 8;
                            } else if (styleCode.charAt(5) == 'S') {
                                talle = 1;
                            } else if (styleCode.charAt(5) == 'M') {
                                talle = 2;
                            } else if (styleCode.charAt(5) == 'L') {
                                talle = 3;
                            } else {
                                talle = Character.getNumericValue(styleCode.charAt(5));
                            }
                            final String color = styleCode.substring(6, 8);

                            articuloProducido.setStyleCode(styleCode);
                            articuloProducido.setNumero(numero);
                            articuloProducido.setColor(color);
                            articuloProducido.setTalle(talle);
                            final int unidades = rs.getInt("Unidades");
                            articuloProducido.setUnidades((int) Math.round(unidades * 0.98));
                            articuloProducido.setDocenas((BigDecimal.valueOf(unidades * 0.98 / 24d).setScale(1, RoundingMode.HALF_UP)).doubleValue());
                            articuloProducido.setIdealCycle(rs.getInt("IdealCycle"));
                            final String produciendo = rs.getString("Produciendo");
                            articuloProducido.setProduciendo(produciendo);
                            if (produciendo.contains("SI")) {
                                articuloProducido.setMaquinas(produciendo.substring(produciendo.indexOf(":") + 2).split("-"));
                            }
                            articulosProducidos.add(articuloProducido);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return articulosProducidos;
    }

}

// Query vieja
//            "SELECT pm.StyleCode," +
//            "    COALESCE(SUM(pm.Pieces), 0) + COALESCE(m.LastpiecesSum, 0) AS 'Unidades'," +
//            "       'Produciendo' = CASE" +
//            "            WHEN EXISTS (" +
//            "                SELECT 1" +
//            "                FROM Machines m2" +
//            "                WHERE m2.StyleCode = pm.StyleCode" +
//            "                    AND m2.state IN (0, 2, 3, 4, 5, 7)" +
//            "            ) THEN 'SI: ' + (" +
//            "                SELECT CONVERT(varchar, m2.MachCode) + '-' AS [text()]" +
//            "                FROM Machines m2" +
//            "                WHERE m2.StyleCode = pm.StyleCode AND m2.state IN (0, 2, 3, 4, 5, 7)" +
//            "                FOR XML PATH (''), TYPE" +
//            "            ).value('text()[1]', 'nvarchar(max)')" +
//            "            ELSE 'NO'" +
//            "       END," +
//            " MAX(m.IdealCycle) AS IdealCycle" +
//            " FROM PRODUCTIONS_MONITOR pm" +
//            " FULL JOIN (" + // LEFT JOIN?
//            "   SELECT StyleCode, SUM(Lastpieces) AS LastpiecesSum, MAX(IdealCycle) AS IdealCycle" +
//            "   FROM MACHINES" +
//            "   WHERE RoomCode = 'HOMBRE'" +
//            "   GROUP BY StyleCode" +
//            ") m ON pm.StyleCode = m.StyleCode" +
//            " WHERE pm.RoomCode = 'HOMBRE'" +
//            " AND DateRec >= '" + fechaInicioMes + "'" +
//            " GROUP BY pm.StyleCode, m.LastpiecesSum" +
//            " ORDER BY pm.StyleCode";
