package re.tsuku.confikure.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import re.tsuku.confikure.gui.GuiPrimitives.Direction;
import re.tsuku.confikure.gui.color.ColorPickerPopup;
import re.tsuku.confikure.gui.editor.DefaultOptionEditors;
import re.tsuku.confikure.gui.editor.EditorContext;
import re.tsuku.confikure.gui.editor.OptionEditor;
import re.tsuku.confikure.gui.format.NumberDisplay;
import re.tsuku.confikure.gui.input.TextInputState;
import re.tsuku.confikure.gui.input.TextInputState.KeyResult;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.layout.OptionListLayout;
import re.tsuku.confikure.gui.layout.OptionListLayout.GroupBlock;
import re.tsuku.confikure.gui.layout.OptionListLayout.OptionRow;
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
    private static final int TOP_BAR_HEIGHT = 32;
    private static final int DROP_ITEM_HEIGHT = 18;
    private static final int OPTION_GAP = 0;
    private static final int GROUP_HEADER_HEIGHT = 24;
    private static final int GROUP_HEADER_STEP = GROUP_HEADER_HEIGHT;
    private static final int GROUP_HEADER_ACCENT_WIDTH = 3;
    private static final int GROUP_BODY_ACCENT_WIDTH = 2;
    private static final int TEXT_LINE_GAP = 2;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int SCROLLBAR_GUTTER_WIDTH = 10;
    private static final int SCROLLBAR_GUTTER_GAP = 6;

    private final ConfigDefinition definition;
    private final Map<EditorType, OptionEditor> editors;
    private final Map<ConfigOption, TextInputState> textStates = new HashMap<ConfigOption, TextInputState>();
    private final Map<String, Boolean> collapsedGroups = new HashMap<String, Boolean>();
    private final ColorPickerPopup colorPicker = new ColorPickerPopup();
    private final ConfigGuiColorPickerHost colorPickerHost = new ConfigGuiColorPickerHost(this);
    private ConfigTheme theme;
    private Supplier<ConfigTheme> themeSupplier;
    private GuiRenderer renderer;
    private KeyNameProvider keyNameProvider = DefaultKeyNameProvider.INSTANCE;
    private int selectedCategory;
    private int scroll;
    private ConfigOption focusedOption;
    private ConfigOption hoveredOption;
    private ConfigOption activeOption;
    private ConfigOption openDropdown;
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

    public ConfigDefinition definition() {
        return definition;
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
        focusedOption = null;
        activeOption = null;
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
        GuiPrimitives.frame(renderer, theme, panel);

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
        if (colorPicker.isOpen()
                && colorPicker.handleMouse(panel, mouseX, mouseY, true, colorPickerHost, theme.padding)) {
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
            activeOption = option;
            focusText(option, mouseX, optionBounds(panel, option));
            return;
        }
        if (option.type() == EditorType.NUMBER) {
            if (ControlLayout.sliderField(optionBounds(panel, option)).contains(mouseX, mouseY)) {
                focusedOption = option;
                focusNumber(option);
                focusNumberCursor(option, optionBounds(panel, option), mouseX);
                activeOption = option;
                return;
            }
            if (!ControlLayout.sliderHit(optionBounds(panel, option)).contains(mouseX, mouseY)) {
                focusedOption = null;
                return;
            }
            focusedOption = null;
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
            colorPicker.open(option);
            return;
        }
        if (option.type() == EditorType.KEYBIND) {
            if (option.keybindClearable()
                    && ControlLayout.keybindClear(optionBounds(panel, option)).contains(mouseX, mouseY)) {
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
        if (!interactive(activeOption)) {
            activeOption = null;
            return;
        }
        if (activeOption == focusedOption && isTextSelectionOption(activeOption)) {
            dragTextSelection(panel, activeOption, mouseX);
            return;
        }
        if (activeOption.type() == EditorType.NUMBER) {
            setSliderValue(activeOption, optionBounds(panel, activeOption), mouseX);
            return;
        }
        if (activeOption == colorPicker.option()) {
            colorPicker.handleMouse(panel, mouseX, mouseY, false, colorPickerHost, theme.padding);
        }
    }

    public void release() {
        activeOption = null;
        colorPicker.stopDrag();
        draggingScrollbar = false;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        return keyTyped(typedChar, keyCode, false, false);
    }

    public boolean keyTyped(char typedChar, int keyCode, boolean shift, boolean control) {
        if ((keyCode == 1 || keyCode == 28) && (openDropdown != null || colorPicker.isOpen())
                && focusedOption == null) {
            cancelFocus();
            return true;
        }
        if (focusedOption == null) {
            return false;
        }
        if (!interactive(focusedOption)) {
            focusedOption = null;
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

    public boolean colorPickerOpen(ConfigOption option) {
        return colorPicker.isOpen(option);
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
        if (option.type() == EditorType.NUMBER) {
            return NumberDisplay.format(option);
        }
        if (option.type() == EditorType.COLOR) {
            return ColorPickerPopup.format(option);
        }
        return String.valueOf(option.get());
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    GuiBounds optionBounds(int screenWidth, int screenHeight, ConfigOption option) {
        return optionBounds(panel(screenWidth, screenHeight), option);
    }

    GuiBounds controlBounds(int screenWidth, int screenHeight, ConfigOption option) {
        return controlBounds(optionBounds(screenWidth, screenHeight, option), option);
    }

    GuiBounds colorHexBounds(int screenWidth, int screenHeight, ConfigOption option) {
        GuiBounds panel = panel(screenWidth, screenHeight);
        return colorPicker.hexBounds(panel, option, optionBounds(panel, option), theme.padding);
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
            GuiPrimitives.box(renderer, theme, tab, fill, selected ? theme.accent : theme.border);
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

        OptionListLayout layout = optionLayout(panel, category);
        for (GroupBlock group : layout.groups()) {
            drawGroupShell(renderer, group.shell(), group.collapsed());
            drawGroupHeader(renderer, group.group(), group.header(), group.collapsed());
            for (OptionRow row : group.rows()) {
                drawOption(renderer, row.option(), row.bounds());
            }
            drawGroupBottomBorder(renderer, group.shell());
        }

        renderer.popClip();
        drawScrollbar(renderer, panel);
        drawDropdown(renderer, panel);
        drawColorPicker(renderer, panel);
    }

    private void drawOption(GuiRenderer renderer, ConfigOption option, GuiBounds bounds) {
        if (option.type() == EditorType.INFO) {
            renderer.fill(bounds.x + 1 + GROUP_BODY_ACCENT_WIDTH, bounds.y, bounds.x + bounds.width - 1,
                    bounds.y + bounds.height, theme.panelRaised);
            editor(option).render(option, bounds, renderer, theme, this);
            return;
        }
        int rowX = bounds.x + 1 + GROUP_BODY_ACCENT_WIDTH;
        renderer.fill(rowX, bounds.y, bounds.x + bounds.width - 1, bounds.y + bounds.height,
                theme.panelRaised);
        renderer.fill(rowX, bounds.y, bounds.x + bounds.width - 1, bounds.y + 1, theme.panel);
        int nameColor = !option.enabled()
                ? theme.disabledText
                : option.type() == EditorType.INFO ? theme.mutedText : theme.text;
        boolean hasDescription = !option.description().isEmpty();
        int nameY = optionTextY(renderer, bounds, option, hasDescription);
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
        GuiPrimitives.chevron(renderer, bounds.x + 13, chevronY(bounds, collapsed ? Direction.RIGHT : Direction.DOWN),
                collapsed ? Direction.RIGHT : Direction.DOWN,
                hovered ? theme.accent : theme.mutedText);
        int nameY = bounds.y + Math.max(3, (bounds.height - renderer.fontHeight()) / 2);
        renderer.text(group.name(), bounds.x + 30, nameY, hovered ? theme.accent : theme.text);
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

    private int optionTextY(GuiRenderer renderer, GuiBounds bounds, ConfigOption option, boolean hasDescription) {
        if (rowHeight(option) > Math.max(28, theme.rowHeight)) {
            return bounds.y + 6;
        }
        if (!hasDescription) {
            return bounds.y + Math.max(4, (bounds.height - renderer.fontHeight()) / 2);
        }
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
        int textX = x + theme.padding;
        int textY = y + (TOP_BAR_HEIGHT - renderer.fontHeight()) / 2;
        renderer.text(category.name(), textX, textY, theme.text);
        GuiBounds close = closeBounds(panel);
        boolean hovered = close.contains(mouseX, mouseY);
        GuiPrimitives.box(renderer, theme, close, hovered ? theme.danger : theme.panel,
                hovered ? theme.text : theme.border);
        renderer.centeredText("x", close.x, close.y + 5, close.width, hovered ? theme.text : theme.mutedText);
    }

    private void drawDropdown(GuiRenderer renderer, GuiBounds panel) {
        if (openDropdown == null || openDropdown.choices().isEmpty()) {
            return;
        }
        if (!interactive(openDropdown)) {
            openDropdown = null;
            return;
        }
        GuiBounds bounds = dropdownBounds(panel, openDropdown);
        GuiPrimitives.frame(renderer, theme, bounds);
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
        colorPicker.render(renderer, theme, panel, colorPickerHost);
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
        return optionLayout(category, 0, 0, 0, 0).contentHeight();
    }

    private ConfigOption optionAt(GuiBounds panel, int mouseX, int mouseY) {
        ConfigCategory category = category();
        if (category == null) {
            return null;
        }
        return optionLayout(panel, category).optionAt(mouseX, mouseY);
    }

    private ConfigOption optionAtComponent(GuiBounds panel, int mouseX, int mouseY) {
        ConfigOption option = optionAt(panel, mouseX, mouseY);
        if (option == null) {
            return null;
        }
        if (option.type() == EditorType.INFO) {
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

    GuiBounds optionBounds(GuiBounds panel, ConfigOption target) {
        ConfigCategory category = category();
        int contentX = contentX(panel);
        int contentWidth = contentWidth(panel);
        GuiBounds fallback = new GuiBounds(contentX, panel.y + theme.padding, contentWidth, rowHeight(target));
        return optionLayout(panel, category).optionBounds(target, fallback);
    }

    private String groupHeaderAt(GuiBounds panel, int mouseX, int mouseY) {
        ConfigCategory category = category();
        if (category == null) {
            return null;
        }
        ConfigGroup group = optionLayout(panel, category).groupHeaderAt(mouseX, mouseY);
        return group == null ? null : groupKey(group);
    }

    boolean interactive(ConfigOption target) {
        return target != null && target.enabled() && target.visible() && visibleInCurrentCategory(target);
    }

    private boolean visibleInCurrentCategory(ConfigOption target) {
        ConfigCategory category = category();
        if (category == null) {
            return false;
        }
        return optionLayout(category, 0, 0, 0, 0).containsVisibleOption(target);
    }

    private OptionListLayout optionLayout(GuiBounds panel, ConfigCategory category) {
        return optionLayout(category, contentX(panel), contentY(panel), contentWidth(panel), scroll);
    }

    private OptionListLayout optionLayout(ConfigCategory category, int x, int y, int width, int scroll) {
        return OptionListLayout.create(category, x, y, width, scroll, GROUP_HEADER_HEIGHT, GROUP_HEADER_STEP,
                OPTION_GAP, theme.groupGap, new OptionListLayout.GroupState() {
                    public boolean collapsed(ConfigGroup group) {
                        return ConfigGui.this.collapsed(group);
                    }
                }, new OptionListLayout.RowSizing() {
                    public int height(ConfigOption option) {
                        return rowHeight(option);
                    }
                });
    }

    private GuiBounds categoryBounds(GuiBounds panel, int index) {
        return new GuiBounds(panel.x + 7, panel.y + SIDEBAR_HEADER_HEIGHT + index * 24, SIDEBAR_WIDTH - 14, 20);
    }

    private GuiBounds closeBounds(GuiBounds panel) {
        return new GuiBounds(panel.x + panel.width - 25, panel.y + 1 + (TOP_BAR_HEIGHT - 18) / 2, 18, 18);
    }

    private GuiBounds dropdownBounds(GuiBounds panel, ConfigOption option) {
        GuiBounds row = optionBounds(panel, option);
        GuiBounds control = ControlLayout.editor(row);
        int y = row.y + row.height - 7;
        return new GuiBounds(control.x, y, control.width, option.choices().size() * DROP_ITEM_HEIGHT + 2);
    }

    private GuiBounds controlBounds(GuiBounds row, ConfigOption option) {
        if (option.type() == EditorType.MODE) {
            return ControlLayout.mode(row);
        }
        if (option.type() == EditorType.BOOLEAN) {
            return ControlLayout.switchControl(row);
        }
        if (option.type() == EditorType.NUMBER) {
            return ControlLayout.number(row);
        }
        if (option.type() == EditorType.COLOR) {
            return ControlLayout.colorSwatch(row);
        }
        if (option.type() == EditorType.KEYBIND) {
            return ControlLayout.keybind(row);
        }
        if (option.type() == EditorType.BUTTON) {
            return ControlLayout.button(row);
        }
        return ControlLayout.text(row, option.type() == EditorType.MULTILINE_TEXT);
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
        return new GuiBounds(gutter.x + (gutter.width - SCROLLBAR_WIDTH) / 2, gutter.y, SCROLLBAR_WIDTH,
                gutter.height);
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
        boolean thumbHovered = thumb.contains(mouseX, mouseY) || draggingScrollbar;
        renderer.fill(gutter.x, gutter.y, gutter.x + gutter.width, gutter.y + gutter.height, theme.panel);
        renderer.fill(track.x + 2, track.y, track.x + track.width - 2, track.y + track.height, theme.slot);
        renderer.fill(track.x + 1, track.y, track.x + 2, track.y + track.height, theme.borderDark);
        renderer.fill(track.x + track.width - 2, track.y, track.x + track.width - 1, track.y + track.height,
                theme.borderDark);
        GuiPrimitives.box(renderer, theme, thumb, thumbHovered ? theme.panelRaised : theme.panel,
                thumbHovered ? theme.accent : theme.border);
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
        if (!interactive(openDropdown)) {
            openDropdown = null;
            return true;
        }
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

    private void setSliderValue(ConfigOption option, GuiBounds bounds, int mouseX) {
        NumberRange range = option.range();
        if (range == null) {
            return;
        }
        GuiBounds track = ControlLayout.sliderTrack(bounds);
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
        GuiBounds control = ControlLayout.mode(row);
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
                    ControlLayout.text(bounds, option.type() == EditorType.MULTILINE_TEXT).x + 5, mouseX);
        }
    }

    private void focusNumber(ConfigOption option) {
        TextInputState state = textState(option);
        state.text(NumberDisplay.format(option));
        state.maxLength(32);
        state.filter(new TextInputState.CharacterFilter() {
            public boolean accept(char character) {
                return character == '-' || character == '.' || character >= '0' && character <= '9';
            }
        });
    }

    private void focusNumberCursor(ConfigOption option, GuiBounds row, int mouseX) {
        if (renderer == null) {
            return;
        }
        GuiBounds field = ControlLayout.sliderField(row);
        TextInputState state = textState(option);
        state.cursorAt(renderer, state.text(), field.x + 5, mouseX);
    }

    void focusColor(ConfigOption option, GuiBounds hex, int mouseX) {
        focusedOption = option;
        activeOption = option;
        TextInputState state = textState(option);
        state.text(ColorPickerPopup.format(option));
        state.maxLength(option.colorAlpha() ? 9 : 7);
        state.filter(new TextInputState.CharacterFilter() {
            public boolean accept(char character) {
                return character == '#' || isHex(character);
            }
        });
        if (renderer != null) {
            state.cursorAt(renderer, state.text(), hex.x + 5, mouseX);
        }
    }

    void activatePopup(ConfigOption option) {
        activeOption = option;
        focusedOption = null;
    }

    void clearPopupActive() {
        activeOption = null;
    }

    private boolean isTextSelectionOption(ConfigOption option) {
        return option.type() == EditorType.TEXT || option.type() == EditorType.MULTILINE_TEXT
                || option.type() == EditorType.NUMBER || option.type() == EditorType.COLOR;
    }

    private void dragTextSelection(GuiBounds panel, ConfigOption option, int mouseX) {
        if (renderer == null) {
            return;
        }
        TextInputState state = textState(option);
        if (option.type() == EditorType.TEXT || option.type() == EditorType.MULTILINE_TEXT) {
            GuiBounds row = optionBounds(panel, option);
            GuiBounds control = ControlLayout.text(row, option.type() == EditorType.MULTILINE_TEXT);
            state.selectAt(renderer, firstLine(state.text()), control.x + 5, mouseX);
            return;
        }
        if (option.type() == EditorType.NUMBER) {
            GuiBounds field = ControlLayout.sliderField(optionBounds(panel, option));
            state.selectAt(renderer, state.text(), field.x + 5, mouseX);
            return;
        }
        if (option.type() == EditorType.COLOR && colorPicker.isOpen(option)) {
            GuiBounds hex = colorPicker.hexBounds(panel, option, optionBounds(panel, option), theme.padding);
            state.selectAt(renderer, state.text(), hex.x + 5, mouseX);
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
            state.text(NumberDisplay.format(option));
            focusedOption = null;
        } else if (result == KeyResult.CANCEL) {
            state.text(NumberDisplay.format(option));
            focusedOption = null;
        }
    }

    private void editColor(ConfigOption option, char typedChar, int keyCode, boolean shift, boolean control) {
        TextInputState state = textState(option);
        KeyResult result = state.keyTyped(Character.toUpperCase(typedChar), keyCode, shift, control, false);
        if (result == KeyResult.CHANGED) {
            ColorPickerPopup.parse(option, state.text());
        } else if (result == KeyResult.COMMIT) {
            ColorPickerPopup.parse(option, state.text());
            state.text(ColorPickerPopup.format(option));
            focusedOption = null;
        } else if (result == KeyResult.CANCEL) {
            state.text(ColorPickerPopup.format(option));
            focusedOption = null;
        }
    }

    private void cancelFocus() {
        focusedOption = null;
        openDropdown = null;
        colorPicker.close();
        activeOption = null;
    }

    private void parseNumber(ConfigOption option, String text) {
        try {
            if (!text.isEmpty() && !text.equals("-") && !text.equals(".")) {
                option.set(Double.valueOf(text));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    void drawTextField(GuiRenderer renderer, GuiBounds bounds, String text, int cursor, int selectionStart,
            int selectionEnd, boolean hovered, boolean focused) {
        GuiPrimitives.textField(renderer, theme, bounds, text, cursor, selectionStart, selectionEnd, hovered, focused,
                true, false);
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
        colorPicker.close();
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

    private static boolean isHex(char character) {
        return (character >= '0' && character <= '9') || (character >= 'a' && character <= 'f')
                || (character >= 'A' && character <= 'F');
    }

    public interface KeyNameProvider {
        String name(int keyCode);
    }

    public interface SidebarHeader {
        void render(GuiRenderer renderer, GuiBounds bounds, ConfigTheme theme);
    }
}
