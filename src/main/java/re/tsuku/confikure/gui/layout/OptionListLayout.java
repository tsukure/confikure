package re.tsuku.confikure.gui.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.model.ConfigCategory;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;

public final class OptionListLayout {
    public interface GroupState {
        boolean collapsed(ConfigGroup group);
    }

    public interface RowSizing {
        int height(ConfigOption option);
    }

    private final List<GroupBlock> groups;
    private final List<OptionRow> rows;
    private final int contentHeight;

    private OptionListLayout(List<GroupBlock> groups, List<OptionRow> rows, int contentHeight) {
        this.groups = groups;
        this.rows = rows;
        this.contentHeight = contentHeight;
    }

    public static OptionListLayout create(ConfigCategory category, int x, int y, int width, int scroll,
            int groupHeaderHeight, int groupHeaderStep, int optionGap, int groupGap, GroupState groupState,
            RowSizing rowSizing) {
        if (category == null) {
            return new OptionListLayout(Collections.<GroupBlock>emptyList(), Collections.<OptionRow>emptyList(), 0);
        }
        List<GroupBlock> groups = new ArrayList<GroupBlock>();
        List<OptionRow> rows = new ArrayList<OptionRow>();
        int drawY = y - scroll;
        int contentHeight = 0;
        for (ConfigGroup group : category.groups()) {
            if (!hasVisibleOptions(group)) {
                continue;
            }
            boolean collapsed = groupState.collapsed(group);
            int groupHeight = groupHeight(group, collapsed, groupHeaderStep, optionGap, rowSizing);
            GuiBounds shell = new GuiBounds(x, drawY, width, groupHeight);
            GuiBounds header = new GuiBounds(x, drawY, width, groupHeaderHeight);
            List<OptionRow> groupRows = new ArrayList<OptionRow>();
            int optionY = drawY + groupHeaderStep;
            if (!collapsed) {
                for (ConfigOption option : group.options()) {
                    if (!option.visible()) {
                        continue;
                    }
                    GuiBounds bounds = new GuiBounds(x, optionY, width, rowSizing.height(option));
                    OptionRow row = new OptionRow(option, bounds);
                    rows.add(row);
                    groupRows.add(row);
                    optionY += bounds.height + optionGap;
                }
            }
            groups.add(new GroupBlock(group, shell, header, groupRows, collapsed));
            drawY += groupHeight + groupGap;
            contentHeight += groupHeight + groupGap;
        }
        return new OptionListLayout(groups, rows, contentHeight);
    }

    public List<GroupBlock> groups() {
        return groups;
    }

    public int contentHeight() {
        return contentHeight;
    }

    public ConfigOption optionAt(int mouseX, int mouseY) {
        for (OptionRow row : rows) {
            if (row.bounds().contains(mouseX, mouseY)) {
                return row.option();
            }
        }
        return null;
    }

    public GuiBounds optionBounds(ConfigOption target, GuiBounds fallback) {
        for (OptionRow row : rows) {
            if (row.option() == target) {
                return row.bounds();
            }
        }
        return fallback;
    }

    public ConfigGroup groupHeaderAt(int mouseX, int mouseY) {
        for (GroupBlock group : groups) {
            if (group.header().contains(mouseX, mouseY)) {
                return group.group();
            }
        }
        return null;
    }

    public boolean containsVisibleOption(ConfigOption target) {
        for (OptionRow row : rows) {
            if (row.option() == target) {
                return true;
            }
        }
        return false;
    }

    private static int groupHeight(ConfigGroup group, boolean collapsed, int groupHeaderStep, int optionGap,
            RowSizing rowSizing) {
        int height = groupHeaderStep;
        if (!collapsed) {
            for (ConfigOption option : group.options()) {
                if (option.visible()) {
                    height += rowSizing.height(option) + optionGap;
                }
            }
        }
        return height;
    }

    private static boolean hasVisibleOptions(ConfigGroup group) {
        for (ConfigOption option : group.options()) {
            if (option.visible()) {
                return true;
            }
        }
        return false;
    }

    public static final class GroupBlock {
        private final ConfigGroup group;
        private final GuiBounds shell;
        private final GuiBounds header;
        private final List<OptionRow> rows;
        private final boolean collapsed;

        private GroupBlock(ConfigGroup group, GuiBounds shell, GuiBounds header, List<OptionRow> rows,
                boolean collapsed) {
            this.group = group;
            this.shell = shell;
            this.header = header;
            this.rows = rows;
            this.collapsed = collapsed;
        }

        public ConfigGroup group() {
            return group;
        }

        public GuiBounds shell() {
            return shell;
        }

        public GuiBounds header() {
            return header;
        }

        public List<OptionRow> rows() {
            return rows;
        }

        public boolean collapsed() {
            return collapsed;
        }
    }

    public static final class OptionRow {
        private final ConfigOption option;
        private final GuiBounds bounds;

        private OptionRow(ConfigOption option, GuiBounds bounds) {
            this.option = option;
            this.bounds = bounds;
        }

        public ConfigOption option() {
            return option;
        }

        public GuiBounds bounds() {
            return bounds;
        }
    }
}
