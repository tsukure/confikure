package re.tsuku.confikure.gui;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * persisted gui-only state for a config screen.
 */
public final class ConfigGuiState {
    private String selectedCategoryId;
    private final Set<String> collapsedGroups = new LinkedHashSet<String>();

    public String selectedCategoryId() {
        return selectedCategoryId;
    }

    public void selectedCategoryId(String selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public boolean collapsed(String categoryId, String groupId) {
        return collapsedGroups.contains(groupKey(categoryId, groupId));
    }

    public void collapsed(String categoryId, String groupId, boolean collapsed) {
        String key = groupKey(categoryId, groupId);
        if (collapsed) {
            collapsedGroups.add(key);
        } else {
            collapsedGroups.remove(key);
        }
    }

    public Set<String> collapsedGroups() {
        return Collections.unmodifiableSet(collapsedGroups);
    }

    public void clearCollapsedGroups() {
        collapsedGroups.clear();
    }

    public void clear() {
        selectedCategoryId = null;
        collapsedGroups.clear();
    }

    public static String groupKey(String categoryId, String groupId) {
        return String.valueOf(categoryId) + "/" + String.valueOf(groupId);
    }
}
