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
public final class DepositToGoalUseCase_Factory implements Factory<DepositToGoalUseCase> {
  private final Provider<FinPetRepository> repositoryProvider;

  public DepositToGoalUseCase_Factory(Provider<FinPetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DepositToGoalUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DepositToGoalUseCase_Factory create(Provider<FinPetRepository> repositoryProvider) {
    return new DepositToGoalUseCase_Factory(repositoryProvider);
  }

  public static DepositToGoalUseCase newInstance(FinPetRepository repository) {
    return new DepositToGoalUseCase(repository);
  }
}
