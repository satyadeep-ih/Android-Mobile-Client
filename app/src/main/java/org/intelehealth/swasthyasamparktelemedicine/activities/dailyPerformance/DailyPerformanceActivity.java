package org.intelehealth.swasthyasamparktelemedicine.activities.dailyPerformance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import org.checkerframework.checker.units.qual.C;
import org.intelehealth.swasthyasamparktelemedicine.R;
import org.intelehealth.swasthyasamparktelemedicine.app.AppConstants;
import org.intelehealth.swasthyasamparktelemedicine.models.DailyPerformanceModel;
import org.intelehealth.swasthyasamparktelemedicine.models.MissedCallModel;
import org.intelehealth.swasthyasamparktelemedicine.utilities.SessionManager;
import org.intelehealth.swasthyasamparktelemedicine.utilities.UrlModifiers;
import org.intelehealth.swasthyasamparktelemedicine.widget.materialprogressbar.CustomProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
/* Created By:
Developer: Nishita Goyal
Dated: 07-04-2022
Github: nishitagoyal
Ticket Number: IS-165 */

public class DailyPerformanceActivity extends AppCompatActivity {

    TextView totalCallTV, ableReachTV, unableReachTV, needNoRegTV, deniedCOVIDTV, diedTV,
    teleCallerRegTV, specialistRegTV, notPickUpTV, notReachTV, notValidNoTV;
    SessionManager sessionManager;
    String providerPhoneNum;
    String encoded = "", url = "", todayDate_string="";
    UrlModifiers urlModifiers = new UrlModifiers();
    int count_able_reach,count_unable_reach, count_not_reg, count_specialist, count_teleCaller, count_denied_covid, count_died,
            count_not_valid, count_not_pickUp, count_not_reachable = 0;
    Context context;
    Toolbar mToolbar;
    CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_performance);
        setTitle(getString(R.string.daily_performance));
        mToolbar = findViewById(R.id.dailyPerformanceToolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        initViews();
        apiCall(todayDate_string);
    }

    private void apiCall(String todayDate_string) {
        customProgressDialog.show();
        Single<DailyPerformanceModel> dailyPerformanceRequest = AppConstants.apiInterface.DAILY_PERFORMANCE(url + "/" + providerPhoneNum, "Basic " + encoded);
        dailyPerformanceRequest.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<DailyPerformanceModel>() {
                    @Override
                    public void onSuccess(DailyPerformanceModel dailyPerformanceModel) {
                        count_able_reach = 0; count_unable_reach = 0; count_not_reg = 0; count_specialist = 0;
                        count_teleCaller = 0;
                        count_denied_covid = 0;
                        count_died = 0;
                        count_not_valid = 0; count_not_pickUp = 0; count_not_reachable = 0;
                        if(dailyPerformanceModel!=null && dailyPerformanceModel.getData()!=null) {
                            for(int i=0;i<dailyPerformanceModel.getData().size();i++)
                            {
                                if(dailyPerformanceModel.getData().get(i).getDateOfCalls().equals(todayDate_string)) {
                                    if (dailyPerformanceModel.getData().get(i).getActionIfCompleted() != null && !dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals("")) {
                                        if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.need_not_registered)))
                                            count_not_reg = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.patient_reg_caller)))
                                            count_teleCaller = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.patient_reg_specialist)))
                                            count_specialist = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.denied_covid)))
                                            count_denied_covid = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.died)))
                                            count_died = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.not_valid_number)))
                                            count_not_valid = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.not_picked_up)))
                                            count_not_pickUp = dailyPerformanceModel.getData().get(i).getCount();
                                        else if (dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.not_reachable)))
                                            count_not_reachable = dailyPerformanceModel.getData().get(i).getCount();
                                    }
                                }
                            }

                            populateFields(count_not_reg,count_teleCaller,count_specialist,count_denied_covid,count_died,count_not_valid,count_not_pickUp,count_not_reachable);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        customProgressDialog.dismiss();
                        System.out.println(e);
                    }
                });
    }

    private void initViews() {
        totalCallTV = findViewById(R.id.calls_made_TV);
        ableReachTV = findViewById(R.id.able_to_reach_TV);
        unableReachTV = findViewById(R.id.unable_to_reach_TV);
        needNoRegTV = findViewById(R.id.need_no_reg_TV);
        deniedCOVIDTV = findViewById(R.id.denied_covid_TV);
        diedTV = findViewById(R.id.died_TV);
        teleCallerRegTV = findViewById(R.id.patient_caller_TV);
        specialistRegTV = findViewById(R.id.patient_specialist_TV);
        notPickUpTV = findViewById(R.id.not_pickup_TV);
        notReachTV = findViewById(R.id.not_reach_TV);
        notValidNoTV = findViewById(R.id.not_valid_number_TV);
        context = DailyPerformanceActivity.this;
        sessionManager = new SessionManager(context);
        customProgressDialog = new CustomProgressDialog(context);
        encoded = sessionManager.getEncoded();
        url = urlModifiers.getDailyPerformanceUrl();
        if(sessionManager.getProviderPhoneno()!= null)
            providerPhoneNum = sessionManager.getProviderPhoneno();
        else
            providerPhoneNum = "9999999999";
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Date todayDate = new Date();
        todayDate_string = currentDate.format(todayDate) + " 00:00";
        String language = sessionManager.getAppLanguage();

        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
    }



    private void populateFields(int count_not_reg,int count_teleCaller,int count_specialist,int count_denied_covid,int count_died,int count_not_valid,int count_not_pickUp,int count_not_reachable){
        count_able_reach = count_not_reg + count_denied_covid + count_specialist + count_teleCaller + count_died;
        count_unable_reach = count_not_reachable + count_not_pickUp + count_not_valid;
        totalCallTV.setText(getString(R.string.total_call_made) + " " + String.valueOf(count_able_reach + count_unable_reach));
        ableReachTV.setText(getString(R.string.able_to_reach) + " " + String.valueOf(count_able_reach));
        unableReachTV.setText(getString(R.string.unable_to_reach) + " " + String.valueOf(count_unable_reach));
        notPickUpTV.setText(getString(R.string.not_picked_up) + ": " + String.valueOf(count_not_pickUp));
        diedTV.setText(getString(R.string.died) + ": " + String.valueOf(count_died));
        deniedCOVIDTV.setText(getString(R.string.denied_covid) + ": " + String.valueOf(count_denied_covid));
        notValidNoTV.setText(getString(R.string.not_valid_number) + ": " + String.valueOf(count_not_valid));
        notReachTV.setText(getString(R.string.not_reachable) + ": " + String.valueOf(count_not_reachable));
        needNoRegTV.setText(getString(R.string.need_not_registered) + ": " + String.valueOf(count_not_reg));
        teleCallerRegTV.setText(getString(R.string.patient_reg_caller) + ": " + String.valueOf(count_teleCaller));
        specialistRegTV.setText(getString(R.string.patient_reg_specialist) + ": " + String.valueOf(count_specialist));
        customProgressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_sync:
                apiCall(todayDate_string);
                return true;

            case R.id.action_calendar:
                getCalendarPicker();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getCalendarPicker() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date_string = "";
                SimpleDateFormat todaydateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                date_string = todaydateFormat.format(calendar.getTime()) + " 00:00";
                apiCall(date_string);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();

        Button positiveButton = datePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = datePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
    }
}