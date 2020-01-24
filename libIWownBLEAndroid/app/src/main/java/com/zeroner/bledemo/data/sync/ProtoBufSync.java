package com.zeroner.bledemo.data.sync;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import com.zeroner.bledemo.BleApplication;
import com.zeroner.bledemo.bean.data.ProtobufSyncSeq;
import com.zeroner.bledemo.bean.sql.PbSupportInfo;
import com.zeroner.bledemo.bean.sql.ProtoBuf_index_80;
import com.zeroner.bledemo.bean.sql.TB_64_index_table;
import com.zeroner.bledemo.bean.sql.TB_mtk_statue;
import com.zeroner.bledemo.eventbus.SyncDataEvent;
import com.zeroner.bledemo.utils.BaseActionUtils;
import com.zeroner.bledemo.utils.DateUtil;
import com.zeroner.bledemo.utils.PrefUtil;
import com.zeroner.blemidautumn.bluetooth.cmdimpl.ProtoBufSendBluetoothCmdImpl;
import com.zeroner.blemidautumn.bluetooth.model.ProtoBufHisIndexTable;
import com.zeroner.blemidautumn.library.KLog;
import com.zeroner.blemidautumn.task.BackgroundThreadManager;
import com.zeroner.blemidautumn.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 同步数据
 */
public class ProtoBufSync {

    /**
     * HEALTH_DATA(0),
     * /**
     * <code>GNSS_DATA = 1;</code>
     * GNSS_DATA(1),
     * <code>ECG_DATA = 2;</code>
     * ECG_DATA(2),
     * <code>PPG_DATA = 3;</code>
     * PPG_DATA(3),
     * <code>RRI_DATA = 4;</code>
     * RRI_DATA(4),
     */
    private volatile static ProtoBufSync instance;
    public static final int HEALTH_DATA = 0;
    public static final int GNSS_DATA = 1;
    public static final int ECG_DATA = 2;
    public static final int PPG_DATA = 3;
    public static final int RRI_DATA = 4;
    private List<Integer> typeArray = new ArrayList<>();
    private SparseArray<List<ProtobufSyncSeq>> totalSeqList = new SparseArray<>();
    private SparseArray<List<ProtoBuf_index_80>> array = new SparseArray<>();
    //    private int totalSeq = 0;//总条数
    private boolean isSync = false;//是否同步
    private int lastPosition = -1;
    private int currentType;//当前同步的类型
    private int timeDelay = 40 * 1000;
    private boolean hasData = false;//有数据
    public static boolean isFirstSync = false;//在收到90指令之后发送同步指令

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static ProtoBufSync getInstance() {
        if (instance == null) {
            synchronized (ProtoBufSync.class) {
                if (instance == null) {
                    instance = new ProtoBufSync();
                }
            }
        }
        return instance;
    }


    /**
     * 同步数据
     */
    public void syncData() {
        String data_from = PrefUtil.getString(BleApplication.getInstance(), BaseActionUtils.ACTION_DEVICE_NAME) + "";
        byte[] realHealthData = ProtoBufSendBluetoothCmdImpl.getInstance().getRealHealthData();
        BackgroundThreadManager.getInstance().addWriteData(BleApplication.getInstance(), realHealthData);

        if (isSync) {
            KLog.d("正在同步..");
            return;
        }

        //查询表数据
        PbSupportInfo protoBufSupportInfo = DataSupport.where("data_from=?", data_from).findFirst(PbSupportInfo.class);
        if(protoBufSupportInfo == null){
            return;
        }
        typeArray = getTypeArray(protoBufSupportInfo);


        isSync = true;

        currentType = 0;

        initData();

    }

    private void initData() {
        if(currentType < typeArray.size()){
            byte[] indexTab = ProtoBufSendBluetoothCmdImpl.getInstance().itHisData(typeArray.get(currentType));
            BackgroundThreadManager.getInstance().addWriteData(BleApplication.getInstance(), indexTab);
        }
    }

