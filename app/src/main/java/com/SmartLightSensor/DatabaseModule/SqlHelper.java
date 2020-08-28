package com.SmartLightSensor.DatabaseModule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.SmartLightSensor.PogoClasses.GroupDetailsClass;

import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.ADD_DEVICE_TABLE;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_DEVICE_UID;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_GROUP_ID;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.COLUMN_ID;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.DATABASE_NAME;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.DATABASE_VERSION;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.DROP_TABLE;
import static com.SmartLightSensor.DatabaseModule.DatabaseConstant.GROUP_TABLE_NAME;


public class SqlHelper extends SQLiteOpenHelper
{
    String TAG1="SqlHelper";
    public SqlHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    public SqlHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseConstant.CREATE_DEVICE_TABLE);
        db.execSQL(DatabaseConstant.CREATE_GROUP_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE+ ADD_DEVICE_TABLE);
        db.execSQL(DROP_TABLE+ GROUP_TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    /************************  INSERT DATA ***********************************/

    public long insertData(String tableName, ContentValues values)
    {

        SQLiteDatabase db = this.getWritableDatabase();

        long a=db.insert(tableName, null, values);
//        Log.w(TAG1,"a="+a);
//        Cursor cursor=get
// AllData(tableName);
//        if (cursor!=null && cursor.getCount()>10)
//            db.execSQL("DELETE FROM "+tableName+" WHERE "+COLUMN_ID+" IN (" +
//                " SELECT "+COLUMN_ID+" FROM "+tableName+" ORDER BY "+COLUMN_TIMESTAMP+" DESC LIMIT "+cursor.getCount() +" OFFSET " +10+
//                "  )");
//        db.close();
        return a;
    }

    public boolean insertDataNotification(ContentValues values)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        long a=db.insert(GROUP_TABLE_NAME, null, values);
//        Log.w(TAG1,"a="+a);

        return a > 0;
    }
    //***********************  UPDATE DATA **********************************/

    public boolean updateGroupDimming(int groupId, ContentValues args)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(GROUP_TABLE_NAME, args, COLUMN_GROUP_ID + "='" + groupId +"'", null) > 0;
    }

    public boolean updateGroupDevice(int ID, ContentValues args) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(ADD_DEVICE_TABLE, args, COLUMN_GROUP_ID + "='" + ID+"'", null) > 0;
    }

    public boolean updateGroup(int ID, ContentValues args)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(GROUP_TABLE_NAME, args, COLUMN_GROUP_ID + "='" + ID +"'", null) > 0;
    }
    public boolean updateDevice(long ID, ContentValues args) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(ADD_DEVICE_TABLE, args, COLUMN_DEVICE_UID + "='" + ID+"'", null) > 0;
    }
    public boolean updateDevice(String ID, ContentValues args) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(ADD_DEVICE_TABLE, args, COLUMN_DEVICE_UID + "='" + ID+"'", null) > 0;
    }

    public boolean removeLight(int groupId, ContentValues args) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(ADD_DEVICE_TABLE, args, COLUMN_GROUP_ID + "='" + groupId+"'", null) > 0;
    }

    //************************  GET DATA ***************************************/

    public GroupDetailsClass getGroupDetails(int  groupId)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "select * from "+ GROUP_TABLE_NAME +" where "+COLUMN_GROUP_ID+"='"+groupId+"'" , null );  ///
        GroupDetailsClass groupData = new GroupDetailsClass();
        if (cursor.moveToFirst())
        {

            groupData.setGroupId(cursor.getInt(cursor.getColumnIndex(COLUMN_GROUP_ID)));
            groupData.setGroupDimming(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_PROGRESS)));
            groupData.setGroupName(cursor.getString(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_NAME)));
            groupData.setGroupStatus(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.COLUMN_GROUP_STATUS)) == 1);

            // do what ever you want here
            // do what ever you want here
        }
        cursor.close();
        return groupData;
    }
    public Cursor getDataNotification(String from1)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+ GROUP_TABLE_NAME +" where "+ COLUMN_DEVICE_UID +"='"+from1+"'", null );
    }

    public Cursor getData(String tableName, String id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+ tableName +" where "+COLUMN_ID+"='"+id+"'", null );
    }
    //************************  GET DATA ***************************************/
    public Cursor getAllGroup()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+ GROUP_TABLE_NAME /*+" where "+COLUMN_ID+"='"+id+"'"*/, null );
    }

    public Cursor getAllDevice(String tableName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+ tableName , null );  ///+" where "+COLUMN_GROUP_ID+"='"+0+"'"
    }
    public Cursor getNonGroupDevice(String tableName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+ tableName +" where "+COLUMN_GROUP_ID+"='"+0+"'" , null );  ///
    }

    public Cursor getAllGroupLight()
    {
        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery( "select * from (SELECT GroupTable.GROUP_NAME,GroupTable.GROUP_ID,AddDeviceTable.DEVICE_UID,AddDeviceTable.DEVICE_NAME FROM 'GroupTable' INNER JOIN 'AddDeviceTable' ON GroupTable.GROUP_ID=AddDeviceTable.GROUP_ID) ORDER BY  GROUP_ID ASC", null );
        return db.rawQuery( "select * from (SELECT GroupTable.GROUP_NAME,GroupTable.GROUP_ID,AddDeviceTable.DEVICE_UID,AddDeviceTable.DEVICE_NAME,AddDeviceTable.MASTER_STATUS FROM 'GroupTable' INNER JOIN 'AddDeviceTable' ON GroupTable.GROUP_ID=AddDeviceTable.GROUP_ID) ORDER BY  GROUP_ID ASC", null );
    }
    //select *from (SELECT GroupTable.GROUP_NAME,GroupTable.GROUP_ID,AddDeviceTable.DEVICE_UID,AddDeviceTable.DEVICE_NAME FROM 'GroupTable' INNER JOIN 'AddDeviceTable' ON GroupTable.GROUP_ID=AddDeviceTable.GROUP_ID) order by  GROUP_ID ASC;
    public Cursor getLightInGroup(int  id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+ ADD_DEVICE_TABLE +" where "+COLUMN_GROUP_ID+"='"+id+"'", null );
    }

    public Cursor getLightDetails(long  lightId)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery( "select * from "+ ADD_DEVICE_TABLE +" where "+COLUMN_DEVICE_UID+"='"+lightId+"'", null );
    }

    public  void  getLightDetail(long ligthId,int groupId)
    {

    }
    public boolean isExist(String id)
    {
        Cursor cursor=getData(ADD_DEVICE_TABLE,id);
        if (cursor==null)
            return false;
//        Log.w(TAG1,"name="+tableName+cursor.getCount());
        return cursor.getCount() > 0;

    }

    //**************************  DELETE DATA ****************************************/
    public Integer deleteData (String tableName, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName,
                COLUMN_ID +" = ? ",
                new String[] { id });
    }
    public Integer deleteGroup (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(GROUP_TABLE_NAME,
                COLUMN_GROUP_ID +" = ? ",
                new String[] { String.valueOf(id) });
    }
    public Integer deleteLight(long uid, ContentValues contentValues1) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ADD_DEVICE_TABLE,
                COLUMN_GROUP_ID +" = ? ",
                new String[] { String.valueOf(uid) });
    }

}
