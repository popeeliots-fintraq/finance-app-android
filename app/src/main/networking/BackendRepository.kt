object BackendRepository {

fun sendTransactionToBackend(userId: String, amount: Double, description: String, date: String) {
    val request = CategorizeRequest(
        userId = userId,
        transactionData = TransactionData(amount, description, date)
    )

    RetrofitClient.api.categorizeTransaction(request).enqueue(object : retrofit2.Callback<CategorizeResponse> {
        override fun onResponse(
            call: retrofit2.Call<CategorizeResponse>,
            response: retrofit2.Response<CategorizeResponse>
        ) {
            if (response.isSuccessful) {
                val categorized = response.body()?.transaction
                println("✅ Transaction categorized: $categorized")
                // Here, save/update the transaction in your app DB/UI
            } else {
                println("❌ API Error: ${response.code()} - ${response.message()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<CategorizeResponse>, t: Throwable) {
            println("❌ Network failure: ${t.localizedMessage}")
        }
    })
}
fun handleIncomingSms(smsText: String, amount: Double, userId: String) {
    val today = java.time.LocalDate.now().toString() // "YYYY-MM-DD"
    sendTransactionToBackend(userId, amount, smsText, today)
}
}
