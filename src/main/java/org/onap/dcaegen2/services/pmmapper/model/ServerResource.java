/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.onap.dcaegen2.services.pmmapper.model;

import static io.undertow.util.Methods.GET_STRING;

import io.undertow.server.HttpHandler;

public abstract class ServerResource implements HttpHandler { // HttpHandler是包里的
    protected String httpMethod;
    protected String endpointTemplate;

    /**
     * Creates a new server resource with a custom method and endpoint.使用自定义方法和端点创建新的服务器资源。
     * @param httpMethod Method that the resource should be accessible by. 资源通过该方法被访问
     * @param endpointTemplate Endpoint that the resource should be accessible by. 资源通过该端点被访问
     * 该类就是服务资源类，包含的属性有访问方法和访问端点，可再创建时自定义，也可使用库里默认的。
     */
    public ServerResource(String httpMethod, String endpointTemplate) {
        this.httpMethod = httpMethod;
        this.endpointTemplate = endpointTemplate;
    }

    /**
     * Creates a new server resource with a custom endpoint and method 'get'.
     * @param endpointTemplate Endpoint that the resource should be accessible by.
     */
    public ServerResource(String endpointTemplate) {
        this.httpMethod = GET_STRING;// 默认的
        this.endpointTemplate = endpointTemplate;
    }

    public String getHTTPMethod() {
        return this.httpMethod;
    }

    public String getEndpointTemplate() {
        return this.endpointTemplate;
    }

     public HttpHandler getHandler() {
        return this;
    }
}
