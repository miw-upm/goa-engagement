package es.upm.api.domain.services;

import es.upm.miw.pdf.PdfException;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.draw.LineSeparator;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PdfBuilder {

    private final Document document;
    private final String filename;

    private static final Font FONT_NORMAL = new Font(Font.HELVETICA, 10);
    private static final Font FONT_BOLD = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_SMALL = new Font(Font.HELVETICA, 8);
    private static final Font FONT_HEADER = new Font(Font.HELVETICA, 9);

    private static final String COMPANY_NAME = "Ocaña Abogados";
    private static final String COMPANY_NIF = "46882956D";
    private static final String COMPANY_ADDRESS = "Paseo de la Castellana, 93-2º, 28046 Madrid";
    private static final String COMPANY_PHONE = "+34 644 993 593";
    private static final String COMPANY_EMAIL = "nuria@ocanabogados.es";
    private static final String COMPANY_WEB = "www.ocanabogados.es";
    public static final String LOGO_PATH = "images/oa.png";

    public PdfBuilder(String name) {
        this.filename = Path.of(System.getProperty("java.io.tmpdir"), name + ".pdf").toString();
        this.document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
        } catch (Exception e) {
            throw this.onError("creating PDF", e);
        }
    }

    public PdfBuilder header() {
        try {
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{59, 41});

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            this.addLogoToCell(logoCell);
            header.addCell(logoCell);

            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph info = new Paragraph();
            info.add(new Chunk(COMPANY_NAME + "\n", FONT_BOLD));
            info.add(new Chunk("NIF: " + COMPANY_NIF + "\n", FONT_HEADER));
            info.add(new Chunk(COMPANY_ADDRESS + "\n", FONT_HEADER));
            info.add(new Chunk("Tel: " + COMPANY_PHONE + "\n", FONT_HEADER));
            info.add(new Chunk(COMPANY_EMAIL + " | " + COMPANY_WEB, FONT_HEADER));
            infoCell.addElement(info);

            header.addCell(infoCell);
            document.add(header);
            document.add(Chunk.NEWLINE);
            this.line();
        } catch (DocumentException e) {
            throw this.onError("adding header", e);
        }
        return this;
    }

    private void addLogoToCell(PdfPCell logoCell) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(LOGO_PATH)) {
            if (is != null) {
                byte[] logoBytes = is.readAllBytes();
                Image logo = Image.getInstance(logoBytes);
                logo.scaleToFit(80, 80);
                logoCell.addElement(logo);
            }
        } catch (IOException | BadElementException ignored) {
        }
    }

    public PdfBuilder footer() {
        try {
            this.line();
            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Chunk(COMPANY_EMAIL + " | " + COMPANY_WEB, FONT_SMALL));
            document.add(footer);
        } catch (DocumentException e) {
            throw this.onError("adding footer", e);
        }
        return this;
    }

    public PdfBuilder title(String text) {
        try {
            Paragraph title = new Paragraph(text, FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);
        } catch (DocumentException e) {
            throw this.onError("adding title", e);
        }
        return this;
    }

    public PdfBuilder section(String text) {
        try {
            document.add(Chunk.NEWLINE);
            Paragraph section = new Paragraph(text, FONT_SECTION);
            section.setSpacingAfter(5);
            document.add(section);
        } catch (DocumentException e) {
            throw this.onError("adding section", e);
        }
        return this;
    }

    public PdfBuilder paragraph(String text) {
        try {
            Paragraph p = new Paragraph(text, FONT_NORMAL);
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(p);
        } catch (DocumentException e) {
            throw this.onError("adding paragraph", e);
        }
        return this;
    }

    public PdfBuilder paragraphBold(String text) {
        return paragraphBold(text, Element.ALIGN_JUSTIFIED);
    }

    public PdfBuilder paragraphBold(String text, int alignment) {
        try {
            Paragraph p = new Paragraph(text, FONT_BOLD);
            p.setAlignment(alignment);
            document.add(p);
        } catch (DocumentException e) {
            throw this.onError("adding bold paragraph", e);
        }
        return this;
    }

    public PdfBuilder paragraphs(String text) {
        String[] blocks = text.split("\n\n");
        for (String block : blocks) {
            block = block.trim();
            if (!block.isEmpty()) {
                paragraph(block);
                space();
            }
        }
        return this;
    }

    public PdfBuilder labelValue(String label, String value) {
        try {
            Paragraph p = new Paragraph();
            p.add(new Chunk(label + ": ", FONT_BOLD));
            p.add(new Chunk(value != null ? value : "-", FONT_NORMAL));
            document.add(p);
        } catch (DocumentException e) {
            throw this.onError("adding label-value", e);
        }
        return this;
    }

    public PdfBuilder line() {
        try {
            document.add(new Paragraph(new Chunk(new LineSeparator())));
        } catch (DocumentException e) {
            throw this.onError("adding line", e);
        }
        return this;
    }

    public PdfBuilder space() {
        try {
            Paragraph p = new Paragraph(" ");
            p.setLeading(6);
            document.add(p);
        } catch (DocumentException e) {
            throw this.onError("adding space", e);
        }
        return this;
    }

    public PdfBuilder twoColumns(Consumer<ColumnBuilder> leftContent,
                                 Consumer<ColumnBuilder> rightContent) {
        try {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPaddingRight(10);
            leftContent.accept(new ColumnBuilder(leftCell));
            table.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPaddingLeft(10);
            rightContent.accept(new ColumnBuilder(rightCell));
            table.addCell(rightCell);

            document.add(table);
        } catch (DocumentException e) {
            throw this.onError("adding two columns", e);
        }
        return this;
    }

    public PdfBuilder table(String[] headers, java.util.List<String[]> rows) {
        try {
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setSpacingAfter(5);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FONT_BOLD));
                cell.setBackgroundColor(new Color(240, 240, 240));
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (String[] row : rows) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", FONT_NORMAL));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

            document.add(table);
        } catch (DocumentException e) {
            throw this.onError("adding table", e);
        }
        return this;
    }

    public PdfBuilder table(String[] headers, float[] widths, java.util.List<String[]> rows) {
        try {
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setSpacingAfter(5);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FONT_BOLD));
                cell.setBackgroundColor(new Color(240, 240, 240));
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (String[] row : rows) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", FONT_NORMAL));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

            document.add(table);
        } catch (DocumentException e) {
            throw this.onError("adding table", e);
        }
        return this;
    }

    public PdfBuilder tableWithTotal(String[] headers, java.util.List<String[]> rows, String totalLabel, String totalValue) {
        try {
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setSpacingAfter(5);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FONT_BOLD));
                cell.setBackgroundColor(new Color(240, 240, 240));
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (String[] row : rows) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", FONT_NORMAL));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

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
        } catch (DocumentException e) {
            throw this.onError("adding table with total", e);
        }
        return this;
    }

    public PdfBuilder list(java.util.List<String> items) {
        try {
            List list = new List(List.UNORDERED);
            list.setListSymbol("- ");
            list.setIndentationLeft(15);
            items.forEach(item -> list.add(new ListItem(item, FONT_NORMAL)));
            document.add(list);
        } catch (DocumentException e) {
            throw this.onError("adding list", e);
        }
        return this;
    }

    public PdfBuilder numberedList(java.util.List<String> items) {
        try {
            List list = new List(List.ORDERED);
            items.forEach(item -> list.add(new ListItem(item, FONT_NORMAL)));
            document.add(list);
        } catch (DocumentException e) {
            throw this.onError("adding numbered list", e);
        }
        return this;
    }

    public PdfBuilder image(byte[] imageBytes, float width) {
        try {
            Image img = Image.getInstance(imageBytes);
            img.scaleToFit(width, 1000);
            img.setAlignment(Image.ALIGN_CENTER);
            document.add(img);
        } catch (Exception e) {
            throw this.onError("adding image", e);
        }
        return this;
    }

    public PdfBuilder signatureLine(String label) {
        try {
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            Paragraph signature = new Paragraph();
            signature.add(new Chunk("_".repeat(40) + "\n", FONT_NORMAL));
            signature.add(new Chunk(label, FONT_SMALL));
            document.add(signature);
        } catch (DocumentException e) {
            throw this.onError("adding signature line", e);
        }
        return this;
    }

    public PdfBuilder twoColumnSignature(String leftLabel, String rightLabel) {
        try {
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            Paragraph left = new Paragraph();
            left.add(new Chunk("_".repeat(30) + "\n", FONT_NORMAL));
            left.add(new Chunk(leftLabel, FONT_SMALL));
            leftCell.addElement(left);
            table.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph right = new Paragraph();
            right.setAlignment(Element.ALIGN_RIGHT);
            right.add(new Chunk("_".repeat(30) + "\n", FONT_NORMAL));
            right.add(new Chunk(rightLabel, FONT_SMALL));
            rightCell.addElement(right);
            table.addCell(rightCell);

            document.add(table);
        } catch (DocumentException e) {
            throw this.onError("adding signature lines", e);
        }
        return this;
    }

    public PdfBuilder pageBreak() {
        document.newPage();
        return this;
    }

    private PdfException onError(String action, Exception e) {
        return new PdfException("Error " + action + ": " + e.getMessage());
    }

    public byte[] build() {
        document.close();
        try {
            return Files.readAllBytes(Path.of(filename));
        } catch (IOException e) {
            throw this.onError("reading PDF", e);
        }
    }

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
}