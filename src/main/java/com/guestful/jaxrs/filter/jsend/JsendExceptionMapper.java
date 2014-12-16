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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class JsendExceptionMapper implements ExceptionMapper<Throwable> {

    private final Provider<ContainerRequestContext> requestProvider;

    @Inject
    public JsendExceptionMapper(Provider<ContainerRequestContext> requestProvider) {
        this.requestProvider = requestProvider;
    }

    @Override
    public Response toResponse(final Throwable e) {
        final Level level;
        // having a request info means we were in a jsend filter => @Jsend is there
        final RequestInfo infos = (RequestInfo) requestProvider.get().getProperty(RequestInfo.class.getName());

        Response response = null;
        boolean logStackTrace = true;

        requestProvider.get().removeProperty(RequestInfo.class.getName());

        if (e instanceof WebApplicationException) {
            response = ((WebApplicationException) e).getResponse();
        } else if (e instanceof JSendBadRequestException) {
            response = ((JSendBadRequestException) e).getResponse();
        }

        if (response != null) {
            level = Level.FINEST;
            Object ct = response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);
            MediaType mt = ct == null ? null : MediaType.valueOf(ct.toString());
            Throwable t = e.getCause() == null ? e : e.getCause();
            response = mt == null || MediaType.APPLICATION_JSON_TYPE.isCompatible(mt) ?
                JSendFeature.wrapResponse(requestProvider.get(), response, t, null) :
                response;
            logStackTrace = e.getCause() != null;

        } else {
            level = Level.SEVERE;
            response = JSendFeature.wrapResponse(requestProvider.get(), Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(), e, null);
        }

        Logger logger = Logger.getLogger(e.getClass().getName());
        if (logger.isLoggable(level)) {
            if (infos != null) {
                if (logStackTrace) {
                    logger.log(level, e.getMessage() + "\n" + infos.describe() + "\n< " + response.getEntity(), e);
                } else {
                    logger.log(level, e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage()) + " : " + infos.describe() + "\n< " + response.getEntity());
                }
            } else {
                if (logStackTrace) {
                    logger.log(level, e.getMessage() + "\n< " + response.getEntity(), e);
                } else {
                    logger.log(level, e.getMessage() + "\n< " + response.getEntity());
                }
            }
        }

        return response;
    }
}
