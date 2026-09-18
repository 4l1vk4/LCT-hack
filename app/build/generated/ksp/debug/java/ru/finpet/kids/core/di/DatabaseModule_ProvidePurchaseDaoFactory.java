package ru.finpet.kids.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.data.local.AppDatabase;
import ru.finpet.kids.core.data.local.dao.PurchaseDao;

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
public final class DatabaseModule_ProvidePurchaseDaoFactory implements Factory<PurchaseDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvidePurchaseDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PurchaseDao get() {
    return providePurchaseDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePurchaseDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvidePurchaseDaoFactory(dbProvider);
  }

  public static PurchaseDao providePurchaseDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePurchaseDao(db));
  }
}
