package com.example.financeapp.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.financeapp.api.ApiService
import com.example.financeapp.auth.ITokenStore
import com.example.financeapp.data.dao.RawTransactionDao
import com.example.financeapp.data.model.RawSmsIn
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException

@HiltWorker
class RawSmsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    private val rawTransactionDao: RawTransactionDao,
    private val tokenStore: ITokenStore
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val pending = rawTransactionDao.getUnsentRawTransactions()
            if (pending.isEmpty()) return Result.success()

            val token = tokenStore.getToken().trim()
            if (token.isEmpty()) {
                Log.e("RawSmsSyncWorker", "No token found, aborting sync")
                return Result.retry()
            }

            pending.forEach { entity ->
                try {
                    val requestBody = RawSmsIn(
                        smsText = entity.rawText,
                        senderId = entity.sender,
                        timestamp = entity.smsTimestamp
                    )

                    // FIXED: API expects only (token, RawSmsIn)
                    val response = apiService.ingestRawSms(token, requestBody)

                    if (response.isSuccessful) {
                        rawTransactionDao.updateIngestionStatus(entity.id, "SENT")
                        Log.d("RawSmsSyncWorker", "Synced SMS ${entity.id} successfully.")
                    } else {
                        rawTransactionDao.updateIngestionStatus(entity.id, "FAILED")
                        Log.e(
                            "RawSmsSyncWorker",
                            "Backend error for SMS ${entity.id}: ${response.code()}"
                        )
                    }

                } catch (e: HttpException) {
                    rawTransactionDao.updateIngestionStatus(entity.id, "FAILED")
                    Log.e("RawSmsSyncWorker", "HTTP error for SMS ${entity.id}", e)

                } catch (e: Exception) {
                    rawTransactionDao.updateIngestionStatus(entity.id, "FAILED")
                    Log.e("RawSmsSyncWorker", "Unexpected error for SMS ${entity.id}", e)
                }
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("RawSmsSyncWorker", "Worker-level failure: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        fun enqueueSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val req = OneTimeWorkRequestBuilder<RawSmsSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(req)
        }
    }
}
