package re.tsuku.confikure.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link OptionAccess} implementation that invokes a zero-argument method.
 */
public final class ButtonOptionAccess implements OptionAccess {
    private final Method method;
    private final Object owner;

    public ButtonOptionAccess(Method method, Object owner) {
        this.method = method;
        this.owner = owner;
    }

    /**
     * returns {@link Void#TYPE} because button options do not expose a value.
     */
    public Class<?> valueType() {
        return Void.TYPE;
    }

    /**
     * returns {@code null} because button options do not expose a value.
     */
    public Object get() {
        return null;
    }

    /**
     * invokes the configured method.
     */
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
