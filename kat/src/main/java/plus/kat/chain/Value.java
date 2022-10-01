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
package plus.kat.chain;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import plus.kat.crash.*;
import plus.kat.kernel.*;
import plus.kat.stream.*;
import plus.kat.utils.*;

import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * @author kraity
 * @since 0.0.1
 */
public class Value extends Dram {
    /**
     * default
     */
    public Value() {
        super();
    }

    /**
     * @param size the initial capacity
     */
    public Value(
        int size
    ) {
        super(size);
    }

    /**
     * @param data the initial byte array
     */
    public Value(
        @NotNull byte[] data
    ) {
        super(data);
    }

    /**
     * @param data specify the {@link Chain} to be mirrored
     */
    public Value(
        @NotNull Chain data
    ) {
        super(data);
    }

    /**
     * @param bucket the specified {@link Bucket} to be used
     */
    public Value(
        @Nullable Bucket bucket
    ) {
        super(bucket);
    }

    /**
     * @param sequence specify the {@link CharSequence} to be mirrored
     */
    public Value(
        @Nullable CharSequence sequence
    ) {
        super(sequence);
    }

    /**
     * Returns the modifier type
     *
     * @since 0.0.4
     */
    @Nullable
    public Type getType() {
        return type;
    }

    /**
     * Sets the modifier type of {@link Value}
     *
     * @param type the specified type
     * @since 0.0.4
     */
    public void setType(
        @Nullable Type type
    ) {
        this.type = type;
    }

    /**
     * Returns {@code true} if, and only if, internal {@code byte[]} can be shared
     *
     * @see Chain#getSource()
     * @since 0.0.2
     */
    @Override
    public boolean isShared() {
        return bucket == null;
    }

    /**
     * Returns a {@link Value} of this {@link Value}
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     */
    @NotNull
    @Override
    public Value subSequence(
        int start, int end
    ) {
        return new Value(
            toBytes(start, end)
        );
    }

    /**
     * Sets the value of the specified location.
     * Only if the index is within the internal value range
     *
     * <pre>{@code
     *   Value value = ...
     *   value.set(0, (byte) 'k');
     * }</pre>
     *
     * @param i the specified index
     * @param b the specified value
     * @throws ArrayIndexOutOfBoundsException if the index argument is negative
     */
    public void set(
        int i, byte b
    ) {
        byte[] it = value;
        if (i < it.length) {
            hash = 0;
            it[i] = b;
        }
    }

    /**
     * Appends the byte value to this {@link Value}
     *
     * <pre>{@code
     *   Value value = ...
     *   value.add((byte) 'k');
     * }</pre>
     *
     * @param b the specified byte value
     */
    public void add(
        byte b
    ) {
        chain(b);
    }

    /**
     * Appends the char value to this {@link Value}
     *
     * <pre>{@code
     *   Value value = ...
     *   value.add('k');
     * }</pre>
     *
     * @param c the specified char value
     */
    public void add(
        char c
    ) {
        chain(c);
    }

    /**
     * Appends the number to this {@link Value}
     *
     * <pre>{@code
     *   Value value = ...
     *   value.add(1024);
     * }</pre>
     *
     * @param num the specified int value
     * @since 0.0.4
     */
    public void add(
        int num
    ) {
        chain(num);
    }

    /**
     * Appends the number to this {@link Value}
     *
     * <pre>{@code
     *   Value value = ...
     *   value.add(1024L);
     * }</pre>
     *
     * @param num the specified long value
     * @since 0.0.4
     */
    public void add(
        long num
    ) {
        chain(num);
    }

    /**
     * Appends the byte array to this {@link Value}
     *
     * @param b the specified byte array
     */
    public void add(
        byte[] b
    ) {
        if (b != null) {
            chain(
                b, 0, b.length
            );
        }
    }

    /**
     * Appends the byte array to this {@link Value}
     *
     * @param b the specified byte array
     * @param i the specified index
     * @param l the specified length
     * @throws ArrayIndexOutOfBoundsException If the {@code index} or {@code length} ou of range
     * @since 0.0.4
     */
    public void add(
        byte[] b, int i, int l
    ) {
        if (b != null) {
            if (i >= 0 && i + l <= b.length) {
                chain(
                    b, i, l
                );
            } else {
                throw new ArrayIndexOutOfBoundsException(
                    "Out of bounds, i:" + i + " l:" + l + " length:" + b.length
                );
            }
        }
    }

    /**
     * Appends the char array to this {@link Value}
     *
     * @param c the specified char array
     */
    public void add(
        char[] c
    ) {
        if (c != null) {
            chain(
                c, 0, c.length
            );
        }
    }

    /**
     * Appends the char array to this {@link Value}
     *
     * @param c the specified byte array
     * @param i the specified index
     * @param l the specified length
     * @throws ArrayIndexOutOfBoundsException If the {@code index} or {@code length} ou of range
     * @since 0.0.4
     */
    public void add(
        char[] c, int i, int l
    ) {
        if (c != null) {
            if (i >= 0 && i + l <= c.length) {
                chain(
                    c, i, l
                );
            } else {
                throw new ArrayIndexOutOfBoundsException(
                    "Out of bounds, i:" + i + " l:" + l + " length:" + c.length
                );
            }
        }
    }

    /**
     * Appends the {@link InputStream} to this {@link Value}
     *
     * @param in the specified {@link InputStream}
     * @since 0.0.3
     */
    public void add(
        InputStream in
    ) {
        if (in != null) {
            chain(in);
        }
    }

