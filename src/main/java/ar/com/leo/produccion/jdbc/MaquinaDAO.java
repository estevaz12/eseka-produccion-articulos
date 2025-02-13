package ar.com.leo.produccion.jdbc;

import ar.com.leo.produccion.model.Maquina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static ar.com.leo.produccion.jdbc.DataSourceConfig.dataSource;

// * @author Leo
public class MaquinaDAO {

    public static List<Maquina> obtenerMaquinas(String roomCode) {

        List<Maquina> maquinas = new ArrayList<>();

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "WITH LatestProduction AS (" +
                            " SELECT pm.MachCode, pm.StyleCode, pm.TargetPieces, SUM(pm.Pieces) AS TotalPieces" +
                            " FROM dbo.PRODUCTIONS_MONITOR pm" +
                            " WHERE pm.Reason = 5" +
                            "  AND pm.DateRec = (" +
                            "   SELECT MAX(DateRec)" +
                            "   FROM dbo.PRODUCTIONS_MONITOR" +
                            "   WHERE MachCode = pm.MachCode AND StyleCode = pm.StyleCode" +
                            "    AND MONTH(DateRec) = MONTH(GETDATE()) AND YEAR(DateRec) = YEAR(GETDATE()) AND Reason = 5" +
                            "  )" +
                            " GROUP BY pm.MachCode, pm.StyleCode, pm.TargetPieces" +
                            ")," +
                            "PreviousProduction AS (" +
                            "    SELECT pm.MachCode, pm.StyleCode, SUM(pm.Pieces) AS PreviousPieces" +
                            "    FROM dbo.PRODUCTIONS_MONITOR pm" +
                            "    LEFT JOIN LatestProduction lp ON pm.MachCode = lp.MachCode AND pm.StyleCode = lp.StyleCode" +
                            "    WHERE pm.DateRec > (" +
                            "        SELECT MAX(pm2.DateRec)" +
                            "        FROM dbo.PRODUCTIONS_MONITOR pm2" +
                            "        WHERE pm2.MachCode = lp.MachCode AND pm2.StyleCode = lp.StyleCode" +
                            "            AND MONTH(pm2.DateRec) = MONTH(GETDATE()) AND YEAR(pm2.DateRec) = YEAR(GETDATE()) AND pm2.Reason = 5" +
                            "    )" +
                            "    GROUP BY pm.MachCode, pm.StyleCode" +
                            ")," +
                            "MachineData AS (" +
                            "    SELECT" +
                            "        m.MachCode," +
                            "        m.StyleCode," +
                            "        m.Pieces," +
                            "        m.TargetOrder," +
                            "        m.State," +
                            "        MAX(m.IdealCycle) AS IdealCycle," +
                            "        MAX(m.LastPieces) AS LastPieces," +
                            "        c.Numero AS Color" +
                            "    FROM [dbNautilus].dbo.MACHINES m" +
                            "    LEFT JOIN (" +
                            "        SELECT StyleCode, ROW_NUMBER() OVER (ORDER BY StyleCode ASC) AS Numero" +
                            "        FROM [dbNautilus].dbo.MACHINES" +
                            "         WHERE RoomCode = '" + roomCode + "'" +
                            "        GROUP BY StyleCode" +
                            "        HAVING COUNT(*) > 1" +
                            "    ) AS c ON c.StyleCode = m.StyleCode" +
                            "    WHERE m.RoomCode = '" + roomCode + "'" +
                            "    GROUP BY m.MachCode, m.StyleCode, m.Pieces, m.TargetOrder, m.State, c.Numero" +
                            ")," +
                            "LatestTargetPieces AS (" +
                            "    SELECT MachCode, MAX(TargetPieces) AS LatestTargetPieces" +
                            "    FROM LatestProduction" +
                            "    GROUP BY MachCode" +
                            ")" +
                            " SELECT DISTINCT" +
                            "    md.MachCode," +
                            "    md.StyleCode," +
                            "    CASE" +
                            "        WHEN md.TargetOrder = 0 THEN" +
                            "            CASE" +
                            "                WHEN lp.TargetPieces IS NOT NULL THEN" +
                            "                    (lp.TargetPieces + ISNULL(pp.PreviousPieces, 0) + md.LastPieces)" +
                            "                ELSE" +
                            "                    md.Pieces" +
                            "            END" +
                            "        ELSE" +
                            "            md.Pieces" +
                            "    END AS Pieces," +
                            "    CASE" +
                            "        WHEN md.TargetOrder = 0 THEN" +
                            "            CASE" +
                            "                WHEN lp.TargetPieces IS NOT NULL THEN lp.TargetPieces" +
                            "                ELSE md.TargetOrder" +
                            "            END" +
                            "        ELSE" +
                            "            md.TargetOrder" +
                            "    END AS TargetOrder," +
                            "    CASE" +
                            "       WHEN md.TargetOrder = 0 THEN" +
                            "            CASE" +
                            "                WHEN lp.TargetPieces IS NOT NULL THEN (lp.TargetPieces + ISNULL(pp.PreviousPieces, 0) + md.LastPieces)*100/lp.TargetPieces" +
                            "                ELSE 0" +
                            "            END" +
                            "        ELSE" +
                            "           md.Pieces*100 / md.TargetOrder" +
                            "    END AS '%'," +
                            "    md.State," +
                            "    md.IdealCycle," +
                            "    md.Color" +
                            " FROM MachineData md" +
                            " LEFT JOIN LatestProduction lp ON md.MachCode = lp.MachCode AND md.StyleCode = lp.StyleCode" +
                            " LEFT JOIN PreviousProduction pp ON md.MachCode = pp.MachCode AND md.StyleCode = pp.StyleCode" +
                            " LEFT JOIN LatestTargetPieces ltp ON md.MachCode = ltp.MachCode" +
                            " ORDER BY md.MachCode;");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Maquina maquina = new Maquina();
                    maquina.setMachCode(rs.getInt("MachCode"));
                    maquina.setStyleCode(rs.getString("StyleCode").trim());
                    maquina.setPieces(rs.getInt("Pieces"));
                    maquina.setTargetOrder(rs.getInt("TargetOrder"));
                    maquina.setProduccion(rs.getInt("%"));
                    maquina.setIdealCycle(rs.getInt("IdealCycle"));
                    maquina.setState(rs.getInt("State"));
                    maquinas.add(maquina);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                //consider a re-throw, throwing a wrapping exception, etc
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maquinas;
    }

//    "SELECT MachCode, StyleCode, Pieces, TargetOrder, IdealCycle, State," +
//            " CASE WHEN (TargetOrder = 0) THEN" +
//            "   CASE WHEN (EXISTS(SELECT TOP 1 State FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND Reason = 5)) THEN" +
//            "       ((SELECT TOP 1 pm.TargetPieces FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND Reason = 5 " +
//            "       ORDER BY DateRec DESC) + " +
//            "       ISNULL((SELECT SUM(pm.Pieces) FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND pm.DateRec > (SELECT TOP 1 DateRec FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND Reason = 5 ORDER BY DateRec DESC)), 0) + MAX(m.LastPieces)) * 100 / (SELECT TOP 1 pm.TargetPieces FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND Reason = 5 ORDER BY DateRec DESC)" +
//            "   ELSE" +
//            "       0" +
//            "   END" +
//            " ELSE" +
//            "   Pieces*100/TargetOrder" +
//            " END AS '%'" +
//            " FROM Machines AS m" +
//            " WHERE m.RoomCode = '" + roomCode + "'" +
//            " GROUP BY MachCode, StyleCode, Pieces, TargetOrder, IdealCycle, State" +
//            " ORDER BY m.MachCode"
}
