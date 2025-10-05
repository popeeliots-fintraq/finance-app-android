// Request
data class CategorizeRequest(
    val userId: String,
    val transactionData: TransactionData
)

data class TransactionData(
    val amount: Double,
    val description: String,
    val date: String // "YYYY-MM-DD" format
)

// Response
data class CategorizeResponse(
    val message: String,
    val transaction: CategorizedTransaction
)

data class CategorizedTransaction(
    val userId: String,
    val description: String,
    val amount: Double,
    val category: String,
    val timestamp: String
)
