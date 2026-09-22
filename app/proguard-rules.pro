# Proguard rules for FinPet
-keepattributes *Annotation*
-keepattributes Signature, InnerClasses, EnclosingMethod

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# kotlinx.serialization — не обфусцировать сериализуемые модели контента
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keep class kotlinx.serialization.** { *; }
-keep class ru.finpet.kids.core.data.repository.** { *; }

# DataStore / Preferences
-keep class androidx.datastore.** { *; }
