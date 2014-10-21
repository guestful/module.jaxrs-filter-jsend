/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.jaxrs.filter.jsend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class PeekInputStream extends InputStream {

    private static final int MAX_SIZE = 8 * 1024;

    private final InputStream delegate;
    private final ByteArrayOutputStream _read = new ByteArrayOutputStream();

    PeekInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    byte[] getReadContentAsBytes() {
        return _read.toByteArray();
    }

    final String getReadContentAsString(Charset charset) {
        return new String(getReadContentAsBytes(), charset);
    }

    private void collect(int offset, int len, byte... buff) {
        int s = _read.size();
        if (len > 0 && s < MAX_SIZE) {
            len = Math.min(len, MAX_SIZE - s);
            _read.write(buff, offset, len);
            if (s + len >= MAX_SIZE) {
                byte[] b = "...(more)".getBytes(StandardCharsets.UTF_8);
                _read.write(b, 0, b.length);
            }
        }
    }

    @Override
    public int read() throws IOException {
        int read = delegate.read();
        if (read != -1) collect(0, 1, (byte) read);
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int c = delegate.read(b);
        if (c > 0) collect(0, c, b);
        return c;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int c = delegate.read(b, off, len);
        if (c > 0) collect(off, c, b);
        return c;
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
