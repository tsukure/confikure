package re.tsuku.confikure.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import re.tsuku.confikure.gui.editor.DefaultOptionEditors;
import re.tsuku.confikure.gui.editor.EditorContext;
import re.tsuku.confikure.gui.editor.OptionEditor;
import re.tsuku.confikure.gui.input.TextInputState;
import re.tsuku.confikure.gui.input.TextInputState.KeyResult;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigCategory;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;
import re.tsuku.confikure.model.NumberRange;

/**
 * default immediate renderer for a scanned config definition.
 */
public final class ConfigGui implements EditorContext {
    private static final int SIDEBAR_WIDTH = 112;
    private static final int SIDEBAR_HEADER_HEIGHT = 44;
    private static final int TOP_BAR_HEIGHT = 28;
    private static final int EDITOR_WIDTH = 142;
    private static final int CONTROL_RIGHT_PADDING = 6;
    private static final int SLIDER_TRACK_WIDTH = 94;
    private static final int SLIDER_FIELD_WIDTH = 42;
    private static final int DROP_ITEM_HEIGHT = 18;
    private static final int OPTION_GAP = 0;
    private static final int GROUP_HEADER_HEIGHT = 24;
    private static final int GROUP_HEADER_STEP = GROUP_HEADER_HEIGHT;
    private static final int GROUP_HEADER_ACCENT_WIDTH = 3;
    private static final int GROUP_BODY_ACCENT_WIDTH = 2;
    private static final int TEXT_LINE_GAP = 2;
    private static final int SCROLLBAR_WIDTH = 2;
    private static final int SCROLLBAR_GUTTER_WIDTH = SCROLLBAR_WIDTH;
    private static final int SCROLLBAR_GUTTER_GAP = 6;

    private final ConfigDefinition definition;
    private final Map<EditorType, OptionEditor> editors;
    private final Map<ConfigOption, TextInputState> textStates = new HashMap<ConfigOption, TextInputState>();
    private final Map<String, Boolean> collapsedGroups = new HashMap<String, Boolean>();
    private ConfigTheme theme;
    private Supplier<ConfigTheme> themeSupplier;
    private GuiRenderer renderer;
    private KeyNameProvider keyNameProvider = new KeyNameProvider() {
        public String name(int keyCode) {
            return String.valueOf(keyCode);
        }
    };
    private int selectedCategory;
    private int scroll;
    private ConfigOption focusedOption;
    private ConfigOption hoveredOption;
    private ConfigOption activeOption;
    private ConfigOption openDropdown;
    private ConfigOption openColorPicker;
    private ColorDrag colorDrag = ColorDrag.NONE;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;
    private SidebarHeader sidebarHeader;
    private Runnable closeHandler;
    private boolean drawBackdrop;
    private int mouseX;
    private int mouseY;

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

    public void keyNameProvider(KeyNameProvider keyNameProvider) {
        if (keyNameProvider == null) {
            throw new NullPointerException("keyNameProvider");
        }
        this.keyNameProvider = keyNameProvider;
    }

    public void sidebarHeader(SidebarHeader sidebarHeader) {
        this.sidebarHeader = sidebarHeader;
    }

    public void closeHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    public void drawBackdrop(boolean drawBackdrop) {
        this.drawBackdrop = drawBackdrop;
    }

    public void theme(ConfigTheme theme) {
        if (theme == null) {
            throw new NullPointerException("theme");
        }
        this.theme = theme;
        this.themeSupplier = null;
    }

    public void themeSupplier(Supplier<ConfigTheme> themeSupplier) {
        this.themeSupplier = themeSupplier;
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
        closePopups();
    }

    public int scroll() {
        return scroll;
    }

    public void scroll(int amount) {
        this.scroll = Math.max(0, this.scroll + amount);
    }

    public void render(GuiRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (themeSupplier != null) {
            ConfigTheme supplied = themeSupplier.get();
            if (supplied != null) {
                theme = supplied;
            }
        }
        GuiBounds panel = panel(screenWidth, screenHeight);
        this.renderer = renderer;
        clampScroll(panel);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        hoveredOption = optionAtComponent(panel, mouseX, mouseY);
        if (drawBackdrop && theme.background != 0) {
            renderer.fill(0, 0, screenWidth, screenHeight, theme.background);
        }
        frame(renderer, panel.x, panel.y, panel.width, panel.height);

        renderer.fill(panel.x + 1, panel.y + 1, panel.x + SIDEBAR_WIDTH, panel.y + panel.height - 1, theme.sidebar);
        renderer.fill(panel.x + SIDEBAR_WIDTH, panel.y + 1, panel.x + panel.width - 1, panel.y + panel.height - 1,
                theme.panel);
        drawSidebarHeader(renderer, panel);
        drawCategories(renderer, panel, mouseX, mouseY);
        drawCategory(renderer, panel, mouseX, mouseY);
    }

