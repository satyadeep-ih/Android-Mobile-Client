package org.intelehealth.swasthyasamparktelemedicine.activities.dailyPerformance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import org.intelehealth.swasthyasamparktelemedicine.R;
import org.intelehealth.swasthyasamparktelemedicine.app.AppConstants;
import org.intelehealth.swasthyasamparktelemedicine.models.DailyPerformanceModel;
import org.intelehealth.swasthyasamparktelemedicine.models.MissedCallModel;
import org.intelehealth.swasthyasamparktelemedicine.utilities.SessionManager;
import org.intelehealth.swasthyasamparktelemedicine.utilities.UrlModifiers;

import java.util.HashMap;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
/* Created By:
Developer: Nishita Goyal
Dated: 07-04-2022
Github: nishitagoyal */

public class DailyPerformanceActivity extends AppCompatActivity {

    TextView totalCallTV, ableReachTV, unableReachTV, needNoRegTV, deniedCOVIDTV, diedTV,
    teleCallerRegTV, specialistRegTV, notPickUpTV, notReachTV, notValidNoTV;
    SessionManager sessionManager;
    String providerPhoneNum;
    String encoded = "", url = "";
    UrlModifiers urlModifiers = new UrlModifiers();
    int count_able_reach,count_unable_reach, count_not_reg, count_specialist, count_teleCaller, count_denied_covid, count_died,
            count_not_valid, count_not_pickUp, count_not_reachable = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_performance);
        initViews();
        apiCall();
    }

    private void apiCall() {
        Single<DailyPerformanceModel> dailyPerformanceRequest = AppConstants.apiInterface.DAILY_PERFORMANCE(url, "Basic " + encoded);
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
//                                if(dailyPerformanceModel.getData().get(i).getStatus()!=null && !dailyPerformanceModel.getData().get(i).getStatus().equals(""))
//                                {
//                                    /*if(stringCountMap.containsKey(dailyPerformanceModel.getData().get(i).getStatus()))
//                                        stringCountMap.put(dailyPerformanceModel.getData().get(i).getStatus(), stringCountMap.get(dailyPerformanceModel.getData().get(i).getStatus()) + 1);
//                                    else
//                                        stringCountMap.put(dailyPerformanceModel.getData().get(i).getStatus(), 1);*/
//                                    if(dailyPerformanceModel.getData().get(i).getStatus().equals("Unable to reach patient"))
//                                        count_unable_reach += 1;
//                                    else if(dailyPerformanceModel.getData().get(i).getStatus().equals("Able to reach patient"))
//                                        count_able_reach += 1;
//                                }
                                if(dailyPerformanceModel.getData().get(i).getActionIfCompleted()!=null && !dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(""))
                                {
                                    if(dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.need_not_registered)))
                                        count_not_reg = dailyPerformanceModel.getData().get(i).getCount();
                                    else if(dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.patient_reg_caller)))
                                        count_teleCaller = dailyPerformanceModel.getData().get(i).getCount();
                                    else if(dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.patient_reg_specialist)))
                                        count_specialist = dailyPerformanceModel.getData().get(i).getCount();
                                    else if(dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.denied_covid)))
                                        count_denied_covid = dailyPerformanceModel.getData().get(i).getCount();
                                    else if(dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.died)))
                                        count_died = dailyPerformanceModel.getData().get(i).getCount();
                                    else if(dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.not_valid_number)))
                                        count_not_valid = dailyPerformanceModel.getData().get(i).getCount();
                                    else if(dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.not_picked_up)))
                                        count_not_pickUp = dailyPerformanceModel.getData().get(i).getCount();
                                    else if(dailyPerformanceModel.getData().get(i).getActionIfCompleted().equals(getString(R.string.not_reachable)))
                                        count_not_reachable = dailyPerformanceModel.getData().get(i).getCount();
                                }
                            }

                            populateFields(count_not_reg,count_teleCaller,count_specialist,count_denied_covid,count_died,count_not_valid,count_not_pickUp,count_not_reachable);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

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
        sessionManager = new SessionManager(DailyPerformanceActivity.this);
        encoded = sessionManager.getEncoded();
        url = urlModifiers.getDailyPerformanceUrl();
        if(sessionManager.getProviderPhoneno()!= null)
            providerPhoneNum = sessionManager.getProviderPhoneno();
        else
            providerPhoneNum = "9999999999";

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


    }
}