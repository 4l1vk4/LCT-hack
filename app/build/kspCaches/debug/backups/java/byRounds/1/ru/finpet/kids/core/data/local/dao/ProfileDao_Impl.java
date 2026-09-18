package ru.finpet.kids.core.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import ru.finpet.kids.core.data.local.entity.ProfileEntity;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProfileDao_Impl implements ProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProfileEntity> __insertionAdapterOfProfileEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateBalance;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStats;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public ProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProfileEntity = new EntityInsertionAdapter<ProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `profiles` (`id`,`petName`,`petType`,`bodyColor`,`eyesType`,`accessoryId`,`balance`,`currentPeriodIndex`,`carePoints`,`growthStage`,`satiety`,`health`,`mood`,`activeGoalId`,`isDemoMode`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProfileEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPetName());
        statement.bindString(3, entity.getPetType());
        statement.bindLong(4, entity.getBodyColor());
        statement.bindLong(5, entity.getEyesType());
        statement.bindString(6, entity.getAccessoryId());
        statement.bindLong(7, entity.getBalance());
        statement.bindLong(8, entity.getCurrentPeriodIndex());
        statement.bindLong(9, entity.getCarePoints());
        statement.bindString(10, entity.getGrowthStage());
        statement.bindLong(11, entity.getSatiety());
        statement.bindLong(12, entity.getHealth());
        statement.bindLong(13, entity.getMood());
        if (entity.getActiveGoalId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getActiveGoalId());
        }
        final int _tmp = entity.isDemoMode() ? 1 : 0;
        statement.bindLong(15, _tmp);
      }
    };
    this.__preparedStmtOfUpdateBalance = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE profiles SET balance = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateStats = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE profiles \n"
                + "        SET satiety = ?, health = ?, mood = ?, carePoints = ?, growthStage = ?\n"
                + "        WHERE id = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM profiles";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final ProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProfileEntity.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBalance(final int newBalance, final String id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateBalance.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, newBalance);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateBalance.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStats(final int satiety, final int health, final int mood,
      final int carePoints, final String growthStage, final String id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStats.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, satiety);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, health);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, mood);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, carePoints);
        _argIndex = 5;
        _stmt.bindString(_argIndex, growthStage);
        _argIndex = 6;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateStats.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<ProfileEntity> getProfile(final String id) {
    final String _sql = "SELECT * FROM profiles WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"profiles"}, new Callable<ProfileEntity>() {
      @Override
      @Nullable
      public ProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPetName = CursorUtil.getColumnIndexOrThrow(_cursor, "petName");
          final int _cursorIndexOfPetType = CursorUtil.getColumnIndexOrThrow(_cursor, "petType");
          final int _cursorIndexOfBodyColor = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyColor");
          final int _cursorIndexOfEyesType = CursorUtil.getColumnIndexOrThrow(_cursor, "eyesType");
          final int _cursorIndexOfAccessoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "accessoryId");
          final int _cursorIndexOfBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "balance");
          final int _cursorIndexOfCurrentPeriodIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPeriodIndex");
          final int _cursorIndexOfCarePoints = CursorUtil.getColumnIndexOrThrow(_cursor, "carePoints");
          final int _cursorIndexOfGrowthStage = CursorUtil.getColumnIndexOrThrow(_cursor, "growthStage");
          final int _cursorIndexOfSatiety = CursorUtil.getColumnIndexOrThrow(_cursor, "satiety");
          final int _cursorIndexOfHealth = CursorUtil.getColumnIndexOrThrow(_cursor, "health");
          final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
          final int _cursorIndexOfActiveGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "activeGoalId");
          final int _cursorIndexOfIsDemoMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDemoMode");
          final ProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPetName;
            _tmpPetName = _cursor.getString(_cursorIndexOfPetName);
            final String _tmpPetType;
            _tmpPetType = _cursor.getString(_cursorIndexOfPetType);
            final int _tmpBodyColor;
            _tmpBodyColor = _cursor.getInt(_cursorIndexOfBodyColor);
            final int _tmpEyesType;
            _tmpEyesType = _cursor.getInt(_cursorIndexOfEyesType);
            final String _tmpAccessoryId;
            _tmpAccessoryId = _cursor.getString(_cursorIndexOfAccessoryId);
            final int _tmpBalance;
            _tmpBalance = _cursor.getInt(_cursorIndexOfBalance);
            final int _tmpCurrentPeriodIndex;
            _tmpCurrentPeriodIndex = _cursor.getInt(_cursorIndexOfCurrentPeriodIndex);
            final int _tmpCarePoints;
            _tmpCarePoints = _cursor.getInt(_cursorIndexOfCarePoints);
            final String _tmpGrowthStage;
            _tmpGrowthStage = _cursor.getString(_cursorIndexOfGrowthStage);
            final int _tmpSatiety;
            _tmpSatiety = _cursor.getInt(_cursorIndexOfSatiety);
            final int _tmpHealth;
            _tmpHealth = _cursor.getInt(_cursorIndexOfHealth);
            final int _tmpMood;
            _tmpMood = _cursor.getInt(_cursorIndexOfMood);
            final String _tmpActiveGoalId;
            if (_cursor.isNull(_cursorIndexOfActiveGoalId)) {
              _tmpActiveGoalId = null;
            } else {
              _tmpActiveGoalId = _cursor.getString(_cursorIndexOfActiveGoalId);
            }
            final boolean _tmpIsDemoMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDemoMode);
            _tmpIsDemoMode = _tmp != 0;
            _result = new ProfileEntity(_tmpId,_tmpPetName,_tmpPetType,_tmpBodyColor,_tmpEyesType,_tmpAccessoryId,_tmpBalance,_tmpCurrentPeriodIndex,_tmpCarePoints,_tmpGrowthStage,_tmpSatiety,_tmpHealth,_tmpMood,_tmpActiveGoalId,_tmpIsDemoMode);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getProfileSync(final String id,
      final Continuation<? super ProfileEntity> $completion) {
    final String _sql = "SELECT * FROM profiles WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProfileEntity>() {
      @Override
      @Nullable
      public ProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPetName = CursorUtil.getColumnIndexOrThrow(_cursor, "petName");
          final int _cursorIndexOfPetType = CursorUtil.getColumnIndexOrThrow(_cursor, "petType");
          final int _cursorIndexOfBodyColor = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyColor");
          final int _cursorIndexOfEyesType = CursorUtil.getColumnIndexOrThrow(_cursor, "eyesType");
          final int _cursorIndexOfAccessoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "accessoryId");
          final int _cursorIndexOfBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "balance");
          final int _cursorIndexOfCurrentPeriodIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPeriodIndex");
          final int _cursorIndexOfCarePoints = CursorUtil.getColumnIndexOrThrow(_cursor, "carePoints");
          final int _cursorIndexOfGrowthStage = CursorUtil.getColumnIndexOrThrow(_cursor, "growthStage");
          final int _cursorIndexOfSatiety = CursorUtil.getColumnIndexOrThrow(_cursor, "satiety");
          final int _cursorIndexOfHealth = CursorUtil.getColumnIndexOrThrow(_cursor, "health");
          final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
          final int _cursorIndexOfActiveGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "activeGoalId");
          final int _cursorIndexOfIsDemoMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDemoMode");
          final ProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPetName;
            _tmpPetName = _cursor.getString(_cursorIndexOfPetName);
            final String _tmpPetType;
            _tmpPetType = _cursor.getString(_cursorIndexOfPetType);
            final int _tmpBodyColor;
            _tmpBodyColor = _cursor.getInt(_cursorIndexOfBodyColor);
            final int _tmpEyesType;
            _tmpEyesType = _cursor.getInt(_cursorIndexOfEyesType);
            final String _tmpAccessoryId;
            _tmpAccessoryId = _cursor.getString(_cursorIndexOfAccessoryId);
            final int _tmpBalance;
            _tmpBalance = _cursor.getInt(_cursorIndexOfBalance);
            final int _tmpCurrentPeriodIndex;
            _tmpCurrentPeriodIndex = _cursor.getInt(_cursorIndexOfCurrentPeriodIndex);
            final int _tmpCarePoints;
            _tmpCarePoints = _cursor.getInt(_cursorIndexOfCarePoints);
            final String _tmpGrowthStage;
            _tmpGrowthStage = _cursor.getString(_cursorIndexOfGrowthStage);
            final int _tmpSatiety;
            _tmpSatiety = _cursor.getInt(_cursorIndexOfSatiety);
            final int _tmpHealth;
            _tmpHealth = _cursor.getInt(_cursorIndexOfHealth);
            final int _tmpMood;
            _tmpMood = _cursor.getInt(_cursorIndexOfMood);
            final String _tmpActiveGoalId;
            if (_cursor.isNull(_cursorIndexOfActiveGoalId)) {
              _tmpActiveGoalId = null;
            } else {
              _tmpActiveGoalId = _cursor.getString(_cursorIndexOfActiveGoalId);
            }
            final boolean _tmpIsDemoMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDemoMode);
            _tmpIsDemoMode = _tmp != 0;
            _result = new ProfileEntity(_tmpId,_tmpPetName,_tmpPetType,_tmpBodyColor,_tmpEyesType,_tmpAccessoryId,_tmpBalance,_tmpCurrentPeriodIndex,_tmpCarePoints,_tmpGrowthStage,_tmpSatiety,_tmpHealth,_tmpMood,_tmpActiveGoalId,_tmpIsDemoMode);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
