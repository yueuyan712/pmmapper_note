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
package org.onap.dcaegen2.services.pmmapper.model;

import lombok.Data;
import org.onap.dcaegen2.services.pmmapper.utils.GSONRequired;

/**
 * Metadata for inbound event onto data router subscriber.
 */
@Data
public class EventMetadata { // 事件元数据
    @GSONRequired
    private String productName;
    @GSONRequired
    private String vendorName;
    @GSONRequired
    private String startEpochMicrosec; // 开始时期微秒
    @GSONRequired
    private String lastEpochMicrosec; // 结束实其微秒，是一个时间戳
    @GSONRequired
    private String sourceName;
    @GSONRequired
    private String timeZoneOffset; // 时域偏移，格林威治时间和本地时间之间的时差？猜测
    @GSONRequired
    private String location; // 地理位置
    @GSONRequired
    private String compression; // 压缩
    @GSONRequired
    private String fileFormatType; // 文件格式类型
    @GSONRequired
    private String fileFormatVersion; // 文件格式版本
}
