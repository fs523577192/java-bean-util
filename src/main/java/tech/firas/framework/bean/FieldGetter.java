/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.firas.framework.bean;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class FieldGetter<T> extends FieldAccessor {

    private final Method getMethod;

    /**
     * Construct a FieldGetter to get a field in `srcClass`
     *
     * The `field` may be declared in a base class of `srcClass`
     *
     * @param srcClass  the type of the Java bean whose field is to be read
     * @param field  the field to be read
     * @param configuration  specifies how to get the field value
     * @throws NoSuchMethodException  if the configuration does not allow directly access to the field but
     *                                 there is no public getter of the field
     */
    public FieldGetter(final Class<? super T> srcClass, final Field field, final Configuration configuration)
            throws NoSuchMethodException {
        super(field);
        final Class<?> fieldType = field.getType();
        final Class<?> declaringClass = field.getDeclaringClass();

        final Pattern getterPattern = Pattern.compile(
                (fieldType.equals(boolean.class) ?
                        (configuration.isAllowGetBoolean() ? "(get|is)" : "is") : "get") +
                        field.getName().substring(0, 1).toUpperCase(Locale.US) + field.getName().substring(1));
        for (Class<?> clazz = srcClass; declaringClass.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
            for (final Method method : clazz.getMethods()) {
                if (getterPattern.matcher(method.getName()).matches() &&
                        method.getReturnType().isAssignableFrom(fieldType) &&
                        method.getParameterTypes().length == 0) {
                    this.getMethod = method;
                    return;
                }
            }
        }
        if (!configuration.allowDirectlyGetField) {
            throw new NoSuchMethodException("Found no getter for \"" + field.getName() + "\" in \"" +
                    srcClass.getName() + '\"');
        }
        this.getMethod = null;
        field.setAccessible(true);
    }

    /**
     *
     * @return  the type of the value got from this getter
     */
    public Class<?> getReturnType() {
        if (this.getMethod == null) {
            return this.field.getType();
        }
        return this.getMethod.getReturnType();
    }

    /**
     * Get the field value of the Java bean `obj`
     * @param obj  the Java bean to set
     * @return  the value of the field
     * @throws InvocationTargetException  if it fails to set the value of the field in `obj`
     * @throws IllegalAccessException  if it fails to access the field / its getter
     */
    public Object get(final T obj) throws IllegalAccessException, InvocationTargetException {
        if (this.getMethod == null) {
            return field.get(obj);
        }
        return this.getMethod.invoke(obj);
    }

    public static final class Configuration implements Serializable {
        /**
         * whether allow using getXXX method to get a boolean field
         */
        private boolean allowGetBoolean;

        /**
         * whether try to access the field directly when there is no getter
         */
        private boolean allowDirectlyGetField;

        public boolean isAllowGetBoolean() {
            return allowGetBoolean;
        }

        public void setAllowGetBoolean(boolean allowGetBoolean) {
            this.allowGetBoolean = allowGetBoolean;
        }

        public boolean isAllowDirectlyGetField() {
            return allowDirectlyGetField;
        }

        public void setAllowDirectlyGetField(boolean allowDirectlyGetField) {
            this.allowDirectlyGetField = allowDirectlyGetField;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Configuration that = (Configuration) o;
            return allowGetBoolean == that.allowGetBoolean &&
                    allowDirectlyGetField == that.allowDirectlyGetField;
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowGetBoolean, allowDirectlyGetField);
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "allowGetBoolean=" + allowGetBoolean +
                    ", allowDirectlyGetField=" + allowDirectlyGetField +
                    '}';
        }
    }
}