const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { smartCategorizeAndSave } = require('./rules_engine');

// Initialize the Firebase Admin SDK
admin.initializeApp();

// HTTP endpoint for the Kotlin App to call
exports.categorizeTransaction = functions.https.onRequest(async (request, response) => {
    // 1. API Method and Body Validation
    if (request.method !== 'POST' || !request.body || !request.body.transactionData || !request.body.userId) {
        return response.status(400).send('Invalid request. Requires POST with transactionData and userId.');
    }

    // 2. Security Check (Crucial for production apps)
    // You would typically use Firebase Authentication here to verify the ID token.
    // For this example, we assume the userId is passed directly.
    const userId = request.body.userId;
    const transactionData = request.body.transactionData;

    try {
        const categorizedTransaction = await smartCategorizeAndSave(transactionData, userId);
        
        // Success response
        response.status(200).json({ 
            message: 'Transaction processed successfully.', 
            transaction: categorizedTransaction 
        });
        
    } catch (error) {
        // Log the error for debugging
        console.error("FUNCTION ERROR:", error);
        response.status(500).send('Server failed to process transaction.');
    }
});
