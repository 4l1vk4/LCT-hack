package ru.finpet.kids.core.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.data.repository.ContentRepository;
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
public final class ResetDemoProfileUseCase_Factory implements Factory<ResetDemoProfileUseCase> {
  private final Provider<FinPetRepository> repositoryProvider;

  private final Provider<ContentRepository> contentRepositoryProvider;

  public ResetDemoProfileUseCase_Factory(Provider<FinPetRepository> repositoryProvider,
      Provider<ContentRepository> contentRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.contentRepositoryProvider = contentRepositoryProvider;
  }

  @Override
  public ResetDemoProfileUseCase get() {
    return newInstance(repositoryProvider.get(), contentRepositoryProvider.get());
  }

  public static ResetDemoProfileUseCase_Factory create(
      Provider<FinPetRepository> repositoryProvider,
      Provider<ContentRepository> contentRepositoryProvider) {
    return new ResetDemoProfileUseCase_Factory(repositoryProvider, contentRepositoryProvider);
  }

  public static ResetDemoProfileUseCase newInstance(FinPetRepository repository,
      ContentRepository contentRepository) {
    return new ResetDemoProfileUseCase(repository, contentRepository);
  }
}
