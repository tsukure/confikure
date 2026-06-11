package re.tsuku.confikure.gui.editor;

import java.util.HashMap;
import java.util.Map;
import re.tsuku.confikure.model.EditorType;

public final class DefaultOptionEditors {
    private DefaultOptionEditors() {
    }

    public static Map<EditorType, OptionEditor> create() {
        Map<EditorType, OptionEditor> editors = new HashMap<>();
        editors.put(EditorType.BOOLEAN, new SwitchEditor());
        editors.put(EditorType.NUMBER, new SliderEditor());
        editors.put(EditorType.TEXT, new TextEditor(false));
        editors.put(EditorType.MULTILINE_TEXT, new TextEditor(true));
        editors.put(EditorType.DROPDOWN, new DropdownEditor());
        editors.put(EditorType.MODE, new ModeEditor());
        editors.put(EditorType.COLOR, new ColorEditor());
        editors.put(EditorType.KEYBIND, new KeybindEditor());
        editors.put(EditorType.BUTTON, new ButtonEditor());
        editors.put(EditorType.DRAGGABLE_LIST, new ListEditor());
        editors.put(EditorType.INFO, new InfoEditor());
        editors.put(EditorType.CUSTOM, new TextEditor(false));
        return editors;
    }
}
