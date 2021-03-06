/*
 * Copyright 2012-2014, Continuuity, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.continuuity.loom.codec.json;

import com.continuuity.loom.admin.Administration;
import com.continuuity.loom.admin.LeaseDuration;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;

/**
 * Codec for serializing/deserializing a {@link com.continuuity.loom.admin.Administration}.
 */
public class AdministrationCodec extends AbstractCodec<Administration> {
  @Override
  public Administration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException {
    JsonObject jsonObj = json.getAsJsonObject();

    LeaseDuration leaseDuration = context.deserialize(jsonObj.get("leaseduration"), LeaseDuration.class);

    return new Administration(leaseDuration);
  }

  @Override
  public JsonElement serialize(Administration administration, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObj = new JsonObject();

    jsonObj.add("leaseduration", context.serialize(administration.getLeaseDuration()));

    return jsonObj;
  }
}
