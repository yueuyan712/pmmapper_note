/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.pmmapper.datarouter;

import static io.undertow.util.Methods.PUT_STRING;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import io.undertow.util.HeaderValues;
import lombok.Data;
import lombok.NonNull;

import org.onap.dcaegen2.services.pmmapper.exceptions.NoMetadataException;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import org.onap.dcaegen2.services.pmmapper.model.ServerResource;
import org.onap.dcaegen2.services.pmmapper.utils.HttpServerExchangeAdapter;
import org.onap.dcaegen2.services.pmmapper.utils.RequiredFieldDeserializer;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Optional;

/**
 * Provides an undertow HttpHandler to be used as an endpoint for data router to send events to.
 */
@Data
public class DeliveryHandler extends ServerResource {

    public static final String METADATA_HEADER = "X-DMAAP-DR-META"; // 一些常量  元数据头
    public static final String PUB_ID_HEADER = "X-DMAAP-DR-PUBLISH-ID";// 发布id头

    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(DeliveryHandler.class));// 日志

    private static final String BAD_METADATA_MESSAGE = "Malformed Metadata."; // 元数据错误信息
    private static final String NO_METADATA_MESSAGE = "Missing Metadata."; // 无元数据信息
    private static final String HTTP_METHOD = PUT_STRING; // http方法
    private static final String DELIVERY_ENDPOINT = "/delivery/{filename}"; //发布端点

    private Gson metadataBuilder;

    @NonNull
    private EventReceiver eventReceiver; // 同一个文件夹下，不需要import  接口对象可以指向他的实现类对象

    /**
     * @param eventReceiver receiver for any inbound events.
     */
    public DeliveryHandler(EventReceiver eventReceiver) {
        super(HTTP_METHOD, DELIVERY_ENDPOINT); // 调用父类serverresource的构造方法，里面是传参
        this.eventReceiver = eventReceiver;
        this.metadataBuilder = new GsonBuilder() // builder设计模式
                .registerTypeAdapter(EventMetadata.class, new RequiredFieldDeserializer<EventMetadata>()) // 需要自定义 Gson 的序列化类型转换 类名.class指类的class对象。
                .create(); //TypeAdapter的使用需要注册到 Gson 的实例上。new GsonBuilder().registerTypeAdapter(类型,TypeAdapter实例).create();
    } //这里只是注册到gson，没有使用

    private EventMetadata getMetadata(HttpServerExchange httpServerExchange) throws NoMetadataException {
        String metadata = Optional.ofNullable(httpServerExchange.getRequestHeaders()
                .get(METADATA_HEADER))
                .map((HeaderValues headerValues) -> headerValues.get(0))
                .orElseThrow(() -> new NoMetadataException("Metadata Not found"));
        return metadataBuilder.fromJson(metadata, EventMetadata.class);
    }

    /**
     * Receives inbound requests, verifies that required headers are valid
     * and passes an Event onto the eventReceiver.
     * The forwarded httpServerExchange response is the responsibility of the eventReceiver.
     *
     * @param httpServerExchange inbound http server exchange.
     */
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) {
        try {
            logger.entering(new HttpServerExchangeAdapter(httpServerExchange));
            try {
                Map<String,String> mdc = MDC.getCopyOfContextMap();
                EventMetadata metadata = getMetadata(httpServerExchange);
                String publishIdentity = httpServerExchange.getRequestHeaders().get(PUB_ID_HEADER).getFirst();
                httpServerExchange.getRequestReceiver()
                        .receiveFullString((callbackExchange, body) ->
                                httpServerExchange.dispatch(() ->
                                        eventReceiver.receive(new Event(
                                                callbackExchange, body, metadata, mdc, publishIdentity)))
                        );
            } catch (NoMetadataException exception) {
                logger.unwrap().info("Bad Request: no metadata found under '{}' header.", METADATA_HEADER, exception);
                httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST)
                        .getResponseSender()
                        .send(NO_METADATA_MESSAGE);
            } catch (JsonParseException exception) {
                logger.unwrap().info("Bad Request: Failure to parse metadata", exception);
                httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST)
                        .getResponseSender()
                        .send(BAD_METADATA_MESSAGE);
            }
        } finally {
            logger.exiting();
        }
    }
}
