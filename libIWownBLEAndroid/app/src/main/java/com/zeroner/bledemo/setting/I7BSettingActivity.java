package com.zeroner.bledemo.setting;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bigkoo.pickerview.OptionsPickerView;
import com.zeroner.bledemo.R;
import com.zeroner.bledemo.bean.sql.BraceletSetting;
import com.zeroner.bledemo.firmware.ProtoBufFirmwareUpdateActivity;
import com.zeroner.bledemo.setting.alarm.AddClockActivity;
import com.zeroner.bledemo.setting.schedule.ScheduleActivity;
import com.zeroner.bledemo.utils.BaseActionUtils;
import com.zeroner.bledemo.utils.DateUtil;
import com.zeroner.bledemo.utils.OptionsPickerViewUtils;
import com.zeroner.bledemo.utils.PrefUtil;
import com.zeroner.bledemo.utils.SqlBizUtils;
import com.zeroner.bledemo.utils.UI;
import com.zeroner.bledemo.view.LSettingItem;
import com.zeroner.blemidautumn.bluetooth.SuperBleSDK;
import com.zeroner.blemidautumn.task.BackgroundThreadManager;
import com.zeroner.blemidautumn.task.BleWriteDataTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class I7BSettingActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.item_alarm)
    LSettingItem item_alarm;
    @BindView(R.id.item_calendar)
    LSettingItem item_calendar;
    @BindView(R.id.item_time)
    LSettingItem item_time;
    @BindView(R.id.item_motor)
    LSettingItem item_motor;
    @BindView(R.id.item_time_unit)
    LSettingItem item_time_unit;
    @BindView(R.id.item_date_unit)
    LSettingItem item_date_unit;
    @BindView(R.id.item_temp_unit)
    LSettingItem item_temp_unit;
    @BindView(R.id.item_auto_sport)
    LSettingItem item_auto_sport;
    @BindView(R.id.item_habit_hand)
    LSettingItem item_habit_hand;
    @BindView(R.id.item_language)
    LSettingItem item_language;
    @BindView(R.id.item_firmware_update)
    LSettingItem item_firmware_update;

    private int alarmId = 0;

    private Context context;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i7b_setting);
        ButterKnife.bind(this);

        context = this;

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        initListener();


    }

    private void initListener() {

        item_alarm.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                UI.startActivity(I7BSettingActivity.this,AddClockActivity.class);
            }
        });
        item_calendar.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                UI.startActivity((Activity) context,ScheduleActivity.class);
            }
        });

        item_time.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                byte[] bytes = SuperBleSDK.getSDKSendBluetoothCmdImpl(I7BSettingActivity.this).setTime();
                BackgroundThreadManager.getInstance().addWriteData(I7BSettingActivity.this,bytes);
                DateUtil dateUtil = new DateUtil();
                DateUtil dateUtil2 = new DateUtil(dateUtil.getYear(),dateUtil.getMonth(),dateUtil.getDay(),dateUtil.getHour(),0,0);

                byte[] bytes1 = SuperBleSDK.getSDKSendBluetoothCmdImpl(I7BSettingActivity.this).setWeather((int) dateUtil2.getUnixTimestamp(),0,0,20,20,0);
                BackgroundThreadManager.getInstance().addWriteData(I7BSettingActivity.this,bytes1);

            }
        });

        item_motor.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                OptionsPickerView option = OptionsPickerViewUtils.getOptionsPickerView(I7BSettingActivity.this, OptionsPickerViewUtils.getShakeName(I7BSettingActivity.this), new OptionsPickerView.OnOptionsSelectListener() {

                    @Override
                    public void onOptionsSelect(int i, int i1, int i2, View view) {
                        item_motor.setRightText(OptionsPickerViewUtils.getShakeName(I7BSettingActivity.this).get(i));
                        PrefUtil.save(I7BSettingActivity.this, BaseActionUtils.Action_Setting_Shake, OptionsPickerViewUtils.getShakeName(I7BSettingActivity.this).get(i));
                        byte[] bytes = SuperBleSDK.getSDKSendBluetoothCmdImpl(context).setMotorVibrate(OptionsPickerViewUtils.getZGShakeModel(I7BSettingActivity.this)[i],2);
                        BleWriteDataTask task = new BleWriteDataTask(getApplicationContext(), bytes);
                        BackgroundThreadManager.getInstance().addTask(task);
                    }

                });
                option.show();
            }
        });

        item_time_unit.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                OptionsPickerView option = OptionsPickerViewUtils.getOptionsPickerView(context, OptionsPickerViewUtils.getTimeItemOptions(context), new OptionsPickerView.OnOptionsSelectListener() {

                    @Override
                    public void onOptionsSelect(int i, int i1, int i2, View view) {
                        item_time_unit.setRightText(OptionsPickerViewUtils.getTimeItemOptions(context).get(i));
                        BraceletSetting bs1 = SqlBizUtils.querySetting(BaseActionUtils.Action_Setting_Time_Format);
                        bs1.setKey(BaseActionUtils.Action_Setting_Time_Format);
                        bs1.setValue(i);
                        SqlBizUtils.saveBraceletSetting(bs1);
                        byte[] bytes = SuperBleSDK.getSDKSendBluetoothCmdImpl(context).setHourFormat(i == 1);
                        BackgroundThreadManager.getInstance().addWriteData(context,bytes);
                    }
                });
                option.show();
            }
        });

        item_date_unit.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                OptionsPickerView option = OptionsPickerViewUtils.getOptionsPickerView(context, OptionsPickerViewUtils.getDateItemOptions(context), new OptionsPickerView.OnOptionsSelectListener() {

                    @Override
                    public void onOptionsSelect(int i, int i1, int i2, View view) {
                        item_date_unit.setRightText(OptionsPickerViewUtils.getDateItemOptions(context).get(i));
                        PrefUtil.save(context, BaseActionUtils.Action_Setting_Date_Format, OptionsPickerViewUtils.getDateItemOptions(context).get(i));
                        BraceletSetting setting = SqlBizUtils.querySetting(BaseActionUtils.Action_Setting_Date_Format);
                        setting.setKey(BaseActionUtils.Action_Setting_Date_Format);
                        setting.setValue(i);
                        SqlBizUtils.saveBraceletSetting(setting);
                        byte[] bytes = SuperBleSDK.getSDKSendBluetoothCmdImpl(context).setDateFormat(i == 1);
                        BackgroundThreadManager.getInstance().addWriteData(context,bytes);
                    }
                });
                option.show();
            }
        });

        item_temp_unit.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                OptionsPickerView option = OptionsPickerViewUtils.getOptionsPickerView(context, OptionsPickerViewUtils.getWeatherItemOptions(context), new OptionsPickerView.OnOptionsSelectListener() {

                    @Override
                    public void onOptionsSelect(int i, int i1, int i2, View view) {
                        item_temp_unit.setRightText(OptionsPickerViewUtils.getWeatherItemOptions(context).get(i));
                        PrefUtil.save(context, BaseActionUtils.Action_Setting_Weather_Unit, OptionsPickerViewUtils.getWeatherItemOptions(context).get(i));
                        BraceletSetting setting = SqlBizUtils.querySetting(BaseActionUtils.Action_Setting_Weather_Unit);
                        setting.setKey(BaseActionUtils.Action_Setting_Weather_Unit);
                        setting.setValue(i);
                        SqlBizUtils.saveBraceletSetting(setting);
                        byte[] bytes = SuperBleSDK.getSDKSendBluetoothCmdImpl(context).setTemperatureUnit(i == 1);
                        BackgroundThreadManager.getInstance().addWriteData(context,bytes);
                    }
                });
                option.show();
            }
        });
        item_habit_hand.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                OptionsPickerView option = OptionsPickerViewUtils.getOptionsPickerView(context, OptionsPickerViewUtils.getHandItemOptions(context), new OptionsPickerView.OnOptionsSelectListener() {

                    @Override
                    public void onOptionsSelect(int i, int i1, int i2, View view) {
                        byte[] bytes = SuperBleSDK.getSDKSendBluetoothCmdImpl(context).setHabitualHand(i == 1);
                        BackgroundThreadManager.getInstance().addWriteData(context,bytes);
                    }
                });
                option.show();
            }
        });

        item_language.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                OptionsPickerView option = OptionsPickerViewUtils.getOptionsPickerView(context, OptionsPickerViewUtils.getLanguage(context), new OptionsPickerView.OnOptionsSelectListener() {

                    @Override
                    public void onOptionsSelect(int i, int i1, int i2, View view) {
                        SuperBleSDK.getSDKSendBluetoothCmdImpl(context).setLanguage(context,i);
                    }
                });
                option.show();
            }
        });

        item_auto_sport.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {

            }
        });

        item_firmware_update.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                UI.startActivity((Activity) context, ProtoBufFirmwareUpdateActivity.class);
            }
        });



    }

}
