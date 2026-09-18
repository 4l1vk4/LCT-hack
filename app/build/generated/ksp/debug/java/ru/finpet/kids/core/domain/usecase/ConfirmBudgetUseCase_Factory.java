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
public final class ConfirmBudgetUseCase_Factory implements Factory<ConfirmBudgetUseCase> {
  private final Provider<FinPetRepository> repositoryProvider;

  public ConfirmBudgetUseCase_Factory(Provider<FinPetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ConfirmBudgetUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ConfirmBudgetUseCase_Factory create(Provider<FinPetRepository> repositoryProvider) {
    return new ConfirmBudgetUseCase_Factory(repositoryProvider);
  }

  public static ConfirmBudgetUseCase newInstance(FinPetRepository repository) {
    return new ConfirmBudgetUseCase(repository);
  }
}
