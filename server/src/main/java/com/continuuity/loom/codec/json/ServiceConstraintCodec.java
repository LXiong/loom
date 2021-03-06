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

import com.continuuity.loom.admin.ServiceConstraint;
import com.continuuity.utils.ImmutablePair;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * Codec for serializing/deserializing a {@link ServiceConstraint}.
 */
public class ServiceConstraintCodec extends AbstractCodec<ServiceConstraint> {

  @Override
  public JsonElement serialize(ServiceConstraint serviceConstraint, Type type, JsonSerializationContext context) {
    JsonObject jsonObj = new JsonObject();

    jsonObj.add("hardwaretypes", context.serialize(serviceConstraint.getRequiredHardwareTypes()));
    jsonObj.add("imagetypes", context.serialize(serviceConstraint.getRequiredImageTypes()));
    JsonObject quantities = new JsonObject();
    quantities.add("min", context.serialize(serviceConstraint.getMinCount()));
    quantities.add("max", context.serialize(serviceConstraint.getMaxCount()));
    quantities.add("stepSize", context.serialize(serviceConstraint.getStepSize()));
    quantities.add("ratio", context.serialize(ratioToString(serviceConstraint.getRatio())));
    jsonObj.add("quantities", quantities);

    return jsonObj;
  }

  @Override
  public ServiceConstraint deserialize(JsonElement json, Type type, JsonDeserializationContext context)
    throws JsonParseException {
    JsonObject jsonObj = json.getAsJsonObject();

    JsonObject quantities = jsonObj.get("quantities").getAsJsonObject();
    Integer min = context.deserialize(quantities.get("min"), Integer.class);
    Integer max = context.deserialize(quantities.get("max"), Integer.class);
    Integer stepSize = context.deserialize(quantities.get("stepSize"), Integer.class);
    ImmutablePair<Integer, Integer> ratio =
      stringToRatio(context.<String>deserialize(quantities.get("ratio"), String.class));
    Set<String> requiredHardwareTypes = context.deserialize(jsonObj.get("hardwaretypes"),
                                                            new TypeToken<Set<String>>() {}.getType());
    Set<String> requiredImageTypes = context.deserialize(jsonObj.get("imagetypes"),
                                                         new TypeToken<Set<String>>() {}.getType());


    return new ServiceConstraint(requiredHardwareTypes, requiredImageTypes, min, max, stepSize, ratio);
  }

  private String ratioToString(ImmutablePair<Integer, Integer> ratio) {
    return (ratio == null) ? null : ratio.getFirst() + ":" + ratio.getSecond();
  }

  private ImmutablePair<Integer, Integer> stringToRatio(String ratioStr) {
    if (ratioStr == null) {
      return null;
    }
    int index = ratioStr.indexOf(":");
    return new ImmutablePair<Integer, Integer>(
      Integer.valueOf(ratioStr.substring(0, index)), Integer.valueOf(ratioStr.substring(index + 1))
    );
  }
}