    public void click(int screenWidth, int screenHeight, int mouseX, int mouseY) {
        GuiBounds panel = panel(screenWidth, screenHeight);
        if (openDropdown != null && handleDropdownClick(panel, mouseX, mouseY)) {
            return;
        }
        if (openColorPicker != null && handleColorPickerMouse(panel, mouseX, mouseY, true)) {
            return;
        }
        closePopups();
        activeOption = null;
        if (closeBounds(panel).contains(mouseX, mouseY)) {
            if (closeHandler != null) {
                closeHandler.run();
            }
            return;
        }
        if (handleScrollbarClick(panel, mouseX, mouseY)) {
            focusedOption = null;
            return;
        }

        for (int i = 0; i < definition.categories().size(); i++) {
            GuiBounds tab = categoryBounds(panel, i);
            if (tab.contains(mouseX, mouseY)) {
                selectedCategory(i);
                return;
            }
        }

        String groupKey = groupHeaderAt(panel, mouseX, mouseY);
        if (groupKey != null) {
            collapsedGroups.put(groupKey, !Boolean.TRUE.equals(collapsedGroups.get(groupKey)));
            focusedOption = null;
            return;
        }

        ConfigOption option = optionAtComponent(panel, mouseX, mouseY);
        if (option == null || !option.enabled()) {
            focusedOption = null;
            return;
        }
        if (option.type() == EditorType.TEXT || option.type() == EditorType.MULTILINE_TEXT) {
            focusedOption = option;
            focusText(option, mouseX, optionBounds(panel, option));
            return;
        }
        if (option.type() == EditorType.NUMBER) {
            if (sliderFieldBounds(optionBounds(panel, option)).contains(mouseX, mouseY)) {
                focusedOption = option;
                focusNumber(option);
                return;
            }
            if (!sliderTrackBounds(optionBounds(panel, option)).contains(mouseX, mouseY)) {
                focusedOption = null;
                return;
            }
            activeOption = option;
            setSliderValue(option, optionBounds(panel, option), mouseX);
            return;
        }
        if (option.type() == EditorType.DROPDOWN) {
            focusedOption = null;
            openDropdown = option;
            return;
        }
        if (option.type() == EditorType.MODE) {
            focusedOption = null;
            int direction = modeDirection(optionBounds(panel, option), mouseX);
            if (direction != 0) {
                cycle(option, direction);
            }
            return;
        }
        if (option.type() == EditorType.COLOR) {
            focusedOption = null;
            openColorPicker = option;
            return;
        }
        if (option.type() == EditorType.KEYBIND) {
            if (option.keybindClearable() && keybindClearBounds(optionBounds(panel, option)).contains(mouseX, mouseY)) {
                clearKeybind(option);
                return;
            }
            focusedOption = option;
            return;
        }
        focusedOption = null;
        activeOption = option;
        editor(option).click(option, optionBounds(panel, option), mouseX, mouseY);
    }

    public void drag(int screenWidth, int screenHeight, int mouseX, int mouseY) {
        GuiBounds panel = panel(screenWidth, screenHeight);
        if (draggingScrollbar) {
            scrollToScrollbar(panel, mouseY - scrollbarDragOffset);
            return;
        }
        if (activeOption == null) {
            return;
        }
        if (activeOption.type() == EditorType.NUMBER) {
            setSliderValue(activeOption, optionBounds(panel, activeOption), mouseX);
            return;
        }
        if (activeOption == openColorPicker) {
            handleColorPickerMouse(panel, mouseX, mouseY, false);
        }
    }

    public void release() {
        activeOption = null;
        colorDrag = ColorDrag.NONE;
        draggingScrollbar = false;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        return keyTyped(typedChar, keyCode, false, false);
    }

    public boolean keyTyped(char typedChar, int keyCode, boolean shift, boolean control) {
        if ((keyCode == 1 || keyCode == 28) && (openDropdown != null || openColorPicker != null)
                && focusedOption == null) {
            cancelFocus();
            return true;
        }
        if (focusedOption == null || !focusedOption.enabled()) {
            return false;
        }
        if (focusedOption.type() == EditorType.KEYBIND) {
            if (keyCode == 1) {
                cancelFocus();
                return true;
            }
            focusedOption.set(keyCode);
            focusedOption = null;
            return true;
        }
        if (focusedOption.type() == EditorType.TEXT || focusedOption.type() == EditorType.MULTILINE_TEXT) {
            KeyResult result = editText(focusedOption, typedChar, keyCode, shift, control);
            if (result == KeyResult.COMMIT || result == KeyResult.CANCEL) {
                return true;
            }
            return true;
        }
        if (focusedOption.type() == EditorType.NUMBER) {
            editNumber(focusedOption, typedChar, keyCode, shift, control);
            return true;
        }
        if (focusedOption.type() == EditorType.COLOR) {
            editColor(focusedOption, typedChar, keyCode, shift, control);
            return true;
        }
        return false;
    }

    public boolean hovered(ConfigOption option) {
        return option == hoveredOption;
    }

    public boolean focused(ConfigOption option) {
        return option == focusedOption;
    }

    public boolean active(ConfigOption option) {
        return option == activeOption;
    }

    public boolean dropdownOpen(ConfigOption option) {
        return option == openDropdown;
    }

