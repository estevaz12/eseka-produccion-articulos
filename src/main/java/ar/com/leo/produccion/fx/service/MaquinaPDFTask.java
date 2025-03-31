package ar.com.leo.produccion.fx.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Desktop;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import javafx.concurrent.Task;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class MaquinaPDFTask extends Task<Void> {

    private final List<List<String>> data;
    private static PDType1Font textFont;
    private static int textFontSize;

    public MaquinaPDFTask(List<List<String>> data) {
        this.data = data;
        textFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        textFontSize = 10;
    }

    @Override
    protected Void call() throws Exception {
        createPDF(data);
        return null;
    }

    /**
     * Creates an A4 PDF with a table that automatically paginates.
     * On the first page, a centered title is drawn at the top with the current date and time.
     *
     * @param data a list where each inner list represents a row of data;
     *             each row must contain data in the order:
     *             Maquina, Artículo, Un. Producidas, Target, Tiempo estimado, Estado.
     * @throws IOException if there is an error creating the PDF.
     * @throws NullPointerException if data is null.
     * @throws IndexOutOfBoundsException if any inner list in data has fewer than 6 elements.
     */
    public static void createPDF(List<List<String>> data) throws IOException {
        if (data == null) {
            throw new NullPointerException("Data to be written to the PDF cannot be null.");
        }

        PDDocument document = new PDDocument();

        // Use A4 page size.
        PDRectangle pageSize = PDRectangle.A4;
        float margin = 25;
        float yStart = pageSize.getHeight() - margin;
        float rowHeight = 20;
        // Compute table width based on margin.
        float tableWidth = pageSize.getWidth() - 2 * margin;
        int numberOfColumns = 6;

        // Fixed headers.
        String[] headers = {"Maquina", "Artículo", "Un. Producidas", "Target", "Tiempo Estimado", "Estado"};
        
        // Define column widths as fractions of the table width.
        float[] colWidths = calcColWidths(data, headers, textFont, textFontSize, tableWidth);
        
        // Create the first page and prepare the content stream.
        PDPage page = new PDPage(pageSize);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Draw the title at the top of the first page.
        // Setup title text: "CARGA DE MAQUINAS AL " with current date and time.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String currentDateTime = LocalDateTime.now().format(formatter);
        String title = "CARGA DE MAQUINAS AL " + currentDateTime;
        float titleFontSize = 16;
        // Use a bold font for the title.
        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        contentStream.setFont(titleFont, titleFontSize);
        // Compute width of the title string in points.
        float titleWidth = titleFont.getStringWidth(title) / 1000 * titleFontSize;
        float titleX = (pageSize.getWidth() - titleWidth) / 2;
        float titleY = pageSize.getHeight() - margin;
        
        contentStream.beginText();
        contentStream.newLineAtOffset(titleX, titleY);
        contentStream.showText(title);
        contentStream.endText();

        // Update yPosition for the table header below the title.
        // Create a gap of 10 points between the title and the table header.
        float yPosition = titleY - titleFontSize - 10;

        // Draw the table header on the first page.
        yPosition = drawTableHeader(contentStream, margin, yPosition, tableWidth, rowHeight, headers, colWidths);
        contentStream.setFont(textFont, textFontSize);
        // Move yPosition below the header.
        yPosition = yPosition - (rowHeight * 2);

        // Iterate through the data rows.
        for (List<String> row : data) {
            // Check if there is enough space for the next row.
            if (yPosition - rowHeight < margin) {
                // Close current page's content stream.
                contentStream.close();
                // Create a new page.
                page = new PDPage(pageSize);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                // Reset yPosition to the top (without title) and draw table header.
                yPosition = yStart;
                yPosition = drawTableHeader(contentStream, margin, yPosition, tableWidth, rowHeight, headers, colWidths);
                contentStream.setFont(textFont, textFontSize);
                yPosition = yPosition - (rowHeight * 2);
            }

            // Write each cell's text.
            float textX = margin;
            float textY = yPosition + rowHeight / 2 - 4; // Adjust text vertically within the cell.
            for (int i = 0; i < row.size() && i < numberOfColumns; i++) {
                String cellValue = row.get(i) == null ? "" : row.get(i);
                float textWidth = textFont.getStringWidth(cellValue) / 1000 * textFontSize; // Font size for data rows
                float cellCenterX = textX + (colWidths[i] - textWidth) / 2; // Calculate center for cell
                contentStream.beginText();
                contentStream.newLineAtOffset(cellCenterX, textY);
                contentStream.showText(cellValue);
                contentStream.endText();
                textX += colWidths[i]; // Move to next column
            }
            
            // Draw only the bottom stroke (horizontal line) for the row.
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + tableWidth, yPosition);
            contentStream.stroke();
            
            // Move to the next row.
            yPosition -= rowHeight;
        }
        
        // Close content stream and save the document.
        contentStream.close();
        document.save("maquinas.pdf");
        document.close();

        File pdfFile = new File("maquinas.pdf");
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(pdfFile);
            } catch (IOException e) {
                System.out.println("Error opening PDF file: " + e.getMessage());
            }
        } else {
            System.out.println("Desktop is not supported");
        }
    }

    /**
     * Draws the table header row and vertical underline separators.
     *
     * @param contentStream the current PDPageContentStream.
     * @param margin the left margin.
     * @param yPosition the y coordinate for the header row.
     * @param tableWidth the total width of the table.
     * @param rowHeight the height of the header row.
     * @param headers the header texts.
     * @param colWidths the column widths.
     * @return the current yPosition (unchanged here).
     * @throws IOException if there is an error writing the header.
     * @throws NullPointerException if any of the parameters are null.
     * @throws ArrayIndexOutOfBoundsException if headers.length > colWidths.length.
     */
    private static float drawTableHeader(PDPageContentStream contentStream, 
                                           float margin, 
                                           float yPosition, 
                                           float tableWidth, 
                                           float rowHeight, 
                                           String[] headers, 
                                           float[] colWidths) throws IOException {
        if (headers == null || colWidths == null) {
            throw new NullPointerException("headers and/or colWidths is null");
        }
        if (headers.length > colWidths.length) {
            throw new ArrayIndexOutOfBoundsException("headers.length > colWidths.length");
        }
        float textY = yPosition - rowHeight / 2 - 4;  // Adjust vertical text alignment.
        float textX = margin;
        
        // Set the header font (you can change font/style if desired).
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), textFontSize);
        
        // Draw header texts.
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            float textWidth = textFont.getStringWidth(header) / 1000 * textFontSize; // Font size for header
            float cellCenterX = textX + (colWidths[i] - textWidth) / 2; // Calculate center for header
            contentStream.beginText();
            contentStream.newLineAtOffset(cellCenterX, textY);
            contentStream.showText(header);
            contentStream.endText();
            textX += colWidths[i]; // Move to next column
        }
        
        // Draw a rectangle border for the header row.
        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
        contentStream.stroke();
        
        return yPosition;
    }

    private static float[] calcColWidths(List<List<String>> data, String[] headers, PDType1Font font, float fontSize, float tableWidth) throws IOException {
        int numColumns = headers.length;
        float[] colWidths = new float[numColumns];

        // Step 1: Autofit columns based on content
        for (int i = 0; i < numColumns; i++) {
            float headerWidth = font.getStringWidth(headers[i]) / 1000 * fontSize;
            colWidths[i] = headerWidth;
        }

        for (List<String> row : data) {
            for (int i = 0; i < numColumns && i < row.size(); i++) {
                String cellText = row.get(i) == null ? "" : row.get(i);
                float cellWidth = font.getStringWidth(cellText) / 1000 * fontSize;
                if (cellWidth > colWidths[i]) {
                    colWidths[i] = cellWidth;
                }
            }
        }

        // Step 2: Calculate total column width after autofitting
        float totalWidth = 0;
        for (float width : colWidths) {
            totalWidth += width;
        }

        // Step 3: Scale columns proportionally to fill the table width
        if (totalWidth < tableWidth) {
            float remainingSpace = tableWidth - totalWidth;
            float scalingFactor = remainingSpace / totalWidth;
            for (int i = 0; i < colWidths.length; i++) {
                colWidths[i] += colWidths[i] * scalingFactor; // Add proportional space to each column
            }
        }

        return colWidths;
    }
}
