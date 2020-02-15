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

public final class ObjectType {

    private ObjectType() {}

    /**
     *
     * @param clazz a type
     * @return the boxed object type if clazz is a primitive type,
     *          or else clazz
     */
    public static Class<?> getObjectType(final Class<?> clazz) {
        if (int.class.equals(clazz)) {
            return Integer.class;
        } else if (boolean.class.equals(clazz)) {
            return Boolean.class;
        } else if (long.class.equals(clazz)) {
            return Long.class;
        } else if (double.class.equals(clazz)) {
            return Double.class;
        } else if (float.class.equals(clazz)) {
            return Float.class;
        } else if (short.class.equals(clazz)) {
            return Short.class;
        } else if (byte.class.equals(clazz)) {
            return Byte.class;
        } else if (char.class.equals(clazz)) {
            return Character.class;
        } else {
            return clazz;
        }
    }

    public static boolean isObjectTypeAssignableFrom(final Class<?> base, final Class<?> derived) {
        return getObjectType(base).isAssignableFrom(getObjectType(derived));
    }
}