    public String displayValue(ConfigOption option) {
        if (option.type() == EditorType.KEYBIND && option.get() instanceof Number) {
            return keyNameProvider.name(((Number) option.get()).intValue());
        }
        TextInputState state = textStates.get(option);
        if ((option.type() == EditorType.NUMBER || option.type() == EditorType.COLOR) && state != null
                && option == focusedOption) {
            return state.text();
        }
        return String.valueOf(option.get());
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public int textCursor(ConfigOption option) {
        return textState(option).cursor();
    }

    public int textSelectionStart(ConfigOption option) {
        return textState(option).selectionStart();
    }

    public int textSelectionEnd(ConfigOption option) {
        return textState(option).selectionEnd();
    }

    private void drawCategories(GuiRenderer renderer, GuiBounds panel, int mouseX, int mouseY) {
        if (definition.categories().isEmpty()) {
            return;
        }
        for (int i = 0; i < definition.categories().size(); i++) {
            ConfigCategory category = definition.categories().get(i);
            GuiBounds tab = categoryBounds(panel, i);
            boolean selected = i == selectedCategory;
            boolean hovered = tab.contains(mouseX, mouseY);
            int fill = selected ? theme.accentDark : hovered ? theme.panel : theme.panelRaised;
            boxed(renderer, tab, fill, selected ? theme.accent : theme.border);
            renderer.fill(tab.x, tab.y, tab.x + 2, tab.y + tab.height,
                    selected ? theme.accent : hovered ? theme.accentDark : theme.border);
            renderer.text(category.name(), tab.x + 8, tab.y + 6,
                    selected || hovered ? theme.text : theme.mutedText);
        }
    }

    private void drawCategory(GuiRenderer renderer, GuiBounds panel, int mouseX, int mouseY) {
        ConfigCategory category = category();
        if (category == null) {
            renderer.centeredText("no categories", panel.x, panel.y + panel.height / 2, panel.width, theme.mutedText);
            return;
        }

        int contentX = contentX(panel);
        drawContentTopBar(renderer, panel, category);
        int contentY = contentY(panel);
        int contentWidth = contentWidth(panel);
        int contentHeight = contentViewportHeight(panel);
        renderer.pushClip(contentX, contentY, contentWidth, contentHeight);

        int y = contentY - scroll;
        for (ConfigGroup group : category.groups()) {
            if (!hasVisibleOptions(group)) {
                continue;
            }
            boolean collapsed = collapsed(group);
            int groupHeight = groupBlockHeight(group);
            drawGroupShell(renderer, new GuiBounds(contentX, y, contentWidth, groupHeight), collapsed);
            drawGroupHeader(renderer, group, new GuiBounds(contentX, y, contentWidth, GROUP_HEADER_HEIGHT), collapsed);
            int optionY = y + GROUP_HEADER_STEP;
            if (!collapsed) {
                for (ConfigOption option : group.options()) {
                    if (!option.visible()) {
                        continue;
                    }
                    int rowHeight = rowHeight(option);
                    GuiBounds row = new GuiBounds(contentX, optionY, contentWidth, rowHeight);
                    drawOption(renderer, option, row);
                    optionY += rowHeight + OPTION_GAP;
                }
            }
            drawGroupBottomBorder(renderer, new GuiBounds(contentX, y, contentWidth, groupHeight));
            y += groupHeight + theme.groupGap;
        }

        renderer.popClip();
        drawScrollbar(renderer, panel);
        drawDropdown(renderer, panel);
        drawColorPicker(renderer, panel);
    }

    private void drawOption(GuiRenderer renderer, ConfigOption option, GuiBounds bounds) {
        int rowX = bounds.x + 1 + GROUP_BODY_ACCENT_WIDTH;
        renderer.fill(rowX, bounds.y, bounds.x + bounds.width - 1, bounds.y + bounds.height,
                theme.panelRaised);
        renderer.fill(rowX, bounds.y, bounds.x + bounds.width - 1, bounds.y + 1, theme.panel);
        int nameColor = !option.enabled()
                ? theme.disabledText
                : option.type() == EditorType.INFO ? theme.mutedText : theme.text;
        boolean hasDescription = !option.description().isEmpty();
        int nameY = hasDescription
                ? twoLineTextY(renderer, bounds)
                : bounds.y + Math.max(4, (bounds.height - renderer.fontHeight()) / 2);
        renderer.text(option.name(), bounds.x + 8, nameY, nameColor);
        if (hasDescription) {
            renderer.text(option.description(), bounds.x + 8, nameY + renderer.fontHeight() + TEXT_LINE_GAP,
                    theme.mutedText);
        }
        editor(option).render(option, bounds, renderer, theme, this);
    }

    private void drawGroupHeader(GuiRenderer renderer, ConfigGroup group, GuiBounds bounds, boolean collapsed) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        int headerBottom = collapsed ? bounds.y + bounds.height - 1 : bounds.y + bounds.height;
        renderer.fill(bounds.x + 1, bounds.y + 1, bounds.x + bounds.width - 1, headerBottom,
                hovered ? theme.panel : theme.panelRaised);
        renderer.fill(bounds.x + 1, bounds.y + 1, bounds.x + 1 + GROUP_HEADER_ACCENT_WIDTH,
                bounds.y + bounds.height - 1, theme.accentDark);
        if (!collapsed) {
            renderer.fill(bounds.x + 1, bounds.y + bounds.height - 1, bounds.x + bounds.width - 1,
                    bounds.y + bounds.height, theme.borderDark);
        }
        drawChevron(renderer, bounds.x + 13, chevronY(bounds, collapsed ? Direction.RIGHT : Direction.DOWN),
                collapsed ? Direction.RIGHT : Direction.DOWN,
                hovered ? theme.accent : theme.mutedText);
        boolean hasDescription = !group.description().isEmpty();
        int nameY = hasDescription
                ? twoLineTextY(renderer, bounds)
                : bounds.y + Math.max(3, (bounds.height - renderer.fontHeight()) / 2);
        renderer.text(group.name(), bounds.x + 30, nameY, hovered ? theme.accent : theme.text);
        if (hasDescription) {
            renderer.text(group.description(), bounds.x + 30, nameY + renderer.fontHeight() + TEXT_LINE_GAP,
                    theme.mutedText);
        }
    }

