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
public final class CompletePeriodUseCase_Factory implements Factory<CompletePeriodUseCase> {
  private final Provider<FinPetRepository> repositoryProvider;

  public CompletePeriodUseCase_Factory(Provider<FinPetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CompletePeriodUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static CompletePeriodUseCase_Factory create(
      Provider<FinPetRepository> repositoryProvider) {
    return new CompletePeriodUseCase_Factory(repositoryProvider);
  }

  public static CompletePeriodUseCase newInstance(FinPetRepository repository) {
    return new CompletePeriodUseCase(repository);
  }
}
