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

public class FieldSetter<E> extends FieldAccessor {

    private final Method setMethod;

    /**
     * Construct a FieldSetter to get a field in `destClass`
     *
     * The `field` may be declared in a base class of `destClass`
     *
     * @param destClass  the type of the Java bean whose field is to be written
     * @param field  the field to be written
     * @param configuration  specifies how to set the field value
     * @throws NoSuchMethodException  if the configuration does not allow directly access to the field but
     *                                 there is no public setter of the field
     */
    public FieldSetter(final Class<? super E> destClass, final Field field, final Configuration configuration)
            throws NoSuchMethodException {
        super(field);
        final Class<?> fieldType = field.getType();
        final Class<?> declaringClass = field.getDeclaringClass();

        final String setterName = "set" + field.getName().substring(0, 1).toUpperCase(Locale.US) +
                field.getName().substring(1);
        for (Class<?> clazz = destClass; declaringClass.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
            for (final Method method : clazz.getMethods()) {
                if (!setterName.equals(method.getName())) {
                    continue;
                }
                Class<?>[] types = method.getParameterTypes();
                if (types.length == 1 && ObjectType.isObjectTypeAssignableFrom(fieldType, types[0])) {
                    this.setMethod = method;
                    return;
                }
            }
        }
        if (!configuration.allowDirectlySetField) {
            throw new NoSuchMethodException("Found no setter for \"" + field.getName() + "\" in \"" +
                    destClass.getName() + '\"');
        }
        this.setMethod = null;
        field.setAccessible(true);
    }

    /**
     *
     * @return  the type of the field value that this setter accepts
     */
    public Class<?> getParameterType() {
        if (this.setMethod == null) {
            return this.field.getType();
        }
        return this.setMethod.getParameterTypes()[0];
    }

    /**
     * Set the field value of the Java bean `obj` to `value`
     * @param obj  the Java bean to set
     * @param value  the value of the field
     * @throws InvocationTargetException  if it fails to set the value of the field in `obj`
     * @throws IllegalAccessException  if it fails to access the field / its setter
     */
    public void set(E obj, Object value) throws InvocationTargetException, IllegalAccessException {
        if (this.setMethod == null) {
            this.field.set(obj, value);
        } else {
            this.setMethod.invoke(obj, value);
        }
    }

    public static final class Configuration implements Serializable {
        /**
         * whether try to access the field directly when there is no setter
         */
        private boolean allowDirectlySetField;

        public boolean isAllowDirectlySetField() {
            return allowDirectlySetField;
        }

        public void setAllowDirectlySetField(boolean allowDirectlySetField) {
            this.allowDirectlySetField = allowDirectlySetField;
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
            return allowDirectlySetField == that.allowDirectlySetField;
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowDirectlySetField);
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "allowDirectlySetField=" + allowDirectlySetField +
                    '}';
        }
    }
}