    /**
     * Appends the {@link CharSequence} to this {@link Value}
     *
     * @param c the specified char array
     */
    public void add(
        CharSequence c
    ) {
        if (c != null) {
            chain(
                c, 0, c.length()
            );
        }
    }

    /**
     * Appends the {@link CharSequence} to this {@link Value}
     *
     * @param c the specified byte array
     * @param i the specified index
     * @param l the specified length
     * @throws ArrayIndexOutOfBoundsException If the {@code index} or {@code length} ou of range
     * @since 0.0.4
     */
    public void add(
        CharSequence c, int i, int l
    ) {
        if (c != null) {
            if (i >= 0 && i + l <= c.length()) {
                chain(
                    c, i, l
                );
            } else {
                throw new ArrayIndexOutOfBoundsException(
                    "Out of bounds, i:" + i + " l:" + l + " length:" + c.length()
                );
            }
        }
    }

    /**
     * Adds this data to uppercase hexadecimal
     *
     * <pre>{@code
     *   Value value = ...
     *   value.upper(new byte[]{1, 11, 111}); // 1B6F
     * }</pre>
     *
     * @param data specify the {@code byte[]} to be encoded
     * @see Value#lower(byte[])
     * @since 0.0.4
     */
    public void upper(
        byte[] data
    ) {
        if (data != null) {
            grow(count * data.length * 2);
            int i = 0;
            hash = 0;
            byte[] it = value;
            while (i < data.length) {
                int o = data[i++] & 0xFF;
                it[count++] = Binary.upper(o >> 4);
                it[count++] = Binary.upper(o & 0xF);
            }
        }
    }

    /**
     * Adds this data to lowercase hexadecimal
     *
     * <pre>{@code
     *   Value value = ...
     *   value.lower(new byte[]{1, 11, 111}); // 1b6f
     * }</pre>
     *
     * @param data specify the {@code byte[]} to be encoded
     * @see Value#upper(byte[])
     * @since 0.0.4
     */
    public void lower(
        byte[] data
    ) {
        if (data != null) {
            grow(count * data.length * 2);
            int i = 0;
            hash = 0;
            byte[] it = value;
            while (i < data.length) {
                int o = data[i++] & 0xFF;
                it[count++] = Binary.lower(o >> 4);
                it[count++] = Binary.lower(o & 0xF);
            }
        }
    }

    /**
     * @param length the specified length
     * @throws ArrayIndexOutOfBoundsException if the index argument is negative or out of range
     */
    public void slip(
        int length
    ) {
        if (length == 0) {
            hash = 0;
            count = 0;
        } else {
            if (length < 0 || length > value.length) {
                throw new ArrayIndexOutOfBoundsException();
            }
            hash = 0;
            count = length;
        }
    }

    /**
     * Creates and returns a copy of this {@link Value}
     *
     * @since 0.0.3
     */
    @NotNull
    public Value copy() {
        return new Value(this);
    }

    /**
     * @param b the {@code byte} to be compared
     */
    public static boolean esc(byte b) {
        switch (b) {
            case '^':
            case '(':
            case ')': {
                return true;
            }
        }
        return false;
    }

    /**
     * @see Value#Value(Bucket)
     */
    public static Value apply() {
        return new Value(
            Buffer.INS
        );
    }

    /**
     * @author kraity
     * @since 0.0.4
     */
    public static class Buffer implements Bucket {

        private static final int SIZE, LIMIT, SCALE;

        static {
            SIZE = Config.get(
                "kat.value.size", 8
            );
            LIMIT = Config.get(
                "kat.value.limit", 16
            );

            if (LIMIT < SIZE) {
                throw new Error(
                    "Bucket's size(" + SIZE + ") cannot be greater than the limit(" + LIMIT + ")"
                );
            }

            SCALE = Config.get(
                "kat.value.scale", 1024
            );
        }

        public static final Buffer
            INS = new Buffer();

        private final byte[][]
            bucket = new byte[SIZE][];

        @NotNull
        @Override
        public byte[] alloc(
            @NotNull byte[] it, int len, int min
        ) {
            byte[] data;
            int i = min / SCALE;

            if (i < SIZE) {
                synchronized (this) {
                    data = bucket[i];
                    bucket[i] = null;
                }
                if (data == null ||
                    data.length < min) {
                    data = new byte[(i + 1) * SCALE - 1];
                }
            } else {
                if (i < LIMIT) {
                    data = new byte[(i + 1) * SCALE - 1];
                } else {
                    throw new Collapse(
                        "Unexpectedly, Exceeding range '" + LIMIT * SCALE + "' in value"
                    );
                }
            }

            if (it.length != 0) {
                System.arraycopy(
                    it, 0, data, 0, len
                );

                int k = it.length / SCALE;
                if (k < SIZE) {
                    synchronized (this) {
                        bucket[k] = it;
                    }
                }
            }

            return data;
        }

        @Override
        public void push(
            @NotNull byte[] it
        ) {
            int i = it.length / SCALE;
            if (i < SIZE) {
                synchronized (this) {
                    bucket[i] = it;
                }
            }
        }

        @Nullable
        @Override
        public byte[] revert(
            @NotNull byte[] it
        ) {
            int i = it.length / SCALE;
            if (i == 0) {
                return it;
            }

            byte[] data;
            synchronized (this) {
                if (i < SIZE) {
                    bucket[i] = it;
                }
                data = bucket[i];
                bucket[i] = null;
            }
            return data;
        }
    }
}
