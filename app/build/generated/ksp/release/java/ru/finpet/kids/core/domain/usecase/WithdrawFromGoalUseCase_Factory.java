package ru.finpet.kids.core.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.domain.repository.FinPetRepository;

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
public final class WithdrawFromGoalUseCase_Factory implements Factory<WithdrawFromGoalUseCase> {
  private final Provider<FinPetRepository> repositoryProvider;

  public WithdrawFromGoalUseCase_Factory(Provider<FinPetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public WithdrawFromGoalUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static WithdrawFromGoalUseCase_Factory create(
      Provider<FinPetRepository> repositoryProvider) {
    return new WithdrawFromGoalUseCase_Factory(repositoryProvider);
  }

  public static WithdrawFromGoalUseCase newInstance(FinPetRepository repository) {
    return new WithdrawFromGoalUseCase(repository);
  }
}
