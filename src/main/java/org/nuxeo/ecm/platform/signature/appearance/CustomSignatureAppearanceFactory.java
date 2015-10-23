package org.nuxeo.ecm.platform.signature.appearance;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.signature.api.sign.SignatureAppearanceFactory;
import org.nuxeo.ecm.platform.signature.core.sign.DefaultSignatureAppearanceFactory;

import com.lowagie.text.pdf.PdfSignatureAppearance;

public class CustomSignatureAppearanceFactory implements SignatureAppearanceFactory {

    protected static final Log LOGGER = LogFactory.getLog(CustomSignatureAppearanceFactory.class);

    @Override
    public void format(PdfSignatureAppearance pdfSignatureAppearance, DocumentModel doc, String principal, String reason) {
        final String logPrefix = "<format> ";
        if (isLastSignature(doc, principal, reason)) {
            pdfSignatureAppearance.setReason(reason);
            // use Acrobat 6 layers (n0 - background layer, n2 - signature appearance)
            pdfSignatureAppearance.setAcro6Layers(true);
            String[] contributors = (String[]) doc.getPropertyValue("dc:contributors");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
            StringBuilder sb = new StringBuilder("This document has been approved by all contributors listed: ");
            sb.append(StringUtils.join(contributors, ", ")).append("\n")
            .append("Date: ").append(dateFormat.format(pdfSignatureAppearance.getSignDate().getTime())).append("\n")
            .append("Reason: ").append(reason).append("\n");
            LOGGER.debug(logPrefix + pdfSignatureAppearance.getLayer2Font());
            pdfSignatureAppearance.setLayer2Text(sb.toString());
        } else {
            new DefaultSignatureAppearanceFactory().format(pdfSignatureAppearance, doc, principal, reason);
        }
    }
    
    protected boolean isLastSignature(DocumentModel doc, String principal, String reason) {
        return "Administrator".equals(principal);
    }

}
