package org.nuxeo.ecm.platform.signature.appearance;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.signature.api.sign.SignatureAppearanceFactory;
import org.nuxeo.ecm.platform.signature.api.sign.SignatureLayout;
import org.nuxeo.ecm.platform.signature.api.sign.SignatureService;
import org.nuxeo.ecm.platform.signature.core.sign.DefaultSignatureAppearanceFactory;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfSignatureAppearance;

public class CustomSignatureAppearanceFactory implements SignatureAppearanceFactory {

    private static final String LAST_SIGNATURE_USER = "Administrator";
    protected static final Log LOGGER = LogFactory.getLog(CustomSignatureAppearanceFactory.class);

    @Override
    public void format(PdfSignatureAppearance pdfSignatureAppearance, DocumentModel doc, String principal, String reason) {
        final String logPrefix = "<format> ";
        if (isLastSignature(doc, principal, reason)) {
            // set background image
            addBackgroundImage(pdfSignatureAppearance);

            pdfSignatureAppearance.setReason(reason);
            
            // change signature height - hard-coded values in 'signature' service are too small!!
            Rectangle pageRect = pdfSignatureAppearance.getPageRect();
            LOGGER.info("position upper right X:" + pageRect.getRight());
            LOGGER.info("position upper right Y:" + pageRect.getTop());
            LOGGER.info("position lower left X:" + pageRect.getLeft());
            LOGGER.info("position lower left Y:" + pageRect.getBottom());
            // TODO this does not work
            //pageRect.setBottom(pageRect.getBottom()+50); // add 50 pixels
            
            // use Acrobat 6 layers (n0 - background layer, n2 - signature appearance)
            pdfSignatureAppearance.setAcro6Layers(true);
            String[] contributors = (String[]) doc.getPropertyValue("dc:contributors");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
            DocumentModel userModel = Framework.getService(UserManager.class).getUserModel(principal);
            StringBuilder sb = new StringBuilder("Digitally signed by ");
            sb.append(userModel.getPropertyValue("user:firstName")).append(" ").append(userModel.getPropertyValue("user:lastName")).append("\n")
            .append("Date: ").append(dateFormat.format(pdfSignatureAppearance.getSignDate().getTime())).append("\n")
            .append("Reason: ").append(reason).append("\n")
            .append("This document has been approved by all contributors listed: ").append(StringUtils.join(contributors, ", ")).append("\n");
            LOGGER.debug(logPrefix + pdfSignatureAppearance.getLayer2Font());
            pdfSignatureAppearance.setLayer2Text(sb.toString());
            SignatureService service = Framework.getService(SignatureService.class);
            SignatureLayout layout = service.getSignatureLayout();
            Font layer2Font = FontFactory.getFont(FontFactory.TIMES, layout.getTextSize(), Font.NORMAL, new Color(0x00, 0x00, 0x00));
            pdfSignatureAppearance.setLayer2Font(layer2Font);
        } else {
            new DefaultSignatureAppearanceFactory().format(pdfSignatureAppearance, doc, principal, reason);
        }
    }

    private void addBackgroundImage(PdfSignatureAppearance pdfSignatureAppearance) {
        // TODO adjust image size, ratio, ...
        InputStream in = getClass().getResourceAsStream("/ICON_sign.png");
        try {
            pdfSignatureAppearance.setImage(Image.getInstance(IOUtils.toByteArray(in)));
        } catch (BadElementException e) {
            LOGGER.error(e, e);
        } catch (MalformedURLException e) {
            LOGGER.error(e, e);
        } catch (IOException e) {
            LOGGER.error(e, e);
        }
    }
    
    protected boolean isLastSignature(DocumentModel doc, String principal, String reason) {
        return LAST_SIGNATURE_USER.equals(principal);
    }

}
