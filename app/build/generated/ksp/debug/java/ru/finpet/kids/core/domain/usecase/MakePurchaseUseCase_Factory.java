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
public final class MakePurchaseUseCase_Factory implements Factory<MakePurchaseUseCase> {
  private final Provider<FinPetRepository> repositoryProvider;

  public MakePurchaseUseCase_Factory(Provider<FinPetRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public MakePurchaseUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static MakePurchaseUseCase_Factory create(Provider<FinPetRepository> repositoryProvider) {
    return new MakePurchaseUseCase_Factory(repositoryProvider);
  }

  public static MakePurchaseUseCase newInstance(FinPetRepository repository) {
    return new MakePurchaseUseCase(repository);
  }
}