    public void syncDetailData(Context context, List<ProtoBuf_index_80> index_80s) {

        //查询表中是否有记录
        if (index_80s != null && index_80s.size() > 0) {
            KLog.e("有数据"+hasData);
            hasData = true;
            int indexType = index_80s.get(0).getIndexType();
            array.put(indexType, index_80s);
            List<ProtobufSyncSeq> protobufSyncSeqs = new ArrayList<>();
            for (int i = 0; i < index_80s.size(); i++) {
                ProtoBuf_index_80 dbIndex = index_80s.get(i);
                int startIdx = dbIndex.getStart_idx();
                int endIdx = dbIndex.getEnd_idx();
                int totalSeq = dbIndex.getEnd_idx() - dbIndex.getStart_idx();
                ProtobufSyncSeq protobufSyncSeq = new ProtobufSyncSeq(totalSeq, startIdx, i + 1, endIdx, indexType);
                protobufSyncSeqs.add(protobufSyncSeq);

                saveIndexTable(indexType, dbIndex);
            }
            totalSeqList.put(indexType, protobufSyncSeqs);
            syncDetailByIndex(context, indexType, index_80s, 0);
        } else {
            //同步完成
            currentType++;
            if (currentType < typeArray.size()) {
                initData();
            } else {
                if (!hasData) {
                    hasData = false;
                    ProtoBufSync.getInstance().progressFinish();
                }
            }
        }

    }

