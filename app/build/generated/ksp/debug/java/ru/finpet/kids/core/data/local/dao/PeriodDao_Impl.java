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
import java.lang.Double;
import java.lang.Exception;
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
import ru.finpet.kids.core.data.local.entity.PeriodEntity;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PeriodDao_Impl implements PeriodDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PeriodEntity> __insertionAdapterOfPeriodEntity;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public PeriodDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPeriodEntity = new EntityInsertionAdapter<PeriodEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `periods` (`periodIndex`,`plannedMandatory`,`plannedOptional`,`plannedSavings`,`actualMandatory`,`actualOptional`,`actualSavings`,`isBudgetConfirmed`,`isPeriodClosed`,`compliancePercent`,`carePointsEarned`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PeriodEntity entity) {
        statement.bindLong(1, entity.getPeriodIndex());
        statement.bindLong(2, entity.getPlannedMandatory());
        statement.bindLong(3, entity.getPlannedOptional());
        statement.bindLong(4, entity.getPlannedSavings());
        statement.bindLong(5, entity.getActualMandatory());
        statement.bindLong(6, entity.getActualOptional());
        statement.bindLong(7, entity.getActualSavings());
        final int _tmp = entity.isBudgetConfirmed() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.isPeriodClosed() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getCompliancePercent());
        statement.bindLong(11, entity.getCarePointsEarned());
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM periods";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final PeriodEntity period,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPeriodEntity.insert(period);
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
  public Flow<PeriodEntity> getPeriod(final int index) {
    final String _sql = "SELECT * FROM periods WHERE periodIndex = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, index);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"periods"}, new Callable<PeriodEntity>() {
      @Override
      @Nullable
      public PeriodEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPeriodIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "periodIndex");
          final int _cursorIndexOfPlannedMandatory = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedMandatory");
          final int _cursorIndexOfPlannedOptional = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedOptional");
          final int _cursorIndexOfPlannedSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedSavings");
          final int _cursorIndexOfActualMandatory = CursorUtil.getColumnIndexOrThrow(_cursor, "actualMandatory");
          final int _cursorIndexOfActualOptional = CursorUtil.getColumnIndexOrThrow(_cursor, "actualOptional");
          final int _cursorIndexOfActualSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "actualSavings");
          final int _cursorIndexOfIsBudgetConfirmed = CursorUtil.getColumnIndexOrThrow(_cursor, "isBudgetConfirmed");
          final int _cursorIndexOfIsPeriodClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPeriodClosed");
          final int _cursorIndexOfCompliancePercent = CursorUtil.getColumnIndexOrThrow(_cursor, "compliancePercent");
          final int _cursorIndexOfCarePointsEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "carePointsEarned");
          final PeriodEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpPeriodIndex;
            _tmpPeriodIndex = _cursor.getInt(_cursorIndexOfPeriodIndex);
            final int _tmpPlannedMandatory;
            _tmpPlannedMandatory = _cursor.getInt(_cursorIndexOfPlannedMandatory);
            final int _tmpPlannedOptional;
            _tmpPlannedOptional = _cursor.getInt(_cursorIndexOfPlannedOptional);
            final int _tmpPlannedSavings;
            _tmpPlannedSavings = _cursor.getInt(_cursorIndexOfPlannedSavings);
            final int _tmpActualMandatory;
            _tmpActualMandatory = _cursor.getInt(_cursorIndexOfActualMandatory);
            final int _tmpActualOptional;
            _tmpActualOptional = _cursor.getInt(_cursorIndexOfActualOptional);
            final int _tmpActualSavings;
            _tmpActualSavings = _cursor.getInt(_cursorIndexOfActualSavings);
            final boolean _tmpIsBudgetConfirmed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBudgetConfirmed);
            _tmpIsBudgetConfirmed = _tmp != 0;
            final boolean _tmpIsPeriodClosed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPeriodClosed);
            _tmpIsPeriodClosed = _tmp_1 != 0;
            final int _tmpCompliancePercent;
            _tmpCompliancePercent = _cursor.getInt(_cursorIndexOfCompliancePercent);
            final int _tmpCarePointsEarned;
            _tmpCarePointsEarned = _cursor.getInt(_cursorIndexOfCarePointsEarned);
            _result = new PeriodEntity(_tmpPeriodIndex,_tmpPlannedMandatory,_tmpPlannedOptional,_tmpPlannedSavings,_tmpActualMandatory,_tmpActualOptional,_tmpActualSavings,_tmpIsBudgetConfirmed,_tmpIsPeriodClosed,_tmpCompliancePercent,_tmpCarePointsEarned);
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
  public Object getPeriodSync(final int index,
      final Continuation<? super PeriodEntity> $completion) {
    final String _sql = "SELECT * FROM periods WHERE periodIndex = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, index);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PeriodEntity>() {
      @Override
      @Nullable
      public PeriodEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPeriodIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "periodIndex");
          final int _cursorIndexOfPlannedMandatory = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedMandatory");
          final int _cursorIndexOfPlannedOptional = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedOptional");
          final int _cursorIndexOfPlannedSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedSavings");
          final int _cursorIndexOfActualMandatory = CursorUtil.getColumnIndexOrThrow(_cursor, "actualMandatory");
          final int _cursorIndexOfActualOptional = CursorUtil.getColumnIndexOrThrow(_cursor, "actualOptional");
          final int _cursorIndexOfActualSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "actualSavings");
          final int _cursorIndexOfIsBudgetConfirmed = CursorUtil.getColumnIndexOrThrow(_cursor, "isBudgetConfirmed");
          final int _cursorIndexOfIsPeriodClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPeriodClosed");
          final int _cursorIndexOfCompliancePercent = CursorUtil.getColumnIndexOrThrow(_cursor, "compliancePercent");
          final int _cursorIndexOfCarePointsEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "carePointsEarned");
          final PeriodEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpPeriodIndex;
            _tmpPeriodIndex = _cursor.getInt(_cursorIndexOfPeriodIndex);
            final int _tmpPlannedMandatory;
            _tmpPlannedMandatory = _cursor.getInt(_cursorIndexOfPlannedMandatory);
            final int _tmpPlannedOptional;
            _tmpPlannedOptional = _cursor.getInt(_cursorIndexOfPlannedOptional);
            final int _tmpPlannedSavings;
            _tmpPlannedSavings = _cursor.getInt(_cursorIndexOfPlannedSavings);
            final int _tmpActualMandatory;
            _tmpActualMandatory = _cursor.getInt(_cursorIndexOfActualMandatory);
            final int _tmpActualOptional;
            _tmpActualOptional = _cursor.getInt(_cursorIndexOfActualOptional);
            final int _tmpActualSavings;
            _tmpActualSavings = _cursor.getInt(_cursorIndexOfActualSavings);
            final boolean _tmpIsBudgetConfirmed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBudgetConfirmed);
            _tmpIsBudgetConfirmed = _tmp != 0;
            final boolean _tmpIsPeriodClosed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPeriodClosed);
            _tmpIsPeriodClosed = _tmp_1 != 0;
            final int _tmpCompliancePercent;
            _tmpCompliancePercent = _cursor.getInt(_cursorIndexOfCompliancePercent);
            final int _tmpCarePointsEarned;
            _tmpCarePointsEarned = _cursor.getInt(_cursorIndexOfCarePointsEarned);
            _result = new PeriodEntity(_tmpPeriodIndex,_tmpPlannedMandatory,_tmpPlannedOptional,_tmpPlannedSavings,_tmpActualMandatory,_tmpActualOptional,_tmpActualSavings,_tmpIsBudgetConfirmed,_tmpIsPeriodClosed,_tmpCompliancePercent,_tmpCarePointsEarned);
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

  @Override
  public Flow<List<PeriodEntity>> getAllPeriods() {
    final String _sql = "SELECT * FROM periods ORDER BY periodIndex ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"periods"}, new Callable<List<PeriodEntity>>() {
      @Override
      @NonNull
      public List<PeriodEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPeriodIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "periodIndex");
          final int _cursorIndexOfPlannedMandatory = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedMandatory");
          final int _cursorIndexOfPlannedOptional = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedOptional");
          final int _cursorIndexOfPlannedSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedSavings");
          final int _cursorIndexOfActualMandatory = CursorUtil.getColumnIndexOrThrow(_cursor, "actualMandatory");
          final int _cursorIndexOfActualOptional = CursorUtil.getColumnIndexOrThrow(_cursor, "actualOptional");
          final int _cursorIndexOfActualSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "actualSavings");
          final int _cursorIndexOfIsBudgetConfirmed = CursorUtil.getColumnIndexOrThrow(_cursor, "isBudgetConfirmed");
          final int _cursorIndexOfIsPeriodClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPeriodClosed");
          final int _cursorIndexOfCompliancePercent = CursorUtil.getColumnIndexOrThrow(_cursor, "compliancePercent");
          final int _cursorIndexOfCarePointsEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "carePointsEarned");
          final List<PeriodEntity> _result = new ArrayList<PeriodEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PeriodEntity _item;
            final int _tmpPeriodIndex;
            _tmpPeriodIndex = _cursor.getInt(_cursorIndexOfPeriodIndex);
            final int _tmpPlannedMandatory;
            _tmpPlannedMandatory = _cursor.getInt(_cursorIndexOfPlannedMandatory);
            final int _tmpPlannedOptional;
            _tmpPlannedOptional = _cursor.getInt(_cursorIndexOfPlannedOptional);
            final int _tmpPlannedSavings;
            _tmpPlannedSavings = _cursor.getInt(_cursorIndexOfPlannedSavings);
            final int _tmpActualMandatory;
            _tmpActualMandatory = _cursor.getInt(_cursorIndexOfActualMandatory);
            final int _tmpActualOptional;
            _tmpActualOptional = _cursor.getInt(_cursorIndexOfActualOptional);
            final int _tmpActualSavings;
            _tmpActualSavings = _cursor.getInt(_cursorIndexOfActualSavings);
            final boolean _tmpIsBudgetConfirmed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBudgetConfirmed);
            _tmpIsBudgetConfirmed = _tmp != 0;
            final boolean _tmpIsPeriodClosed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPeriodClosed);
            _tmpIsPeriodClosed = _tmp_1 != 0;
            final int _tmpCompliancePercent;
            _tmpCompliancePercent = _cursor.getInt(_cursorIndexOfCompliancePercent);
            final int _tmpCarePointsEarned;
            _tmpCarePointsEarned = _cursor.getInt(_cursorIndexOfCarePointsEarned);
            _item = new PeriodEntity(_tmpPeriodIndex,_tmpPlannedMandatory,_tmpPlannedOptional,_tmpPlannedSavings,_tmpActualMandatory,_tmpActualOptional,_tmpActualSavings,_tmpIsBudgetConfirmed,_tmpIsPeriodClosed,_tmpCompliancePercent,_tmpCarePointsEarned);
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
  public Object getAvgClosedCompliance(final Continuation<? super Double> $completion) {
    final String _sql = "SELECT AVG(compliancePercent) FROM periods WHERE isPeriodClosed = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
