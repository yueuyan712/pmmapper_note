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


import org.onap.dcaegen2.services.pmmapper.model.Event;

/**
 * Sink for Events received from the data router subscriber.接收从数据路由器订户收到的事件
 */
@FunctionalInterface // 函数式接口，在该接口内只能有一个抽象方法
public interface EventReceiver { // 接口，继承该接口的可以接收从DR订户收倒的事件，细化receive方法
    void receive(Event event);
}
