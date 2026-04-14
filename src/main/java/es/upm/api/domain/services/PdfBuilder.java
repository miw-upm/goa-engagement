package es.upm.api.domain.services;

import es.upm.miw.pdf.PdfException;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.List;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PdfBuilder {

    private static final Font FONT_NORMAL = new Font(Font.HELVETICA, 10);
    private static final Font FONT_BOLD = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_SMALL = new Font(Font.HELVETICA, 8);
    private static final Font FONT_HEADER = new Font(Font.HELVETICA, 9);
    private static final Color HEADER_BG = new Color(240, 240, 240);

    private static final String COMPANY_NAME = "Ocaña Abogados";
    private static final String COMPANY_NIF = "46882956D";
    private static final String COMPANY_ADDRESS = "Paseo de la Castellana, 93-2º, 28046 Madrid";
    private static final String COMPANY_PHONE = "+34 644 993 593";
    private static final String COMPANY_EMAIL = "nuria@ocanabogados.es";
    private static final String COMPANY_WEB = "www.ocanabogados.es";
    private static final String LOGO_PATH = "images/oa.png";

    private final Document document;
    private final String filename;
    private final PdfWriter writer;

    public PdfBuilder(String name) {
        this.filename = Path.of(System.getProperty("java.io.tmpdir"), name + ".pdf").toString();
        this.document = new Document(PageSize.A4);
        try {
            this.writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            this.writer.setPageEvent(new PageFooterEvent());
            document.open();
        } catch (Exception e) {
            throw onError("creating PDF", e);
        }
    }

    // === Estructura ===
    public PdfBuilder header() {
        return add("header", () -> {
            PdfPTable table = createTable(2, 59, 41);
            table.addCell(createLogoCell());
            table.addCell(createInfoCell());
            document.add(table);
            document.add(Chunk.NEWLINE);
        }).line();
    }

    public PdfBuilder footer() {
        line().space();
        return add("footer", () -> {
            Paragraph info = new Paragraph();
            info.setAlignment(Element.ALIGN_CENTER);
            info.add(new Chunk(COMPANY_NAME + "\n", FONT_BOLD));
            info.add(new Chunk(COMPANY_ADDRESS + "\n", FONT_SMALL));
            info.add(new Chunk("Tel: " + COMPANY_PHONE + " | " + COMPANY_EMAIL + " | " + COMPANY_WEB, FONT_SMALL));
            document.add(info);
        });
    }

    public PdfBuilder title(String text) {
        return add("title", () -> {
            Paragraph p = new Paragraph(text, FONT_TITLE);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(10);
            document.add(p);
        });
    }

    public PdfBuilder section(String text) {
        if (writer.getVerticalPosition(true) < document.bottomMargin() + 50) {
            document.newPage();
        }
        return add("section", () -> {
            document.add(Chunk.NEWLINE);
            Paragraph p = new Paragraph(text, FONT_SECTION);
            p.setSpacingAfter(5);
            document.add(p);
        });
    }

    public PdfBuilder line() {
        PdfContentByte cb = writer.getDirectContent();
        float y = writer.getVerticalPosition(true) - 5;
        cb.setLineWidth(1f);
        cb.moveTo(document.leftMargin(), y);
        cb.lineTo(document.right(), y);
        cb.stroke();
        return this;
    }

    public PdfBuilder space() {
        return space(1);
    }

    public PdfBuilder space(int times) {
        return add("space", () -> {
            for (int i = 0; i < times; i++) {
                Paragraph p = new Paragraph(" ");
                p.setLeading(6);
                document.add(p);
            }
        });
    }

    public PdfBuilder pageBreak() {
        document.newPage();
        return this;
    }

    // === Texto ===
    public PdfBuilder paragraph(String text) {
        return add("paragraph", () -> {
            Paragraph p = new Paragraph(text, FONT_NORMAL);
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(p);
        });
    }

    public PdfBuilder paragraphBold(String text) {
        return paragraphBold(text, Element.ALIGN_JUSTIFIED);
    }

    public PdfBuilder paragraphBold(String text, int alignment) {
        return add("bold paragraph", () -> {
            Paragraph p = new Paragraph(text, FONT_BOLD);
            p.setAlignment(alignment);
            document.add(p);
        });
    }

    public PdfBuilder paragraphs(String text) {
        for (String block : text.split("\n\n")) {
            block = block.trim();
            if (!block.isEmpty()) {
                paragraph(block);
                space();
            }
        }
        return this;
    }

    public PdfBuilder labelValue(String label, String value) {
        return add("label-value", () -> {
            Paragraph p = new Paragraph();
            p.add(new Chunk(label + ": ", FONT_BOLD));
            p.add(new Chunk(value != null ? value : "-", FONT_NORMAL));
            document.add(p);
        });
    }

    // === Listas ===
    public PdfBuilder list(java.util.List<String> items) {
        return add("list", () -> {
            List list = new List(List.UNORDERED);
            list.setListSymbol("- ");
            list.setIndentationLeft(15);
            items.forEach(item -> list.add(new ListItem(item, FONT_NORMAL)));
            document.add(list);
        });
    }

    public PdfBuilder numberedList(java.util.List<String> items) {
        return add("numbered list", () -> {
            List list = new List(List.ORDERED);
            list.setIndentationLeft(15);
            items.forEach(item -> list.add(new ListItem(item, FONT_NORMAL)));
            document.add(list);
        });
    }

    // === Tablas ===
    public PdfBuilder twoColumns(Consumer<ColumnBuilder> leftContent, Consumer<ColumnBuilder> rightContent) {
        return add("two columns", () -> {
            PdfPTable table = createTable(2);
            table.addCell(createColumnCell(leftContent, 0, 10));
            table.addCell(createColumnCell(rightContent, 10, 0));
            document.add(table);
        });
    }

    public PdfBuilder table(String[] headers, java.util.List<String[]> rows) {
        return add("table", () -> {
            PdfPTable table = createDataTable(headers.length);
            addTableHeaders(table, headers);
            addTableRows(table, rows);
            document.add(table);
        });
    }

    public PdfBuilder table(String[] headers, float[] widths, java.util.List<String[]> rows) {
        return add("table", () -> {
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setSpacingAfter(5);
            addTableHeaders(table, headers);
            addTableRows(table, rows);
            document.add(table);
        });
    }

    public PdfBuilder tableWithTotal(String[] headers, java.util.List<String[]> rows, String totalLabel, String totalValue) {
        return add("table with total", () -> {
            PdfPTable table = createDataTable(headers.length);
            addTableHeaders(table, headers);
            addTableRows(table, rows);

            PdfPCell labelCell = new PdfPCell(new Phrase(totalLabel, FONT_BOLD));
            labelCell.setColspan(headers.length - 1);
            labelCell.setPadding(5);
            labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            labelCell.setBackgroundColor(new Color(250, 250, 250));
            table.addCell(labelCell);

            PdfPCell valueCell = new PdfPCell(new Phrase(totalValue, FONT_BOLD));
            valueCell.setPadding(5);
            valueCell.setBackgroundColor(new Color(250, 250, 250));
            table.addCell(valueCell);

            document.add(table);
        });
    }

    // === Firmas ===

    public PdfBuilder signatureLine(String label) {
        return add("signature", () -> {
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            Paragraph p = new Paragraph();
            p.add(new Chunk("_".repeat(40) + "\n", FONT_NORMAL));
            p.add(new Chunk(label, FONT_SMALL));
            document.add(p);
        });
    }

    public PdfBuilder twoColumnSignature(String leftLabel, String rightLabel) {
        return add("signature lines", () -> {
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            PdfPTable table = createTable(2);
            table.addCell(createSignatureCell(leftLabel, Element.ALIGN_LEFT));
            table.addCell(createSignatureCell(rightLabel, Element.ALIGN_RIGHT));
            document.add(table);
        });
    }

    // === Imagen ===
    public PdfBuilder image(byte[] imageBytes, float width) {
        try {
            Image img = Image.getInstance(imageBytes);
            img.scaleToFit(width, 1000);
            img.setAlignment(Element.ALIGN_CENTER);
            document.add(img);
        } catch (IOException | DocumentException e) {
            throw onError("adding image", e);
        }
        return this;
    }

    // === Build ===
    public byte[] build() {
        document.close();
        try {
            return Files.readAllBytes(Path.of(filename));
        } catch (IOException e) {
            throw onError("reading PDF", e);
        }
    }

    // === Helpers privados ===
    private PdfBuilder add(String action, DocumentAction runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw onError(action, e);
        }
        return this;
    }

    private PdfPTable createTable(int columns, float... widths) {
        try {
            PdfPTable table = widths.length > 0 ? new PdfPTable(widths) : new PdfPTable(columns);
            table.setWidthPercentage(100);
            return table;
        } catch (DocumentException e) {
            throw onError("creating table", e);
        }
    }

    private PdfPTable createDataTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);
        table.setSpacingAfter(5);
        return table;
    }

    private void addTableHeaders(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_BOLD));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableRows(PdfPTable table, java.util.List<String[]> rows) {
        for (String[] row : rows) {
            for (String value : row) {
                PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", FONT_NORMAL));
                cell.setPadding(5);
                table.addCell(cell);
            }
        }
    }

    private PdfPCell noBorderCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        return cell;
    }

    private PdfPCell createLogoCell() {
        PdfPCell cell = noBorderCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(LOGO_PATH)) {
            if (is != null) {
                Image logo = Image.getInstance(is.readAllBytes());
                logo.scaleToFit(80, 80);
                cell.addElement(logo);
            }
        } catch (Exception ignored) {
        }
        return cell;
    }

    private PdfPCell createInfoCell() {
        PdfPCell cell = noBorderCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph info = new Paragraph();
        info.add(new Chunk(COMPANY_NAME + "\n", FONT_BOLD));
        info.add(new Chunk("NIF: " + COMPANY_NIF + "\n", FONT_HEADER));
        info.add(new Chunk(COMPANY_ADDRESS + "\n", FONT_HEADER));
        info.add(new Chunk("Tel: " + COMPANY_PHONE + "\n", FONT_HEADER));
        info.add(new Chunk(COMPANY_EMAIL + " | " + COMPANY_WEB, FONT_HEADER));
        cell.addElement(info);
        return cell;
    }

    private PdfPCell createColumnCell(Consumer<ColumnBuilder> content, int paddingLeft, int paddingRight) {
        PdfPCell cell = noBorderCell();
        cell.setPaddingLeft(paddingLeft);
        cell.setPaddingRight(paddingRight);
        content.accept(new ColumnBuilder(cell));
        return cell;
    }

    private PdfPCell createSignatureCell(String label, int alignment) {
        PdfPCell cell = noBorderCell();
        Paragraph p = new Paragraph();
        p.setAlignment(alignment);
        p.add(new Chunk("_".repeat(30) + "\n", FONT_NORMAL));
        p.add(new Chunk(label, FONT_SMALL));
        cell.addElement(p);
        return cell;
    }

    private PdfException onError(String action, Exception e) {
        return new PdfException("Error " + action + ": " + e.getMessage());
    }

    @FunctionalInterface
    private interface DocumentAction {
        void run() throws DocumentException;
    }

    // === ColumnBuilder ===
    public static class ColumnBuilder {
        private final PdfPCell cell;

        ColumnBuilder(PdfPCell cell) {
            this.cell = cell;
        }

        public ColumnBuilder paragraph(String text) {
            cell.addElement(new Paragraph(text, FONT_NORMAL));
            return this;
        }

        public ColumnBuilder paragraphBold(String text) {
            cell.addElement(new Paragraph(text, FONT_BOLD));
            return this;
        }

        public ColumnBuilder section(String text) {
            cell.addElement(new Paragraph(text, FONT_SECTION));
            return this;
        }

        public ColumnBuilder space() {
            cell.addElement(Chunk.NEWLINE);
            return this;
        }

        public ColumnBuilder list(java.util.List<String> items) {
            List list = new List(List.UNORDERED);
            list.setListSymbol("• ");
            items.forEach(item -> list.add(new ListItem(item, FONT_NORMAL)));
            cell.addElement(list);
            return this;
        }

        public ColumnBuilder labelValue(String label, String value) {
            Paragraph p = new Paragraph();
            p.add(new Chunk(label + ": ", FONT_BOLD));
            p.add(new Chunk(value != null ? value : "-", FONT_NORMAL));
            cell.addElement(p);
            return this;
        }
    }

    // === PageFooterEvent ===
    private static class PageFooterEvent extends PdfPageEventHelper {

        private PdfTemplate totalPages;
        private BaseFont baseFont;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPages = writer.getDirectContent().createTemplate(30, 12);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                baseFont = null;
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottomMargin() - 10;

            // Línea
            cb.setLineWidth(0.5f);
            cb.moveTo(document.leftMargin(), y + 15);
            cb.lineTo(document.right(), y + 15);
            cb.stroke();

            // Contacto centrado
            Phrase footer = new Phrase(COMPANY_EMAIL + "  |  " + COMPANY_PHONE, FONT_SMALL);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                    (document.right() + document.leftMargin()) / 2, y, 0);

            // Número de página a la derecha
            String pageText = "Página " + writer.getPageNumber() + " de ";
            float pageTextWidth = baseFont.getWidthPoint(pageText, 8);
            cb.beginText();
            cb.setFontAndSize(baseFont, 8);
            cb.setTextMatrix(document.right() - pageTextWidth - 20, y);
            cb.showText(pageText);
            cb.endText();
            cb.addTemplate(totalPages, document.right() - 20, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPages.beginText();
            totalPages.setFontAndSize(baseFont, 8);
            totalPages.showText(String.valueOf(writer.getPageNumber()));
            totalPages.endText();
        }
    }
}
