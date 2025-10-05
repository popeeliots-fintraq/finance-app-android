// Load environment variables (safe fallback for CI environments)
try {
  require('dotenv').config();
} catch (err) {
  console.warn('⚠️ dotenv not loaded (probably running in CI/CD)');
}

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');
const { smartCategorizeAndSave } = require('./rules_engine');

// === Flexible Firebase Admin Initialization ===

let serviceAccount;
const serviceAccountPath = path.join(__dirname, 'service-account.json');

// Try loading credentials from file
if (fs.existsSync(serviceAccountPath)) {
  serviceAccount = require(serviceAccountPath);
}
// Fallback: try decoding from env var (used in GitHub Actions)
else if (process.env.SERVICE_ACCOUNT_BASE64) {
  try {
    const decoded = Buffer.from(process.env.SERVICE_ACCOUNT_BASE64, 'base64').toString('utf8');
    serviceAccount = JSON.parse(decoded);
  } catch (error) {
    console.error('❌ Failed to decode SERVICE_ACCOUNT_BASE64:', error);
  }
} else {
  console.warn('⚠️ No Firebase service account credentials found.');
}

// Initialize Firebase Admin SDK
if (serviceAccount) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
  console.log('✅ Firebase Admin initialized with service account credentials');
} else {
  admin.initializeApp(); // fallback to default credentials (e.g., for Firebase-hosted env)
  console.log('⚠️ Firebase Admin initialized with default credentials');
}

// === Cloud Function ===

exports.categorizeTransaction = functions.https.onRequest(async (request, response) => {
  if (
    request.method !== 'POST' ||
    !request.body ||
    !request.body.transactionData ||
    !request.body.userId
  ) {
    return response
      .status(400)
      .send('Invalid request. Requires POST with transactionData and userId.');
  }

  const { userId, transactionData } = request.body;

  try {
    const categorizedTransaction = await smartCategorizeAndSave(transactionData, userId);

    return response.status(200).json({
      message: 'Transaction processed successfully.',
      transaction: categorizedTransaction,
    });
  } catch (error) {
    console.error('❌ FUNCTION ERROR:', error);
    return response.status(500).send('Server failed to process transaction.');
  }
});
