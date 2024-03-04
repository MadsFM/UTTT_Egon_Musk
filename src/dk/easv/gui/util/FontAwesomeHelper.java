/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.easv.gui.util;
// https://bintray.com/jerady/maven/FontAwesomeFX/8.15#files/de%2Fjensd%2Ffontawesomefx-commons%2F8.15
// https://bintray.com/jerady/maven/FontAwesomeFX/8.15#files/de%2Fjensd%2Ffontawesomefx-fontawesome%2F4.7.0-5
// https://bintray.com/jerady/maven/FontAwesomeFX/8.15#files/de%2Fjensd%2Ffontawesomefx-materialdesignfont%2F1.7.22-4
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.scene.text.Text;

/**
 *
 * @author jeppjleemoritzled
 */
public class FontAwesomeHelper {
    public static Text getFontAwesomeIconFromPlayerId(String playerId) throws RuntimeException {
        switch (playerId) {
            case "0":

                return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.DIAMOND);
            case "1":
                return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.TRASH);
            case "TIE":
                return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.BLACK_TIE);
            default:
                throw new RuntimeException("PlayerId not valid");
        }
    }
}
