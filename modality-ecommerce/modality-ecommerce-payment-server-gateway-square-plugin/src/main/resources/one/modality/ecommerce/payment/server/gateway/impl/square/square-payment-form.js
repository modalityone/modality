console.log("Starting Modality-Square script");

// Parameters injected by SquarePaymentGateway java class on server-side
const square_webPaymentsSDKUrl = '${square_webPaymentsSDKUrl}';
const square_appId = '${square_appId}';
const square_locationId = '${square_locationId}';
const square_idempotencyKey = window.crypto.randomUUID();
const modality_seamless = ${modality_seamless};
const modality_paymentId = '${modality_paymentId}';
const modality_amount = ${modality_amount};
const modality_currencyCode = "${modality_currencyCode}";
const modality_completePaymentRoute = "${modality_completePaymentRoute}";

// Parameter injected by WebPaymentForm java class on client-side (to allow JS -> Java callbacks)
let modality_javaPaymentForm;

let modality_inited;
let modality_initError;
let modality_initNotificationCalled;
let modality_containerElement;

// Constants
const modality_seamlessContainerId = 'modality-payment-form-container';
const square_cardElementId = 'square-card-container';

// Variables
var square_card; // Using 'var' declaration so that we can get the object back when executing the script a second time

// Methods called by WebPaymentForm java class on client-side

function modality_injectJavaPaymentForm(jpf) {
    console.log("modality_injectJavaPaymentForm() called");
    modality_javaPaymentForm = jpf;
    if (square_card) { // happens if the customer use the payment form several times on the same session
        try { // We destroy the previous card, otherwise attaching the new card won't work
            console.log("Destroying previous card");
            square_card.destroy();
        } catch (e) {
            console.error('Destroying previous card failed', e);
            modality_notifyInitFailure('Destroying previous card failed')
            return;
        }
    }
    if (modality_inited) {
        if (modality_initError)
            modality_notifyInitFailure(modality_initError);
        else
            modality_notifyInitSuccess();
    }
}

function modality_submitGatewayPayment(firstName, lastName, email, phone, address, city, state, countryCode) {
    console.log("modality_submitGatewayPayment() called");
    handlePaymentMethodSubmission(firstName, lastName, email, phone, address, city, state, countryCode);
}

// Methods called by this JS script to call back the Java WebPaymentForm class

function modality_notifyInitSuccess() {
    modality_inited = true;
    if (modality_javaPaymentForm) {
        modality_javaPaymentForm.onInitSuccess();
        modality_initNotificationCalled = true;
    }
}

function modality_notifyInitFailure(error) {
    modality_inited = true;
    modality_initError = error;
    if (modality_javaPaymentForm) {
        modality_javaPaymentForm.onInitFailure(error);
        modality_initNotificationCalled = true;
    }
}

function modality_notifyGatewayRecoveredFailure(error) {
    if (modality_javaPaymentForm)
        modality_javaPaymentForm.onGatewayRecoveredFailure(error);
}

function modality_notifyGatewayFailure(error) {
    if (modality_javaPaymentForm)
        modality_javaPaymentForm.onGatewayFailure(error);
}

function modality_notifyModalityFailure(error) {
    if (modality_javaPaymentForm)
        modality_javaPaymentForm.onModalityFailure(error);
}

function modality_notifyFinalStatus(status) {
    if (modality_javaPaymentForm)
        modality_javaPaymentForm.onFinalStatus(status);
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

        async function createPayment(square_sourceId, square_verificationToken) {
            const body = JSON.stringify({
                modality_paymentId: modality_paymentId,
                modality_amount: modality_amount,
                modality_currencyCode: modality_currencyCode,
                square_locationId : square_locationId,
                square_sourceId: square_sourceId,
                square_verificationToken: square_verificationToken,
                square_idempotencyKey: square_idempotencyKey,
            });

            // Calling the Modality server to complete the payment (will call Square to make the actual final payment)
            const paymentResponse = await fetch(modality_completePaymentRoute, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body,
            });

            const text = await paymentResponse.text();
            if (!paymentResponse.ok) {
                throw new Error(text);
            } else {
                let status = text.toUpperCase();
                if (status === "APPROVED" || status === "PENDING" || status === "COMPLETED" || status === "CANCELED" || status === "FAILED") {
                    return status;
                } else {
                    throw new Error(text);
                }
            }
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
                    addressLines: [address],
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
                modality_notifyInitFailure('Square.js failed to load properly');
                return;
            }

            modality_containerElement = modality_seamless ? document.getElementById(modality_seamlessContainerId) : document.body;

            if (!modality_containerElement) {
                modality_notifyInitFailure('Expected seamless container #' + modality_seamlessContainerId + ' but was not found in main DOM');
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
                modality_notifyInitFailure('Square payments failed to initialize')
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
                modality_notifyInitFailure('Square card initialization failed')
                return;
            }

            modality_notifyInitSuccess();
        }

        async function handlePaymentMethodSubmission(firstName, lastName, email, phone, address, city, state, countryCode) {
            // Firstly: Card number verification
            let token;
            try {
                token = await tokenize(square_card);
            } catch (e) {
                displayPaymentResults('FAILURE');
                console.error(e.message);
                modality_notifyGatewayRecoveredFailure(e.message);
                return;
            }

            // Secondly: Buyer verification
            let verificationToken;
            try {
                verificationToken = await verifyBuyer(token, firstName, lastName, email, phone, address, city, state, countryCode);
            } catch (e) {
                displayPaymentResults('FAILURE');
                console.error(e.message);
                modality_notifyGatewayFailure(e.message);
                return;
            }

            // Thirdly: Modality server process (includes Square payment completion + Modality payment database update)
            try {
                const paymentStatus = await createPayment(
                    token,
                    verificationToken,
                );
                displayPaymentResults('SUCCESS');

                console.debug('Payment Success', paymentStatus);
                modality_notifyFinalStatus(paymentStatus);
            } catch (e) {
                displayPaymentResults('FAILURE');
                console.error(e.message);
                modality_notifyModalityFailure(e.message);
            }
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

