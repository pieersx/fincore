package com.fincore.transfers.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fincore.transfers.dto.TransferView;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.springframework.stereotype.Service;

@Service
public class TransferReceiptService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z")
            .withZone(ZoneId.of("America/Lima"));

    /** Genera el comprobante bajo demanda; no almacena un archivo duplicado en la base. */
    public byte[] generate(TransferView transfer) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = 740;
                y = writeLine(content, "FinCore", 18, y, true);
                y = writeLine(content, "Comprobante de transferencia interna", 14, y - 8, true);
                y = writeLine(content, "Simulador educativo - no representa dinero real", 10, y - 8, false);
                y = writeLine(content, "Referencia: " + transfer.reference(), 11, y - 22, false);
                y = writeLine(content, "Estado: " + transfer.status(), 11, y, false);
                y = writeLine(content, "Fecha: " + DATE_TIME.format(transfer.completedAt()), 11, y, false);
                y = writeLine(content, "Cuenta origen: " + mask(transfer.sourceAccountNumber()), 11, y, false);
                y = writeLine(content, "Cuenta destino: " + mask(transfer.destinationAccountNumber()), 11, y, false);
                y = writeLine(content, "Moneda: " + transfer.currency(), 11, y, false);
                y = writeLine(content, "Monto: " + money(transfer.amount()), 13, y, true);
                writeLine(
                        content,
                        "Descripcion: " + (transfer.description() == null ? "Sin descripcion" : transfer.description()),
                        11,
                        y,
                        false);
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el comprobante PDF.", exception);
        }
    }

    private float writeLine(PDPageContentStream content, String text, float size, float y, boolean bold)
            throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(bold ? FontName.HELVETICA_BOLD : FontName.HELVETICA), size);
        content.newLineAtOffset(55, y);
        content.showText(toPdfText(text));
        content.endText();
        return y - 24;
    }

    private String mask(String accountNumber) {
        return "********" + accountNumber.substring(Math.max(0, accountNumber.length() - 4));
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2).toPlainString();
    }

    /** Las fuentes PDF estándar requieren texto WinAnsi; normalizamos entradas Unicode. */
    private String toPdfText(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x20-\\x7E]", "?");
    }
}
