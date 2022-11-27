/*
 * Copyright 2022 Kat+ Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package plus.kat.spare;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import plus.kat.*;
import plus.kat.chain.*;
import plus.kat.stream.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * @author kraity
 * @since 0.0.1
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ArraySpare extends Property<Object> {

    public static final ArraySpare
        INSTANCE = new ArraySpare(Object[].class);

    public static final Object[]
        EMPTY_ARRAY = new Object[0];

    private Object EMPTY;
    protected final Class<?> elem;

    public ArraySpare(
        @NotNull Class<?> clazz
    ) {
        super((Class<Object>) clazz);
        elem = clazz.getComponentType();
        if (elem == null) {
            throw new IllegalStateException(
                "Specified `" + clazz + "` is not an array type"
            );
        }
    }

    @Override
    public Object apply() {
        Object array = EMPTY;
        if (array != null) {
            return array;
        }

        Class<?> e = elem;
        if (e == Object.class) {
            return EMPTY = EMPTY_ARRAY;
        } else if (e == byte[].class) {
            return EMPTY = Chain.EMPTY_BYTES;
        } else if (e == char[].class) {
            return EMPTY = Chain.EMPTY_CHARS;
        } else {
            return EMPTY = Array.newInstance(e, 0);
        }
    }

    @Override
    public String getSpace() {
        return "A";
    }

    @Override
    public Boolean getFlag() {
        return Boolean.FALSE;
    }

    @Override
    public Object read(
        @NotNull Flag flag,
        @NotNull Value value
    ) throws IOException {
        if (flag.isFlag(Flag.VALUE_AS_BEAN)) {
            Algo algo = Algo.of(value);
            if (algo == null) {
                return null;
            }
            return solve(
                algo, new Event<>(value).with(flag)
            );
        }
        return null;
    }

    @Override
    public void write(
        @NotNull Chan chan,
        @NotNull Object value
    ) throws IOException {
        if (elem == Object.class) {
            for (Object val : (Object[]) value) {
                chan.set(null, val);
            }
        } else {
            int l = Array.getLength(value);
            for (int i = 0; i < l; i++) {
                chan.set(
                    null, Array.get(value, i)
                );
            }
        }
    }

    @Override
    public Object apply(
        @NotNull Spoiler spoiler,
        @NotNull Supplier supplier
    ) {
        if (!spoiler.hasNext()) {
            return apply();
        }

        Class<?> e = elem;
        if (e == Object.class) {
            Object[] data = new Object[]{
                spoiler.getValue()
            };
            while (spoiler.hasNext()) {
                Object[] copy = new Object[data.length + 1];
                System.arraycopy(
                    data, 0, copy, 0, data.length
                );
                copy[data.length] = spoiler.getValue();
                data = copy;
            }
            return data;
        } else {
            int size = 0;
            Object data = null;
            Spare<?> spare = null;

            do {
                Object copy;
                if (size == 0) {
                    copy = Array.newInstance(e, 1);
                } else {
                    copy = Array.newInstance(e, size + 1);
                    //noinspection SuspiciousSystemArraycopy
                    System.arraycopy(
                        data, 0, copy, 0, size
                    );
                }

                Object val = spoiler.getValue();
                if (!e.isInstance(val)) {
                    if (spare == null) {
                        spare = supplier.lookup(e);
                    }
                    val = spare.cast(val, supplier);
                }
                Array.set(copy, size++, val);
                data = copy;
            } while (
                spoiler.hasNext()
            );
            return data;
        }
    }

    @Override
    public Object apply(
        @NotNull Supplier supplier,
        @NotNull ResultSet resultSet
    ) throws SQLException {
        ResultSetMetaData meta =
            resultSet.getMetaData();

        int size = meta.getColumnCount();
        if (size == 0) {
            return apply();
        }

        Class<?> e = elem;
        if (e == Object.class) {
            Object[] data = new Object[size];
            for (int i = 0; i < size; ) {
                data[i++] = resultSet.getObject(i);
            }
            return data;
        } else {
            Spare<?> spare = null;
            Object data = Array.newInstance(e, size);

            for (int i = 0; i < size; ) {
                int k = i++;
                Object val = resultSet.getObject(i);
                if (!e.isInstance(val)) {
                    if (spare == null) {
                        spare = supplier.lookup(e);
                    }
                    val = spare.cast(val, supplier);
                }
                Array.set(data, k, val);
            }

            return data;
        }
    }

    @Override
    public Object cast(
        @Nullable Object object,
        @NotNull Supplier supplier
    ) {
        if (object == null) {
            return null;
        }

        if (klass.isInstance(object)) {
            return object;
        }

        if (object.getClass().isArray()) {
            int size = Array.getLength(object);
            if (size == 0) {
                return apply();
            }

            Class<?> e = elem;
            if (e == Object.class) {
                Object[] array = new Object[size];
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(
                    object, 0, array, 0, size
                );
                return array;
            }

            Spare<?> spare = null;
            Object array = Array.newInstance(e, size);

            for (int i = 0; i < size; i++) {
                Object val = Array.get(object, i);
                if (!e.isInstance(val)) {
                    if (spare == null) {
                        spare = supplier.lookup(e);
                    }
                    val = spare.cast(val, supplier);
                }
                Array.set(array, i, val);
            }

            return array;
        }

        if (object instanceof Collection) {
            Collection col = (Collection) object;
            int size = col.size();
            if (size == 0) {
                return apply();
            }

            Class<?> e = elem;
            if (e == Object.class) {
                return col.toArray();
            }

            int i = 0;
            Spare<?> spare = null;

            Object array = Array.newInstance(e, size);
            for (Object val : col) {
                if (!e.isInstance(val)) {
                    if (spare == null) {
                        spare = supplier.lookup(e);
                    }
                    val = spare.cast(val, supplier);
                }
                Array.set(array, i++, val);
            }

            return array;
        }

        if (object instanceof CharSequence) {
            CharSequence cs =
                (CharSequence) object;
            Algo algo = Algo.of(cs);
            if (algo == null) {
                return null;
            }
            return solve(
                algo, new Event<>(cs).with(supplier)
            );
        }

        if (object instanceof Map) {
            Map map = (Map) object;

            int size = map.size();
            if (size == 0) {
                return apply();
            }

            Class<?> e = elem;
            if (e == Object.class) {
                return map.values().toArray();
            }

            int i = 0;
            Spare<?> spare = null;

            Object array = Array.newInstance(e, size);
            for (Object val : map.values()) {
                if (!e.isInstance(val)) {
                    if (spare == null) {
                        spare = supplier.lookup(e);
                    }
                    val = spare.cast(val, supplier);
                }
                Array.set(array, i++, val);
            }

            return array;
        }

        if (object instanceof Spoiler) {
            return apply(
                (Spoiler) supplier, supplier
            );
        }

        if (object instanceof ResultSet) {
            try {
                return apply(
                    supplier, (ResultSet) object
                );
            } catch (SQLException e) {
                throw new IllegalStateException(
                    object + " cannot be converted to " + klass, e
                );
            }
        }

        Spoiler spoiler =
            supplier.flat(object);
        if (spoiler != null) {
            return apply(
                spoiler, supplier
            );
        } else {
            throw new IllegalStateException(
                object + " cannot be converted to " + klass
            );
        }
    }

    @Override
    public Builder<Object> getBuilder(
        @Nullable Type type
    ) {
        if (type == null) {
            Class<?> e = elem;
            if (e.isPrimitive()) {
                return new Builder0(e);
            } else {
                return new Builder1(e, e);
            }
        }

        if (type instanceof Class) {
            Class<?> k = (Class<?>) type;
            k = k.getComponentType();
            if (k == null) {
                k = elem;
            }
            if (k.isPrimitive()) {
                return new Builder0(k);
            } else {
                return new Builder1(k, k);
            }
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new Builder2(
                p.getActualTypeArguments()
            );
        }

        if (type instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType) type;
            Class<?> cls = Space.wipe(
                type = g.getGenericComponentType()
            );
            return new Builder1(
                type, cls != null ? cls : elem
            );
        }

        return null;
    }

    public static class Builder0 extends Builder<Object> {

        protected int size;
        protected int mark;

        protected int length;
        protected Object bundle;

        protected Class<?> elem;
        protected Spare<?> spare;

        public Builder0(
            Class<?> type
        ) {
            elem = type;
        }

        @Override
        public void onOpen() throws IOException {
            spare = supplier.lookup(elem);
            if (spare != null) {
                size = 0;
                mark = 1;
                bundle = Array.newInstance(
                    elem, length = 1
                );
            } else {
                throw new IOException(
                    "Can't lookup the Spare of '" + elem + "'"
                );
            }
        }

        @Override
        public void onEmit(
            @NotNull Space space,
            @NotNull Alias alias,
            @NotNull Value value
        ) throws IOException {
            if (length == size) {
                enlarge();
            }
            Array.set(
                bundle, size++, spare.read(flag, value)
            );
        }

        /**
         * capacity expansion
         */
        protected void enlarge() {
            int capacity;
            if (Integer.MAX_VALUE - mark > size) {
                // fibonacci
                capacity = mark + size;
            } else {
                if (Integer.MAX_VALUE - 8 < size) {
                    throw new OutOfMemoryError();
                }
                capacity = size + 8;
            }

            mark = size;
            Object make = Array.newInstance(
                elem, length = capacity
            );

            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(
                bundle, 0, make, 0, size
            );
            bundle = make;
        }

        @Override
        public Object build() {
            if (length == size) {
                return bundle;
            }

            Object data = Array
                .newInstance(elem, size);
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(
                bundle, 0, data, 0, size
            );
            return data;
        }

        @Override
        public void onClose() {
            bundle = null;
        }
    }

    public static class Builder1 extends Builder0 implements Callback {

        protected Type tag;
        protected Class<?> kind;

        public Builder1(
            Type type,
            Class<?> clazz
        ) {
            super(clazz);
            this.tag = type;
        }

        @Override
        public void onOpen() {
            Class<?> clazz = elem;
            if (clazz != Object.class) {
                kind = clazz;
                spare = supplier.lookup(clazz);
            }

            size = 0;
            mark = 1;
            bundle = Array.newInstance(
                clazz, length = 1
            );
        }

        @Override
        public Pipage onOpen(
            @NotNull Space space,
            @NotNull Alias alias
        ) throws IOException {
            Spare<?> spare0 = spare;
            if (spare0 == null) {
                spare0 = supplier.search(
                    kind, space
                );
                if (spare0 == null) {
                    return null;
                }
            }

            Builder<?> child =
                spare0.getBuilder(tag);

            if (child == null) {
                return null;
            }

            return child.init(this, this);
        }

        @Override
        public void onEmit(
            @NotNull Pipage pipage,
            @Nullable Object result
        ) throws IOException {
            if (length == size) {
                enlarge();
            }
            Array.set(
                bundle, size++, result
            );
        }

        @Override
        public void onEmit(
            @NotNull Space space,
            @NotNull Alias alias,
            @NotNull Value value
        ) throws IOException {
            Spare<?> spare0 = spare;
            if (spare0 == null) {
                spare0 = supplier.search(
                    kind, space
                );
                if (spare0 == null) {
                    return;
                }
            }

            if (length == size) {
                enlarge();
            }

            Array.set(
                bundle, size++, spare0.read(flag, value)
            );
        }

        @Override
        public void onClose() {
            tag = null;
            bundle = null;
        }
    }

    public static class Builder2 extends Builder<Object> implements Callback {

        protected int index;
        protected Type[] tag;
        protected Object[] bundle;

        public Builder2(
            Type[] types
        ) {
            tag = types;
        }

        @Override
        public void onOpen() {
            index = -1;
            bundle = new Object[tag.length];
        }

        @Override
        public Pipage onOpen(
            @NotNull Space space,
            @NotNull Alias alias
        ) throws IOException {
            Type[] types = tag;
            if (++index < types.length) {
                Type type = types[index];
                Class<?> clazz = Space.wipe(type);

                Spare<?> spare;
                if (clazz == Object.class) {
                    spare = supplier.lookup(space);
                } else {
                    spare = supplier.lookup(clazz, space);
                }

                if (spare == null) {
                    return null;
                }

                Builder<?> child =
                    spare.getBuilder(type);

                if (child == null) {
                    return null;
                }

                return child.init(this, this);
            } else {
                throw new IOException(
                    "The number of elements exceeds the range: " + types.length
                );
            }
        }

        @Override
        public void onEmit(
            @NotNull Pipage pipage,
            @Nullable Object result
        ) throws IOException {
            bundle[index] = result;
        }

        @Override
        public void onEmit(
            @NotNull Space space,
            @NotNull Alias alias,
            @NotNull Value value
        ) throws IOException {
            Type[] types = tag;
            if (++index < types.length) {
                Class<?> clazz = Space.wipe(
                    types[index]
                );

                Spare<?> spare;
                if (clazz == Object.class) {
                    spare = supplier.lookup(space);
                } else {
                    spare = supplier.lookup(clazz, space);
                }

                if (spare != null) {
                    bundle[index] =
                        spare.read(
                            flag, value
                        );
                }
            } else {
                throw new IOException(
                    "The number of elements exceeds the range: " + types.length
                );
            }
        }

        @Override
        public Object[] build() {
            return bundle;
        }

        @Override
        public void onClose() {
            bundle = null;
        }
    }
}
