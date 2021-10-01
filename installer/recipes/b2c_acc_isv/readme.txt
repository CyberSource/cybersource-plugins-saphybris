This recipe provides everything you need to install SAP Hybris Commerce Responsive B2C Commerce Accelerator with ISV Payment Extension, Smart Edit, Live Edit, Promotions Engine, Coupons, Textfield Configurator, Assisted Services and Customer Ticketing addons.

Required Configurations:
* For features that require a Google API key (such as the Store Locator, which uses Google Maps), you need to set the “googleApiKey”. For information on generating your API Key: https://developers.google.com/maps/documentation/javascript/tutorial#api_key
* To configure merchants review isvpaymentsampledata extension and replace it with project specific data extension
* For payment reports, you need to set the “isv.payment.decisionmanager.reporting.username” and “isv.payment.decisionmanager.reporting.password”
* For Decision Manager conversion reports, you need to configure cronjob triggers, or use trigger configuration provided in isvpaymentsampledata extension