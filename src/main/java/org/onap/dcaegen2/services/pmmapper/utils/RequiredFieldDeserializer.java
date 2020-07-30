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
package org.onap.dcaegen2.services.pmmapper.utils;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer; // gson反序列化
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import lombok.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Extension of the default deserializer with support for GSONRequired annotation.默认反序列化程序的扩展，支持GSONRequired注解
 * @param <T> Type of object for deserialization process. // 反序列化过程的对象类型
 */
public class RequiredFieldDeserializer<T> implements JsonDeserializer<T> { //反序列化，只处理T类数据，“<T>”是泛型的默认值,可以被任意类型所代替

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        T obj = new Gson().fromJson(jsonElement, type); // 从Json相关对象到Java实体的方法
        validateRequiredFields(obj.getClass().getDeclaredFields(), obj); // 深层检验一下该对象的类的fileds中每一项，有GSONRequired注解的那项，有没有值，没有值要抛出异常。如果该对象为list递归遍历，或fields中某项为对象，也要往下深层遍历
        return obj;
    }
// 传参就是对象的类的fileds及对象本身，作用是遍历检查该对象的类的filed有没有需要的字段却找不到的
    private void validateRequiredFields(@NonNull Field[] fields, @NonNull Object pojo) {
        if (pojo instanceof List) { // 如果pojo是list类型
            final List<?> pojoList = (List<?>) pojo; // 元素类型未知的List
            for (final Object pojoListPojo : pojoList) {
                validateRequiredFields(pojoListPojo.getClass().getDeclaredFields(), pojoListPojo);
            } // getClass返回的Class对象，getDeclaredFields()：获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段
            // 遍历pojoList，并返回其中对象的类的所有字段数组
        } //此处递归应该是为了深层遍历pojo执行该方法
//下面为真正操作
        Stream.of(fields) //Stream API 对集合数据进行操作，将fields转为流从而遍历操作
            .filter(field -> field.getAnnotation(GSONRequired.class) != null) //返回注解，看是每个filed对象否有GSONRequired注解，过滤掉没有的，GSONRequired应该是个自定义注解
            .forEach(field -> { // 遍历过滤完的filed对象们
                try {
                    field.setAccessible(true); // 将该对象的accessible设为true，允许通过反射访问
                    Object fieldObj = Optional.ofNullable(field.get(pojo)) // 创建Optional对象，创建Optional对象，允许为空，获得pojo对象中这个field的值，也许值还是个对象
                         .orElseThrow(()-> new JsonParseException( // 如果有值则将其返回，否则抛出Supplier接口创建的异常
                            String.format("Field '%s' in class '%s' is required but not found.",
                            field.getName(), pojo.getClass().getSimpleName()))); // getName？返回此Field对象表示的字段的名称  getSimpleName获取源代码中给出的“底层类”简称

                    Field[] declaredFields = fieldObj.getClass().getDeclaredFields(); // 往下层
                    validateRequiredFields(declaredFields, fieldObj);
                }
                catch (Exception exception) {
                    throw new JsonParseException("Failed to check fields.", exception);
                }
            });
    }

}
