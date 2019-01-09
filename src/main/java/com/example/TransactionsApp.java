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
import com.google.actions.api.response.helperintent.TransactionDecision;
import com.google.actions.api.response.helperintent.TransactionRequirements;
import com.google.api.services.actions_fulfillment.v2.model.ActionProvidedPaymentOptions;
import com.google.api.services.actions_fulfillment.v2.model.Argument;
import com.google.api.services.actions_fulfillment.v2.model.Button;
import com.google.api.services.actions_fulfillment.v2.model.Cart;
import com.google.api.services.actions_fulfillment.v2.model.DeliveryAddressValueSpecAddressOptions;
import com.google.api.services.actions_fulfillment.v2.model.GoogleProvidedPaymentOptions;
import com.google.api.services.actions_fulfillment.v2.model.LineItem;
import com.google.api.services.actions_fulfillment.v2.model.LineItemSubLine;
import com.google.api.services.actions_fulfillment.v2.model.Location;
import com.google.api.services.actions_fulfillment.v2.model.Merchant;
import com.google.api.services.actions_fulfillment.v2.model.Money;
import com.google.api.services.actions_fulfillment.v2.model.OpenUrlAction;
import com.google.api.services.actions_fulfillment.v2.model.Order;
import com.google.api.services.actions_fulfillment.v2.model.OrderLocation;
import com.google.api.services.actions_fulfillment.v2.model.OrderOptions;
import com.google.api.services.actions_fulfillment.v2.model.OrderState;
import com.google.api.services.actions_fulfillment.v2.model.OrderUpdate;
import com.google.api.services.actions_fulfillment.v2.model.OrderUpdateAction;
import com.google.api.services.actions_fulfillment.v2.model.OrderUpdateUserNotification;
import com.google.api.services.actions_fulfillment.v2.model.PaymentMethodTokenizationParameters;
import com.google.api.services.actions_fulfillment.v2.model.PaymentOptions;
import com.google.api.services.actions_fulfillment.v2.model.PostalAddress;
import com.google.api.services.actions_fulfillment.v2.model.Price;
import com.google.api.services.actions_fulfillment.v2.model.ProposedOrder;
import com.google.api.services.actions_fulfillment.v2.model.Receipt;
import com.google.api.services.actions_fulfillment.v2.model.StructuredResponse;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * AoG Transactions code sample for the Java client lib
 */
public class TransactionsApp extends DialogflowApp {

  private static final Logger LOGGER = Logger
      .getLogger(TransactionsApp.class.getName());
  private static final String UNIQUE_ORDER_ID = "<UNIQUE_ORDER_ID>";
  private static final String SERVICE_ACCOUNT_FILE = "service-account.json";

  // Starts the flow for a making a transaction using an action provided payment.
  @ForIntent("transaction_check_action")
  public ActionResponse transactionCheckAction(ActionRequest request) {
    LOGGER.info("transaction_check_action start");

    // Create order options
    OrderOptions orderOptions = new OrderOptions()
        .setRequestDeliveryAddress(false);

    // Create payment options
    ActionProvidedPaymentOptions actionProvidedPaymentOptions =
        new ActionProvidedPaymentOptions().setDisplayName("VISA-1234")
            .setPaymentType("PAYMENT_CARD");
    PaymentOptions paymentOptions = new PaymentOptions()
        .setActionProvidedOptions(actionProvidedPaymentOptions);

    // Send TransactionRequirements
    LOGGER.info("transaction_check_action end");
    return getResponseBuilder(request)
        .add("Placeholder for transaction requirements text")
        .add(new TransactionRequirements().setOrderOptions(orderOptions)
            .setPaymentOptions(paymentOptions))
        .build();
  }

  // Starts the flow for a making a transaction using a google provided payment.
  @ForIntent("transaction_check_google")
  public ActionResponse transactionCheckGoogle(ActionRequest request) {
    LOGGER.info("transaction_check_google start");

    // Create order options
    OrderOptions orderOptions = new OrderOptions()
        .setRequestDeliveryAddress(false);

    // Create payment options
    Map<String, String> parameters = new HashMap<>();
    parameters.put("gateway", "braintree");
    parameters.put("braintree:sdkVersion", "1.4.0");
    parameters.put("braintree:apiVersion", "v1");
    parameters.put("braintree:merchantId", "xxxxxxxxxxx");
    parameters.put("braintree:clientKey", "sandbox_xxxxxxxxxxxxxxx");
    parameters
        .put("braintree:authorizationFingerprint", "sandbox_xxxxxxxxxxxxxxx");
    PaymentMethodTokenizationParameters tokenizationParameters =
        new PaymentMethodTokenizationParameters()
            .setTokenizationType("PAYMENT_GATEWAY")
            .setParameters(parameters);
    GoogleProvidedPaymentOptions googleProvidedPaymentOptions =
        new GoogleProvidedPaymentOptions()
            .setPrepaidCardDisallowed(false)
            .setSupportedCardNetworks(Arrays.asList("VISA", "AMEX"))
            .setTokenizationParameters(tokenizationParameters);
    PaymentOptions paymentOptions = new PaymentOptions()
        .setGoogleProvidedOptions(googleProvidedPaymentOptions);

    // Send TransactionRequirements
    LOGGER.info("transaction_check_google end");
    return getResponseBuilder(request)
        .add("Placeholder for transaction requirements text")
        .add(new TransactionRequirements()
            .setOrderOptions(orderOptions)
            .setPaymentOptions(paymentOptions))
        .build();
  }

