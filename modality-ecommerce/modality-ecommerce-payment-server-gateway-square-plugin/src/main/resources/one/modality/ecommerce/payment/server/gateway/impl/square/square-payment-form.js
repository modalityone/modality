console.log("Starting Modality-Square script");

// Parameters injected by SquarePaymentGateway java class on the server side
const modality_amount = ${modality_amount};
const modality_currencyCode = "${modality_currencyCode}";
const modality_seamless = ${modality_seamless};
const square_webPaymentsSDKUrl = '${square_webPaymentsSDKUrl}';
const square_appId = '${square_appId}';
const square_locationId = '${square_locationId}';
const square_idempotencyKey = window.crypto.randomUUID();

// Parameter injected by WebPaymentForm java class on the client side (to allow JS -> Java callbacks)
let modality_javaPaymentForm;

let modality_initialized;
let modality_initError;
let modality_initNotificationCalled;
let modality_containerElement;

// Constants
const modality_seamlessContainerId = 'modality-payment-form-container';
const square_cardElementId = 'square-card-container';

// Variables
var square_card; // Using 'var' declaration so that we can get the object back when executing the script a second time

// Methods called by WebPaymentForm java class on the client side

function modality_injectJavaPaymentForm(jpf) {
    console.log("modality_injectJavaPaymentForm() called");
    modality_javaPaymentForm = jpf;
    if (square_card) { // happens if the customer uses the payment form several times in the same session
        try { // We destroy the previous card, otherwise attaching the new card won't work
            console.log("Destroying previous card");
            square_card.destroy();
        } catch (e) {
            console.error('Destroying previous card failed', e);
            modality_notifyGatewayInitFailure('Destroying previous card failed: ' + e.message);
            return;
        }
    }
    if (modality_initialized) {
        if (modality_initError)
            modality_notifyGatewayInitFailure(modality_initError);
        else
            modality_notifyGatewayInitSuccess();
    }
}

function modality_submitGatewayPayment(firstName, lastName, email, phone, address, city, state, postCode, countryCode, countryName) {
    console.log("modality_submitGatewayPayment() called");
    handlePaymentMethodSubmission(firstName, lastName, email, phone, address, city, state, countryCode);
}

// Methods called by this JS script to call back the Java WebPaymentForm class to notify about the progress in the gateway process

function modality_notifyGatewayInitSuccess() {
    modality_initialized = true;
    if (modality_javaPaymentForm) {
        modality_javaPaymentForm.onGatewayInitSuccess();
        modality_initNotificationCalled = true;
    }
}

function modality_notifyGatewayInitFailure(error) {
    modality_initialized = true;
    modality_initError = error;
    if (modality_javaPaymentForm) {
        modality_javaPaymentForm.onGatewayInitFailure(error);
        modality_initNotificationCalled = true;
    }
}

function modality_notifyGatewayCardVerificationFailure(error) {
    if (modality_javaPaymentForm)
        modality_javaPaymentForm.onGatewayCardVerificationFailure(error);
}

function modality_notifyGatewayBuyerVerificationFailure(error) {
    if (modality_javaPaymentForm)
        modality_javaPaymentForm.onGatewayBuyerVerificationFailure(error);
}

function modality_notifyGatewayPaymentVerificationSuccess(paymentCompletionPayload) {
    if (modality_javaPaymentForm)
        modality_javaPaymentForm.onGatewayPaymentVerificationSuccess(paymentCompletionPayload);
}

console.log("Loading Square module");

