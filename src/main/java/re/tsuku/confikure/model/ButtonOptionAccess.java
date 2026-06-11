package re.tsuku.confikure.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ButtonOptionAccess implements OptionAccess {
    private final Method method;
    private final Object owner;

    public ButtonOptionAccess(Method method, Object owner) {
        this.method = method;
        this.owner = owner;
    }

    public Class<?> valueType() {
        return Void.TYPE;
    }

    public Object get() {
        return null;
    }

    public void set(Object value) {
        try {
            method.invoke(owner);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("unable to invoke button method: " + method, exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("button method failed: " + method, exception.getCause());
        }
    }
}
