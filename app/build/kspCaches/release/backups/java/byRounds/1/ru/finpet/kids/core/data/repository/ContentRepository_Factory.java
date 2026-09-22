package ru.finpet.kids.core.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import ru.finpet.kids.core.domain.repository.FinPetRepository;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ContentRepository_Factory implements Factory<ContentRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<FinPetRepository> finPetRepositoryProvider;

  public ContentRepository_Factory(Provider<Context> contextProvider,
      Provider<FinPetRepository> finPetRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.finPetRepositoryProvider = finPetRepositoryProvider;
  }

  @Override
  public ContentRepository get() {
    return newInstance(contextProvider.get(), finPetRepositoryProvider.get());
  }

  public static ContentRepository_Factory create(Provider<Context> contextProvider,
      Provider<FinPetRepository> finPetRepositoryProvider) {
    return new ContentRepository_Factory(contextProvider, finPetRepositoryProvider);
  }

  public static ContentRepository newInstance(Context context, FinPetRepository finPetRepository) {
    return new ContentRepository(context, finPetRepository);
  }
}
