package com.zeroner.bledemo.sleep;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zeroner.bledemo.R;
import com.zeroner.bledemo.bean.TimeLineAdapter;
import com.zeroner.bledemo.bean.data.OrderStatus;
import com.zeroner.bledemo.bean.data.Orientation;
import com.zeroner.bledemo.bean.data.SleepStatusFlag;
import com.zeroner.bledemo.bean.data.SleepTime;
import com.zeroner.bledemo.bean.data.TimeLineModel;
import com.zeroner.bledemo.data.viewData.ViewData;
import com.zeroner.bledemo.utils.BaseActionUtils;
import com.zeroner.bledemo.utils.DateUtil;
import com.zeroner.bledemo.utils.PrefUtil;
import com.zeroner.bledemo.utils.SqlBizUtils;
import com.zeroner.bledemo.utils.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SleepActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_device_sleep)
    Toolbar toolbarDeviceSleep;
    @BindView(R.id.sleep_recyclerView)
    RecyclerView sleepRecyclerView;

    private Context context;
    private TimeLineAdapter mTimeLineAdapter;
    private List<TimeLineModel> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        context=this;
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        setSupportActionBar(toolbarDeviceSleep);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarDeviceSleep.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setDataListItems();
        mTimeLineAdapter = new TimeLineAdapter(mDataList, Orientation.VERTICAL, true);
        sleepRecyclerView.setAdapter(mTimeLineAdapter);
        sleepRecyclerView.setLayoutManager(getLinearLayoutManager());
        sleepRecyclerView.setHasFixedSize(true);
    }


    private void setDataListItems(){
        DateUtil date=new DateUtil();
        SleepTime sleep=ViewData.sleepDetail(ViewData.deleteSoberSleepData(SqlBizUtils.querySleepData(PrefUtil.getString(context,BaseActionUtils.ACTION_DEVICE_NAME),date.getYear(),date.getMonth(),date.getDay())));
        mDataList.add(new TimeLineModel(100,getString(R.string.sleep_detail_start), Util.minToTime(sleep.getStartMin()),"", OrderStatus.ACTIVE));
        ArrayList<SleepStatusFlag> sleeps = sleep.getSleepStatus();
        for (int i = 0; i <sleeps.size() ; i++) {
            if(sleeps.get(i).isDeepFlag()==SleepStatusFlag.Deep){
                mDataList.add(new TimeLineModel(SleepStatusFlag.Deep,getString(R.string.sleep_detail_deep_1),Util.minToTime(sleeps.get(i).getStartTime()),Util.minToTime(sleeps.get(i).getStartTime()+sleeps.get(i).getTime()), OrderStatus.COMPLETED));
            }else if(sleeps.get(i).isDeepFlag()==SleepStatusFlag.Light){
                mDataList.add(new TimeLineModel(SleepStatusFlag.Light,getString(R.string.sleep_detail_light_1),Util.minToTime(sleeps.get(i).getStartTime()),Util.minToTime(sleeps.get(i).getStartTime()+sleeps.get(i).getTime()), OrderStatus.COMPLETED));
            }else {
                mDataList.add(new TimeLineModel(SleepStatusFlag.Placement,getString(R.string.sleep_detail_placement),Util.minToTime(sleeps.get(i).getStartTime()),Util.minToTime(sleeps.get(i).getStartTime()+sleeps.get(i).getTime()), OrderStatus.COMPLETED));
            }
        }
        mDataList.add(new TimeLineModel(100,getString(R.string.sleep_detail_end), Util.minToTime(sleep.getEndMin()),"", OrderStatus.ACTIVE));
    }


    private LinearLayoutManager getLinearLayoutManager() {
        return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }
}
