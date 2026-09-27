package ru.finpet.kids.core.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.data.local.AppDatabase;
import ru.finpet.kids.core.data.local.dao.GoalDao;
import ru.finpet.kids.core.data.local.dao.PeriodDao;
import ru.finpet.kids.core.data.local.dao.ProfileDao;
import ru.finpet.kids.core.data.local.dao.PurchaseDao;
import ru.finpet.kids.core.data.local.dao.QuestProgressDao;

@ScopeMetadata("javax.inject.Singleton")
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
public final class FinPetRepositoryImpl_Factory implements Factory<FinPetRepositoryImpl> {
  private final Provider<AppDatabase> databaseProvider;

  private final Provider<ProfileDao> profileDaoProvider;

  private final Provider<PeriodDao> periodDaoProvider;

  private final Provider<PurchaseDao> purchaseDaoProvider;

  private final Provider<GoalDao> goalDaoProvider;

  private final Provider<QuestProgressDao> questProgressDaoProvider;

  public FinPetRepositoryImpl_Factory(Provider<AppDatabase> databaseProvider,
      Provider<ProfileDao> profileDaoProvider, Provider<PeriodDao> periodDaoProvider,
      Provider<PurchaseDao> purchaseDaoProvider, Provider<GoalDao> goalDaoProvider,
      Provider<QuestProgressDao> questProgressDaoProvider) {
    this.databaseProvider = databaseProvider;
    this.profileDaoProvider = profileDaoProvider;
    this.periodDaoProvider = periodDaoProvider;
    this.purchaseDaoProvider = purchaseDaoProvider;
    this.goalDaoProvider = goalDaoProvider;
    this.questProgressDaoProvider = questProgressDaoProvider;
  }

  @Override
  public FinPetRepositoryImpl get() {
    return newInstance(databaseProvider.get(), profileDaoProvider.get(), periodDaoProvider.get(), purchaseDaoProvider.get(), goalDaoProvider.get(), questProgressDaoProvider.get());
  }

  public static FinPetRepositoryImpl_Factory create(Provider<AppDatabase> databaseProvider,
      Provider<ProfileDao> profileDaoProvider, Provider<PeriodDao> periodDaoProvider,
      Provider<PurchaseDao> purchaseDaoProvider, Provider<GoalDao> goalDaoProvider,
      Provider<QuestProgressDao> questProgressDaoProvider) {
    return new FinPetRepositoryImpl_Factory(databaseProvider, profileDaoProvider, periodDaoProvider, purchaseDaoProvider, goalDaoProvider, questProgressDaoProvider);
  }

  public static FinPetRepositoryImpl newInstance(AppDatabase database, ProfileDao profileDao,
      PeriodDao periodDao, PurchaseDao purchaseDao, GoalDao goalDao,
      QuestProgressDao questProgressDao) {
    return new FinPetRepositoryImpl(database, profileDao, periodDao, purchaseDao, goalDao, questProgressDao);
  }
}
