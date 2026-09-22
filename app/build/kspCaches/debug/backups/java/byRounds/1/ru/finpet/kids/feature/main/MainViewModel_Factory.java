package ru.finpet.kids.feature.main;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.data.local.dao.CalendarDao;
import ru.finpet.kids.core.data.repository.ContentRepository;
import ru.finpet.kids.core.data.repository.SettingsRepository;
import ru.finpet.kids.core.domain.repository.FinPetRepository;
import ru.finpet.kids.core.domain.usecase.AdultSectionUseCase;
import ru.finpet.kids.core.domain.usecase.CompetencyTracker;
import ru.finpet.kids.core.domain.usecase.CompletePeriodUseCase;
import ru.finpet.kids.core.domain.usecase.ConfirmBudgetUseCase;
import ru.finpet.kids.core.domain.usecase.DepositToGoalUseCase;
import ru.finpet.kids.core.domain.usecase.MakePurchaseUseCase;
import ru.finpet.kids.core.domain.usecase.QuestEngineUseCase;
import ru.finpet.kids.core.domain.usecase.ResetDemoProfileUseCase;
import ru.finpet.kids.core.domain.usecase.WithdrawFromGoalUseCase;

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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<FinPetRepository> repositoryProvider;

  private final Provider<ContentRepository> contentRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<CalendarDao> calendarDaoProvider;

  private final Provider<ConfirmBudgetUseCase> confirmBudgetUseCaseProvider;

  private final Provider<MakePurchaseUseCase> makePurchaseUseCaseProvider;

  private final Provider<DepositToGoalUseCase> depositToGoalUseCaseProvider;

  private final Provider<CompletePeriodUseCase> completePeriodUseCaseProvider;

  private final Provider<WithdrawFromGoalUseCase> withdrawFromGoalUseCaseProvider;

  private final Provider<QuestEngineUseCase> questEngineUseCaseProvider;

  private final Provider<AdultSectionUseCase> adultSectionUseCaseProvider;

  private final Provider<CompetencyTracker> competencyTrackerProvider;

  private final Provider<ResetDemoProfileUseCase> resetDemoProfileUseCaseProvider;

  public MainViewModel_Factory(Provider<FinPetRepository> repositoryProvider,
      Provider<ContentRepository> contentRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<CalendarDao> calendarDaoProvider,
      Provider<ConfirmBudgetUseCase> confirmBudgetUseCaseProvider,
      Provider<MakePurchaseUseCase> makePurchaseUseCaseProvider,
      Provider<DepositToGoalUseCase> depositToGoalUseCaseProvider,
      Provider<CompletePeriodUseCase> completePeriodUseCaseProvider,
      Provider<WithdrawFromGoalUseCase> withdrawFromGoalUseCaseProvider,
      Provider<QuestEngineUseCase> questEngineUseCaseProvider,
      Provider<AdultSectionUseCase> adultSectionUseCaseProvider,
      Provider<CompetencyTracker> competencyTrackerProvider,
      Provider<ResetDemoProfileUseCase> resetDemoProfileUseCaseProvider) {
    this.repositoryProvider = repositoryProvider;
    this.contentRepositoryProvider = contentRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.calendarDaoProvider = calendarDaoProvider;
    this.confirmBudgetUseCaseProvider = confirmBudgetUseCaseProvider;
    this.makePurchaseUseCaseProvider = makePurchaseUseCaseProvider;
    this.depositToGoalUseCaseProvider = depositToGoalUseCaseProvider;
    this.completePeriodUseCaseProvider = completePeriodUseCaseProvider;
    this.withdrawFromGoalUseCaseProvider = withdrawFromGoalUseCaseProvider;
    this.questEngineUseCaseProvider = questEngineUseCaseProvider;
    this.adultSectionUseCaseProvider = adultSectionUseCaseProvider;
    this.competencyTrackerProvider = competencyTrackerProvider;
    this.resetDemoProfileUseCaseProvider = resetDemoProfileUseCaseProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(repositoryProvider.get(), contentRepositoryProvider.get(), settingsRepositoryProvider.get(), calendarDaoProvider.get(), confirmBudgetUseCaseProvider.get(), makePurchaseUseCaseProvider.get(), depositToGoalUseCaseProvider.get(), completePeriodUseCaseProvider.get(), withdrawFromGoalUseCaseProvider.get(), questEngineUseCaseProvider.get(), adultSectionUseCaseProvider.get(), competencyTrackerProvider.get(), resetDemoProfileUseCaseProvider.get());
  }

  public static MainViewModel_Factory create(Provider<FinPetRepository> repositoryProvider,
      Provider<ContentRepository> contentRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<CalendarDao> calendarDaoProvider,
      Provider<ConfirmBudgetUseCase> confirmBudgetUseCaseProvider,
      Provider<MakePurchaseUseCase> makePurchaseUseCaseProvider,
      Provider<DepositToGoalUseCase> depositToGoalUseCaseProvider,
      Provider<CompletePeriodUseCase> completePeriodUseCaseProvider,
      Provider<WithdrawFromGoalUseCase> withdrawFromGoalUseCaseProvider,
      Provider<QuestEngineUseCase> questEngineUseCaseProvider,
      Provider<AdultSectionUseCase> adultSectionUseCaseProvider,
      Provider<CompetencyTracker> competencyTrackerProvider,
      Provider<ResetDemoProfileUseCase> resetDemoProfileUseCaseProvider) {
    return new MainViewModel_Factory(repositoryProvider, contentRepositoryProvider, settingsRepositoryProvider, calendarDaoProvider, confirmBudgetUseCaseProvider, makePurchaseUseCaseProvider, depositToGoalUseCaseProvider, completePeriodUseCaseProvider, withdrawFromGoalUseCaseProvider, questEngineUseCaseProvider, adultSectionUseCaseProvider, competencyTrackerProvider, resetDemoProfileUseCaseProvider);
  }

  public static MainViewModel newInstance(FinPetRepository repository,
      ContentRepository contentRepository, SettingsRepository settingsRepository,
      CalendarDao calendarDao, ConfirmBudgetUseCase confirmBudgetUseCase,
      MakePurchaseUseCase makePurchaseUseCase, DepositToGoalUseCase depositToGoalUseCase,
      CompletePeriodUseCase completePeriodUseCase, WithdrawFromGoalUseCase withdrawFromGoalUseCase,
      QuestEngineUseCase questEngineUseCase, AdultSectionUseCase adultSectionUseCase,
      CompetencyTracker competencyTracker, ResetDemoProfileUseCase resetDemoProfileUseCase) {
    return new MainViewModel(repository, contentRepository, settingsRepository, calendarDao, confirmBudgetUseCase, makePurchaseUseCase, depositToGoalUseCase, completePeriodUseCase, withdrawFromGoalUseCase, questEngineUseCase, adultSectionUseCase, competencyTracker, resetDemoProfileUseCase);
  }
}
