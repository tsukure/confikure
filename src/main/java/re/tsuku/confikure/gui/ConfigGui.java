package re.tsuku.confikure.gui;

import java.util.Map;

import re.tsuku.confikure.gui.editor.DefaultOptionEditors;
import re.tsuku.confikure.gui.editor.OptionEditor;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigCategory;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;

/**
 * default immediate renderer for a scanned config definition.
 */
public final class ConfigGui {
    private final ConfigDefinition definition;
    private final ConfigTheme theme;
    private final Map<EditorType, OptionEditor> editors;
    private int selectedCategory;
    private int scroll;

    public ConfigGui(ConfigDefinition definition) {
        this(definition, new ConfigTheme(), DefaultOptionEditors.create());
    }

    public ConfigGui(ConfigDefinition definition, ConfigTheme theme, Map<EditorType, OptionEditor> editors) {
        if (definition == null) {
            throw new NullPointerException("definition");
        }
        this.definition = definition;
        this.theme = theme;
        this.editors = editors;
    }

    public int selectedCategory() {
        return selectedCategory;
    }

    public void selectedCategory(int selectedCategory) {
        if (definition.categories().isEmpty()) {
            this.selectedCategory = 0;
        } else {
            this.selectedCategory = Math.max(0, Math.min(definition.categories().size() - 1, selectedCategory));
        }
        this.scroll = 0;
    }

    public int scroll() {
        return scroll;
    }

    public void scroll(int amount) {
        this.scroll = Math.max(0, this.scroll + amount);
    }

    public void render(GuiRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        GuiBounds panel = panel(screenWidth, screenHeight);
        int sidebarWidth = 104;
        renderer.fill(0, 0, screenWidth, screenHeight, theme.background);
        frame(renderer, panel.x, panel.y, panel.width, panel.height);

        renderer.fill(panel.x + 1, panel.y + 1, panel.x + sidebarWidth, panel.y + panel.height - 1, theme.sidebar);
        renderer.fill(panel.x + sidebarWidth, panel.y + 1, panel.x + panel.width - 1, panel.y + panel.height - 1,
                theme.panel);
        renderer.text(definition.name(), panel.x + 10, panel.y + 9, theme.text);
        drawCategories(renderer, panel, sidebarWidth, mouseX, mouseY);
        drawCategory(renderer, panel, sidebarWidth, mouseX, mouseY);
    }

    public void click(int screenWidth, int screenHeight, int mouseX, int mouseY) {
        GuiBounds panel = panel(screenWidth, screenHeight);
        int sidebarWidth = 104;
        for (int i = 0; i < definition.categories().size(); i++) {
            GuiBounds tab = new GuiBounds(panel.x + 6, panel.y + 32 + i * 24, sidebarWidth - 12, 20);
            if (tab.contains(mouseX, mouseY)) {
                selectedCategory(i);
                return;
            }
        }

        ConfigCategory category = category();
        if (category == null) {
            return;
        }
        int contentX = panel.x + sidebarWidth + theme.padding;
        int contentY = panel.y + theme.padding - scroll;
        int contentWidth = panel.width - sidebarWidth - theme.padding * 2;
        for (ConfigGroup group : category.groups()) {
            contentY += 22;
            for (ConfigOption option : group.options()) {
                GuiBounds row = new GuiBounds(contentX, contentY, contentWidth, rowHeight(option));
                if (row.contains(mouseX, mouseY)) {
                    editor(option).click(option, row, mouseX, mouseY);
                    return;
                }
                contentY += row.height;
            }
            contentY += theme.groupGap;
        }
    }

    private void drawCategories(GuiRenderer renderer, GuiBounds panel, int sidebarWidth, int mouseX, int mouseY) {
        for (int i = 0; i < definition.categories().size(); i++) {
            ConfigCategory category = definition.categories().get(i);
            int x = panel.x + 6;
            int y = panel.y + 32 + i * 24;
            boolean selected = i == selectedCategory;
            boolean hovered = new GuiBounds(x, y, sidebarWidth - 12, 20).contains(mouseX, mouseY);
            renderer.fill(x, y, x + sidebarWidth - 12, y + 20,
                    selected ? theme.accentDark : hovered ? theme.panelRaised : theme.panelSunken);
            renderer.fill(x, y + 19, x + sidebarWidth - 12, y + 20, selected ? theme.accent : theme.borderDark);
            renderer.text(category.name(), x + 7, y + 6, selected ? theme.text : theme.mutedText);
        }
    }

    private void drawCategory(GuiRenderer renderer, GuiBounds panel, int sidebarWidth, int mouseX, int mouseY) {
        ConfigCategory category = category();
        if (category == null) {
            renderer.centeredText("no categories", panel.x, panel.y + panel.height / 2, panel.width, theme.mutedText);
            return;
        }

        int contentX = panel.x + sidebarWidth + theme.padding;
        int contentY = panel.y + theme.padding;
        int contentWidth = panel.width - sidebarWidth - theme.padding * 2;
        int contentHeight = panel.height - theme.padding * 2;
        renderer.pushClip(contentX, contentY, contentWidth, contentHeight);

        int y = contentY - scroll;
        for (ConfigGroup group : category.groups()) {
            renderer.text(group.name(), contentX, y + 6, theme.text);
            y += 22;
            for (ConfigOption option : group.options()) {
                int rowHeight = rowHeight(option);
                GuiBounds row = new GuiBounds(contentX, y, contentWidth, rowHeight);
                drawOption(renderer, option, row, row.contains(mouseX, mouseY));
                y += rowHeight;
            }
            y += theme.groupGap;
        }

        renderer.popClip();
    }

    private void drawOption(GuiRenderer renderer, ConfigOption option, GuiBounds bounds, boolean hovered) {
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
                hovered ? theme.panelRaised : theme.panel);
        renderer.fill(bounds.x, bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
        renderer.text(option.name(), bounds.x + 6, bounds.y + 6, option.type() == EditorType.INFO
                ? theme.mutedText
                : theme.text);
        if (!option.description().isEmpty()) {
            renderer.text(option.description(), bounds.x + 6, bounds.y + 17, theme.mutedText);
        }
        editor(option).render(option, bounds, renderer, theme, hovered);
    }

    private OptionEditor editor(ConfigOption option) {
        OptionEditor editor = editors.get(option.type());
        if (editor == null) {
            editor = editors.get(EditorType.CUSTOM);
        }
        return editor;
    }

    private ConfigCategory category() {
        if (definition.categories().isEmpty()) {
            return null;
        }
        if (selectedCategory >= definition.categories().size()) {
            selectedCategory = 0;
        }
        return definition.categories().get(selectedCategory);
    }

    private int rowHeight(ConfigOption option) {
        return option.type() == EditorType.MULTILINE_TEXT ? 44 : theme.rowHeight;
    }

    private GuiBounds panel(int screenWidth, int screenHeight) {
        int width = Math.min(520, Math.max(320, screenWidth - 42));
        int height = Math.min(360, Math.max(220, screenHeight - 36));
        return new GuiBounds((screenWidth - width) / 2, (screenHeight - height) / 2, width, height);
    }

    private void frame(GuiRenderer renderer, int x, int y, int width, int height) {
        renderer.fill(x, y, x + width, y + height, theme.panel);
        renderer.fill(x, y, x + width, y + 1, theme.border);
        renderer.fill(x, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x, y, x + 1, y + height, theme.border);
        renderer.fill(x + width - 1, y, x + width, y + height, theme.borderDark);
    }
}
