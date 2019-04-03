# Actions on Google: Transactions Sample using Java

This sample shows everything you need to facilitate transactions for your app. It includes the main checkout flows, including checking for transaction requirements, getting the user's delivery address, and confirming the transaction. There is also an order update gradle task (sendOrderUpdate) that can be used to asynchronously update order status at any time.

This sample provides examples of transaction payment configurations for action provided payments, but the Actions on Google library also offers functionality for Google provided payment by providing tokenization parameters from your payment processor. There are comments in TransactionsApp.java demonstrating this behavior.

### Setup Instructions

#### Action Configuration
1. From the [Actions on Google Console](https://console.actions.google.com/), add a new project (this will become your *Project ID*) > **Create Project**.
1. Scroll down to the **More Options** section, and click on the **Conversational** card.
1. From the left navigation menu under **Build** > **Actions** > **Add Your First Action** > **BUILD** (this will bring you to the Dialogflow console) > Select language and time zone > **CREATE**.
1. In Dialogflow, go to **Settings** ⚙ > **Export and Import** > **Restore from zip**.
   + Follow the directions to restore from the `agent.zip` file in this repo.
1. From the Dialogflow console, select Integrations from the left navigation menu and open the Integration Settings menu for Actions on Google. Click Manage Assistant App, which will take you to the Actions on Google Console.
1. On the left navigation menu under **DEPLOY**, click on **Directory Information**.
1. Add your App info, including images, a contact email and privacy policy. This information can all be edited before submitting for review.
1. Check the box at the bottom to indicate this app uses Transactions under Additional Information. Click Save.
1. Set up a payment method for your account in the Google Assistant settings on your phone if you haven't set one up already.

#### App Engine Deployment & Webhook Configuration
When a new project is created using the Actions Console, it also creates a Google Cloud project in the background.

1. Download & install the [Google Cloud SDK](https://cloud.google.com/sdk/docs/)
1. Configure the gcloud CLI and set your Google Cloud project to the name of your Actions on Google Project ID, which you can find from the [Actions on Google console](https://console.actions.google.com/) under Settings ⚙
   + `gcloud init`
   + `gcloud auth application-default login`
   + `gcloud components install app-engine-java`
   + `gcloud components update`
1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle):
   + `gradle appengineDeploy` OR
   +  From within IntelliJ, open the Gradle tray and run the appEngineDeploy task.
1. Back in the [Dialogflow console](https://console.dialogflow.com), from the left navigation menu under **Fulfillment** > **Enable Webhook**, set the value of **URL** to `https://<YOUR_PROJECT_ID>.appspot.com` > **Save**.

#### Testing this Sample
1. In the [Dialogflow console](https://console.dialogflow.com), from the left navigation menu > **Integrations** > **Integration Settings** under Google Assistant > Enable **Auto-preview changes** >  **Test** to open the Actions on Google simulator.
1. Type `Talk to my test app` in the simulator, or say `OK Google, talk to my test app` to Google Assistant on a mobile device associated with your Action's account.

##### Test a transaction

1. Determine a unique Order ID for the transaction you want to test, and
replace the `<UNIQUE_ORDER_ID>` string constant found in `TransactionsApp.java`. You may
need to change this and redeploy your webhook each time you want to test a transaction
confirmation.
1. Determine the [payment method](https://developers.google.com/actions/transactions/dev-guide#choose_a_payment_method)
you wish to accept in the app. The sample Action demonstrates the flow for both action provided payment and google provided payment by default.
1. It must be confirmed that the [user can transact](https://developers.google.com/actions/transactions/dev-guide#check_for_transaction_requirements).
To check this, say/type either
      * `check transaction with Google payment` - to check requirements for a transaction where
      the user pays with an Google-provided payment instrument stored under their account.
      * `check transaction with action payment` - to check requirements for a transaction where
      the user will pay with a payment instrument that you are providing.
1. (Optional) The user's delivery address can then be acquired by saying/typing
`get delivery address`. This will present the user with a flow to select from
an available delivery address.
5. To confirm the transaction, simply say/type `confirm transaction`. Here, the
`transaction_decision_action` intent will be handled in `TransactionsApp.java`.
6. You should see a transaction receipt, and a final confirmation of the order.

##### Test sending Order Updates

1. Visit the [Google Cloud console](https://console.cloud.google.com/) for the project used in the [Actions console](https://console.actions.google.com).
1. Navigate to the [API Library](https://console.cloud.google.com/apis/library).
1. Search for and enable the Google Actions API.
1. Navigate to the Credentials page in the API manager.
1. Click Create credentials > Service Account Key.
1. Click the Select box under Service Account and click New Service Account.
1. Give the Service Account the name (i.e. "service-account") and the role of Project Owner.
1. Select the JSON key type.
1. Click Create.
1. Place the newly downloaded file in the 'src/main/resources/' directory calling the file `service-account.json`.
1. In `TransactionsApp.java`, replace the `<UNIQUE_ORDER_ID>` placeholder string assigned to actionOrderId with the ID of the order you wish to update.
1. To send an order update, open a terminal and run the following command: `./gradlew sendOrderUpdate`.
1. If the order update succeeds, a `200 OK` response should be logged to the console.

### Troubleshooting

If the app isn't working, try the following:
* Make sure your Actions console project has filled App Information section,
including name, images, email address, etc. This is required for testing transactions.
After changing this, you may need to re-enable testing in the Actions console.
* Make sure your Actions console project indicates that it is using Transactions
using the checkbox at the bottom of App Information
* Make sure you've replaced the `<UNIQUE_ORDER_ID>` string constant in `TransactionsApp.java`,  and replace it
each time you test the app.
* The full transactions flow may only be testable on a phone.

### References & Issues
+ Questions? Go to [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google), [Assistant Developer Community on Reddit](https://www.reddit.com/r/GoogleAssistantDev/) or [Support](https://developers.google.com/actions/support/).
+ For bugs, please report an issue on Github.
+ Actions on Google [Documentation](https://developers.google.com/actions/extending-the-assistant)
+ [Webhook Boilerplate Template](https://github.com/actions-on-google/dialogflow-webhook-boilerplate-java) for Actions on Google.
+ More info about [Gradle & the App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-gradle).
+ More info about deploying [Java apps with App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart).
 
### Make Contributions
Please read and follow the steps in the [CONTRIBUTING.md](CONTRIBUTING.md).
 
### License
See [LICENSE](LICENSE).
 
### Terms
Your use of this sample is subject to, and by using or downloading the sample files you agree to comply with, the [Google APIs Terms of Service](https://developers.google.com/terms/).

