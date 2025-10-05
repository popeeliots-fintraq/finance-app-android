const admin = require('firebase-admin');

/**
 * Applies smart rules to a transaction and saves the result to Firestore.
 * @param {object} transactionData - The structured data sent from your Kotlin app.
 * @param {string} userId - The unique ID of the user.
 * @returns {object} The categorized transaction object.
 */
async function smartCategorizeAndSave(transactionData, userId) {
    // Get Firestore instance (after initializeApp has run)
    const db = admin.firestore();

    const rawDescription = transactionData.description.toUpperCase();
    let category = "Uncategorized";

    // 1. Rule Set: User-Defined Overrides (Future: check Firestore for user-specific rules)
    const userRulesRef = db.collection('users').doc(userId).collection('customRules');
    // For now, skipping the custom rules query for simplicity

    // 2. Rule Set: Global Keyword/Heuristic Rules
    if (rawDescription.includes("UBER") || rawDescription.includes("OLA") || rawDescription.includes("Rapido")) {
        category = "Transportation";
    } else if (rawDescription.includes("ZOMATO") || rawDescription.includes("SWIGGY") || rawDescription.includes("STARBUCKS")) {
        category = "Food & Dining";
    } else if (rawDescription.includes("AMAZON") || rawDescription.includes("FLIPKART") || rawDescription.includes("Meesho")) {
        category = "Shopping - Online";
    }
    // Add more keyword rules as needed...

    // 3. (Optional Future) ML Model
    // if (category === "Uncategorized") {
    //     category = await runMLPrediction(rawDescription);
    // }

    // 4. Finalize Transaction Object
    const finalTransaction = {
        ...transactionData,
        userId: userId,
        category: category,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
    };

    // 5. Save to Firestore
    await db.collection('transactions').add(finalTransaction);

    return finalTransaction;
}

module.exports = { smartCategorizeAndSave };
