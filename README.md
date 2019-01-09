# Actions on Google: Java client library boilerplate

Boilerplate to help you get started quickly with the Java client library for Actions on Google.

## Setup Instructions

### Action configuration
1. Use the [Actions on Google Console](https://console.actions.google.com) to add a new project with a name of your choosing and click *Create Project*.
1. Click *Skip*, located on the top right.
1. On the left navigation menu under *BUILD*, click on *Actions*. Click on *Add Your First Action* and choose your app's language(s).
1. Select *Custom intent*, click *BUILD*. This will open a Dialogflow console. Click *CREATE*.
1. Click on the gear icon to see the project settings.
1. Select *Export and Import*.
1. Select *Restore from zip*. Follow the directions to restore from the `agent.zip` file in this repo.
1. Deploy the fulfillment webhook as described in the *Webhook* section of this README.
1. Go back to the Dialogflow console and select *Fulfillment* from the left navigation menu. Enable *Webhook*, set the value of *URL* to the webhook from the next section, then click *Save*.
1. From the Dialogflow console, select Integrations from the left navigation menu and open the Integration Settings menu for Actions on Google. Click Manage Assistant App, which will take you to the Actions on Google Console.
1. On the left navigation menu under DEPLOY, click on Directory Information.
1. Add your App info, including images, a contact email and privacy policy. This information can all be edited before submitting for review.
1. Check the box at the bottom to indicate this app uses Transactions under Additional Information. Click Save.
1. Set up a payment method for your account in the Google Assistant settings on your phone if you haven't set one up already.
1. Return [Actions on Google Console](https://console.actions.google.com), on the left navigation menu under *Test*, click on *Simulator*.
1. Click *Start Testing* and select the latest version (VERSION - Draft).
1. Type `Talk to my test app` in the simulator, or say `OK Google, talk to my test app` to any Actions on Google enabled device signed into your
developer account.
1. Follow the instructions in the *Test a transaction* section of this README to test a transaction.
1. To test payment when confirming transaction, uncheck the box in the Actions
console simulator indicating testing in Sandbox mode.

### Webhook

When a new project is created using the Actions Console, it also creates a Google Cloud project in the background.
Copy the name of this project from the Action Console project settings page.

#### Build for Google Cloud Platform
    1. Instructions for [Google Cloud App Engine Standard Environment](https://cloud.google.com/appengine/docs/standard/java/)
    1. Use gcloud CLI to set the project to the name of your Actions project. Use 'gcloud init' to initialize and set your Google cloud project to the name of the Actions project.
    1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle) by running the following command: `gradle appengineDeploy`. You can do this directly from
    IntelliJ by opening the Gradle tray and running the appEngineDeploy task. This will start the process to deploy the fulfillment code to Google Cloud App Engine.

    For more detailed information on deployment, see the [documentation](https://developers.google.com/actions/dialogflow/deploy-fulfillment).

### Test a transaction

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

### Test sending Order Updates

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

## References and How to report bugs
* Actions on Google documentation: [https://developers.google.com/actions/](https://developers.google.com/actions/).
* If you find any issues, please open a bug here on GitHub.
* Questions are answered on [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google).

## How to make contributions?
Please read and follow the steps in the [CONTRIBUTING.md](CONTRIBUTING.md).

## License
See [LICENSE](LICENSE).

## Terms
Your use of this sample is subject to, and by using or downloading the sample files you agree to comply with, the [Google APIs Terms of Service](https://developers.google.com/terms/).

## Google+
Actions on Google Developers Community on Google+ [https://g.co/actionsdev](https://g.co/actionsdev).
