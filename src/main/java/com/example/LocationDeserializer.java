/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.google.api.services.actions_fulfillment.v2.model.LatLng;
import com.google.api.services.actions_fulfillment.v2.model.Location;
import com.google.api.services.actions_fulfillment.v2.model.PostalAddress;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class LocationDeserializer implements
    JsonDeserializer<Location> {

  @Override
  public Location deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();

    Location location = new Location();
    JsonElement city = jsonObject.get("city");
    JsonElement coordinates = jsonObject.get("coordinates");
    JsonElement name = jsonObject.get("name");
    JsonElement notes = jsonObject.get("notes");
    JsonElement phoneNumber = jsonObject.get("phoneNumber");
    JsonElement placeId = jsonObject.get("placeId");
    JsonElement zipCode = jsonObject.get("zipCode");
    JsonElement formattedAddress = jsonObject.get("formattedAddress");
    JsonElement postalAddress = jsonObject.get("postalAddress");

    if (city != null) {
      location.setCity(city.getAsString());
    }
    if (name != null) {
      location.setName(name.getAsString());
    }
    if (notes != null) {
      location.setNotes(notes.getAsString());
    }
    if (phoneNumber != null) {
      location.setPhoneNumber(phoneNumber.getAsString());
    }
    if (placeId != null) {
      location.setPlaceId(placeId.getAsString());
    }
    if (zipCode != null) {
      location.setZipCode(zipCode.getAsString());
    }
    if (formattedAddress != null) {
      location.setFormattedAddress(formattedAddress.getAsString());
    }

    if (postalAddress != null) {
      PostalAddress address = context.deserialize(
          postalAddress.getAsJsonObject(), PostalAddress.class);
      location.setPostalAddress(address);
    }

    if (coordinates != null) {
      LatLng coords = context.deserialize(
          coordinates.getAsJsonObject(), LatLng.class);
      location.setCoordinates(coords);
    }

    return location;
  }
}
