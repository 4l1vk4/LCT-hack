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
public final class AdultSectionUseCase_Factory implements Factory<AdultSectionUseCase> {
  private final Provider<FinPetRepository> repositoryProvider;

  public AdultSectionUseCase_Factory(Provider<FinPetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AdultSectionUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AdultSectionUseCase_Factory create(Provider<FinPetRepository> repositoryProvider) {
    return new AdultSectionUseCase_Factory(repositoryProvider);
  }

  public static AdultSectionUseCase newInstance(FinPetRepository repository) {
    return new AdultSectionUseCase(repository);
  }
}
