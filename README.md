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
The response should never be manipulated or modified (mutable), so utilizing a record makes more sence here to keep the response as an immutable object.


#### Updating the payment status from an internal String to Enum
Having the payment status as an internal String inside the PaymentProcessor is not secure, and it opens the door for easy mistakes, a developer could easily update that status to whatever they wanted or mistype the status.
Using a PaymentStatus enum allows us to easier maintain the statuses, use the status inside other entities for example if we wanted to create a Payment class with a status attached to it, or write extended business logic in the future that is dependent on the status of the payment.


#### Throwing an exception during a failed payment
In the original implementation, payment failure was only communicated via a boolean return value. This makes it easy for failures to be ignored and does not clearly signal exceptional behavior.

By throwing a specific exception when a payment fails, error handling becomes explicit and forces the caller to handle the failure case. This results in clearer control flow and better separation between successful and unsuccessful execution paths.