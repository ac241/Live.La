package com.acel.streamlivetool.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class DbManager private constructor(mContext: Context) {
    private val DB_NAME = "anchor.db"
    private var mDevOpenHelper: DaoMaster.DevOpenHelper? = null
    private var mDaoMaster: DaoMaster? = null
    private var mDaoSession: DaoSession? = null

    init {
        mDevOpenHelper = DaoMaster.DevOpenHelper(mContext, DB_NAME)
        mDaoMaster = getDaoMaster(mContext)
        mDaoSession = getDaoSession(mContext)
    }

    companion object {
        var instance: DbManager? = null

        fun getInstance(mContext: Context): DbManager? {
            if (instance == null) {
                synchronized(DbManager::class.java) {
                    if (instance == null)
                        instance = DbManager(mContext)
                }
            }
            return instance
        }
    }

    /**
     * 获取可读数据库
     */
    fun getReadableDatabase(mContext: Context): SQLiteDatabase? {
        if (null == mDevOpenHelper) {
            getInstance(mContext)
        }
        return mDevOpenHelper?.readableDatabase
    }

    /**
     * 获取可写数据库
     */
    fun getWritableDatabase(mContext: Context): SQLiteDatabase? {
        if (null == mDevOpenHelper) {
            getInstance(mContext)
        }
        return mDevOpenHelper?.readableDatabase
    }

    /**
     * 获取DaoMaster
     */
    fun getDaoMaster(mContext: Context): DaoMaster? {
        if (null == mDaoMaster) {
            synchronized(DbManager::class.java) {
                if (null == mDaoMaster) {
                    mDaoMaster = DaoMaster(getWritableDatabase(mContext))
                }
            }
        }
        return mDaoMaster
    }

    /**
     * 获取DaoSession
     *
     * @param context
     * @return
     */
    fun getDaoSession(context: Context): DaoSession? {
        if (null == mDaoSession) {
            synchronized(DbManager::class.java) {
                mDaoSession = getDaoMaster(context)?.newSession()
            }
        }
        return mDaoSession
    }
}