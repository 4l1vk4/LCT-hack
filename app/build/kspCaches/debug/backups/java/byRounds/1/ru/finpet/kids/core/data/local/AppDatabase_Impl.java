package ru.finpet.kids.core.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import ru.finpet.kids.core.data.local.dao.GoalDao;
import ru.finpet.kids.core.data.local.dao.GoalDao_Impl;
import ru.finpet.kids.core.data.local.dao.PeriodDao;
import ru.finpet.kids.core.data.local.dao.PeriodDao_Impl;
import ru.finpet.kids.core.data.local.dao.ProfileDao;
import ru.finpet.kids.core.data.local.dao.ProfileDao_Impl;
import ru.finpet.kids.core.data.local.dao.PurchaseDao;
import ru.finpet.kids.core.data.local.dao.PurchaseDao_Impl;
import ru.finpet.kids.core.data.local.dao.QuestProgressDao;
import ru.finpet.kids.core.data.local.dao.QuestProgressDao_Impl;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ProfileDao _profileDao;

  private volatile PeriodDao _periodDao;

  private volatile PurchaseDao _purchaseDao;

  private volatile GoalDao _goalDao;

  private volatile QuestProgressDao _questProgressDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `profiles` (`id` TEXT NOT NULL, `petName` TEXT NOT NULL, `petType` TEXT NOT NULL, `bodyColor` INTEGER NOT NULL, `eyesType` INTEGER NOT NULL, `accessoryId` TEXT NOT NULL, `balance` INTEGER NOT NULL, `currentPeriodIndex` INTEGER NOT NULL, `carePoints` INTEGER NOT NULL, `growthStage` TEXT NOT NULL, `satiety` INTEGER NOT NULL, `health` INTEGER NOT NULL, `mood` INTEGER NOT NULL, `activeGoalId` TEXT, `isDemoMode` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `periods` (`periodIndex` INTEGER NOT NULL, `plannedMandatory` INTEGER NOT NULL, `plannedOptional` INTEGER NOT NULL, `plannedSavings` INTEGER NOT NULL, `actualMandatory` INTEGER NOT NULL, `actualOptional` INTEGER NOT NULL, `actualSavings` INTEGER NOT NULL, `isBudgetConfirmed` INTEGER NOT NULL, `isPeriodClosed` INTEGER NOT NULL, `compliancePercent` INTEGER NOT NULL, `carePointsEarned` INTEGER NOT NULL, PRIMARY KEY(`periodIndex`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `purchases` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `periodIndex` INTEGER NOT NULL, `itemId` TEXT NOT NULL, `itemName` TEXT NOT NULL, `category` TEXT NOT NULL, `price` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `targetCost` INTEGER NOT NULL, `savedAmount` INTEGER NOT NULL, `isReached` INTEGER NOT NULL, `iconName` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `quest_progress` (`questId` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `selectedOptionId` TEXT, `rewardClaimed` INTEGER NOT NULL, `completedInPeriod` INTEGER NOT NULL, PRIMARY KEY(`questId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b813d7192eef66cd6e3b844dcf172bfc')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `profiles`");
        db.execSQL("DROP TABLE IF EXISTS `periods`");
        db.execSQL("DROP TABLE IF EXISTS `purchases`");
        db.execSQL("DROP TABLE IF EXISTS `goals`");
        db.execSQL("DROP TABLE IF EXISTS `quest_progress`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsProfiles = new HashMap<String, TableInfo.Column>(15);
        _columnsProfiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("petName", new TableInfo.Column("petName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("petType", new TableInfo.Column("petType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("bodyColor", new TableInfo.Column("bodyColor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("eyesType", new TableInfo.Column("eyesType", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("accessoryId", new TableInfo.Column("accessoryId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("balance", new TableInfo.Column("balance", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("currentPeriodIndex", new TableInfo.Column("currentPeriodIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("carePoints", new TableInfo.Column("carePoints", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("growthStage", new TableInfo.Column("growthStage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("satiety", new TableInfo.Column("satiety", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("health", new TableInfo.Column("health", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("mood", new TableInfo.Column("mood", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("activeGoalId", new TableInfo.Column("activeGoalId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("isDemoMode", new TableInfo.Column("isDemoMode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProfiles = new TableInfo("profiles", _columnsProfiles, _foreignKeysProfiles, _indicesProfiles);
        final TableInfo _existingProfiles = TableInfo.read(db, "profiles");
        if (!_infoProfiles.equals(_existingProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "profiles(ru.finpet.kids.core.data.local.entity.ProfileEntity).\n"
                  + " Expected:\n" + _infoProfiles + "\n"
                  + " Found:\n" + _existingProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsPeriods = new HashMap<String, TableInfo.Column>(11);
        _columnsPeriods.put("periodIndex", new TableInfo.Column("periodIndex", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("plannedMandatory", new TableInfo.Column("plannedMandatory", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("plannedOptional", new TableInfo.Column("plannedOptional", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("plannedSavings", new TableInfo.Column("plannedSavings", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("actualMandatory", new TableInfo.Column("actualMandatory", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("actualOptional", new TableInfo.Column("actualOptional", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("actualSavings", new TableInfo.Column("actualSavings", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("isBudgetConfirmed", new TableInfo.Column("isBudgetConfirmed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("isPeriodClosed", new TableInfo.Column("isPeriodClosed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("compliancePercent", new TableInfo.Column("compliancePercent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeriods.put("carePointsEarned", new TableInfo.Column("carePointsEarned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPeriods = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPeriods = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPeriods = new TableInfo("periods", _columnsPeriods, _foreignKeysPeriods, _indicesPeriods);
        final TableInfo _existingPeriods = TableInfo.read(db, "periods");
        if (!_infoPeriods.equals(_existingPeriods)) {
          return new RoomOpenHelper.ValidationResult(false, "periods(ru.finpet.kids.core.data.local.entity.PeriodEntity).\n"
                  + " Expected:\n" + _infoPeriods + "\n"
                  + " Found:\n" + _existingPeriods);
        }
        final HashMap<String, TableInfo.Column> _columnsPurchases = new HashMap<String, TableInfo.Column>(7);
        _columnsPurchases.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("periodIndex", new TableInfo.Column("periodIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("itemId", new TableInfo.Column("itemId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("itemName", new TableInfo.Column("itemName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("price", new TableInfo.Column("price", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPurchases = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPurchases = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPurchases = new TableInfo("purchases", _columnsPurchases, _foreignKeysPurchases, _indicesPurchases);
        final TableInfo _existingPurchases = TableInfo.read(db, "purchases");
        if (!_infoPurchases.equals(_existingPurchases)) {
          return new RoomOpenHelper.ValidationResult(false, "purchases(ru.finpet.kids.core.data.local.entity.PurchaseEntity).\n"
                  + " Expected:\n" + _infoPurchases + "\n"
                  + " Found:\n" + _existingPurchases);
        }
        final HashMap<String, TableInfo.Column> _columnsGoals = new HashMap<String, TableInfo.Column>(6);
        _columnsGoals.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("targetCost", new TableInfo.Column("targetCost", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("savedAmount", new TableInfo.Column("savedAmount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("isReached", new TableInfo.Column("isReached", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("iconName", new TableInfo.Column("iconName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGoals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGoals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGoals = new TableInfo("goals", _columnsGoals, _foreignKeysGoals, _indicesGoals);
        final TableInfo _existingGoals = TableInfo.read(db, "goals");
        if (!_infoGoals.equals(_existingGoals)) {
          return new RoomOpenHelper.ValidationResult(false, "goals(ru.finpet.kids.core.data.local.entity.GoalEntity).\n"
                  + " Expected:\n" + _infoGoals + "\n"
                  + " Found:\n" + _existingGoals);
        }
        final HashMap<String, TableInfo.Column> _columnsQuestProgress = new HashMap<String, TableInfo.Column>(5);
        _columnsQuestProgress.put("questId", new TableInfo.Column("questId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuestProgress.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuestProgress.put("selectedOptionId", new TableInfo.Column("selectedOptionId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuestProgress.put("rewardClaimed", new TableInfo.Column("rewardClaimed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuestProgress.put("completedInPeriod", new TableInfo.Column("completedInPeriod", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQuestProgress = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesQuestProgress = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoQuestProgress = new TableInfo("quest_progress", _columnsQuestProgress, _foreignKeysQuestProgress, _indicesQuestProgress);
        final TableInfo _existingQuestProgress = TableInfo.read(db, "quest_progress");
        if (!_infoQuestProgress.equals(_existingQuestProgress)) {
          return new RoomOpenHelper.ValidationResult(false, "quest_progress(ru.finpet.kids.core.data.local.entity.QuestProgressEntity).\n"
                  + " Expected:\n" + _infoQuestProgress + "\n"
                  + " Found:\n" + _existingQuestProgress);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b813d7192eef66cd6e3b844dcf172bfc", "6cfb529baf871b9e2bb10b3f9f7d2975");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "profiles","periods","purchases","goals","quest_progress");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `profiles`");
      _db.execSQL("DELETE FROM `periods`");
      _db.execSQL("DELETE FROM `purchases`");
      _db.execSQL("DELETE FROM `goals`");
      _db.execSQL("DELETE FROM `quest_progress`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ProfileDao.class, ProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PeriodDao.class, PeriodDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PurchaseDao.class, PurchaseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GoalDao.class, GoalDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(QuestProgressDao.class, QuestProgressDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ProfileDao profileDao() {
    if (_profileDao != null) {
      return _profileDao;
    } else {
      synchronized(this) {
        if(_profileDao == null) {
          _profileDao = new ProfileDao_Impl(this);
        }
        return _profileDao;
      }
    }
  }

  @Override
  public PeriodDao periodDao() {
    if (_periodDao != null) {
      return _periodDao;
    } else {
      synchronized(this) {
        if(_periodDao == null) {
          _periodDao = new PeriodDao_Impl(this);
        }
        return _periodDao;
      }
    }
  }

  @Override
  public PurchaseDao purchaseDao() {
    if (_purchaseDao != null) {
      return _purchaseDao;
    } else {
      synchronized(this) {
        if(_purchaseDao == null) {
          _purchaseDao = new PurchaseDao_Impl(this);
        }
        return _purchaseDao;
      }
    }
  }

  @Override
  public GoalDao goalDao() {
    if (_goalDao != null) {
      return _goalDao;
    } else {
      synchronized(this) {
        if(_goalDao == null) {
          _goalDao = new GoalDao_Impl(this);
        }
        return _goalDao;
      }
    }
  }

  @Override
  public QuestProgressDao questProgressDao() {
    if (_questProgressDao != null) {
      return _questProgressDao;
    } else {
      synchronized(this) {
        if(_questProgressDao == null) {
          _questProgressDao = new QuestProgressDao_Impl(this);
        }
        return _questProgressDao;
      }
    }
  }
}