    private void syncDetailByIndex(final Context context, final int hisDataType, final List<ProtoBuf_index_80> indexList, int position) {

        if (position < indexList.size()) {
            ProtoBuf_index_80 index = indexList.get(position);
            final int startSeq = index.getStart_idx();
            final int endSeq = index.getEnd_idx();
            KLog.e("80 data ----- sync ---" + startSeq + "---" + endSeq);
            detailData(context, hisDataType, startSeq, endSeq);
            final int nextPosition = position + 1;
            if (nextPosition < indexList.size()) {
                KLog.e("80 data ----- sync ---" + startSeq + "---" + endSeq);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        syncDetailByIndex(context, hisDataType, indexList, nextPosition);
                    }
                }, 1000);
            } else {
                //同步完一个的指令了.同步下一条
                KLog.e( "yanxi----同步完成一项--2--" + hasData);
                currentType++;
                if (currentType < typeArray.size()) {
                    initData();
                }
            }
        }
    }


    private void detailData(Context context, int type, int startSeq, int endSeq) {
        byte[] hisData = ProtoBufSendBluetoothCmdImpl.getInstance().startHisData(type, startSeq, endSeq);
        BackgroundThreadManager.getInstance().addWriteData(context, hisData);
    }


    public List<ProtoBuf_index_80> parseIndex(ProtoBufHisIndexTable i7BHisIndexTable) {
        if (i7BHisIndexTable == null || i7BHisIndexTable.getIndexList() == null) {
            return null;
        }

        String data_from = PrefUtil.getString(BleApplication.getInstance(), BaseActionUtils.ACTION_DEVICE_NAME)+"";
        List<ProtoBuf_index_80> index_80s = new ArrayList<>();
        for (ProtoBufHisIndexTable.Index index : i7BHisIndexTable.getIndexList()) {
            int[] ints = parseTime(index.getSecond());
            KLog.d("time--"+index.getSecond());
            if (index.getStartSeq() >= index.getEndSeq()) {
                continue;
            }
            DateUtil dateUtil = new DateUtil();
            int endSeq = 0;
            List<ProtoBuf_index_80> index_table;
            if (dateUtil.getYear() == ints[0] && dateUtil.getMonth() == ints[1] && dateUtil.getDay() == ints[2]) {
                ProtoBuf_index_80 end_idx = DataSupport.select("end_idx").where("year=? and month=? and day=? and data_from=?  and indexType=?",
                        dateUtil.getYear() + "",
                        dateUtil.getMonth() + "",
                        dateUtil.getDay() + "",
                        data_from,
                        i7BHisIndexTable.getHisDataType() + "").findLast(ProtoBuf_index_80.class);
                if(end_idx != null){
                    endSeq = end_idx.getEnd_idx();
                }
                com.socks.library.KLog.d("endSEQ"+endSeq);

            }

            index_table = DataSupport.where("year=? and month=? and day=? and data_from=? and start_idx=? and end_idx=? and indexType=?",
                    ints[0] + "",
                    ints[1] + "",
                    ints[2] + "",
                    data_from,
                    index.getStartSeq() + "",
                    index.getEndSeq() + "",
                    i7BHisIndexTable.getHisDataType() + "").find(ProtoBuf_index_80.class);

            if (index_table != null && index_table.size() > 0) {
                continue;
            }

//            if(endSeq > 0 && endSeq >= index.getEndSeq()){
//                continue;
//            }

            KLog.e("更新时间戳"+ints[0] +"--"+ ints[1]  +"--" + ints[2] +"--" + ints[3] +"--" + ints[4] +"--" + ints[5]);

            //数据库中结束seq默认是0

            ProtoBuf_index_80 index_80 = new ProtoBuf_index_80();
            index_80.setYear(ints[0]);
            index_80.setMonth(ints[1]);
            index_80.setDay(ints[2]);
            index_80.setHour(ints[3]);
            index_80.setMin(ints[4]);
            index_80.setSecond(ints[5]);
            index_80.setTime(index.getSecond() - 3600 * Util.getTimeZone());
            index_80.setData_from(data_from);
            if (endSeq > 0 && endSeq < index.getEndSeq()) {
                index_80.setStart_idx(endSeq);
            } else {
                index_80.setStart_idx(index.getStartSeq());
            }
            index_80.setEnd_idx(index.getEndSeq());
            index_80.setIndexType(i7BHisIndexTable.getHisDataType());
            index_80s.add(index_80);

            //保存到数据库
            index_80.saveOrUpdate("year=? and month=? and day=? and data_from=? and start_idx=? and end_idx=? and indexType=?",
                    ints[0] + "",
                    ints[1] + "",
                    ints[2] + "",
                    data_from,
                    index_80.getStart_idx() + "",
                    index_80.getEnd_idx() + "",
                    i7BHisIndexTable.getHisDataType() + "");
            KLog.d(index_80.toString());
        }

        //排序
        Collections.sort(index_80s, new Comparator<ProtoBuf_index_80>() {
            @Override
            public int compare(ProtoBuf_index_80 index1, ProtoBuf_index_80 index2) {
                int i = index1.getYear() * 380 + index1.getMonth() * 31 + index1.getDay();
                int i2 = index2.getYear() * 380 + index2.getMonth() * 31 + index2.getDay();
                if (i > i2) {
                    return -1;
                } else if (i == i2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        return index_80s;

    }


    private int[] parseTime(long second) {
        int[] time = new int[6];
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(second * 1000 - 3600 * Util.getTimeZone() * 1000L);
        time[0] = calendar.get(Calendar.YEAR);
        time[1] = calendar.get(Calendar.MONTH) + 1;
        time[2] = calendar.get(Calendar.DAY_OF_MONTH);
        time[3] = calendar.get(Calendar.HOUR_OF_DAY);
        time[4] = calendar.get(Calendar.MINUTE);
        time[5] = calendar.get(Calendar.SECOND);
        return time;
    }

    private void syncFinish() {
        List<ProtoBuf_index_80> indexTables = array.get(HEALTH_DATA);
        ProtoBufSleepHandler.dispSleepData(indexTables);
        totalSeqList.clear();
        array.clear();
        KLog.e("80 data ----- sync ---finish");
        if(typeArray.contains(GNSS_DATA)) {
            ProtoBufUpdate.getInstance().startUpdate(ProtoBufUpdate.Type.TYPE_GPS);
        }
    }


    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }


    public int currentProgress(int type, int seq) {
        int currentIndex = -1;
        String typeDesc = "";
        if (type == HEALTH_DATA) {
            typeDesc = " health ";
        } else if (type == GNSS_DATA) {
            typeDesc = "GPS";
        } else if (type == ECG_DATA) {
            typeDesc = "ECG";
        }else if (type == PPG_DATA) {
            typeDesc = "PPG";
        }else if (type == RRI_DATA) {
            typeDesc = "RRI";
        }
        List<ProtobufSyncSeq> protobufSyncSeqs = totalSeqList.get(type);
        if(protobufSyncSeqs == null){
            return 0;
        }
        for (int i = 0; i < protobufSyncSeqs.size(); i++) {
            int startSeq = protobufSyncSeqs.get(i).getStartSeq();
            int endSeq = protobufSyncSeqs.get(i).getEndSeq();
            if (seq >= startSeq && seq <= endSeq) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) {
            return 0;
        }
        int progress = (seq - protobufSyncSeqs.get(currentIndex).getStartSeq() + 1) * 100 / protobufSyncSeqs.get(currentIndex).getTotalSeq();
        if (lastPosition != progress) {
            EventBus.getDefault().post(new SyncDataEvent(progress, false, protobufSyncSeqs.size(), protobufSyncSeqs.get(currentIndex).getCurrentDay(), typeDesc));
            lastPosition = progress;
        }
        return progress;
    }

    public void progressFinish() {
        //如果没有执行同步结束指令.执行结束
        if(isSync){
            hasData = false;
            EventBus.getDefault().post(new SyncDataEvent(100, true));
            isSync = false;
            KLog.e("80 data ----- progressFinish");
            syncFinish();
        }
    }



    public void stopSync() {
        isSync = false;
        for (int i = 0; i < typeArray.size(); i++) {
            byte[] bytes = ProtoBufSendBluetoothCmdImpl.getInstance().stopHisData(typeArray.get(i));
            BackgroundThreadManager.getInstance().addWriteData(BleApplication.getInstance(), bytes);
        }
    }


    private void saveIndexTable(int indexType, ProtoBuf_index_80 dbIndex) {
        if (indexType == GNSS_DATA) {
            DateUtil dateUtil = new DateUtil(dbIndex.getYear(), dbIndex.getMonth(), dbIndex.getDay());
            TB_mtk_statue mtk_statue = new TB_mtk_statue();
            mtk_statue.setData_from(dbIndex.getData_from());
            mtk_statue.setType(80);
            mtk_statue.setYear(dbIndex.getYear());
            mtk_statue.setMonth(dbIndex.getMonth());
            mtk_statue.setDay(dbIndex.getDay());
            mtk_statue.setHas_file(2);
            mtk_statue.setHas_up(2);
            mtk_statue.setHas_tb(2);
            mtk_statue.setDate(dateUtil.getUnixTimestamp());
            mtk_statue.saveOrUpdate("data_from=? and type=? and date=?",
                    dbIndex.getData_from(), "80", dateUtil.getUnixTimestamp() + "");

        } else if (indexType == ECG_DATA) {
            TB_64_index_table indexTable = new TB_64_index_table();
            DateUtil d = new DateUtil(dbIndex.getYear(), dbIndex.getMonth(), dbIndex.getDay(), dbIndex.getHour(), dbIndex.getMin(), dbIndex.getSecond());
            indexTable.setData_from(dbIndex.getData_from());
            indexTable.setData_ymd(d.getSyyyyMMddDate());
            indexTable.setSeq_start(dbIndex.getStart_idx());
            indexTable.setSeq_end(dbIndex.getEnd_idx());
            indexTable.setSync_seq(dbIndex.getEnd_idx());
            indexTable.setDate(d.getY_M_D_H_M_S());
            indexTable.setUnixTime(d.getUnixTimestamp());
            indexTable.saveOrUpdate("data_from =? and date=?",
                    dbIndex.getData_from(), d.getY_M_D_H_M_S());
        }
    }

    private List<Integer> getTypeArray(PbSupportInfo protoBufSupportInfo){
        List<Integer> integers = new ArrayList<>();
        if(protoBufSupportInfo.isSupport_health()){
            integers.add(HEALTH_DATA);
        }
        if(protoBufSupportInfo.isSupport_gnss()){
            integers.add(GNSS_DATA);
        }
        if(protoBufSupportInfo.isSupport_ecg()){
            integers.add(ECG_DATA);
        }
        if(protoBufSupportInfo.isSupport_ppg()){
            integers.add(PPG_DATA);
        }
        if(protoBufSupportInfo.isSupport_rri()){
            integers.add(RRI_DATA);
        }
        return integers;
    }

}
