package no.teacherspet.tring.Database.DAOs;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Maybe;
import no.teacherspet.tring.Database.Entities.RoomOEvent;

/**
 * Created by Hermann on 13.02.2018.
 * Class for getting events. Can choose to only get events which are set to active
 */
@Dao
public interface OEventDao {

    @Query("SELECT * FROM o_event")
    Maybe<List<RoomOEvent>> getAll();

    @Query("SELECT * FROM o_event WHERE id = :id")
    Maybe<RoomOEvent> findById(int id);

    @Query("SELECT MAX(id) FROM o_event")
    Maybe<Integer> getMaxID();

    @Query("SELECT * FROM o_event WHERE active = 1")
    Maybe<List<RoomOEvent>> getActiveEvent();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insert(RoomOEvent... roomOEvents);

    @Update
    void update(RoomOEvent... roomOEvents);

    @Query("DELETE FROM o_event WHERE id = :oEventID")
    int deleteOEvent(int oEventID);

    @Delete
    int delete(RoomOEvent... roomOEvents);
}
