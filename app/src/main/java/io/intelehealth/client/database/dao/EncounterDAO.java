package io.intelehealth.client.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.models.dto.EncounterDTO;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.exception.DAOException;

public class EncounterDAO {

    long createdRecordsCount = 0;
    int updatecount = 0;
    private String tag = EncounterDAO.class.getSimpleName();
//    private SQLiteDatabase db = null;

    public boolean insertEncounter(List<EncounterDTO> encounterDTOS) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (EncounterDTO encounter : encounterDTOS) {
                createEncounters(encounter, db);
            }
            db.setTransactionSuccessful();

        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return isInserted;
    }

    private boolean createEncounters(EncounterDTO encounter, SQLiteDatabase db) throws DAOException {
        boolean isCreated = false;

        ContentValues values = new ContentValues();
        try {

            values.put("uuid", encounter.getUuid());
            values.put("visituuid", encounter.getVisituuid());
            values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
            values.put("provider_uuid", encounter.getProvideruuid());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", encounter.getSyncd());
            values.put("voided", encounter.getVoided());
            values.put("privacynotice_value", encounter.getPrivacynotice_value());
            createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
//            db.endTransaction();
        }
        return isCreated;
    }


    public boolean createEncountersToDB(EncounterDTO encounter) throws DAOException {
        boolean isCreated = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {

            values.put("uuid", encounter.getUuid());
            values.put("visituuid", encounter.getVisituuid());
            values.put("encounter_time",encounter.getEncounterTime());
            values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
            values.put("provider_uuid", encounter.getProvideruuid());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", "false");
            values.put("voided", encounter.getVoided());
            values.put("privacynotice_value", encounter.getPrivacynotice_value());
            createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (createdRecordsCount != 0)
                isCreated = true;
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return isCreated;
    }

    public String getEncounterTypeUuid(String attr) {
        String encounterTypeUuid = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_uuid_dictionary where name = ? COLLATE NOCASE", new String[]{attr});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                encounterTypeUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
            }
        }
        cursor.close();
        db.close();

        return encounterTypeUuid;
    }

    public List<EncounterDTO> unsyncedEncounters() {
        List<EncounterDTO> encounterDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where sync = ? OR sync=? COLLATE NOCASE", new String[]{"0", "false"});
        EncounterDTO encounterDTO = new EncounterDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTO.setPrivacynotice_value(idCursor.getString(idCursor.getColumnIndexOrThrow("privacynotice_value")));
                encounterDTOList.add(encounterDTO);
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return encounterDTOList;
    }

    public boolean updateEncounterSync(String synced, String uuid) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("encounterdao", "updatesynv encounter " + uuid + synced);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("sync", synced);
            values.put("uuid", uuid);
            int i = db.update("tbl_encounter", values, whereclause, whereargs);
            Logger.logD(tag, "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD(tag, "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
            db.close();

        }

        return isUpdated;
    }


}
