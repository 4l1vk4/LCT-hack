package ru.finpet.kids.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.data.local.AppDatabase;
import ru.finpet.kids.core.data.local.dao.CalendarDao;

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
public final class DatabaseModule_ProvideCalendarDaoFactory implements Factory<CalendarDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideCalendarDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CalendarDao get() {
    return provideCalendarDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideCalendarDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideCalendarDaoFactory(dbProvider);
  }

  public static CalendarDao provideCalendarDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCalendarDao(db));
  }
}
