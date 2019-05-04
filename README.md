# Actions on Google: Transactions Sample

This sample demonstrates Actions on Google features for use on Google Assistant including physical transactions, specifically [merchant-managed](https://developers.google.com/actions/transactions/physical/dev-guide-physical-custom) as well as through [Google Pay](https://developers.google.com/actions/transactions/physical/dev-guide-physical-gpay) with payment gateways -- using the [Java client library](https://github.com/actions-on-google/actions-on-google-java) and deployed on [App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart).

This sample shows everything you need to facilitate transactions, including:
  + Check for transaction requirements
  + Get the delivery address
  + Confirm the transaction
  + Examples of Google Pay and merchant-managed payment options
  + Order update Gradle task (sendOrderUpdate), to asynchronously update order status at any time

### Setup Instructions
### Prerequisites
1. Download & install the [Google Cloud SDK](https://cloud.google.com/sdk/docs/)
1. [Gradle with App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-gradle)
   + Run `gcloud auth application-default login` with your Google account
   + Install and update the App Engine component,`gcloud components install app-engine-java`
   + Update other components, `gcloud components update`

### Configuration
#### Actions Console
1. From the [Actions on Google Console](https://console.actions.google.com/), add a new project (this will become your *Project ID*) > **Create Project** > under **More options** > **Conversational**.
1. From the left navigation menu under **Build** > **Actions** > **Add Your First Action** > **BUILD** (this will bring you to the Dialogflow console) > Select language and time zone > **CREATE**.
1. In Dialogflow, go to **Settings** ⚙ > **Export and Import** > **Restore from zip**.
   + Follow the directions to restore from the `agent.zip` file in this repo.

#### App Engine Deployment & Webhook Configuration
When a new project is created using the Actions Console, it also creates a Google Cloud project in the background.
1. Configure the gcloud CLI and set your Google Cloud project to the name of your Actions on Google Project ID, which you can find from the [Actions on Google console](https://console.actions.google.com/) under Settings ⚙
   + `gcloud init`
1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle):
   + `gradle appengineDeploy` OR
   +  From within IntelliJ, open the Gradle tray and run the appEngineDeploy task.

#### Dialogflow Console
Return to the [Dialogflow Console](https://console.dialogflow.com), from the left navigation menu under **Fulfillment** > **Enable Webhook**, set the value of **URL** to `https://${YOUR_PROJECT_ID}.appspot.com` > **Save**.
1. From the left navigation menu, click **Integrations** > **Integration Settings** under Google Assistant > Enable **Auto-preview changes** >  **Test** to open the Actions on Google simulator then say or type `Talk to my test app`.

### Running this Sample
+ (Recommended) You can test your Action on any Google Assistant-enabled device on which the Assistant is signed into the same account used to create this project. Just say or type, “OK Google, talk to my test app”.
+ Set up a payment method for your account in the Google Assistant settings on your phone if you haven't set one up already -- sandbox testing is the default setting.

### Send Order Update Configuration
1. In the [Google Cloud Platform console](https://console.cloud.google.com/), select your *Project ID* from the dropdown > **Menu ☰** > **APIs & Services** > **Library**
1. Select **Actions API** > **Enable**
1. Under **Menu ☰** > **APIs & Services** > **Credentials** > **Create Credentials** > **Service Account Key**.
1. From the dropdown, select **New Service Account**
    + name:  `service-account`
    + role:  **Project/Owner**
    + key type: **JSON** > **Create**
    + Your private JSON file will be downloaded to your local machine
1. Place the downloaded file in the 'src/main/resources/' and name `service-account.json`.
1. In `TransactionsApp.java`, in the sendOrderUpdate() method > replace the `<finalOrderId>` placeholder string assigned to finalOrderId with the ID of the order you wish to update.
1. To send an order update, open a terminal and run the following command: `./gradlew sendOrderUpdate`.
   +  Or from within IntelliJ, open the Gradle tray and run the sendOrderUpdate task.
1. If the order update succeeds, a `200 OK` response should be logged to the console.

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
