const admin = require('firebase-admin');

// Initialize Firestore (Database) access
const db = admin.firestore();

/**
 * Applies smart rules to a transaction and saves the result to Firestore.
 * @param {object} transactionData - The structured data sent from your Kotlin app.
 * @param {string} userId - The unique ID of the user.
 * @returns {object} The categorized transaction object.
 */
async function smartCategorizeAndSave(transactionData, userId) {
    const rawDescription = transactionData.description.toUpperCase();
    let category = "Uncategorized";

    // 1. Rule Set: User-Defined Overrides (Check Firestore for custom rules)
    const userRulesRef = db.collection('users').doc(userId).collection('customRules');
    // For simplicity, we skip the query here, but this is the hook for personalized rules.

    // 2. Rule Set: Global Keyword/Heuristic Rules
    if (rawDescription.includes("UBER") || rawDescription.includes("LYFT")) {
        category = "Transportation";
    } else if (rawDescription.includes("ZOMATO") || rawDescription.includes("SWIGGY") || rawDescription.includes("STARBUCKS")) {
        category = "Food & Dining";
    } else if (rawDescription.includes("AMAZON") || rawDescription.includes("FLIPKART")) {
        category = "Shopping - Online";
    }
    // Add many more rules here...

    // 3. ML Model (Placeholder for future feature)
    // if (category === "Uncategorized") {
    //     category = await runMLPrediction(rawDescription);
    // }

    // Finalize the transaction
    const finalTransaction = {
        ...transactionData,
        userId: userId,
        category: category,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
    };

    // 4. Persistence: Save to Firestore
    await db.collection('transactions').add(finalTransaction);

    return finalTransaction;
}

module.exports = { smartCategorizeAndSave };
