package ru.finpet.kids.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.data.local.AppDatabase;
import ru.finpet.kids.core.data.local.dao.PeriodDao;

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
public final class DatabaseModule_ProvidePeriodDaoFactory implements Factory<PeriodDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvidePeriodDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PeriodDao get() {
    return providePeriodDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePeriodDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvidePeriodDaoFactory(dbProvider);
  }

  public static PeriodDao providePeriodDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePeriodDao(db));
  }
}
