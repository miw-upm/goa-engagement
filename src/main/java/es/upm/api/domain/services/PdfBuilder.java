package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.BadGatewayException;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.List;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class PdfBuilder {

    private static final BaseFont BASE_FONT_HELVETICA = requiredBaseFont(BaseFont.HELVETICA);
    private static final BaseFont BASE_FONT_HELVETICA_BOLD = requiredBaseFont(BaseFont.HELVETICA_BOLD);
    private static final Font FONT_NORMAL = createFont(BASE_FONT_HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_BOLD = createFont(BASE_FONT_HELVETICA_BOLD, 10, Font.BOLD);
    private static final Font FONT_TITLE = createFont(BASE_FONT_HELVETICA_BOLD, 14, Font.BOLD);
    private static final Font FONT_SECTION = createFont(BASE_FONT_HELVETICA_BOLD, 12, Font.BOLD);
    private static final Font FONT_SMALL = createFont(BASE_FONT_HELVETICA, 8, Font.NORMAL);
    private static final Font FONT_HEADER = createFont(BASE_FONT_HELVETICA, 9, Font.NORMAL);
    private static final Color HEADER_BG = new Color(240, 240, 240);

    private static final String COMPANY_NAME = "Ocaña Abogados";
    private static final String COMPANY_NIF = "46882956D";
    private static final String COMPANY_ADDRESS = "Paseo de la Castellana, 93-2º, 28046 Madrid";
    private static final String COMPANY_PHONE = "+34 644 993 593";
    private static final String COMPANY_EMAIL = "nuria@ocanabogados.es";
    private static final String COMPANY_WEB = "www.ocanabogados.es";
    private static final String LOGO_PATH = "images/oa.png";
    private static final String STAMP_PATH = "images/stamp.png";

    private final Document document;
    private final ByteArrayOutputStream outputStream;
    private final PdfWriter writer;

    public PdfBuilder() {
        this.outputStream = new ByteArrayOutputStream();
        this.document = new Document(PageSize.A4);
        try {
            this.writer = PdfWriter.getInstance(document, outputStream);
            this.writer.setPageEvent(new PageFooterEvent());
            document.open();
        } catch (Exception e) {
            throw this.onError("creating PDF", e);
        }
    }

    private static Font createFont(BaseFont baseFont, float size, int style) {
        return new Font(baseFont, size, style);
    }

    private static BaseFont requiredBaseFont(String fontName) {
        try {
            return BaseFont.createFont(fontName, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("PDF font initialization failed: " + fontName, e);
        }
    }

    public PdfBuilder header() {
        return this.add("header", () -> {
            PdfPTable table = this.createTable(2, 59, 41);
            table.addCell(this.createLogoCell());
            table.addCell(this.createInfoCell());
            document.add(table);
        }).space().line();
    }

    public PdfBuilder footer() {
        this.line().space();
        return this.add("footer", () -> {
            Paragraph info = new Paragraph();
            info.setAlignment(Element.ALIGN_CENTER);
            info.add(new Chunk(COMPANY_NAME + "\n", FONT_BOLD));
            info.add(new Chunk(COMPANY_ADDRESS + "\n", FONT_SMALL));
            info.add(new Chunk("Tel: " + COMPANY_PHONE + " | " + COMPANY_EMAIL + " | " + COMPANY_WEB, FONT_SMALL));
            document.add(info);
        });
    }

    public PdfBuilder title(String text) {
        return this.add("title", () -> {
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
        return this.add("section", () -> {
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
        return this.space(1);
    }

    public PdfBuilder space(int times) {
        return this.add("space", () -> {
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

    public PdfBuilder paragraph(String text) {
        return this.add("paragraph", () -> {
            Paragraph p = new Paragraph(text, FONT_NORMAL);
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(p);
        });
    }

    public PdfBuilder paragraphBold(String text) {
        return this.paragraphBold(text, Element.ALIGN_JUSTIFIED);
    }

    public PdfBuilder paragraphBold(String text, int alignment) {
        return this.add("bold paragraph", () -> {
            Paragraph p = new Paragraph(text, FONT_BOLD);
            p.setAlignment(alignment);
            document.add(p);
        });
    }

    public PdfBuilder paragraphs(String text) {
        for (String block : text.split("\n\n")) {
            block = block.trim();
            if (!block.isEmpty()) {
                this.paragraph(block);
                this.space();
            }
        }
        return this;
    }

    public PdfBuilder labelValue(String label, String value) {
        return this.add("label-value", () -> {
            Paragraph p = new Paragraph();
            p.add(new Chunk(label + ": ", FONT_BOLD));
            p.add(new Chunk(value != null ? value : "-", FONT_NORMAL));
            document.add(p);
        });
    }

    // === Listas ===
    public PdfBuilder list(java.util.List<String> items) {
        return this.add("list", () -> {
            List list = new List(List.UNORDERED);
            list.setListSymbol("- ");
            list.setIndentationLeft(15);
            items.forEach(item -> list.add(new ListItem(item, FONT_NORMAL)));
            document.add(list);
        });
    }

    public PdfBuilder numberedList(java.util.List<String> items) {
        return this.add("numbered list", () -> {
            List list = new List(List.ORDERED);
            list.setIndentationLeft(15);
            items.forEach(item -> list.add(new ListItem(item, FONT_NORMAL)));
            document.add(list);
        });
    }

    public PdfBuilder twoColumns(Consumer<ColumnBuilder> leftContent, Consumer<ColumnBuilder> rightContent) {
        return this.add("two columns", () -> {
            PdfPTable table = this.createTable(2);
            table.addCell(this.createColumnCell(leftContent, 0, 10));
            table.addCell(this.createColumnCell(rightContent, 10, 0));
            document.add(table);
        });
    }

    public PdfBuilder table(String[] headers, java.util.List<String[]> rows) {
        return this.add("table", () -> {
            this.validateTableInput(headers, rows);
            PdfPTable table = this.createDataTable(headers.length);
            this.addTableHeaders(table, headers);
            this.addTableRows(table, rows);
            document.add(table);
        });
    }

    public PdfBuilder table(String[] headers, float[] widths, java.util.List<String[]> rows) {
        return this.add("table", () -> {
            this.validateTableInput(headers, rows);
            this.validateColumnWidths(headers, widths);
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setSpacingAfter(5);
            this.addTableHeaders(table, headers);
            this.addTableRows(table, rows);
            document.add(table);
        });
    }

    public PdfBuilder tableWithTotal(String[] headers, java.util.List<String[]> rows, String totalLabel, String totalValue) {
        return this.add("table with total", () -> {
            this.validateTableInput(headers, rows);
            if (headers.length < 2) {
                throw new IllegalArgumentException("tableWithTotal requires at least 2 header columns");
            }
            PdfPTable table = this.createDataTable(headers.length);
            this.addTableHeaders(table, headers);
            this.addTableRows(table, rows);

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

    public PdfBuilder signatureLine(String label) {
        return this.add("signature", () -> {
            addStamp();
            Paragraph p = new Paragraph();
            p.add(new Chunk("_".repeat(40) + "\n", FONT_NORMAL));
            p.add(new Chunk(label, FONT_SMALL));
            document.add(p);
        });
    }

    public PdfBuilder multiSignature(java.util.List<String> leftLabels, String rightLabel) {
        return this.add("multi signature", () -> {
            PdfPTable table = this.createTable(2);

            PdfPCell leftCell = this.noBorderCell();
            for (String label : leftLabels) {
                Paragraph p = new Paragraph();
                p.add(new Chunk("\n\n"));
                p.add(new Chunk("_".repeat(30) + "\n", FONT_NORMAL));
                p.add(new Chunk(label, FONT_SMALL));
                leftCell.addElement(p);
            }
            table.addCell(leftCell);

            PdfPCell rightCell = this.noBorderCell();
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addStampToCell(rightCell);
            Paragraph right = new Paragraph();
            right.setAlignment(Element.ALIGN_RIGHT);
            right.add(new Chunk("_".repeat(30) + "\n", FONT_NORMAL));
            right.add(new Chunk(rightLabel, FONT_SMALL));
            rightCell.addElement(right);
            table.addCell(rightCell);

            document.add(table);
        });
    }

    private void addStamp() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(STAMP_PATH)) {
            if (is != null) {
                Image stamp = Image.getInstance(is.readAllBytes());
                stamp.scaleToFit(80, 80);
                stamp.setAlignment(Element.ALIGN_LEFT);
                document.add(stamp);
            }
        } catch (Exception e) {
            throw this.onError("loading stamp", e);
        }
    }

    private void addStampToCell(PdfPCell cell) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(STAMP_PATH)) {
            if (is != null) {
                Image stamp = Image.getInstance(is.readAllBytes());
                stamp.scaleToFit(80, 80);
                stamp.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(stamp);
            }
        } catch (Exception e) {
            throw this.onError("loading stamp", e);
        }
    }

    public PdfBuilder twoColumnSignature(String leftLabel, String rightLabel) {
        return this.add("signature lines", () -> {
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            PdfPTable table = this.createTable(2);
            table.addCell(this.createSignatureCell(leftLabel, Element.ALIGN_LEFT));
            table.addCell(this.createSignatureCell(rightLabel, Element.ALIGN_RIGHT));
            document.add(table);
        });
    }

    public PdfBuilder image(byte[] imageBytes, float width) {
        try {
            Image img = Image.getInstance(imageBytes);
            img.scaleToFit(width, 1000);
            img.setAlignment(Element.ALIGN_CENTER);
            document.add(img);
        } catch (IOException | DocumentException e) {
            throw this.onError("adding image", e);
        }
        return this;
    }

    public byte[] build() {
        document.close();
        return outputStream.toByteArray();
    }

    private PdfBuilder add(String action, DocumentAction runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw this.onError(action, e);
        }
        return this;
    }

    private PdfPTable createTable(int columns, float... widths) {
        try {
            PdfPTable table = widths.length > 0 ? new PdfPTable(widths) : new PdfPTable(columns);
            table.setWidthPercentage(100);
            return table;
        } catch (DocumentException e) {
            throw this.onError("creating table", e);
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

    private void validateTableInput(String[] headers, java.util.List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            throw new IllegalArgumentException("table headers must not be null or empty");
        }
        if (rows == null) {
            throw new IllegalArgumentException("table rows must not be null");
        }
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row == null) {
                throw new IllegalArgumentException("table row " + i + " must not be null");
            }
            if (row.length != headers.length) {
                throw new IllegalArgumentException("table row " + i + " has " + row.length
                        + " columns but expected " + headers.length);
            }
        }
    }

    private void validateColumnWidths(String[] headers, float[] widths) {
        if (widths == null || widths.length == 0) {
            throw new IllegalArgumentException("table widths must not be null or empty");
        }
        if (widths.length != headers.length) {
            throw new IllegalArgumentException("table widths size (" + widths.length
                    + ") must match headers size (" + headers.length + ")");
        }
    }

    private PdfPCell noBorderCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        return cell;
    }

    private PdfPCell createLogoCell() {
        PdfPCell cell = this.noBorderCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(LOGO_PATH)) {
            if (is != null) {
                Image logo = Image.getInstance(is.readAllBytes());
                logo.scaleToFit(80, 80);
                cell.addElement(logo);
            }
        } catch (Exception e) {
            throw this.onError("loading logo", e);
        }
        return cell;
    }

    private PdfPCell createInfoCell() {
        PdfPCell cell = this.noBorderCell();
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
        PdfPCell cell = this.noBorderCell();
        cell.setPaddingLeft(paddingLeft);
        cell.setPaddingRight(paddingRight);
        content.accept(new ColumnBuilder(cell));
        return cell;
    }

    private PdfPCell createSignatureCell(String label, int alignment) {
        PdfPCell cell = this.noBorderCell();
        Paragraph p = new Paragraph();
        p.setAlignment(alignment);
        p.add(new Chunk("_".repeat(30) + "\n", FONT_NORMAL));
        p.add(new Chunk(label, FONT_SMALL));
        cell.addElement(p);
        return cell;
    }

    private BadGatewayException onError(String action, Exception e) {
        return new BadGatewayException("Error " + action + ": " + e.getMessage());
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
            return this.paragraphBold(text, Element.ALIGN_LEFT);
        }

        public ColumnBuilder paragraphBold(String text, int alignment) {
            Paragraph p = new Paragraph(text, FONT_BOLD);
            p.setAlignment(alignment);
            cell.addElement(p);
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

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPages = writer.getDirectContent().createTemplate(30, 12);
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
            float pageTextWidth = BASE_FONT_HELVETICA.getWidthPoint(pageText, 8);
            cb.beginText();
            cb.setFontAndSize(BASE_FONT_HELVETICA, 8);
            cb.setTextMatrix(document.right() - pageTextWidth - 20, y);
            cb.showText(pageText);
            cb.endText();
            cb.addTemplate(totalPages, document.right() - 20, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            int totalPageCount = Math.max(1, writer.getPageNumber() - 1);
            totalPages.beginText();
            totalPages.setFontAndSize(BASE_FONT_HELVETICA, 8);
            totalPages.showText(String.valueOf(totalPageCount));
            totalPages.endText();
        }
    }
}
