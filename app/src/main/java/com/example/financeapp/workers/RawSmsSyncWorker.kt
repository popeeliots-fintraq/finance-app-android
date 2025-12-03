package com.example.financeapp.workers

import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.NetworkType
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financeapp.api.ApiService
import com.example.financeapp.data.dao.RawTransactionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import javax.inject.Inject

@HiltWorker
class RawSmsSyncWorker @AssistedInject constructor(
    @Assisted appContext: android.content.Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    private val rawTransactionDao: RawTransactionDao
): CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            val pending = rawTransactionDao.getUnsentRawTransactions()
            if (pending.isEmpty()) return Result.success()

            pending.forEach { entity ->
                try {
                    // map entity -> RawSmsIn model expected by backend
                    val rawSmsIn = com.example.financeapp.data.model.RawSmsIn(
                        userId = entity.userId,
                        sender = entity.sender,
                        body = entity.rawText,
                        smsTimestamp = entity.smsTimestamp,
                        uniqueSmsId = entity.uniqueSmsId
                    )

                    // NOTE: provide a secure token via some AuthStore
                    val token = "Bearer ${/* get token securely */ ""}"

                    val resp = apiService.ingestRawSms(token, rawSmsIn)
                    if (resp.isSuccessful) {
                        // mark as SENT (or backend id)
                        rawTransactionDao.updateIngestionStatus(entity.id, "SENT")
                    } else {
                        rawTransactionDao.updateIngestionStatus(entity.id, "FAILED")
                    }
                } catch (e: HttpException) {
                    rawTransactionDao.updateIngestionStatus(entity.id, "FAILED")
                } catch (e: Exception) {
                    rawTransactionDao.updateIngestionStatus(entity.id, "FAILED")
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("RawSmsSyncWorker", "Sync failed: ${e.message}", e)
            return Result.retry()
        }
    }

    companion object {
        fun enqueueSync(context: android.content.Context) {
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