import(square_webPaymentsSDKUrl)
    .catch(err => {
        console.error('Failed to load Square module', err);
    })
    .then(module => {
        console.log("Square module loaded");

        let payments;

        async function initializeCard(payments) {
            const card = await payments.card();
            await card.attach('#' + square_cardElementId);
            return card;
        }

        async function tokenize(paymentMethod) {
            const tokenResult = await paymentMethod.tokenize();
            if (tokenResult.status === 'OK') {
                return tokenResult.token;
            } else {
                let errorMessage = `Tokenization failed with status: ${tokenResult.status}`;
                if (tokenResult.errors) {
                    errorMessage += ` and errors: ${JSON.stringify(
                        tokenResult.errors,
                    )}`;
                }

                throw new Error(errorMessage);
            }
        }

        // Required in SCA Mandated Regions: Learn more at https://developer.squareup.com/docs/sca-overview
        async function verifyBuyer(token, firstName, lastName, email, phone, address, city, state, countryCode) {
            if (email)
                email = email.toLowerCase();
            if (countryCode)
                countryCode = countryCode.toUpperCase(); // Square rejects lower case country codes
            const verificationDetails = {
                amount: (Number(modality_amount) / 100).toFixed(2),
                billingContact: {
                    givenName: firstName,
                    familyName: lastName,
                    email: email,
                    phone: phone,
                    addressLines: address ? [address] : [],
                    city: city,
                    state: state,
                    countryCode: countryCode,
                },
                currencyCode: modality_currencyCode,
                intent: 'CHARGE',
            };

            const verificationResults = await payments.verifyBuyer(
                token,
                verificationDetails,
            );
            return verificationResults.token;
        }

        // status is either SUCCESS or FAILURE;
        function displayPaymentResults(status) {
            const statusContainer = document.getElementById(
                'square-payment-status-container',
            );
            if (status === 'SUCCESS') {
                statusContainer.classList.remove('is-failure');
                statusContainer.classList.add('is-success');
            } else {
                statusContainer.classList.remove('is-success');
                statusContainer.classList.add('is-failure');
            }

            statusContainer.style.visibility = 'visible';
        }

        async function onLoaded() {
            console.log("Square DOMContentLoaded");
            if (modality_javaPaymentForm) {
                console.log("modality_javaPaymentForm is set");
            } else {
                console.log("modality_javaPaymentForm is NOT set");
            }

            if (!window.Square) {
                modality_notifyGatewayInitFailure('Square.js failed to load properly');
                return;
            }

            modality_containerElement = modality_seamless ? document.getElementById(modality_seamlessContainerId) : document.body;

            if (!modality_containerElement) {
                modality_notifyGatewayInitFailure('Expected seamless container #' + modality_seamlessContainerId + ' but was not found in main DOM');
                return;
            }

            modality_containerElement.innerHTML = `
                <form id="square-payment-form">
                    <div id="square-card-container"></div>
                </form>
                <div id="square-payment-status-container"></div>
            `;

            try {
                console.log("Calling payments");
                payments = window.Square.payments(square_appId, square_locationId);
            } catch (e) {
                console.error('Payments failed', e);
                modality_notifyGatewayInitFailure('Square payments failed to initialize: ' + e.message);
                const statusContainer = document.getElementById(
                    'square-payment-status-container',
                );
                statusContainer.className = 'missing-credentials';
                statusContainer.style.visibility = 'visible';
                return;
            }

            try {
                console.log("Calling initializeCard");
                square_card = await initializeCard(payments);
            } catch (e) {
                console.error('Initializing Card failed', e);
                modality_notifyGatewayInitFailure('Square card initialization failed: ' + e.message);
                return;
            }

            modality_notifyGatewayInitSuccess();
        }

        async function handlePaymentMethodSubmission(firstName, lastName, email, phone, address, city, state, countryCode) {
            // Firstly: Card number verification
            let token;
            try {
                token = await tokenize(square_card);
            } catch (e) {
                displayPaymentResults('FAILURE');
                console.error(e.message);
                modality_notifyGatewayCardVerificationFailure(e.message);
                return;
            }

            // Secondly: Buyer verification
            let verificationToken;
            try {
                verificationToken = await verifyBuyer(token, firstName, lastName, email, phone, address, city, state, countryCode);
            } catch (e) {
                displayPaymentResults('FAILURE');
                console.error(e.message);
                modality_notifyGatewayBuyerVerificationFailure(e.message);
                return;
            }

            // Thirdly: Modality payment form callback for payment completion
            const paymentCompletionPayload= {
                modality_amount: modality_amount,
                modality_currencyCode: modality_currencyCode,
                square_locationId : square_locationId,
                square_sourceId: token,
                square_verificationToken: verificationToken,
                square_idempotencyKey: square_idempotencyKey,
            };

            // Notifying the Java WebPaymentForm that the verification is successful => it will complete the payment on
            // the server-side, passing it this payload. It will basically call PaymentService.completePayment() - after
            // the UI informed the user this is happening - which will update the payment state in the database in
            // dependence on the result of AuthorizePaymentGateway.completePayment() <= will treat the payload
            modality_notifyGatewayPaymentVerificationSuccess(JSON.stringify(paymentCompletionPayload));
        }

        if (document.readyState === "complete" || document.readyState === "interactive") {
            // Document is already ready, so just call the function directly
            onLoaded();
        } else {
            // Document is not ready yet, so add an event listener for DOMContentLoaded
            document.addEventListener('DOMContentLoaded', onLoaded);
        }

        // Making handlePaymentMethodSubmission function visible for modality_submitGatewayPayment
        window.handlePaymentMethodSubmission = handlePaymentMethodSubmission;

    });

