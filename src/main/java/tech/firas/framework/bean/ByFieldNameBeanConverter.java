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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.convert.converter.Converter;

public class ByFieldNameBeanConverter<S, D> implements Converter<S, D> {

    private static final Logger logger = Logger.getLogger(ByFieldNameBeanConverter.class.getName());

    private final Constructor<D> constructor;
    private final Map<FieldGetter<S>, FieldSetter<D>> map;

    /**
     * Construct a ByFieldNameBeanConverter
     * @param srcClass  the type of the source Java bean to convert from
     * @param destClass  the type of the target Java bean to convert to
     * @param configuration  specifies how to get / set the fields of the Java bean
     * @throws NoSuchMethodException  if the target Java bean has no accessible default constructor
     */
    public ByFieldNameBeanConverter(final Class<S> srcClass, final Class<D> destClass,
            final Configuration configuration) throws NoSuchMethodException {
        final Configuration conf = configuration == null ? new Configuration() : configuration;
        try {
            this.constructor = destClass.getConstructor();
            this.constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, "Fail to access default constructor", ex);
            throw new NoSuchMethodException("There is no accessible default constructor (no parameter) for " +
                    destClass.getName());
        }

        this.map = new HashMap<>();
        for (Class<?> clazz = srcClass; !Object.class.equals(clazz) && clazz != null; clazz = clazz.getSuperclass()) {
            // traverse from srcClass up to Object to search for the properties of srcClass
            for (final Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) ||
                        ( conf.isAllowGetTransient() && Modifier.isTransient(field.getModifiers()) )) {
                    continue;
                }
                try {
                    final FieldGetter<S> getter = new FieldGetter<>(srcClass, field, conf.getterConfiguration);
                    final FieldSetter<D> setter = getCorrespondingSetter(field.getName(),
                            ObjectType.getObjectType(getter.getReturnType()), destClass, conf);
                    if (null != setter) {
                        map.put(getter, setter);
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("from: " + srcClass.getName() + ", to: " + destClass.getName() +
                                    ", field: " + field.getName() + ", fromDeclaringClass: " + clazz.getName());
                        }
                    }
                } catch (NoSuchMethodException ex) {
                    logger.finer(ex.getMessage());
                }
            }
        }
    }

    @Override
    public D convert(final S src) {
        if (null == src) {
            return null;
        }
        try {
            final D dest = this.constructor.newInstance();
            for (final Map.Entry<FieldGetter<S>, FieldSetter<D>> entry : map.entrySet()) {
                final Object value = entry.getKey().get(src);
                entry.getValue().set(dest, value);
            }
            return dest;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Fail to convert an instance of \"" + src.getClass().getName() +
                    "\" to an instance of \"" + this.constructor.getDeclaringClass().getName() + '\"', ex);
        }
    }

    private static <D> FieldSetter<D> getCorrespondingSetter(final String fieldName,
            final Class<?> srcFieldType, final Class<D> destClass,
            final Configuration configuration) throws NoSuchMethodException {
        for (Class<?> clazz = destClass; !Object.class.equals(clazz) && clazz != null; clazz = clazz.getSuperclass()) {
            // traverse from destClass up to Object to search for the property of destClass that
            // 1. is not static
            // 2. is not transient (if configure to not allow transient (default))
            // 3. has a name that equals to fieldName
            // 4. can be assigned from srcFieldType
            for (final Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) &&
                        (configuration.isAllowSetTransient() || !Modifier.isTransient(field.getModifiers())) &&
                        field.getName().equals(fieldName) &&
                        ObjectType.getObjectType(field.getType()).isAssignableFrom(srcFieldType)) {
                    final FieldSetter<D> setter = new FieldSetter<>(destClass, field, configuration.setterConfiguration);
                    final Class<?> targetObjectType = ObjectType.getObjectType(setter.getParameterType());
                    if (targetObjectType.isAssignableFrom(srcFieldType)) {
                        return setter;
                    }
                }
            }
        }
        return null;
    }

    public static final class Configuration implements Serializable {

        private FieldGetter.Configuration getterConfiguration = new FieldGetter.Configuration();
        private FieldSetter.Configuration setterConfiguration = new FieldSetter.Configuration();
        private boolean allowGetTransient;
        private boolean allowSetTransient;

        public FieldGetter.Configuration getGetterConfiguration() {
            return getterConfiguration;
        }

        public void setGetterConfiguration(final FieldGetter.Configuration getterConfiguration) {
            if (null == getterConfiguration) {
                throw new IllegalArgumentException("getterConfiguration can not be null");
            }
            this.getterConfiguration = getterConfiguration;
        }

        public FieldSetter.Configuration getSetterConfiguration() {
            return setterConfiguration;
        }

        public void setSetterConfiguration(FieldSetter.Configuration setterConfiguration) {
            if (null == setterConfiguration) {
                throw new IllegalArgumentException("setterConfiguration can not be null");
            }
            this.setterConfiguration = setterConfiguration;
        }

        public boolean isAllowGetTransient() {
            return allowGetTransient;
        }

        public void setAllowGetTransient(boolean allowGetTransient) {
            this.allowGetTransient = allowGetTransient;
        }

        public boolean isAllowSetTransient() {
            return allowSetTransient;
        }

        public void setAllowSetTransient(boolean allowSetTransient) {
            this.allowSetTransient = allowSetTransient;
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
            return allowGetTransient == that.allowGetTransient &&
                    allowSetTransient == that.allowSetTransient &&
                    Objects.equals(getterConfiguration, that.getterConfiguration) &&
                    Objects.equals(setterConfiguration, that.setterConfiguration);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getterConfiguration, setterConfiguration, allowGetTransient, allowSetTransient);
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "getterConfiguration=" + getterConfiguration +
                    ", setterConfiguration=" + setterConfiguration +
                    ", allowGetTransient=" + allowGetTransient +
                    ", allowSetTransient=" + allowSetTransient +
                    '}';
        }
    }
}
