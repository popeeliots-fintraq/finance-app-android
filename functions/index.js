require('dotenv').config();  // Optional: remove if not using .env

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');
const { smartCategorizeAndSave } = require('./rules_engine');

// Flexible Firebase Admin initialization
let serviceAccount;
const serviceAccountPath = path.join(__dirname, 'service-account.json');

if (fs.existsSync(serviceAccountPath)) {
  serviceAccount = require(serviceAccountPath);
} else if (process.env.SERVICE_ACCOUNT_BASE64) {
  const decoded = Buffer.from(process.env.SERVICE_ACCOUNT_BASE64, 'base64').toString('utf8');
  serviceAccount = JSON.parse(decoded);
} else {
  console.warn('⚠️ No service account credentials found. Firebase Admin may not be initialized.');
}

if (serviceAccount) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
  console.log('✅ Firebase Admin initialized with service account');
} else {
  admin.initializeApp();
  console.log('⚠️ Firebase Admin initialized with default credentials');
}

// Your HTTP function
exports.categorizeTransaction = functions.https.onRequest(async (request, response) => {
    if (request.method !== 'POST' || !request.body || !request.body.transactionData || !request.body.userId) {
        return response.status(400).send('Invalid request. Requires POST with transactionData and userId.');
    }

    const userId = request.body.userId;
    const transactionData = request.body.transactionData;

    try {
        const categorizedTransaction = await smartCategorizeAndSave(transactionData, userId);

        response.status(200).json({ 
            message: 'Transaction processed successfully.', 
            transaction: categorizedTransaction 
        });

    } catch (error) {
        console.error("FUNCTION ERROR:", error);
        response.status(500).send('Server failed to process transaction.');
    }
});
