package ar.com.leo.produccion.fx.service;

import ar.com.leo.produccion.jdbc.ArticuloProducidoDAO;
import ar.com.leo.produccion.model.ArticuloProducido;
import javafx.concurrent.Task;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Leo
 */
public class ExcelTask extends Task<Void> {

    private final String roomCode;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final boolean actual;
    private final String articulo;


    public ExcelTask(String roomCode, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean actual, String articulo) {
        this.roomCode = roomCode;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.actual = actual;
        this.articulo = articulo;
    }

    @Override
    protected Void call() throws Exception {
        produccionTabla(this.roomCode, this.fechaInicio, this.fechaFin, this.actual, this.articulo);
        return null;
    }

    public static void produccionTabla(String roomCode, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean actual, String articulo) throws IOException, SQLException {

        final List<ArticuloProducido> articulosProducidos = ArticuloProducidoDAO.obtenerProduccion(roomCode, fechaInicio, fechaFin, actual, articulo);

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("produccion");

        // Estilos
        final CellStyle styleFechas = styleFechas(workbook);
        final CellStyle styleHeader = styleHeader(workbook);
        final CellStyle styleCell = styleCell(workbook);
        final CellStyle styleTotal = styleTotal(workbook);

        // Fechas
        HSSFRow dateRow = sheet.createRow(0);
        HSSFCell date1Cell = dateRow.createCell(0);
        date1Cell.setCellValue("DESDE: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(fechaInicio));
        date1Cell.setCellStyle(styleFechas);
        HSSFCell date2Cell = dateRow.createCell(1);
        date2Cell.setCellValue("HASTA: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(fechaFin));
        date2Cell.setCellStyle(styleFechas);
        HSSFCell date3Cell = dateRow.createCell(2);
        date3Cell.setCellStyle(styleFechas);
        // Merge cells
        sheet.addMergedRegion(new CellRangeAddress(
                0, // first row
                0, // last row
                1, // first column
                2  // last column
        ));

        // Cabezeras
        HSSFRow headerRow = sheet.createRow(1);

        HSSFCell header0 = headerRow.createCell(0);
        header0.setCellValue("ARTICULO");
        header0.setCellStyle(styleHeader);

        HSSFCell header1 = headerRow.createCell(1);
        header1.setCellValue("UNIDADES");
        header1.setCellStyle(styleHeader);

        HSSFCell header2 = headerRow.createCell(2);
        header2.setCellValue("DOCENAS");
        header2.setCellStyle(styleHeader);

        String styleAnterior = "";
        int i = 2;
        for (ArticuloProducido articuloProducido : articulosProducidos) {
            HSSFRow row = sheet.createRow(i);

            HSSFCell cell0 = row.createCell(0);
            cell0.setCellValue(articuloProducido.getStyleCode());

            HSSFCell cell1 = row.createCell(1);
            cell1.setCellType(CellType.NUMERIC);
            cell1.setCellValue(articuloProducido.getUnidades());

            HSSFCell cell2 = row.createCell(2);
            cell2.setCellType(CellType.NUMERIC);
            cell2.setCellValue(articuloProducido.getDocenas());

            if (articuloProducido.getStyleCode().substring(0, articuloProducido.getStyleCode().indexOf(" ")).equals(styleAnterior)) {
                cell0.setCellStyle(styleCell);
                cell1.setCellStyle(styleCell);
                cell2.setCellStyle(styleCell);
            } else {
                CellStyle styleBorde = styleCell(workbook);
                styleBorde.setBorderTop(BorderStyle.THICK);
                cell0.setCellStyle(styleBorde);
                cell1.setCellStyle(styleBorde);
                cell2.setCellStyle(styleBorde);
            }

            styleAnterior = articuloProducido.getStyleCode().substring(0, articuloProducido.getStyleCode().indexOf(" "));
            i++;
        }
        // TOTALES
        HSSFRow rowTotal = sheet.createRow(i);
        HSSFCell total = rowTotal.createCell(0);
        total.setCellValue("TOTAL");
        total.setCellStyle(styleTotal);

        HSSFCell cellTotaUnidades = rowTotal.createCell(1);
        String formula1 = "SUM(B3:B" + i + ")";
        cellTotaUnidades.setCellFormula(formula1);
        cellTotaUnidades.setCellStyle(styleTotal);

        HSSFCell cellTotalDocenas = rowTotal.createCell(2);
        String formula2 = "SUM(C3:C" + i + ")";
        cellTotalDocenas.setCellFormula(formula2);
        cellTotalDocenas.setCellStyle(styleTotal);

        // Define the range of cells to apply border
        CellRangeAddress tableRange = new CellRangeAddress(0, sheet.getLastRowNum(), 0, 2);
        // Apply border to the range of cells
        RegionUtil.setBorderBottom(BorderStyle.THICK, tableRange, sheet);
        RegionUtil.setBorderTop(BorderStyle.THICK, tableRange, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THICK, tableRange, sheet);
        RegionUtil.setBorderRight(BorderStyle.THICK, tableRange, sheet);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2, true);

        final String destination = System.getProperty("user.dir") + "/Produccion.xls";
        FileOutputStream fileOut = new FileOutputStream(destination);
        workbook.write(fileOut);
        fileOut.close();
    }

    public static CellStyle styleHeader(HSSFWorkbook workbook) {
        // Create a new font and alter it.
        Font fontHeader = workbook.createFont();
        fontHeader.setFontHeightInPoints((short) 18);
        fontHeader.setFontName("Tahoma");
        fontHeader.setBold(true);
        // Fonts are set into a style so create a new one to use.
        CellStyle styleHeader = workbook.createCellStyle();
        styleHeader.setFont(fontHeader);
        styleHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeader.setAlignment(HorizontalAlignment.CENTER);
        styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
//        styleHeader.setWrapText(true);
        styleHeader.setBorderBottom(BorderStyle.THICK);
        styleHeader.setBorderLeft(BorderStyle.THICK);
        styleHeader.setBorderRight(BorderStyle.THICK);
        styleHeader.setBorderTop(BorderStyle.THICK);

        return styleHeader;
    }

    public static CellStyle styleFechas(HSSFWorkbook workbook) {
        // Create a new font and alter it.
        Font fontCell = workbook.createFont();
        fontCell.setFontHeightInPoints((short) 18);
        fontCell.setFontName("Calibri");
        fontCell.setItalic(true);
        CellStyle styleCell = workbook.createCellStyle();
        styleCell.setFont(fontCell);
        styleCell.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        styleCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleCell.setAlignment(HorizontalAlignment.CENTER);
        styleCell.setVerticalAlignment(VerticalAlignment.CENTER);
        styleCell.setWrapText(true);
        styleCell.setBorderBottom(BorderStyle.THICK);
        styleCell.setBorderLeft(BorderStyle.THICK);
        styleCell.setBorderRight(BorderStyle.THICK);
        styleCell.setBorderTop(BorderStyle.THICK);

        return styleCell;
    }

    public static CellStyle styleCell(HSSFWorkbook workbook) {
        // Create a new font and alter it.
        Font fontCell = workbook.createFont();
        fontCell.setFontHeightInPoints((short) 16);
        fontCell.setFontName("Tahoma");
//        fontCell.setColor(Font.COLOR_RED);
//        fontCell.setBold(true);
        // Fonts are set into a style so create a new one to use.
        CellStyle styleCell = workbook.createCellStyle();
        styleCell.setFont(fontCell);
//        styleCell.setFillForegroundColor(IndexedColors.BLUE.getIndex());
//        styleCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleCell.setAlignment(HorizontalAlignment.CENTER);
        styleCell.setVerticalAlignment(VerticalAlignment.CENTER);
//        styleCell.setWrapText(true);
        styleCell.setBorderBottom(BorderStyle.THIN);
        styleCell.setBorderLeft(BorderStyle.THIN);
        styleCell.setBorderRight(BorderStyle.THIN);
        styleCell.setBorderTop(BorderStyle.THIN);

        return styleCell;
    }

    public static CellStyle styleTotal(HSSFWorkbook workbook) {
        Font fontCell = workbook.createFont();
        fontCell.setFontHeightInPoints((short) 18);
        fontCell.setFontName("Tahoma");
        fontCell.setBold(true);
        CellStyle styleCell = workbook.createCellStyle();
        styleCell.setFont(fontCell);
        styleCell.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleCell.setAlignment(HorizontalAlignment.CENTER);
        styleCell.setVerticalAlignment(VerticalAlignment.CENTER);
//        styleCell.setWrapText(true);
        styleCell.setBorderBottom(BorderStyle.THICK);
        styleCell.setBorderLeft(BorderStyle.THICK);
        styleCell.setBorderRight(BorderStyle.THICK);
        styleCell.setBorderTop(BorderStyle.THICK);

        return styleCell;
    }

}
