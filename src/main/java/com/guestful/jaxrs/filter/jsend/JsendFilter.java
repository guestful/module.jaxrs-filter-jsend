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

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@Priority(Priorities.ENTITY_CODER)
public class JsendFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        request.setProperty(RequestInfo.class.getName(), new RequestInfo(request));
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if (!JSendFeature.isWrapped(request) && (response.getEntity() == null || MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getMediaType()))) {
            Response resp = JSendFeature.wrapResponse(request, Response.status(response.getStatus())
                .type(response.getMediaType())
                .entity(response.getEntity())
                .build(), null);
            response.setStatus(resp.getStatus());
            response.setEntity(resp.getEntity(), null, resp.getMediaType());
        }
        request.removeProperty(RequestInfo.class.getName());
    }

}
