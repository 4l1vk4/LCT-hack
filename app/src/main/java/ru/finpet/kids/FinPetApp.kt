package ru.finpet.kids

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.finpet.kids.core.data.local.AppDatabase
import ru.finpet.kids.core.data.repository.ContentRepository
import ru.finpet.kids.core.data.repository.SettingsRepository
import javax.inject.Inject

@HiltAndroidApp
class FinPetApp : Application() {

    // Lazy-обёртки: реальное создание БД/репозиториев произойдёт только
    // при .get() внутри фоновой корутины, а не на главном потоке.
    @Inject
    lateinit var database: dagger.Lazy<AppDatabase>

    @Inject
    lateinit var contentRepository: dagger.Lazy<ContentRepository>

    @Inject
    lateinit var settingsRepository: dagger.Lazy<SettingsRepository>

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Греем всё тяжёлое (Room, JSON контента, DataStore) в фоне, пока
        // система рисует Splash. К моменту создания MainActivity данные
        // уже в памяти — первый кадр не ждёт диск.
        appScope.launch {
            runCatching {
                // 1. Открываем БД заранее: schema-валидация и WAL вне главного потока
                database.get().openHelper.writableDatabase
            }
            runCatching {
                // 2. Парсим маленькие JSON и создаём стартовый профиль/цели
                contentRepository.get().preload()
            }
            runCatching {
                // 3. Первое чтение DataStore — самое дорогое, делаем его здесь
                settingsRepository.get().isSoundEnabled.first()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        contentRepository.get().trimMemoryCache()
    }
}