  // Verifies the transaction requirements check result
  @ForIntent("transaction_check_complete")
  public ActionResponse transactionCheckComplete(ActionRequest request) {
    LOGGER.info("transaction_check_complete start");
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
        result = resultType != null && resultType.equals("OK");
      }
    }

    ResponseBuilder responseBuilder = getResponseBuilder(request);
    if (result) {
      responseBuilder
          .add(rb.getString("get_delivery_address"));
    } else {
      responseBuilder.add(rb.getString("transaction_failed"));
    }
    LOGGER.info("transaction_check_complete end");
    return responseBuilder.build();
  }

  // Asks for a delivery address to associate with the transaction
  @ForIntent("delivery_address")
  public ActionResponse deliveryAddress(ActionRequest request) {
    LOGGER.info("delivery_address start");
    ResourceBundle rb = ResourceBundle
        .getBundle("resources", request.getLocale());

    // Create options containing reason for asking for delivery address
    DeliveryAddressValueSpecAddressOptions addressOptions =
        new DeliveryAddressValueSpecAddressOptions()
            .setReason(rb.getString("reason"));
    LOGGER.info("delivery_address end");

    return getResponseBuilder(request)
        .add("Placeholder for delivery address text")
        .add(new DeliveryAddress().setAddressOptions(addressOptions))
        .build();
  }

  // Verifies delivery address and caches it for later use
  @ForIntent("delivery_address_complete")
  public ActionResponse deliveryAddressComplete(ActionRequest request) {
    LOGGER.info("delivery_address_complete start");
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
      conversationData.put("postalAddress",
          new Gson().toJson(deliveryAddress.getPostalAddress()));
      responseBuilder
          .add(rb.getString("confirm_transaction"));
    } else {
      responseBuilder.add(rb.getString("delivery_address_failed"));
    }
    LOGGER.info("delivery_address_complete end");
    return responseBuilder.build();
  }

  // Sends a transaction decision with the proposed order
  @ForIntent("transaction_decision_action")
  public ActionResponse transactionDecisionAction(ActionRequest request) {
    LOGGER.info("transaction_decision_action start");

    // Create proposed order with cart
    Merchant merchant = new Merchant().setId("book_store_1")
        .setName("Book Store");
    Cart cart = new Cart()
        .setMerchant(merchant)
        .setNotes("The Memoir collection")
        .setLineItems(getLineItems())
        .setOtherItems(getOtherItems());
    Money totalAmount = new Money().setCurrencyCode("USD").setNanos(0)
        .setUnits(35L);
    Price totalPrice = new Price().setAmount(totalAmount).setType("ESTIMATE");
    ProposedOrder proposedOrder = new ProposedOrder().setId(UNIQUE_ORDER_ID)
        .setCart(cart).setTotalPrice(totalPrice);

    // Check context to see if transaction with
    // action payment or google payment.
    OrderOptions orderOptions;
    PaymentOptions paymentOptions;
    if (request.getContext("action_payment") != null) {
      // Setup action provided payment options
      orderOptions = new OrderOptions().setRequestDeliveryAddress(true);
      ActionProvidedPaymentOptions actionProvidedPaymentOptions =
          new ActionProvidedPaymentOptions()
              .setPaymentType("PAYMENT_CARD")
              .setDisplayName("VISA-1234");
      paymentOptions = new PaymentOptions()
          .setActionProvidedOptions(actionProvidedPaymentOptions);
    } else {
      // Setup Google provided payment options
      Map<String, String> parameters = new HashMap<>();
      parameters.put("gateway", "braintree");
      parameters.put("braintree:sdkVersion", "1.4.0");
      parameters.put("braintree:apiVersion", "v1");
      parameters.put("braintree:merchantId", "xxxxxxxxxxx");
      parameters.put("braintree:clientKey", "sandbox_xxxxxxxxxxxxxxx");
      parameters
          .put("braintree:authorizationFingerprint", "sandbox_xxxxxxxxxxxxxxx");
      PaymentMethodTokenizationParameters tokenizationParameters =
          new PaymentMethodTokenizationParameters()
              .setTokenizationType("PAYMENT_GATEWAY")
              .setParameters(parameters);
      orderOptions = new OrderOptions()
          .setRequestDeliveryAddress(false);
      GoogleProvidedPaymentOptions googleProvidedPaymentOptions =
          new GoogleProvidedPaymentOptions().setPrepaidCardDisallowed(false)
              .setSupportedCardNetworks(Arrays.asList("VISA", "AMEX"))
              .setTokenizationParameters(tokenizationParameters);
      paymentOptions = new PaymentOptions()
          .setGoogleProvidedOptions(googleProvidedPaymentOptions);
    }

    // Add delivery address as an extension to order
    Map<String, Object> extension = new HashMap<>();
    extension.put("@type",
        "type.googleapis.com/google.actions.v2.orders.GenericExtension");
    PostalAddress postalAddress =
        new Gson().fromJson(
            (String) request.getConversationData().get("postalAddress"),
            PostalAddress.class);
    extension
        .put("locations",
            Collections.singletonList(new OrderLocation().setType("DELIVERY")
                .setLocation(new Location().setPostalAddress(postalAddress))));
    proposedOrder.setExtension(extension);

    LOGGER.info("transaction_decision_action end");
    return getResponseBuilder(request)
        .add("Placeholder for transaction decision text")
        .add(new TransactionDecision()
            .setOrderOptions(orderOptions)
            .setPaymentOptions(paymentOptions)
            .setProposedOrder(proposedOrder))
        .build();
  }

  // Verified the order has been accepted
  @ForIntent("transaction_decision_complete")
  public ActionResponse transactionDecisionComplete(ActionRequest request) {
    LOGGER.info("transaction_decision_complete start");
    ResourceBundle rb = ResourceBundle
        .getBundle("resources", request.getLocale());

    // Check transaction decision value
    Argument transactionDecisionValue = request
        .getArgument("TRANSACTION_DECISION_VALUE");
    Map<String, Object> extension = null;
    if (transactionDecisionValue != null) {
      extension = transactionDecisionValue.getExtension();
    }
    String userDecision = null;
    if (extension != null) {
      userDecision = (String) extension.get("userDecision");
    }
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    if ((userDecision != null && userDecision.equals("ORDER_ACCEPTED"))) {
      // If the order is accepted, send an order update to create order
      String finalOrderId = ((Order) extension.get("order")).getFinalOrder()
          .getId();
      OrderUpdate orderUpdate = new OrderUpdate().setActionOrderId(finalOrderId)
          .setOrderState(
              new OrderState().setLabel("Order created").setState("CREATED"))
          .setReceipt(new Receipt().setConfirmedActionOrderId(UNIQUE_ORDER_ID))
          .setOrderManagementActions(
              Collections.singletonList(new OrderUpdateAction()
                  .setButton(new Button().setOpenUrlAction(new OpenUrlAction()
                      .setUrl("http://example.com/customer-service"))
                      .setTitle("Customer Service"))
                  .setType("CUSTOMER_SERVICE")))
          .setUserNotification(new OrderUpdateUserNotification()
              .setText("Notification text.").setTitle("Notification Title"))
          .setUpdateTime(Instant.now().toString());
      responseBuilder.add(rb.getString("transaction_completed"))
          .add(new StructuredResponse().setOrderUpdate(orderUpdate));
    } else if (userDecision != null && userDecision
        .equals("DELIVERY_ADDRESS_UPDATED")) {
      responseBuilder.add(new DeliveryAddress().setAddressOptions(
          new DeliveryAddressValueSpecAddressOptions()
              .setReason(rb.getString("reason"))));
    } else {
      responseBuilder.add(rb.getString("transaction_failed"));
    }
    LOGGER.info("transaction_decision_complete end");
    return responseBuilder.build();
  }

  /**
   * Gets the a list of four {@link LineItem}s.
   *
   * @return an {@link List} of four {@link LineItem}s
   */
  private List<LineItem> getLineItems() {
    // Create first line item
    LineItemSubLine firstSubline = new LineItemSubLine()
        .setNote("Note from the author");
    LineItem firstItem = createLineItem("My Memoirs", "memoirs_1", 3, 99,
        Collections.singletonList(firstSubline));

    // Create second line item
    LineItemSubLine secondSubline = new LineItemSubLine()
        .setNote("Special introduction by author");
    LineItem secondItem = createLineItem("Memoirs of a person", "memoirs_2", 5,
        99,
        Collections.singletonList(secondSubline));

    // Third line item
    LineItemSubLine thirdSubline = new LineItemSubLine().setLineItem(
        new LineItem().setName("Special memoir epilogue")
            .setId("memoirs_epilogue").setPrice(
            new Price().setAmount(
                new Money().setCurrencyCode("USD").setNanos(990000000)
                    .setUnits(3L))
        ).setType("REGULAR"));
    LineItem thirdItem = createLineItem("Their memoirs", "memoirs_3", 15, 75,
        Collections.singletonList(thirdSubline));

    // Fourth line item
    LineItemSubLine fourthSubline = new LineItemSubLine()
        .setNote("Special introduction by author");
    LineItem fourthItem = createLineItem("Our memoirs", "memoirs_4", 6, 49,
        Collections.singletonList(fourthSubline));

    return Arrays.asList(firstItem, secondItem, thirdItem, fourthItem);
  }

  /**
   * Creates a {@link LineItem} given a name, id, price, and list of {@link
   * LineItemSubLine}s.
   *
   * @return a newly created {@link LineItem}.
   */
  private LineItem createLineItem(String name, String id, int dollar, int cents,
      List<LineItemSubLine> sublines) {
    Money amount = new Money()
        .setCurrencyCode("USD")
        .setUnits((long) dollar)
        .setNanos(cents * 10000000);
    Price price = new Price().setAmount(amount).setType("ACTUAL");
    return new LineItem().setName(name)
        .setId(id)
        .setPrice(price).setQuantity(1).setType("REGULAR")
        .setSubLines(sublines);
  }

  /**
   * Gets the a list of other {@link LineItem}s.
   *
   * @return an {@link List} of four {@link LineItem}s
   */
  private List<LineItem> getOtherItems() {
    // Subtotal
    Money subtotalAmount = new Money().setCurrencyCode("USD")
        .setNanos(220000000).setUnits(32L);
    Price subtotalPrice = new Price().setAmount(subtotalAmount)
        .setType("ESTIMATE");
    LineItem subtotalItem = new LineItem().setName("Subtotal").setId("subtotal")
        .setPrice(subtotalPrice).setType("SUBTOTAL");

    // Tax
    Money taxAmount = new Money().setCurrencyCode("USD").setNanos(780000000)
        .setUnits(2L);
    Price taxPrice = new Price().setAmount(taxAmount).setType("ESTIMATE");
    LineItem taxItem = new LineItem().setName("Tax").setId("tax")
        .setPrice(taxPrice).setType("TAX");

    return Arrays.asList(subtotalItem, taxItem);
  }

  private static void sendOrderUpdate() throws IOException {
    // Setup service account credentials
    String serviceAccountFile = TransactionsApp.class.getClassLoader()
        .getResource(SERVICE_ACCOUNT_FILE)
        .getFile();
    InputStream actionsApiServiceAccount = new FileInputStream(
        serviceAccountFile);
    ServiceAccountCredentials serviceAccountCredentials = (ServiceAccountCredentials)
        ServiceAccountCredentials.fromStream(actionsApiServiceAccount)
            .createScoped(Collections.singleton(
                "https://www.googleapis.com/auth/actions.fulfillment.conversation"));
    AccessToken token = serviceAccountCredentials.refreshAccessToken();

    // Setup request with headers
    HttpPost request = new HttpPost(
        "https://actions.googleapis.com/v2/conversations:send");
    request.setHeader("Content-type", "application/json");
    request.setHeader("Authorization", "Bearer " + token.getTokenValue());

    // Create order update
    OrderUpdate orderUpdate = new OrderUpdate()
        .setActionOrderId(UNIQUE_ORDER_ID)
        .setOrderState(new OrderState()
            .setLabel("Order has been delivered!")
            .setState("FULFILLED"))
        .setUpdateTime(Instant.now().toString());

    // Setup JSON body containing order update
    JsonParser parser = new JsonParser();
    JsonObject orderUpdateElement =
        parser.parse(new Gson().toJson(orderUpdate)).getAsJsonObject();
    JsonObject orderUpdateJson = new JsonObject();
    orderUpdateJson.add("order_update", orderUpdateElement);
    JsonObject body = new JsonObject();
    body.add("custom_push_message", orderUpdateJson);
    body.addProperty("is_in_sandbox", true);
    LOGGER.info("Full JSON: " + body.toString());
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
    sendOrderUpdate();
  }
}

