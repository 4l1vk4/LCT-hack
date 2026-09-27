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
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class QuestProgressDao_Impl implements QuestProgressDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<QuestProgressEntity> __insertionAdapterOfQuestProgressEntity;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public QuestProgressDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfQuestProgressEntity = new EntityInsertionAdapter<QuestProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `quest_progress` (`questId`,`isCompleted`,`selectedOptionId`,`rewardClaimed`,`completedInPeriod`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QuestProgressEntity entity) {
        statement.bindString(1, entity.getQuestId());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(2, _tmp);
        if (entity.getSelectedOptionId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSelectedOptionId());
        }
        final int _tmp_1 = entity.getRewardClaimed() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        statement.bindLong(5, entity.getCompletedInPeriod());
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM quest_progress";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final QuestProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfQuestProgressEntity.insert(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Flow<List<QuestProgressEntity>> getAllProgress() {
    final String _sql = "SELECT * FROM quest_progress";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"quest_progress"}, new Callable<List<QuestProgressEntity>>() {
      @Override
      @NonNull
      public List<QuestProgressEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfQuestId = CursorUtil.getColumnIndexOrThrow(_cursor, "questId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfSelectedOptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedOptionId");
          final int _cursorIndexOfRewardClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "rewardClaimed");
          final int _cursorIndexOfCompletedInPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "completedInPeriod");
          final List<QuestProgressEntity> _result = new ArrayList<QuestProgressEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QuestProgressEntity _item;
            final String _tmpQuestId;
            _tmpQuestId = _cursor.getString(_cursorIndexOfQuestId);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final String _tmpSelectedOptionId;
            if (_cursor.isNull(_cursorIndexOfSelectedOptionId)) {
              _tmpSelectedOptionId = null;
            } else {
              _tmpSelectedOptionId = _cursor.getString(_cursorIndexOfSelectedOptionId);
            }
            final boolean _tmpRewardClaimed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfRewardClaimed);
            _tmpRewardClaimed = _tmp_1 != 0;
            final int _tmpCompletedInPeriod;
            _tmpCompletedInPeriod = _cursor.getInt(_cursorIndexOfCompletedInPeriod);
            _item = new QuestProgressEntity(_tmpQuestId,_tmpIsCompleted,_tmpSelectedOptionId,_tmpRewardClaimed,_tmpCompletedInPeriod);
            _result.add(_item);
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
  public Flow<QuestProgressEntity> getProgress(final String questId) {
    final String _sql = "SELECT * FROM quest_progress WHERE questId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, questId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"quest_progress"}, new Callable<QuestProgressEntity>() {
      @Override
      @Nullable
      public QuestProgressEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfQuestId = CursorUtil.getColumnIndexOrThrow(_cursor, "questId");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfSelectedOptionId = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedOptionId");
          final int _cursorIndexOfRewardClaimed = CursorUtil.getColumnIndexOrThrow(_cursor, "rewardClaimed");
          final int _cursorIndexOfCompletedInPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "completedInPeriod");
          final QuestProgressEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpQuestId;
            _tmpQuestId = _cursor.getString(_cursorIndexOfQuestId);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final String _tmpSelectedOptionId;
            if (_cursor.isNull(_cursorIndexOfSelectedOptionId)) {
              _tmpSelectedOptionId = null;
            } else {
              _tmpSelectedOptionId = _cursor.getString(_cursorIndexOfSelectedOptionId);
            }
            final boolean _tmpRewardClaimed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfRewardClaimed);
            _tmpRewardClaimed = _tmp_1 != 0;
            final int _tmpCompletedInPeriod;
            _tmpCompletedInPeriod = _cursor.getInt(_cursorIndexOfCompletedInPeriod);
            _result = new QuestProgressEntity(_tmpQuestId,_tmpIsCompleted,_tmpSelectedOptionId,_tmpRewardClaimed,_tmpCompletedInPeriod);
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
  public Object getCompletedCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM quest_progress WHERE isCompleted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
