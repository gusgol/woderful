package me.goldhardt.woderful.di

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.goldhardt.woderful.data.local.DefaultWorkoutRepository
import me.goldhardt.woderful.data.local.WorkoutRepository
import me.goldhardt.woderful.domain.VibrateUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

    @Provides
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Singleton
    @Provides
    fun provideHealthServicesClient(@ApplicationContext context: Context): HealthServicesClient =
        HealthServices.getClient(context)

    @Singleton
    @Provides
    fun provideApplicationCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Singleton
    @Provides
    fun providesVibrator(@ApplicationContext context: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    @Singleton
    @Provides
    fun providerVibrationUseCase(vibrator: Vibrator): VibrateUseCase =
        VibrateUseCase(vibrator)
}

@Module
@InstallIn(SingletonComponent::class)
interface MainModuleBinds {

    @Singleton
    @Binds
    fun bindsWorkoutRepository(
        workoutRepository: DefaultWorkoutRepository
    ): WorkoutRepository
}