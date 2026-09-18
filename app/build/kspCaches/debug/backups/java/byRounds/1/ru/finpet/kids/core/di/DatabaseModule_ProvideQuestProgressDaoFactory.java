package ru.finpet.kids.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.data.local.AppDatabase;
import ru.finpet.kids.core.data.local.dao.QuestProgressDao;

@ScopeMetadata
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
public final class DatabaseModule_ProvideQuestProgressDaoFactory implements Factory<QuestProgressDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideQuestProgressDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public QuestProgressDao get() {
    return provideQuestProgressDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideQuestProgressDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideQuestProgressDaoFactory(dbProvider);
  }

  public static QuestProgressDao provideQuestProgressDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideQuestProgressDao(db));
  }
}
