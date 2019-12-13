/*
 * Copyright 2018 Google LLC
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

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.DeliveryAddress;
import com.google.actions.api.response.helperintent.transactions.v3.TransactionDecision;
import com.google.actions.api.response.helperintent.transactions.v3.TransactionRequirements;
import com.google.api.services.actions_fulfillment.v2.model.Action;
import com.google.api.services.actions_fulfillment.v2.model.Argument;
import com.google.api.services.actions_fulfillment.v2.model.DeliveryAddressValueSpecAddressOptions;
import com.google.api.services.actions_fulfillment.v2.model.GooglePaymentOption;
import com.google.api.services.actions_fulfillment.v2.model.LineItemV3;
import com.google.api.services.actions_fulfillment.v2.model.Location;
import com.google.api.services.actions_fulfillment.v2.model.MerchantPaymentMethod;
import com.google.api.services.actions_fulfillment.v2.model.MerchantPaymentOption;
import com.google.api.services.actions_fulfillment.v2.model.MerchantV3;
import com.google.api.services.actions_fulfillment.v2.model.MoneyV3;
import com.google.api.services.actions_fulfillment.v2.model.OpenUrlAction;
import com.google.api.services.actions_fulfillment.v2.model.OrderContents;
import com.google.api.services.actions_fulfillment.v2.model.OrderOptionsV3;
import com.google.api.services.actions_fulfillment.v2.model.OrderUpdateV3;
import com.google.api.services.actions_fulfillment.v2.model.OrderV3;
import com.google.api.services.actions_fulfillment.v2.model.PaymentMethodDisplayInfo;
import com.google.api.services.actions_fulfillment.v2.model.PaymentMethodStatus;
import com.google.api.services.actions_fulfillment.v2.model.PaymentParameters;
import com.google.api.services.actions_fulfillment.v2.model.PresentationOptionsV3;
import com.google.api.services.actions_fulfillment.v2.model.PriceAttribute;
import com.google.api.services.actions_fulfillment.v2.model.PurchaseFulfillmentInfo;
import com.google.api.services.actions_fulfillment.v2.model.PurchaseItemExtension;
import com.google.api.services.actions_fulfillment.v2.model.PurchaseItemExtensionItemOption;
import com.google.api.services.actions_fulfillment.v2.model.PurchaseOrderExtension;
import com.google.api.services.actions_fulfillment.v2.model.PurchaseReturnsInfo;
import com.google.api.services.actions_fulfillment.v2.model.StructuredResponse;
import com.google.api.services.actions_fulfillment.v2.model.TimeV3;
import com.google.api.services.actions_fulfillment.v2.model.UserInfo;
import com.google.api.services.actions_fulfillment.v2.model.UserInfoOptions;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;

public class TransactionsApp extends DialogflowApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsApp.class.getName());

  private static final GsonBuilder GSON_BUILDER;
  private static final LocationDeserializer LOCATION_DESERIALIZER;
  static {
    LOCATION_DESERIALIZER = new LocationDeserializer();
    GSON_BUILDER = new GsonBuilder();
    GSON_BUILDER.registerTypeAdapter(Location.class, LOCATION_DESERIALIZER);
  }

  private static String generateRandomOrderId() {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    String validCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    for (int i = 0; i < 6; i++) {
      sb.append(validCharacters.charAt(random.nextInt(validCharacters.length())));
    }
    return sb.toString();
  }

  private static void sendOrderUpdate(String orderId) throws IOException {
    // Setup service account credentials
    String serviceAccountKeyFileName = "service-account.json";
    // Setup service account credentials
    String serviceAccountFile = TransactionsApp.class.getClassLoader()
        .getResource(serviceAccountKeyFileName)
        .getFile();
    InputStream actionsApiServiceAccount = new FileInputStream(
        serviceAccountFile);
    ServiceAccountCredentials serviceAccountCredentials = (ServiceAccountCredentials)
        ServiceAccountCredentials.fromStream(actionsApiServiceAccount)
            .createScoped(Collections.singleton(
                "https://www.googleapis.com/auth/actions.order.developer"));
    AccessToken token = serviceAccountCredentials.refreshAccessToken();

    // Setup request with headers
    HttpPatch request = new HttpPatch(
        "https://actions.googleapis.com/v3/orders/" + orderId);
    request.setHeader("Content-type", "application/json");
    request.setHeader("Authorization", "Bearer " + token.getTokenValue());

    // Create order update
    FieldMask fieldMask = FieldMask.newBuilder().addAllPaths(Arrays.asList(
        "lastUpdateTime",
        "purchase.status",
        "purchase.userVisibleStatusLabel"))
        .build();

    OrderUpdateV3 orderUpdate = new OrderUpdateV3()
        .setOrder(new OrderV3()
            .setMerchantOrderId(orderId)
            .setLastUpdateTime(Instant.now().toString())
            .setPurchase(new PurchaseOrderExtension()
                .setStatus("DELIVERED")
                .setUserVisibleStatusLabel("Order delivered.")))
        .setUpdateMask(FieldMaskUtil.toString(fieldMask))
        .setReason("Order status was updated to delivered.");

    // Setup JSON body containing order update
    JsonParser parser = new JsonParser();
    JsonObject orderUpdateJson =
        parser.parse(new Gson().toJson(orderUpdate)).getAsJsonObject();
    JsonObject body = new JsonObject();
    body.add("orderUpdate", orderUpdateJson);
    JsonObject header = new JsonObject();
    header.addProperty("isInSandbox", true);
    body.add("header", header);
    StringEntity entity = new StringEntity(body.toString());
    entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
    request.setEntity(entity);

    // Make request
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(request);
    LOGGER.info(response.getStatusLine().getStatusCode() + " " + response
        .getStatusLine().getReasonPhrase());
  }

  public static void main(String[] args) throws IOException {
    String uniqueOrderId = "<UNIQUE_ORDER_ID>";
    sendOrderUpdate(uniqueOrderId);
  }

  @ForIntent("Default Welcome Intent")
  public ActionResponse welcome(ActionRequest request) {
    ResourceBundle rb = ResourceBundle.getBundle("resources", request.getLocale());
    return getResponseBuilder(request)
        .add(rb.getString("welcome"))
        .addSuggestions(new String[]{"Merchant Transaction", "Google Pay Transaction"})
        .build();
  }

  // Check transaction requirements for Merchant payment
  @ForIntent("Transaction Merchant")
  public ActionResponse transactionRequirementsMerchant(ActionRequest request) {
    LOGGER.info("Checking Transaction Requirements for merchant payment.");
    return getResponseBuilder(request)
        .add(new TransactionRequirements())
        .build();
  }

  // Check transaction requirements for Google payment
  @ForIntent("Transaction Google")
  public ActionResponse transactionRequirementsGoogle(ActionRequest request) {
      LOGGER.info("Checking Transaction Requirements for google payment.");
      return getResponseBuilder(request)
          .add(new TransactionRequirements())
          .build();
  }

  // Check result of transaction requirements
  @ForIntent("Transaction Check Complete")
  public ActionResponse transactionCheckComplete(ActionRequest request) {
    LOGGER.info("Checking Transaction Requirements Result.");

    ResourceBundle rb = ResourceBundle
        .getBundle("resources", request.getLocale());

    // Check result of transaction requirements check
    Argument transactionCheckResult = request
        .getArgument("TRANSACTION_REQUIREMENTS_CHECK_RESULT");
    boolean result = false;
    if (transactionCheckResult != null) {
      Map<String, Object> map = transactionCheckResult.getExtension();
      if (map != null) {
        String resultType = (String) map.get("resultType");
        result = resultType != null && resultType.equals("CAN_TRANSACT");
      }
    }

    ResponseBuilder responseBuilder = getResponseBuilder(request);
    if (result) {
      responseBuilder
          .add(rb.getString("get_delivery_address"))
          .addSuggestions(new String[]{"get delivery address"});
    } else {
      responseBuilder.add(rb.getString("transaction_failed"));
    }
    return responseBuilder.build();
  }

  // Asks for a delivery address to associate with the transaction
  @ForIntent("Delivery Address")
  public ActionResponse deliveryAddress(ActionRequest request) {
    ResourceBundle rb = ResourceBundle
        .getBundle("resources", request.getLocale());

    // Create options containing reason for asking for delivery address
    DeliveryAddressValueSpecAddressOptions addressOptions =
        new DeliveryAddressValueSpecAddressOptions()
            .setReason(rb.getString("reason"));

    return getResponseBuilder(request)
        .add("Placeholder for delivery address text")
        .add(new DeliveryAddress().setAddressOptions(addressOptions))
        .build();
  }

  // Verifies delivery address and caches it for later use
  @ForIntent("Delivery Address Complete")
  public ActionResponse deliveryAddressComplete(ActionRequest request) {
    ResourceBundle rb = ResourceBundle
        .getBundle("resources", request.getLocale());

    // Check delivery address value
    Argument deliveryAddressValue = request
        .getArgument("DELIVERY_ADDRESS_VALUE");
    Location deliveryAddress = null;
    if (deliveryAddressValue != null) {
      Map<String, Object> map = deliveryAddressValue.getExtension();
      if (map != null) {
        String userDecision = (String) map.get("userDecision");
        Location location = (Location) map.get("location");
        deliveryAddress =
            userDecision != null && userDecision.equals("ACCEPTED") ? location
                : null;
      }
    }

    ResponseBuilder responseBuilder = getResponseBuilder(request);
    if (deliveryAddress != null) {
      // Cache delivery address in conversation data for later use
      Map<String, Object> conversationData = request.getConversationData();
      conversationData.put("location",
          GSON_BUILDER.create().toJson(deliveryAddress, Location.class));
      responseBuilder
          .add(rb.getString("confirm_transaction"))
          .addSuggestions(new String[]{"confirm transaction"});
    } else {
      responseBuilder.add(rb.getString("delivery_address_failed"));
    }
    return responseBuilder.build();
  }

  @ForIntent("Transaction Decision")
  public ActionResponse transactionDecision(ActionRequest request) {
    LOGGER.info("Checking Transaction Decision.");

    Map<String, Object> conversationData = request.getConversationData();
    String orderId = generateRandomOrderId();
    conversationData.put("UNIQUE_ORDER_ID", orderId);

    // Build the Order

    // Transaction Merchant
    MerchantV3 transactionMerchant = new MerchantV3()
        .setId("http://www.example.com")
        .setName("Example Merchant");

    // Line Items
    LineItemV3 firstItem = new LineItemV3()
        .setId("memoirs_1")
        .setName("My Memoirs")
        .setPriceAttributes(Arrays.asList(
            new PriceAttribute()
                .setType("REGULAR")
                .setName("Item Price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(3990000L))
                .setTaxIncluded(true),
            new PriceAttribute()
                .setType("TOTAL")
                .setName("Total Price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(3990000L))
                .setTaxIncluded(true)))
        .setNotes(Collections.singletonList("Note from the author."))
        .setPurchase(new PurchaseItemExtension()
            .setQuantity(1));

    LineItemV3 secondItem = new LineItemV3()
        .setId("memoirs_2")
        .setName("Memoirs of a person")
        .setPriceAttributes(Arrays.asList(
            new PriceAttribute()
                .setType("REGULAR")
                .setName("Item Price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(5990000L))
                .setTaxIncluded(true),
            new PriceAttribute()
                .setType("TOTAL")
                .setName("Total Price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(5990000L))
                .setTaxIncluded(true)))
        .setNotes(Collections.singletonList("Special introduction by author."))
        .setPurchase(new PurchaseItemExtension()
            .setQuantity(1));

    LineItemV3 thirdItem = new LineItemV3()
        .setId("memoirs_3")
        .setName("Their memoirs")
        .setPriceAttributes(Arrays.asList(
            new PriceAttribute()
                .setType("REGULAR")
                .setName("Item Price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(15750000L))
                .setTaxIncluded(true),
            new PriceAttribute()
                .setType("TOTAL")
                .setName("Total Price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(15750000L))
                .setTaxIncluded(true)))
        .setPurchase(new PurchaseItemExtension()
            .setQuantity(1)
            .setItemOptions(Collections.singletonList(
                new PurchaseItemExtensionItemOption()
                    .setId("memoirs_epilogue")
                    .setName("Special memoir epilogue")
                    .setPrices(Arrays.asList(
                        new PriceAttribute()
                            .setType("REGULAR")
                            .setName("Item Price")
                            .setState("ACTUAL")
                            .setAmount(new MoneyV3()
                                .setCurrencyCode("USD")
                                .setAmountInMicros(3990000L))
                            .setTaxIncluded(true),
                        new PriceAttribute()
                            .setType("TOTAL")
                            .setName("Total Price")
                            .setState("ACTUAL")
                            .setAmount(new MoneyV3()
                                .setCurrencyCode("USD")
                                .setAmountInMicros(3990000L))
                            .setTaxIncluded(true))))));

    LineItemV3 fourthItem = new LineItemV3()
        .setId("memoirs_4")
        .setName("Our memoirs")
        .setPriceAttributes(Arrays.asList(
            new PriceAttribute()
                .setType("REGULAR")
                .setName("Item Price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(6490000L))
                .setTaxIncluded(true),
            new PriceAttribute()
                .setType("TOTAL")
                .setName("Total Price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(6490000L))
                .setTaxIncluded(true)))
        .setNotes(Collections.singletonList("Special introduction by author."))
        .setPurchase(new PurchaseItemExtension()
            .setQuantity(1));

    // Order Contents
    OrderContents contents = new OrderContents()
        .setLineItems(Arrays.asList(firstItem, secondItem, thirdItem, fourthItem));

    // User Info
    UserInfo buyerInfo = new UserInfo()
        .setEmail("janedoe@gmail.com")
        .setFirstName("Jane")
        .setLastName("Doe")
        .setDisplayName("Jane Doe");

    // Price Attributes
    PriceAttribute subTotal = new PriceAttribute()
        .setType("SUBTOTAL")
        .setName("Subtotal")
        .setState("ESTIMATE")
        .setAmount(new MoneyV3()
            .setCurrencyCode("USD")
            .setAmountInMicros(32220000L)
        )
        .setTaxIncluded(true);

    PriceAttribute deliveryFee = new PriceAttribute()
        .setType("DELIVERY")
        .setName("Delivery")
        .setState("ACTUAL")
        .setAmount(new MoneyV3()
            .setCurrencyCode("USD")
            .setAmountInMicros(2000000L)
        )
        .setTaxIncluded(true);

    PriceAttribute tax = new PriceAttribute()
        .setType("TAX")
        .setName("Tax")
        .setState("ESTIMATE")
        .setAmount(new MoneyV3()
            .setCurrencyCode("USD")
            .setAmountInMicros(2780000L)
        )
        .setTaxIncluded(true);

    PriceAttribute totalPrice = new PriceAttribute()
        .setType("TOTAL")
        .setName("Total Price")
        .setState("ESTIMATE")
        .setAmount(new MoneyV3()
            .setCurrencyCode("USD")
            .setAmountInMicros(37000000L)
        )
        .setTaxIncluded(true);

    // Follow up actions
    Action viewDetails = new Action()
        .setType("VIEW_DETAILS")
        .setTitle("View details")
        .setOpenUrlAction(new OpenUrlAction()
            .setUrl("https://example.com"));

    Action call = new Action()
        .setType("CALL")
        .setTitle("Call us")
        .setOpenUrlAction(new OpenUrlAction()
            .setUrl("tel:+16501112222"));

    Action email = new Action()
        .setType("EMAIL")
        .setTitle("Email us")
        .setOpenUrlAction(new OpenUrlAction()
            .setUrl("mailto:person@example.com"));

    // Terms of service and order note
    String termsOfServiceUrl = "https://example.com";
    String orderNote = "The Memoir collection";

    Location location = GSON_BUILDER.create().fromJson(
        (String) conversationData.get("location"), Location.class);

    // Purchase Order Extension
    PurchaseOrderExtension purchaseOrderExtension = new PurchaseOrderExtension()
        .setStatus("CREATED")
        .setUserVisibleStatusLabel("CREATED")
        .setType("RETAIL")
        .setReturnsInfo(new PurchaseReturnsInfo()
            .setIsReturnable(false)
            .setDaysToReturn(1)
            .setPolicyUrl("https://example.com"))
        .setFulfillmentInfo(new PurchaseFulfillmentInfo()
            .setId("FULFILLMENT_SERVICE_ID")
            .setFulfillmentType("DELIVERY")
            .setExpectedFulfillmentTime(new TimeV3()
                .setTimeIso8601("2025-09-25T18:00:00.877Z"))
            .setLocation(location)
            .setPrice(new PriceAttribute()
                .setType("REGULAR")
                .setName("Delivery price")
                .setState("ACTUAL")
                .setAmount(new MoneyV3()
                    .setCurrencyCode("USD")
                    .setAmountInMicros(2000000L))
                .setTaxIncluded(true))
            .setFulfillmentContact(new UserInfo()
                .setEmail("johnjohnson@gmail.com")
                .setFirstName("John")
                .setLastName("Johnson")
                .setDisplayName("John Johnson")))
        .setPurchaseLocationType("ONLINE_PURCHASE");

    String now = Instant.now().toString();

    OrderV3 order = new OrderV3()
        .setCreateTime(now)
        .setLastUpdateTime(now)
        .setMerchantOrderId(orderId)
        .setUserVisibleOrderId(orderId)
        .setTransactionMerchant(transactionMerchant)
        .setContents(contents)
        .setBuyerInfo(buyerInfo)
        .setPriceAttributes(Arrays.asList(
            subTotal,
            deliveryFee,
            tax,
            totalPrice
        ))
        .setFollowUpActions(Arrays.asList(
            viewDetails,
            call,
            email
        ))
        .setTermsOfServiceUrl(termsOfServiceUrl)
        .setNote(orderNote)
        .setPurchase(purchaseOrderExtension);

    // Create presentation options
    PresentationOptionsV3 presentationOptions = new PresentationOptionsV3()
        .setActionDisplayName("PLACE_ORDER");

    // Create order options
    OrderOptionsV3 orderOptions = new OrderOptionsV3()
        .setUserInfoOptions(new UserInfoOptions()
            .setUserInfoProperties(Collections.singletonList("EMAIL")));

    // Create payment parameters
    PaymentParameters paymentParameters = new PaymentParameters();
    if (request.getContext("google_payment") != null) {

      JSONObject merchantInfo = new JSONObject();
      merchantInfo.put("merchantName", "Example Merchant");

      JSONObject facilitationSpec = new JSONObject();
      facilitationSpec.put("apiVersion", 2);
      facilitationSpec.put("apiVersionMinor", 0);
      facilitationSpec.put("merchantInfo", merchantInfo);

      JSONObject allowedPaymentMethod = new JSONObject();
      allowedPaymentMethod.put("type", "CARD");

      JSONArray allowedAuthMethods = new JSONArray();
      allowedAuthMethods.addAll(Arrays.asList("PAN_ONLY", "CRYPTOGRAM_3DS"));
      JSONArray allowedCardNetworks = new JSONArray();
      allowedCardNetworks.addAll(Arrays.asList("AMEX", "DISCOVER", "JCB", "MASTERCARD", "VISA"));

      JSONObject allowedPaymentMethodParameters = new JSONObject();
      allowedPaymentMethodParameters.put("allowedAuthMethods", allowedAuthMethods);
      allowedPaymentMethodParameters.put("allowedCardNetworks", allowedCardNetworks);

      allowedPaymentMethod.put("parameters", allowedPaymentMethodParameters);

      JSONObject tokenizationSpecificationParameters = new JSONObject();
      tokenizationSpecificationParameters.put("gateway", "example");
      tokenizationSpecificationParameters.put("gatewayMerchantId", "exampleGatewayMerchantId");

      JSONObject tokenizationSpecification = new JSONObject();
      tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
      tokenizationSpecification.put("parameters", tokenizationSpecificationParameters);
      allowedPaymentMethod.put("tokenizationSpecification", tokenizationSpecification);

      JSONArray allowedPaymentMethods = new JSONArray();
      allowedPaymentMethods.add(allowedPaymentMethod);

      facilitationSpec.put("allowedPaymentMethods", allowedPaymentMethods);

      JSONObject transactionInfo = new JSONObject();
      transactionInfo.put("totalPriceStatus", "FINAL");
      transactionInfo.put("totalPrice", "37.00");
      transactionInfo.put("currencyCode", "USD");

      facilitationSpec.put("transactionInfo", transactionInfo);

      GooglePaymentOption googlePaymentOption = new GooglePaymentOption()
          .setFacilitationSpec(facilitationSpec.toJSONString());
      paymentParameters.setGooglePaymentOption(googlePaymentOption);
    } else {
      MerchantPaymentMethod merchantPaymentMethod = new MerchantPaymentMethod()
          .setPaymentMethodDisplayInfo(new PaymentMethodDisplayInfo()
              .setPaymentMethodDisplayName("VISA **** 1234")
              .setPaymentType("PAYMENT_CARD"))
          .setPaymentMethodGroup("Payment method group")
          .setPaymentMethodId("12345678")
          .setPaymentMethodStatus(new PaymentMethodStatus()
              .setStatus("STATUS_OK")
              .setStatusMessage("Status message"));

      MerchantPaymentOption merchantPaymentOption = new MerchantPaymentOption()
          .setDefaultMerchantPaymentMethodId("12345678")
          .setManagePaymentMethodUrl("https://example.com/managePayment")
          .setMerchantPaymentMethod(Collections.singletonList(merchantPaymentMethod));

      paymentParameters.setMerchantPaymentOption(merchantPaymentOption);
    }

    return getResponseBuilder(request)
        .add(new TransactionDecision()
            .setOrder(order)
            .setOrderOptions(orderOptions)
            .setPresentationOptions(presentationOptions)
            .setPaymentParameters(paymentParameters)
        )
        .build();
  }

  // Check result of asking to perform transaction / place order
  @ForIntent("Transaction Decision Complete")
  public ActionResponse transactionDecisionComplete(ActionRequest request) {
    ResourceBundle rb = ResourceBundle.getBundle("resources", request.getLocale());

    // Check transaction decision value
    Argument transactionDecisionValue = request
        .getArgument("TRANSACTION_DECISION_VALUE");
    Map<String, Object> extension = null;
    if (transactionDecisionValue != null) {
      extension = transactionDecisionValue.getExtension();
    }

    String transactionDecision = null;
    if (extension != null) {
      transactionDecision = (String) extension.get("transactionDecision");
    }
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    if ((transactionDecision != null && transactionDecision.equals("ORDER_ACCEPTED"))) {
      OrderV3 order = ((OrderV3) extension.get("order"));
      order.setLastUpdateTime(Instant.now().toString());

      // Update order status
      PurchaseOrderExtension purchaseOrderExtension = order.getPurchase();
      purchaseOrderExtension.setStatus("CONFIRMED");
      purchaseOrderExtension.setUserVisibleStatusLabel("Order confirmed");
      order.setPurchase(purchaseOrderExtension);

      // Order update
      OrderUpdateV3 orderUpdate = new OrderUpdateV3()
          .setType("SNAPSHOT")
          .setReason("Reason string")
          .setOrder(order);

      Map<String, Object> conversationData = request.getConversationData();
      String orderId = (String) conversationData.get("UNIQUE_ORDER_ID");
      String response = MessageFormat.format(
          rb.getString("transaction_decision_result_success"),
          orderId);
      responseBuilder
          .add(response)
          .add(new StructuredResponse().setOrderUpdateV3(orderUpdate));
    } else {
      responseBuilder.add(rb.getString("transaction_failed"));
    }
    return responseBuilder.endConversation().build();
  }
}
