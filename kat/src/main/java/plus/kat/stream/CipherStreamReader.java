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
package plus.kat.stream;

import plus.kat.anno.NotNull;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;

import static plus.kat.stream.Stream.Buffer.INS;

/**
 * @author kraity
 * @since 0.0.1
 */
public class CipherStreamReader extends AbstractReader {

    private int in, mark;
    private byte[] buffer;

    private boolean done;
    private Cipher cipher;
    private InputStream value;

    public CipherStreamReader(
        @NotNull InputStream data,
        @NotNull Cipher cipher
    ) {
        if (data == null ||
            cipher == null) {
            throw new NullPointerException();
        }

        this.value = data;
        this.cipher = cipher;
    }

    @Override
    protected int load()
        throws IOException {
        byte[] buf = buffer;
        if (buf == null) {
            buffer = buf = INS.alloc(range);
        }

        int s = scale(
            buf.length
        );

        int i = in, m = mark;
        if (m == 0) {
            i = value.read(buf);
            if (i <= s) {
                s = i;
            } else {
                in = i;
                mark = s;
            }
        } else {
            int n = i - m;
            if (n <= s) {
                s = n;
                in = 0;
                mark = 0;
            } else {
                mark += s;
            }
        }

        if (i > 0) {
            cache = cipher.update(
                buf, m, s
            );
            if (cache == null ||
                cache.length == 0) {
                return load();
            }
        } else {
            try {
                if (done) {
                    return -1;
                }
                done = true;
                cache = cipher.doFinal();
                if (cache == null) {
                    return -1;
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        return cache.length;
    }

    @Override
    public void close() {
        try {
            INS.join(
                buffer
            );
            value.close();
            if (!done) {
                cipher.doFinal();
            }
        } catch (Exception e) {
            // Nothing
        } finally {
            limit = -1;
            cache = null;
            value = null;
            cipher = null;
            buffer = null;
        }
    }
}
