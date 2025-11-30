@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFinanceRepository(
        apiService: ApiService,
        smsDao: SmsDao
    ): FinanceRepository {
        return FinanceRepository(
            apiService = apiService,
            smsDao = smsDao
            // remove transactionDao, categoryDao, budgetDao until they exist
        )
    }
}
