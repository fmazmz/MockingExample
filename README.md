## Refactoring decisions (PaymentProcessor)

#### Saving both the failed and successful payments
In a prod scenario, it would not be sufficient to persist only the successful payments.
All payment attempts should be stored in order to support auditing, debugging, and traceability of payment history.


#### Removing DatabaseConnection and using a PaymentRepository
Having a static database connection inside the business layer tightly couples the service to the database implementation
which inturn also makes it difficult to mock and test.
By removing the static DatabaseConnection and injecting a repository into the service
the DB persistance logic is seperated from the actual business logic which is ideal for mocking and testing.


#### PaymentApiResponse as a record and not a class
We know that the ApiResponse will merely act as a DTO between the api and the PaymentProcessor.
The response should never be manipulated or modified (mutable), utilizing a record makes more sense here as
immutability is enforced by design, and the intent of the object becomes clearer, as well as we reduce boilerplate code from writing custom getters and setters.


#### Updating the payment status from an internal String to Enum
Having the payment status as an internal String inside the PaymentProcessor is not secure, and it opens the door for easy mistakes, 
a developer could easily update that status to whatever they wanted or mistype the status.
Using a PaymentStatus enum allows us to easier maintain the statuses, 
use the status inside other entities for example if we wanted to create a Payment class with a status attached to it, 
or write extended business logic in the future that is dependent on the status of the payment.


#### Throwing an exception during a failed payment
In the original implementation, payment failure was only communicated via a boolean return value. 
This makes it easy for failures to be ignored and does not clearly signal exceptional behavior.

By throwing a specific exception when a payment fails, error handling becomes explicit and forces the caller to handle 
the failure case. This results in clearer control flow and better separation between successful and unsuccessful execution paths.

#### Removing the static API_KEY from the PaymentProcessor
Self explanitory, to elaborate, a raw API key should never be anywhere within a codebase, for obvious security reasons.
From a dependency standpoint however, the PaymentProcessor should be agnostic as to what implementation of PaymentApi we
are using, 
if we had Stripe as the PaymentApi right now as an example, later if we change to a different payment provider, we would
also have to update the API key within the PaymentProcessor AND the test cases which is not ideal.
This also creates unnecessary coupling between the service and a specific external API.

From a testing standpoint:
removing the static API key improves testability by eliminating hidden state and hardcoded values. 
Instead of relying on a fixed key, tests can supply a test-specific API key via constructor injection or configuration. 
This allows unit tests to focus on verifying behavior rather than being tied to a specific credential, and it makes mocking the PaymentApi simpler and more explicit.

In a production application, configuration often contains multiple values (API keys, timeouts, retry policies, etc.). 
Using a `PaymentConfig` interface creates a natural place to add additional configuration parameters in the future without changing the `PaymentProcessor` constructor signature repeatedly.