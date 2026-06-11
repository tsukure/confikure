package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

public interface OptionEditor {
    void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme, EditorContext context);

    void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY);
}
