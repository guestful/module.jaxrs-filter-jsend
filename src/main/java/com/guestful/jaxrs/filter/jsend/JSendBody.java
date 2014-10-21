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

/**
 * date 2014-05-23
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class JSendBody {

    private JSendMeta meta = new JSendMeta();
    private JSendError error;
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public JSendError getError() {
        return error;
    }

    public void setError(JSendError error) {
        this.error = error;
    }

    public JSendMeta getMeta() {
        return meta;
    }

    public void setMeta(JSendMeta meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "{" +
            "meta=" + meta +
            ", data=" + data +
            ", error=" + error +
            '}';
    }
}
