package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

/**
 * renderer and click handler for one option editor type.
 */
public interface OptionEditor {
    /**
     * renders the option editor inside a row.
     */
    void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme, EditorContext context);

    /**
     * handles a click routed to this editor.
     */
    void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY);
}
