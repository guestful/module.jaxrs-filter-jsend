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

import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status.Family;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class JSendFeature implements DynamicFeature, Feature {

    private static final Map<Integer, String> ERROR_TYPES = new TreeMap<>();
    private static final MediaType JSON_UTF_8 = MediaType.valueOf("application/json; charset=utf-8");
    static final String WRAPPED = JSendFeature.class.getName() + ".WRAPPED";

    static {
        ERROR_TYPES.put(Response.Status.BAD_REQUEST.getStatusCode(), "request");
        ERROR_TYPES.put(Response.Status.UNAUTHORIZED.getStatusCode(), "authc");
        ERROR_TYPES.put(Response.Status.FORBIDDEN.getStatusCode(), "authz");
        ERROR_TYPES.put(Response.Status.NOT_FOUND.getStatusCode(), "notfound");
        ERROR_TYPES.put(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), "method");
        ERROR_TYPES.put(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "server");
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(JsendExceptionMapper.class);
        return true;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        Jsend jsend = resourceInfo.getResourceMethod().getAnnotation(Jsend.class);
        if (jsend == null) {
            jsend = resourceInfo.getResourceClass().getAnnotation(Jsend.class);
        }
        if (jsend != null) {
            Produces produces = resourceInfo.getResourceMethod().getAnnotation(Produces.class);
            if (produces == null) {
                produces = resourceInfo.getResourceClass().getAnnotation(Produces.class);
            }
            if (produces == null || produces.value().length == 0) {
                context.register(JsendFilter.class);
            } else {
                Arrays.asList(produces.value())
                    .stream()
                    .filter(ct -> MediaType.APPLICATION_JSON_TYPE.isCompatible(MediaType.valueOf(ct)))
                    .findFirst()
                    .ifPresent(s -> context.register(JsendFilter.class));
            }
        }
    }

    static Response wrapResponse(ContainerRequestContext request, Response response, Throwable e) {
        JSendBody entity = new JSendBody();
        entity.getMeta().setStatus(response.getStatus());
        Family family = Response.Status.Family.familyOf(response.getStatus());
        boolean error = family == Family.CLIENT_ERROR || family == Family.SERVER_ERROR;
        Object data = response.getEntity();
        if (error || e != null) {
            entity.setError(new JSendError());
            entity.getError().setType(ERROR_TYPES.getOrDefault(response.getStatus(), "other"));
            if (e != null) {
                entity.getError().setMessage(e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "<no description>" : e.getMessage()));
            }
            if (data != null) {
                entity.getError().setData(data);
            }
        } else if (response.getStatus() == Response.Status.OK.getStatusCode() || data != null) {
            entity.setData(data);
        }
        Object ct = response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);
        MediaType type = ct == null ? null : MediaType.valueOf(ct.toString());
        request.setProperty(WRAPPED, true);
        return Response.fromResponse(response)
            .status(response.getStatus() == Response.Status.NO_CONTENT.getStatusCode() ? Response.Status.OK.getStatusCode() : response.getStatus())
            .type(type != null && MediaType.APPLICATION_JSON_TYPE.isCompatible(type) ? type : JSON_UTF_8)
            .entity(entity)
            .build();
    }

    static boolean isWrapped(ContainerRequestContext request) {
        return request.getProperty(WRAPPED) != null;
    }
}
