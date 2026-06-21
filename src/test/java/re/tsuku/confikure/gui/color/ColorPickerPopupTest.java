package re.tsuku.confikure.gui.color;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;
import re.tsuku.confikure.model.OptionAccess;

public final class ColorPickerPopupTest {
    @Test
    public void formatsAndParsesAlphaHexAsRgbaText() {
        MutableIntAccess access = new MutableIntAccess(0x8078A96B);
        ConfigOption option = colorOption("with-alpha", access, true);

        assertEquals("#78A96B80", ColorPickerPopup.format(option));

        ColorPickerPopup.parse(option, "#11223344");

        assertEquals(0x44112233, access.value);
    }

    @Test
    public void nonAlphaColorsIgnoreTypedAlphaAndRejectMalformedInput() {
        MutableIntAccess access = new MutableIntAccess(0xFF78A96B);
        ConfigOption option = colorOption("without-alpha", access, false);

        ColorPickerPopup.parse(option, "#11223344");
        assertEquals(0xFF78A96B, access.value);

        ColorPickerPopup.parse(option, "112233");
        assertEquals(0xFF112233, access.value);

        ColorPickerPopup.parse(option, "#NOPE");
        assertEquals(0xFF112233, access.value);
    }

    private static ConfigOption colorOption(String id, MutableIntAccess access, boolean alpha) {
        return new ConfigOption(id, id, "", "general", 0, EditorType.COLOR, access, access.value, null,
                Collections.<String>emptyList(), Collections.<String>emptyList(), true, false, alpha);
    }

    private static final class MutableIntAccess implements OptionAccess {
        private int value;

        private MutableIntAccess(int value) {
            this.value = value;
        }

        public Class<?> valueType() {
            return int.class;
        }

        public Object get() {
            return value;
        }

        public void set(Object value) {
            this.value = ((Number) value).intValue();
        }
    }
}