    private void drawGroupShell(GuiRenderer renderer, GuiBounds bounds, boolean collapsed) {
        int fill = collapsed ? theme.panelRaised : theme.panel;
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, fill);
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + 1, theme.border);
        renderer.fill(bounds.x, bounds.y, bounds.x + 1, bounds.y + bounds.height - 1, theme.border);
        if (!collapsed && bounds.height > GROUP_HEADER_HEIGHT + 1) {
            renderer.fill(bounds.x + 1, bounds.y + GROUP_HEADER_HEIGHT,
                    bounds.x + 1 + GROUP_BODY_ACCENT_WIDTH, bounds.y + bounds.height - 1, theme.accentDark);
        }
        renderer.fill(bounds.x, bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
        renderer.fill(bounds.x + bounds.width - 1, bounds.y + 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
    }

    private void drawGroupBottomBorder(GuiRenderer renderer, GuiBounds bounds) {
        renderer.fill(bounds.x, bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
    }

    private int twoLineTextY(GuiRenderer renderer, GuiBounds bounds) {
        int blockHeight = renderer.fontHeight() * 2 + TEXT_LINE_GAP;
        return bounds.y + Math.max(2, (bounds.height - blockHeight) / 2);
    }

    private static int chevronY(GuiBounds bounds, Direction direction) {
        int height = direction == Direction.DOWN ? 4 : 7;
        return bounds.y + (bounds.height - height) / 2;
    }

    private void drawSidebarHeader(GuiRenderer renderer, GuiBounds panel) {
        GuiBounds header = new GuiBounds(panel.x + 8, panel.y + 8, SIDEBAR_WIDTH - 16, SIDEBAR_HEADER_HEIGHT - 12);
        if (sidebarHeader != null) {
            sidebarHeader.render(renderer, header, theme);
            return;
        }
        renderer.text(definition.name(), header.x, header.y, theme.text);
        if (!definition.description().isEmpty()) {
            renderer.text(definition.description(), header.x, header.y + 12, theme.mutedText);
        }
    }

    private void drawContentTopBar(GuiRenderer renderer, GuiBounds panel, ConfigCategory category) {
        int x = panel.x + SIDEBAR_WIDTH + 1;
        int y = panel.y + 1;
        int width = panel.width - SIDEBAR_WIDTH - 2;
        renderer.fill(x, y, x + width, y + TOP_BAR_HEIGHT, theme.panelRaised);
        renderer.fill(x, y + TOP_BAR_HEIGHT - 1, x + width, y + TOP_BAR_HEIGHT, theme.borderDark);
        renderer.text(category.name(), x + theme.padding, y + 9, theme.text);
        if (!category.description().isEmpty()) {
            renderer.text(category.description(), x + theme.padding + renderer.textWidth(category.name()) + 8, y + 9,
                    theme.mutedText);
        }
        GuiBounds close = closeBounds(panel);
        boolean hovered = close.contains(mouseX, mouseY);
        boxed(renderer, close, hovered ? theme.danger : theme.panel, hovered ? theme.text : theme.border);
        renderer.centeredText("x", close.x, close.y + 5, close.width, hovered ? theme.text : theme.mutedText);
    }

    private void drawDropdown(GuiRenderer renderer, GuiBounds panel) {
        if (openDropdown == null || openDropdown.choices().isEmpty()) {
            return;
        }
        GuiBounds bounds = dropdownBounds(panel, openDropdown);
        frame(renderer, bounds.x, bounds.y, bounds.width, bounds.height);
        for (int i = 0; i < openDropdown.choices().size(); i++) {
            int y = bounds.y + i * DROP_ITEM_HEIGHT;
            String choice = openDropdown.choices().get(i);
            boolean selected = choice.equals(String.valueOf(openDropdown.get()));
            boolean hovered = mouseX >= bounds.x && mouseY >= y && mouseX < bounds.x + bounds.width
                    && mouseY < y + DROP_ITEM_HEIGHT;
            renderer.fill(bounds.x + 1, y + 1, bounds.x + bounds.width - 1, y + DROP_ITEM_HEIGHT,
                    selected ? theme.accentDark : hovered ? theme.panelRaised : theme.panel);
            renderer.text(choice, bounds.x + 5, y + 5, selected || hovered ? theme.text : theme.mutedText);
        }
    }

    private void drawColorPicker(GuiRenderer renderer, GuiBounds panel) {
        if (openColorPicker == null) {
            return;
        }
        GuiBounds picker = colorPickerBounds(panel, openColorPicker);
        frame(renderer, picker.x, picker.y, picker.width, picker.height);
        int color = color(openColorPicker);
        float[] hsv = rgbToHsv(color);
        int squareX = picker.x + 8;
        int squareY = picker.y + 8;
        for (int y = 0; y < 72; y += 2) {
            for (int x = 0; x < 112; x += 2) {
                float saturation = x / 112.0F;
                float value = 1.0F - y / 72.0F;
                renderer.fill(squareX + x, squareY + y, squareX + x + 2, squareY + y + 2,
                        0xFF000000 | hsvToRgb(hsv[0], saturation, value));
            }
        }
        int hueX = squareX + 120;
        for (int y = 0; y < 72; y += 2) {
            renderer.fill(hueX, squareY + y, hueX + 12, squareY + y + 2,
                    0xFF000000 | hsvToRgb(y / 72.0F, 1.0F, 1.0F));
        }
        int markerX = squareX + Math.round(hsv[1] * 112.0F);
        int markerY = squareY + Math.round((1.0F - hsv[2]) * 72.0F);
        renderer.fill(markerX - 2, markerY - 2, markerX + 3, markerY + 3, theme.text);
        int hueY = squareY + Math.round(hsv[0] * 72.0F);
        renderer.fill(hueX - 2, hueY - 1, hueX + 14, hueY + 2, theme.text);
        renderer.text("preview", picker.x + 8, picker.y + 87, theme.mutedText);
        renderer.fill(picker.x + 58, picker.y + 86, picker.x + picker.width - 8, picker.y + 100, theme.slot);
        renderer.fill(picker.x + 59, picker.y + 87, picker.x + picker.width - 9, picker.y + 99, color);
        GuiBounds hex = colorHexBounds(picker);
        boolean hexFocused = openColorPicker == focusedOption;
        boolean hexHovered = hex.contains(mouseX, mouseY);
        String value = displayValue(openColorPicker);
        drawTextField(renderer, hex, "#" + value, textCursor(openColorPicker) + 1,
                textSelectionStart(openColorPicker) + 1,
                textSelectionEnd(openColorPicker) + 1, hexHovered, hexFocused);
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
        int rowHeight = Math.max(28, theme.rowHeight);
        return option.type() == EditorType.MULTILINE_TEXT ? rowHeight + 22 : rowHeight;
    }

    private void clampScroll(GuiBounds panel) {
        ConfigCategory category = category();
        if (category == null) {
            scroll = 0;
            return;
        }
        int maxScroll = maxScroll(panel, category);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
    }

    private int contentHeight(ConfigCategory category) {
        int height = 0;
        for (ConfigGroup group : category.groups()) {
            if (!hasVisibleOptions(group)) {
                continue;
            }
            height += groupBlockHeight(group);
            height += theme.groupGap;
        }
        return height;
    }

    private int groupBlockHeight(ConfigGroup group) {
        int height = GROUP_HEADER_STEP;
        if (!collapsed(group)) {
            for (ConfigOption option : group.options()) {
                if (option.visible()) {
                    height += rowHeight(option) + OPTION_GAP;
                }
            }
        }
        return height;
    }

    private ConfigOption optionAt(GuiBounds panel, int mouseX, int mouseY) {
        ConfigCategory category = category();
        if (category == null) {
            return null;
        }
        int contentX = contentX(panel);
        int y = contentY(panel) - scroll;
        int contentWidth = contentWidth(panel);
        for (ConfigGroup group : category.groups()) {
            if (!hasVisibleOptions(group)) {
                continue;
            }
            y += GROUP_HEADER_STEP;
            if (!collapsed(group)) {
                for (ConfigOption option : group.options()) {
                    if (!option.visible()) {
                        continue;
                    }
                    GuiBounds row = new GuiBounds(contentX, y, contentWidth, rowHeight(option));
                    if (row.contains(mouseX, mouseY)) {
                        return option;
                    }
                    y += row.height + OPTION_GAP;
                }
            }
            y += theme.groupGap;
        }
        return null;
    }

    private ConfigOption optionAtComponent(GuiBounds panel, int mouseX, int mouseY) {
        ConfigOption option = optionAt(panel, mouseX, mouseY);
        if (option == null) {
            return null;
        }
        GuiBounds row = optionBounds(panel, option);
        if (option.type() == EditorType.MODE) {
            return modeDirection(row, mouseX) != 0 && controlBounds(row, option).contains(mouseX, mouseY)
                    ? option
                    : null;
        }
        return controlBounds(row, option).contains(mouseX, mouseY) ? option : null;
    }

    private GuiBounds optionBounds(GuiBounds panel, ConfigOption target) {
        ConfigCategory category = category();
        int contentX = contentX(panel);
        int y = contentY(panel) - scroll;
        int contentWidth = contentWidth(panel);
        for (ConfigGroup group : category.groups()) {
            if (!hasVisibleOptions(group)) {
                continue;
            }
            y += GROUP_HEADER_STEP;
            if (!collapsed(group)) {
                for (ConfigOption option : group.options()) {
                    if (!option.visible()) {
                        continue;
                    }
                    GuiBounds row = new GuiBounds(contentX, y, contentWidth, rowHeight(option));
                    if (option == target) {
                        return row;
                    }
                    y += row.height + OPTION_GAP;
                }
            }
            y += theme.groupGap;
        }
        return new GuiBounds(contentX, panel.y + theme.padding, contentWidth, rowHeight(target));
    }

    private String groupHeaderAt(GuiBounds panel, int mouseX, int mouseY) {
        ConfigCategory category = category();
        if (category == null) {
            return null;
        }
        int contentX = contentX(panel);
        int y = contentY(panel) - scroll;
        int contentWidth = contentWidth(panel);
        for (ConfigGroup group : category.groups()) {
            if (!hasVisibleOptions(group)) {
                continue;
            }
            if (new GuiBounds(contentX, y, contentWidth, GROUP_HEADER_HEIGHT).contains(mouseX, mouseY)) {
                return groupKey(group);
            }
            y += GROUP_HEADER_STEP;
            if (!collapsed(group)) {
                for (ConfigOption option : group.options()) {
                    if (option.visible()) {
                        y += rowHeight(option) + OPTION_GAP;
                    }
                }
            }
            y += theme.groupGap;
        }
        return null;
    }

    private GuiBounds categoryBounds(GuiBounds panel, int index) {
        return new GuiBounds(panel.x + 7, panel.y + SIDEBAR_HEADER_HEIGHT + index * 24, SIDEBAR_WIDTH - 14, 20);
    }

    private GuiBounds closeBounds(GuiBounds panel) {
        return new GuiBounds(panel.x + panel.width - 25, panel.y + 6, 18, 18);
    }

    private GuiBounds dropdownBounds(GuiBounds panel, ConfigOption option) {
        GuiBounds row = optionBounds(panel, option);
        int x = row.x + row.width - EDITOR_WIDTH - CONTROL_RIGHT_PADDING;
        int y = row.y + row.height - 7;
        return new GuiBounds(x, y, EDITOR_WIDTH, option.choices().size() * DROP_ITEM_HEIGHT + 2);
    }

    private GuiBounds colorPickerBounds(GuiBounds panel, ConfigOption option) {
        GuiBounds row = optionBounds(panel, option);
        int x = row.x + row.width - 164;
        int y = Math.min(row.y + row.height - 4, panel.y + panel.height - 134);
        return new GuiBounds(x, y, 158, 128);
    }

    private GuiBounds controlBounds(GuiBounds row, ConfigOption option) {
        if (option.type() == EditorType.MODE) {
            return modeArrowBounds(row);
        }
        if (option.type() == EditorType.BOOLEAN) {
            return rightBounds(row, 34, 16);
        }
        if (option.type() == EditorType.NUMBER) {
            return rightBounds(row, SLIDER_TRACK_WIDTH + SLIDER_FIELD_WIDTH + 6, 22);
        }
        if (option.type() == EditorType.COLOR) {
            return rightBounds(row, 40, 18);
        }
        if (option.type() == EditorType.KEYBIND) {
            return rightBounds(row, 80, 18);
        }
        if (option.type() == EditorType.BUTTON) {
            return rightBounds(row, 64, 18);
        }
        if (option.type() == EditorType.INFO) {
            return rightBounds(row, 120, 18);
        }
        return rightBounds(row, EDITOR_WIDTH, option.type() == EditorType.MULTILINE_TEXT ? 42 : 18);
    }

    private GuiBounds modeArrowBounds(GuiBounds row) {
        GuiBounds full = rightBounds(row, EDITOR_WIDTH, 18);
        return new GuiBounds(full.x, full.y, full.width, full.height);
    }

    private GuiBounds sliderTrackBounds(GuiBounds row) {
        int x = row.x + row.width - SLIDER_TRACK_WIDTH - SLIDER_FIELD_WIDTH - 6 - CONTROL_RIGHT_PADDING;
        return new GuiBounds(x, row.y + 7, SLIDER_TRACK_WIDTH, 18);
    }

    private GuiBounds sliderFieldBounds(GuiBounds row) {
        int x = row.x + row.width - SLIDER_FIELD_WIDTH - CONTROL_RIGHT_PADDING;
        return new GuiBounds(x, row.y + 7, SLIDER_FIELD_WIDTH, 18);
    }

    private GuiBounds colorHexBounds(GuiBounds picker) {
        return new GuiBounds(picker.x + 8, picker.y + 104, picker.width - 16, 18);
    }

    private GuiBounds keybindClearBounds(GuiBounds row) {
        GuiBounds keybind = rightBounds(row, 80, 18);
        return new GuiBounds(keybind.x + 64, keybind.y, 16, 18);
    }

    private GuiBounds rightBounds(GuiBounds row, int width, int height) {
        return new GuiBounds(row.x + row.width - width - CONTROL_RIGHT_PADDING, row.y + (row.height - height) / 2,
                width, height);
    }

    private int contentX(GuiBounds panel) {
        return panel.x + SIDEBAR_WIDTH + theme.padding;
    }

    private int contentY(GuiBounds panel) {
        return panel.y + TOP_BAR_HEIGHT + theme.padding;
    }

    private int contentWidth(GuiBounds panel) {
        return Math.max(120,
                panel.width - SIDEBAR_WIDTH - theme.padding * 2 - SCROLLBAR_GUTTER_WIDTH - SCROLLBAR_GUTTER_GAP);
    }

    private int contentViewportHeight(GuiBounds panel) {
        return panel.height - TOP_BAR_HEIGHT - theme.padding * 2;
    }

    private int maxScroll(GuiBounds panel, ConfigCategory category) {
        return Math.max(0, contentHeight(category) - contentViewportHeight(panel));
    }

    private GuiBounds scrollbarGutterBounds(GuiBounds panel) {
        return new GuiBounds(panel.x + panel.width - theme.padding - SCROLLBAR_GUTTER_WIDTH, contentY(panel),
                SCROLLBAR_GUTTER_WIDTH, contentViewportHeight(panel));
    }

    private GuiBounds scrollbarTrackBounds(GuiBounds panel) {
        GuiBounds gutter = scrollbarGutterBounds(panel);
        return new GuiBounds(gutter.x, gutter.y, gutter.width, gutter.height);
    }

    private GuiBounds scrollbarThumbBounds(GuiBounds panel) {
        ConfigCategory category = category();
        GuiBounds track = scrollbarTrackBounds(panel);
        if (category == null) {
            return new GuiBounds(track.x, track.y, track.width, track.height);
        }
        int contentHeight = contentHeight(category);
        int viewportHeight = contentViewportHeight(panel);
        if (contentHeight <= viewportHeight) {
            return new GuiBounds(track.x, track.y, track.width, track.height);
        }
        int thumbHeight = Math.max(24, track.height * viewportHeight / contentHeight);
        int travel = Math.max(1, track.height - thumbHeight);
        int thumbY = track.y + (int) Math.round(travel * (scroll / (double) maxScroll(panel, category)));
        return new GuiBounds(track.x, thumbY, track.width, thumbHeight);
    }

    private void drawScrollbar(GuiRenderer renderer, GuiBounds panel) {
        GuiBounds gutter = scrollbarGutterBounds(panel);
        ConfigCategory category = category();
        if (category == null || maxScroll(panel, category) <= 0) {
            return;
        }
        GuiBounds track = scrollbarTrackBounds(panel);
        GuiBounds thumb = scrollbarThumbBounds(panel);
        renderer.fill(track.x, track.y, track.x + track.width, track.y + track.height, theme.panel);
        boolean hovered = gutter.contains(mouseX, mouseY) || draggingScrollbar;
        renderer.fill(thumb.x, thumb.y, thumb.x + thumb.width, thumb.y + thumb.height,
                hovered ? theme.accentDark : theme.border);
    }

    private boolean handleScrollbarClick(GuiBounds panel, int mouseX, int mouseY) {
        ConfigCategory category = category();
        if (category == null || maxScroll(panel, category) <= 0) {
            return false;
        }
        GuiBounds gutter = scrollbarGutterBounds(panel);
        if (!gutter.contains(mouseX, mouseY)) {
            return false;
        }
        GuiBounds thumb = scrollbarThumbBounds(panel);
        draggingScrollbar = true;
        scrollbarDragOffset = thumb.contains(mouseX, mouseY) ? mouseY - thumb.y : thumb.height / 2;
        scrollToScrollbar(panel, mouseY - scrollbarDragOffset);
        return true;
    }

    private void scrollToScrollbar(GuiBounds panel, int thumbY) {
        ConfigCategory category = category();
        if (category == null) {
            scroll = 0;
            return;
        }
        GuiBounds track = scrollbarTrackBounds(panel);
        GuiBounds thumb = scrollbarThumbBounds(panel);
        int maxScroll = maxScroll(panel, category);
        int travel = Math.max(1, track.height - thumb.height);
        int localY = Math.max(0, Math.min(travel, thumbY - track.y));
        scroll = (int) Math.round(maxScroll * (localY / (double) travel));
    }

    private boolean handleDropdownClick(GuiBounds panel, int mouseX, int mouseY) {
        GuiBounds bounds = dropdownBounds(panel, openDropdown);
        if (bounds.contains(mouseX, mouseY)) {
            int index = Math.max(0,
                    Math.min(openDropdown.choices().size() - 1, (mouseY - bounds.y) / DROP_ITEM_HEIGHT));
            openDropdown.set(openDropdown.choices().get(index));
            openDropdown = null;
            return true;
        }
        if (optionAt(panel, mouseX, mouseY) == openDropdown) {
            openDropdown = null;
            return true;
        }
        openDropdown = null;
        return false;
    }

    private boolean handleColorPickerMouse(GuiBounds panel, int mouseX, int mouseY, boolean pressed) {
        GuiBounds picker = colorPickerBounds(panel, openColorPicker);
        int squareX = picker.x + 8;
        int squareY = picker.y + 8;
        GuiBounds square = new GuiBounds(squareX, squareY, 112, 72);
        GuiBounds hue = new GuiBounds(squareX + 120, squareY, 12, 72);
        if (pressed) {
            if (square.contains(mouseX, mouseY)) {
                colorDrag = ColorDrag.SQUARE;
                activeOption = openColorPicker;
            } else if (hue.contains(mouseX, mouseY)) {
                colorDrag = ColorDrag.HUE;
                activeOption = openColorPicker;
            } else if (colorHexBounds(picker).contains(mouseX, mouseY)) {
                focusedOption = openColorPicker;
                focusColor(openColorPicker, colorHexBounds(picker), mouseX);
                colorDrag = ColorDrag.NONE;
                return true;
            } else if (!picker.contains(mouseX, mouseY)) {
                openColorPicker = null;
                return false;
            }
        }
        if (colorDrag == ColorDrag.NONE) {
            return picker.contains(mouseX, mouseY);
        }
        float[] hsv = rgbToHsv(color(openColorPicker));
        if (colorDrag == ColorDrag.SQUARE) {
            hsv[1] = clamp((mouseX - squareX) / 112.0F);
            hsv[2] = 1.0F - clamp((mouseY - squareY) / 72.0F);
        } else if (colorDrag == ColorDrag.HUE) {
            hsv[0] = clamp((mouseY - squareY) / 72.0F);
        }
        openColorPicker.set(0xFF000000 | hsvToRgb(hsv[0], hsv[1], hsv[2]));
        return true;
    }

    private void setSliderValue(ConfigOption option, GuiBounds bounds, int mouseX) {
        NumberRange range = option.range();
        if (range == null) {
            return;
        }
        GuiBounds track = sliderTrackBounds(bounds);
        double progress = Math.max(0.0D, Math.min(1.0D, (mouseX - track.x) / (double) track.width));
        option.set(range.min() + (range.max() - range.min()) * progress);
    }

    private void cycle(ConfigOption option, int direction) {
        List<String> choices = option.choices();
        if (choices.isEmpty()) {
            return;
        }
        String current = option.valueType().isEnum() ? ((Enum<?>) option.get()).name() : String.valueOf(option.get());
        int index = choices.indexOf(current);
        option.set(choices.get((index + direction + choices.size()) % choices.size()));
    }

    private int modeDirection(GuiBounds row, int mouseX) {
        GuiBounds control = rightBounds(row, EDITOR_WIDTH, 18);
        if (mouseX < control.x + 20) {
            return -1;
        }
        if (mouseX >= control.x + control.width - 20) {
            return 1;
        }
        return 0;
    }

    private void clearKeybind(ConfigOption option) {
        if (!option.keybindClearable()) {
            return;
        }
        if (option.keybindResetOnClear()) {
            option.reset();
            return;
        }
        option.set(0);
    }

    private void focusText(ConfigOption option, int mouseX, GuiBounds bounds) {
        TextInputState state = textState(option);
        String text = String.valueOf(option.get());
        state.text(text);
        state.maxLength(option.type() == EditorType.MULTILINE_TEXT ? 2048 : 256);
        state.filter(TextInputState.CharacterFilter.ANY);
        if (renderer != null) {
            state.cursorAt(renderer, firstLine(text),
                    bounds.x + bounds.width - EDITOR_WIDTH - CONTROL_RIGHT_PADDING + 5,
                    mouseX);
        }
    }

    private void focusNumber(ConfigOption option) {
        TextInputState state = textState(option);
        state.text(formatNumber(option));
        state.maxLength(32);
        state.filter(new TextInputState.CharacterFilter() {
            public boolean accept(char character) {
                return character == '-' || character == '.' || character >= '0' && character <= '9';
            }
        });
    }

    private void focusColor(ConfigOption option, GuiBounds hex, int mouseX) {
        TextInputState state = textState(option);
        state.text(hex(color(option)).substring(1));
        state.maxLength(8);
        state.filter(new TextInputState.CharacterFilter() {
            public boolean accept(char character) {
                return isHex(character);
            }
        });
        if (renderer != null) {
            state.cursorAt(renderer, state.text(), hex.x + 5 + renderer.textWidth("#"), mouseX);
        }
    }

    private KeyResult editText(ConfigOption option, char typedChar, int keyCode, boolean shift, boolean control) {
        TextInputState state = textState(option);
        KeyResult result = state.keyTyped(typedChar, keyCode, shift, control,
                option.type() == EditorType.MULTILINE_TEXT);
        if (result == KeyResult.CHANGED) {
            option.set(state.text());
        } else if (result == KeyResult.COMMIT) {
            option.set(state.text());
            focusedOption = null;
        } else if (result == KeyResult.CANCEL) {
            state.text(String.valueOf(option.get()));
            focusedOption = null;
        }
        return result;
    }

    private void editNumber(ConfigOption option, char typedChar, int keyCode, boolean shift, boolean control) {
        TextInputState state = textState(option);
        KeyResult result = state.keyTyped(typedChar, keyCode, shift, control, false);
        if (result == KeyResult.CHANGED) {
            parseNumber(option, state.text());
        } else if (result == KeyResult.COMMIT) {
            parseNumber(option, state.text());
            state.text(formatNumber(option));
            focusedOption = null;
        } else if (result == KeyResult.CANCEL) {
            state.text(formatNumber(option));
            focusedOption = null;
        }
    }

    private void editColor(ConfigOption option, char typedChar, int keyCode, boolean shift, boolean control) {
        TextInputState state = textState(option);
        KeyResult result = state.keyTyped(Character.toUpperCase(typedChar), keyCode, shift, control, false);
        if (result == KeyResult.CHANGED) {
            parseColor(option, state.text());
        } else if (result == KeyResult.COMMIT) {
            parseColor(option, state.text());
            state.text(hex(color(option)).substring(1));
            focusedOption = null;
        } else if (result == KeyResult.CANCEL) {
            state.text(hex(color(option)).substring(1));
            focusedOption = null;
        }
    }

    private void cancelFocus() {
        focusedOption = null;
        openDropdown = null;
        openColorPicker = null;
        activeOption = null;
        colorDrag = ColorDrag.NONE;
    }

    private void parseNumber(ConfigOption option, String text) {
        try {
            if (!text.isEmpty() && !text.equals("-") && !text.equals(".")) {
                option.set(Double.valueOf(text));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void parseColor(ConfigOption option, String text) {
        if (text.length() != 6 && text.length() != 8) {
            return;
        }
        option.set((int) Long.parseLong(text.length() == 6 ? "FF" + text : text, 16));
    }

    private static String formatNumber(ConfigOption option) {
        Object value = option.get();
        if (!(value instanceof Number)) {
            return String.valueOf(value);
        }
        double number = ((Number) value).doubleValue();
        if (Math.rint(number) == number) {
            return String.valueOf((long) number);
        }
        String text = String.format(java.util.Locale.ROOT, "%.4f", number);
        while (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }

    private void drawTextField(GuiRenderer renderer, GuiBounds bounds, String text, int cursor, int selectionStart,
            int selectionEnd, boolean hovered, boolean focused) {
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
                hovered || focused ? theme.panelRaised : theme.panel);
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width - 1, bounds.y + 1,
                focused ? theme.accent : theme.border);
        renderer.fill(bounds.x, bounds.y, bounds.x + 1, bounds.y + bounds.height - 1,
                focused ? theme.accent : theme.border);
        renderer.fill(bounds.x + 1, bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
        renderer.fill(bounds.x + bounds.width - 1, bounds.y + 1, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
        int textX = bounds.x + 5;
        int textY = bounds.y + 5;
        String clipped = clip(text, bounds.width - 10);
        if (selectionStart != selectionEnd) {
            int start = Math.max(0, Math.min(Math.min(selectionStart, selectionEnd), clipped.length()));
            int end = Math.max(0, Math.min(Math.max(selectionStart, selectionEnd), clipped.length()));
            int left = textX + renderer.textWidth(clipped.substring(0, start));
            int right = textX + renderer.textWidth(clipped.substring(0, end));
            renderer.fill(left, textY - 1, Math.max(left + 1, right), textY + renderer.fontHeight(),
                    theme.accentDark);
        }
        renderer.text(clipped, textX, textY, theme.text);
        if (focused && (System.currentTimeMillis() / 450L) % 2L == 0L) {
            int shownCursor = Math.max(0, Math.min(cursor, clipped.length()));
            int cursorX = textX + renderer.textWidth(clipped.substring(0, shownCursor));
            renderer.fill(cursorX, textY - 1, cursorX + 1, textY + renderer.fontHeight(), theme.text);
        }
    }

    private String clip(String text, int width) {
        if (renderer == null || renderer.textWidth(text) <= width) {
            return text;
        }
        String value = text;
        while (value.length() > 0 && renderer.textWidth(value + "...") > width) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    private static String firstLine(String text) {
        int index = text.indexOf('\n');
        return index < 0 ? text : text.substring(0, index);
    }

    private TextInputState textState(ConfigOption option) {
        TextInputState state = textStates.get(option);
        if (state == null) {
            state = new TextInputState();
            state.text(String.valueOf(option.get()));
            textStates.put(option, state);
        }
        return state;
    }

    private void closePopups() {
        openDropdown = null;
        openColorPicker = null;
        colorDrag = ColorDrag.NONE;
    }

    private boolean collapsed(ConfigGroup group) {
        return Boolean.TRUE.equals(collapsedGroups.get(groupKey(group)));
    }

    private String groupKey(ConfigGroup group) {
        ConfigCategory category = category();
        return (category == null ? "" : category.id()) + "/" + group.id();
    }

    private static boolean hasVisibleOptions(ConfigGroup group) {
        for (ConfigOption option : group.options()) {
            if (option.visible()) {
                return true;
            }
        }
        return false;
    }

    private GuiBounds panel(int screenWidth, int screenHeight) {
        int width = Math.min(620, Math.max(360, screenWidth - 42));
        int height = Math.min(390, Math.max(230, screenHeight - 36));
        return new GuiBounds((screenWidth - width) / 2, (screenHeight - height) / 2, width, height);
    }

    private void frame(GuiRenderer renderer, int x, int y, int width, int height) {
        renderer.fill(x, y, x + width, y + height, theme.panel);
        renderer.fill(x, y, x + width - 1, y + 1, theme.border);
        renderer.fill(x, y, x + 1, y + height - 1, theme.border);
        renderer.fill(x + 1, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x + width - 1, y + 1, x + width, y + height, theme.borderDark);
    }

    private void boxed(GuiRenderer renderer, GuiBounds bounds, int fill, int border) {
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, fill);
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width - 1, bounds.y + 1, border);
        renderer.fill(bounds.x, bounds.y, bounds.x + 1, bounds.y + bounds.height - 1, border);
        renderer.fill(bounds.x + 1, bounds.y + bounds.height - 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
        renderer.fill(bounds.x + bounds.width - 1, bounds.y + 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
    }

    private void drawChevron(GuiRenderer renderer, int x, int y, Direction direction, int color) {
        if (direction == Direction.DOWN) {
            renderer.fill(x, y, x + 7, y + 1, color);
            renderer.fill(x + 1, y + 1, x + 6, y + 2, color);
            renderer.fill(x + 2, y + 2, x + 5, y + 3, color);
            renderer.fill(x + 3, y + 3, x + 4, y + 4, color);
            return;
        }
        if (direction == Direction.RIGHT) {
            renderer.fill(x, y, x + 1, y + 7, color);
            renderer.fill(x + 1, y + 1, x + 2, y + 6, color);
            renderer.fill(x + 2, y + 2, x + 3, y + 5, color);
            renderer.fill(x + 3, y + 3, x + 4, y + 4, color);
        }
    }

    private static int color(ConfigOption option) {
        return option.get() instanceof Number ? ((Number) option.get()).intValue() : 0xFFFFFFFF;
    }

    private static String hex(int color) {
        String hex = Integer.toHexString(color).toUpperCase();
        while (hex.length() < 8) {
            hex = "0" + hex;
        }
        return "#" + hex.substring(2);
    }

    private static boolean isHex(char character) {
        return (character >= '0' && character <= '9') || (character >= 'a' && character <= 'f')
                || (character >= 'A' && character <= 'F');
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float[] rgbToHsv(int color) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;
        float hue;
        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == red) {
            hue = ((green - blue) / delta) % 6.0F;
        } else if (max == green) {
            hue = (blue - red) / delta + 2.0F;
        } else {
            hue = (red - green) / delta + 4.0F;
        }
        hue /= 6.0F;
        if (hue < 0.0F) {
            hue += 1.0F;
        }
        float saturation = max == 0.0F ? 0.0F : delta / max;
        return new float[]{hue, saturation, max};
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        float scaled = hue * 6.0F;
        int section = (int) Math.floor(scaled);
        float fraction = scaled - section;
        float p = value * (1.0F - saturation);
        float q = value * (1.0F - fraction * saturation);
        float t = value * (1.0F - (1.0F - fraction) * saturation);
        float red;
        float green;
        float blue;
        switch (section % 6) {
            case 0 :
                red = value;
                green = t;
                blue = p;
                break;
            case 1 :
                red = q;
                green = value;
                blue = p;
                break;
            case 2 :
                red = p;
                green = value;
                blue = t;
                break;
            case 3 :
                red = p;
                green = q;
                blue = value;
                break;
            case 4 :
                red = t;
                green = p;
                blue = value;
                break;
            default :
                red = value;
                green = p;
                blue = q;
                break;
        }
        return Math.round(red * 255.0F) << 16 | Math.round(green * 255.0F) << 8 | Math.round(blue * 255.0F);
    }

    private enum ColorDrag {
        NONE, SQUARE, HUE
    }

    private enum Direction {
        DOWN, RIGHT
    }

    public interface KeyNameProvider {
        String name(int keyCode);
    }

    public interface SidebarHeader {
        void render(GuiRenderer renderer, GuiBounds bounds, ConfigTheme theme);
    }
}
