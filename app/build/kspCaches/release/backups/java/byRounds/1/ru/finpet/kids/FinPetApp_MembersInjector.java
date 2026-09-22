package ru.finpet.kids;

import dagger.Lazy;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.data.local.AppDatabase;
import ru.finpet.kids.core.data.repository.ContentRepository;
import ru.finpet.kids.core.data.repository.SettingsRepository;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class FinPetApp_MembersInjector implements MembersInjector<FinPetApp> {
  private final Provider<AppDatabase> databaseProvider;

  private final Provider<ContentRepository> contentRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public FinPetApp_MembersInjector(Provider<AppDatabase> databaseProvider,
      Provider<ContentRepository> contentRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.databaseProvider = databaseProvider;
    this.contentRepositoryProvider = contentRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public static MembersInjector<FinPetApp> create(Provider<AppDatabase> databaseProvider,
      Provider<ContentRepository> contentRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new FinPetApp_MembersInjector(databaseProvider, contentRepositoryProvider, settingsRepositoryProvider);
  }

  @Override
  public void injectMembers(FinPetApp instance) {
    injectDatabase(instance, DoubleCheck.lazy(databaseProvider));
    injectContentRepository(instance, DoubleCheck.lazy(contentRepositoryProvider));
    injectSettingsRepository(instance, DoubleCheck.lazy(settingsRepositoryProvider));
  }

  @InjectedFieldSignature("ru.finpet.kids.FinPetApp.database")
  public static void injectDatabase(FinPetApp instance, Lazy<AppDatabase> database) {
    instance.database = database;
  }

  @InjectedFieldSignature("ru.finpet.kids.FinPetApp.contentRepository")
  public static void injectContentRepository(FinPetApp instance,
      Lazy<ContentRepository> contentRepository) {
    instance.contentRepository = contentRepository;
  }

  @InjectedFieldSignature("ru.finpet.kids.FinPetApp.settingsRepository")
  public static void injectSettingsRepository(FinPetApp instance,
      Lazy<SettingsRepository> settingsRepository) {
    instance.settingsRepository = settingsRepository;
  }
}
