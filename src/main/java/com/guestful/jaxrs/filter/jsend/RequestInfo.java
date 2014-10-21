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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

/**
 * date 2014-05-23
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class RequestInfo {

    private final String method;
    private final String uri;
    private final MultivaluedMap<String, String> headers;
    private final Principal principal;
    private final PeekInputStream stream;
    private byte[] buffer = new byte[8 * 1024];

    RequestInfo(ContainerRequestContext request) throws IOException {
        this.principal = request.getSecurityContext().getUserPrincipal();
        this.method = request.getMethod();
        this.uri = request.getUriInfo().getRequestUri().toString();
        this.headers = request.getHeaders();
        if (request.getMediaType() == null || MediaType.APPLICATION_JSON_TYPE.isCompatible(request.getMediaType())) {
            int clen;
            try {
                clen = Integer.parseInt(request.getHeaderString(HttpHeaders.CONTENT_LENGTH));
            } catch (Exception ignored) {
                clen = -1;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(clen <= 0 ? 256 : clen);
            try (InputStream is = request.getEntityStream()) {
                while ((clen = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, clen);
                }
            }
            buffer = baos.toByteArray();
            this.stream = new PeekInputStream(new ByteArrayInputStream(buffer));
            request.setEntityStream(this.stream);
        } else {
            stream = null;
            buffer = null;
        }
    }

    public String describe() {
        String s = "> " + method + " " + uri +
            "\n> User: " + principal +
            "\n> " + String.join("\n> ", (Iterable<String>) headers.entrySet().stream().map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))::iterator);
        if (stream != null && buffer != null) {
            String data = stream.getReadContentAsString(StandardCharsets.UTF_8);
            s += "\n> RECEIVED: " + buffer.length + "c: " + new String(buffer, StandardCharsets.UTF_8) +
                "\n> READ: " + data.length() + "c: " + data;
        }
        return s;
    }

}
