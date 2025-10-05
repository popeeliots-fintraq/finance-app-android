import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CategorizeApi {
    @POST(".") // Root endpoint of your Cloud Run URL
    fun categorizeTransaction(
        @Body request: CategorizeRequest
    ): Call<CategorizeResponse>
}
