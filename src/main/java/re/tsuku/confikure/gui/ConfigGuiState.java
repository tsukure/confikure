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

    /**
     * returns the selected category id.
     */
    public String selectedCategoryId() {
        return selectedCategoryId;
    }

    /**
     * sets the selected category id.
     */
    public void selectedCategoryId(String selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    /**
     * returns whether a group is marked collapsed.
     */
    public boolean collapsed(String categoryId, String groupId) {
        return collapsedGroups.contains(groupKey(categoryId, groupId));
    }

    /**
     * marks a group as collapsed or expanded.
     */
    public void collapsed(String categoryId, String groupId, boolean collapsed) {
        String key = groupKey(categoryId, groupId);
        if (collapsed) {
            collapsedGroups.add(key);
        } else {
            collapsedGroups.remove(key);
        }
    }

    /**
     * returns collapsed group keys in {@code category/group} form.
     */
    public Set<String> collapsedGroups() {
        return Collections.unmodifiableSet(collapsedGroups);
    }

    /**
     * clears collapsed group state.
     */
    public void clearCollapsedGroups() {
        collapsedGroups.clear();
    }

    /**
     * clears all stored gui state.
     */
    public void clear() {
        selectedCategoryId = null;
        collapsedGroups.clear();
    }

    /**
     * builds the stable collapsed-group key used by this state object.
     */
    public static String groupKey(String categoryId, String groupId) {
        return String.valueOf(categoryId) + "/" + String.valueOf(groupId);
    }
}
