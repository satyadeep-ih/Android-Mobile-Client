package app.intelehealth.client.activities.searchPatientActivity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.text.Layout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import app.intelehealth.client.R;
import app.intelehealth.client.activities.todayPatientActivity.TodayPatientActivity;
import app.intelehealth.client.activities.todayPatientActivity.TodayPatientAdapter;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.database.dao.ProviderDAO;
import app.intelehealth.client.models.TodayPatientModel;
import app.intelehealth.client.models.dto.PatientDTO;
import app.intelehealth.client.utilities.Logger;
import app.intelehealth.client.utilities.SessionManager;

import app.intelehealth.client.activities.homeActivity.HomeActivity;
import app.intelehealth.client.utilities.StringUtils;
import app.intelehealth.client.utilities.exception.DAOException;

public class SearchPatientActivity extends AppCompatActivity {
    SearchView searchView;
    String query;
    private SearchPatientAdapter recycler , recyclerforDate;
    RecyclerView recyclerView;
    RelativeLayout relativeLayout;
    SessionManager sessionManager = null;
    TextView msg;
    boolean firstLaunch;
    LinearLayout filter_linearLayout;
    MaterialAlertDialogBuilder dialogBuilder;
    private String TAG = SearchPatientActivity.class.getSimpleName();
    private SQLiteDatabase db;
    TextView tv_sort_age,tv_sort_creator,tv_sort_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        firstLaunch = true;
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_sort_white_24dp);
//        toolbar.setOverflowIcon(drawable);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Get the intent, verify the action and get the query
        sessionManager = new SessionManager(this);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        msg = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.recycle);
        filter_linearLayout = findViewById(R.id.filter_layout);
        tv_sort_creator = findViewById(R.id.sort_creator);
        tv_sort_age = findViewById(R.id.sort_age);
        tv_sort_date = findViewById(R.id.sort_date);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Date");
        final MaterialDatePicker materialDatePicker = builder.build();
        tv_sort_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_sort_date.setBackground(getResources().getDrawable(R.drawable.tv_bg_dark));
                tv_sort_date.setTextColor(Color.WHITE);
                materialDatePicker.show(getSupportFragmentManager(), "DATE PICKER");
                materialDatePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tv_sort_date.setBackground(getResources().getDrawable(R.drawable.tv_bg));
                        tv_sort_date.setTextColor(Color.BLACK);
                        materialDatePicker.dismiss();
                    }
                });
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                    @Override public void onPositiveButtonClick(Pair<Long,Long> selection) {
                        Long startDate = selection.first;
                        Long endDate = selection.second;
                        tv_sort_date.setBackground(getResources().getDrawable(R.drawable.tv_bg_dark));
                        tv_sort_date.setTextColor(Color.WHITE);
                        doQueryWithDate(startDate, endDate);
                    }
                });

























            }
        });

        tv_sort_creator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    tv_sort_creator.setBackground(getResources().getDrawable(R.drawable.tv_bg_dark));
                    tv_sort_creator.setTextColor(Color.WHITE);
                displaySingleSelectionCreatorDialog();
            }
        });
        tv_sort_age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_sort_age.setBackground(getResources().getDrawable(R.drawable.tv_bg_dark));
                tv_sort_age.setTextColor(Color.WHITE);
                displaySingleSelectionAgeDialog();
            }
        });

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            if (sessionManager.isPullSyncFinished()) {
                msg.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                doQuery(query);
            }

        } else {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            if (sessionManager.isPullSyncFinished()) {
                msg.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                firstQuery();
            }

        }


    }

    private void doQuery(String query) {
        try {
            recycler = new SearchPatientAdapter(getQueryPatients(query), SearchPatientActivity.this);
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(reLayoutManager);
           /* recyclerView.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));*/
            recyclerView.setAdapter(recycler);

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("doquery", "doquery", e);
        }
    }

    private void firstQuery() {
        try {
            getAllPatientsFromDB();

            recycler = new SearchPatientAdapter(getAllPatientsFromDB(), SearchPatientActivity.this);

//            Log.i("db data", "" + getAllPatientsFromDB());
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(reLayoutManager);
         /*   recyclerView.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));*/
            recyclerView.setAdapter(recycler);

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("firstquery", "exception", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XMLz
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        inflater.inflate(R.menu.today_filter, menu);
       // inflater.inflate(R.menu.today_filter, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("Hack", "in query text change");
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchPatientActivity.this,
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
                query = newText;
                doQuery(newText);
                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.summary_endAllVisit:
                endAllVisit();

            case R.id.action_filter:

                if(firstLaunch == true)
                {
                    ValueAnimator varl = ValueAnimator.ofInt(250);
                    varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            CoordinatorLayout.LayoutParams linearParams = new CoordinatorLayout.LayoutParams(
                                    new CoordinatorLayout.LayoutParams(
                                            CoordinatorLayout.LayoutParams.MATCH_PARENT,
                                            CoordinatorLayout.LayoutParams.WRAP_CONTENT));
                            linearParams.setMargins(0, (Integer) animation.getAnimatedValue(), 0, 0);
                            relativeLayout.setLayoutParams(linearParams);
                            relativeLayout.requestLayout();
                        }
                    });
                    varl.start();
                    filter_linearLayout.setVisibility(View.VISIBLE);
                    firstLaunch = false;
                }
                else
                {
                    ValueAnimator varl = ValueAnimator.ofInt(150);
                    varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            CoordinatorLayout.LayoutParams linearParams = new CoordinatorLayout.LayoutParams(
                                    new CoordinatorLayout.LayoutParams(
                                            CoordinatorLayout.LayoutParams.MATCH_PARENT,
                                            CoordinatorLayout.LayoutParams.WRAP_CONTENT));
                            linearParams.setMargins(0, (Integer) animation.getAnimatedValue(), 0, 0);
                            relativeLayout.setLayoutParams(linearParams);
                            relativeLayout.requestLayout();
                        }
                    });
                    varl.start();
                    filter_linearLayout.setVisibility(View.GONE);
                    firstLaunch = true;
                }
            case R.id.action_search:


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method is called when no search result is found for patient.
     *
     * @param lvItems variable of type ListView
     * @param query   variable of type String
     */
    public void noneFound(ListView lvItems, String query) {
        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this,
                R.layout.list_item_search,
                R.id.list_item_head, new ArrayList<String>());
        String errorMessage = getString(R.string.alert_none_found).replace("_", query);
        searchAdapter.add(errorMessage);
        lvItems.setAdapter(searchAdapter);
    }

    public List<PatientDTO> getAllPatientsFromDB() {
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        String table = "tbl_patient";
        final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY first_name ASC", null);
        try {
            if (searchCursor.moveToFirst()) {
                do {
                    PatientDTO model = new PatientDTO();
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                    model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                    model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));

                    modelList.add(model);
                } while (searchCursor.moveToNext());
            }
            searchCursor.close();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return modelList;

    }

    private void endAllVisit() {

        int failedUploads = 0;

        String query = "SELECT tbl_visit.patientuuid, tbl_visit.enddate, tbl_visit.uuid," +
                "tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name FROM tbl_visit, tbl_patient WHERE" +
                " tbl_visit.patientuid = tbl_patient.uuid AND tbl_visit.enddate IS NULL OR tbl_visit.enddate = ''";

        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    boolean result = endVisit(
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                    );
                    if (!result) failedUploads++;
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        if (failedUploads == 0) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage("Unable to end " + failedUploads +
                    " visits.Please upload visit before attempting to end the visit.");
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        }

    }

    private boolean endVisit(String patientUuid, String patientName, String visitUUID) {

        return visitUUID != null;

    }

    private void displaySingleSelectionCreatorDialog() {

        ProviderDAO providerDAO = new ProviderDAO();
        ArrayList selectedItems = new ArrayList<>();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        final boolean[] checkedItems = new boolean[creator_names.length];
        Arrays.fill(checkedItems, Boolean.FALSE);
        dialogBuilder = new MaterialAlertDialogBuilder(SearchPatientActivity.this);
        dialogBuilder.setTitle(getString(R.string.filter_by_creator));
        String[] finalCreator_names = creator_names;
        String[] finalCreator_uuid = creator_uuid;
        dialogBuilder.setMultiChoiceItems(creator_names,checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
                Logger.logD(TAG, "multichoice" + which + isChecked);
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    if (finalCreator_uuid != null) {
                        selectedItems.add(finalCreator_uuid[which]);
                    }
                } else if (selectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it
                    if (finalCreator_uuid != null) {
                        selectedItems.remove(finalCreator_uuid[which]);
                    }
                }
            }
        });
        dialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);

                if(selectedItems.size()==0)
                {
                    tv_sort_creator.setBackground(getResources().getDrawable(R.drawable.tv_bg));
                    tv_sort_creator.setTextColor(Color.BLACK);
                }
                else
                    {
                    doQueryWithProviders(query, selectedItems);
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tv_sort_creator.setBackground(getResources().getDrawable(R.drawable.tv_bg));
                tv_sort_creator.setTextColor(Color.BLACK);
            }
        });

        //dialogBuilder.show();
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

//        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

    }

    private void displaySingleSelectionAgeDialog() {

        ProviderDAO providerDAO = new ProviderDAO();
        ArrayList selectedItems = new ArrayList<>();
        String[] age_range_list = {"Below 5","5-12","13-26","27-40","41-55","Above 56"};
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        dialogBuilder = new MaterialAlertDialogBuilder(SearchPatientActivity.this);
        dialogBuilder.setTitle(getString(R.string.filter_by_age));
        String[] finalCreator_names = creator_names;
        String[] finalCreator_uuid = creator_uuid;
        dialogBuilder.setMultiChoiceItems(age_range_list, null, new DialogInterface.OnMultiChoiceClickListener() {


            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                Logger.logD(TAG, "multichoice" + which + isChecked);
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    if (age_range_list != null) {
                        selectedItems.add(age_range_list[which]);
                    }

                } else if (selectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it
                    if (age_range_list != null) {
                        selectedItems.remove(age_range_list[which]);
                    }
                }
            }
        });
        dialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);

                if(selectedItems.size()==0)
                {
                    tv_sort_age.setBackground(getResources().getDrawable(R.drawable.tv_bg));
                    tv_sort_age.setTextColor(Color.BLACK);
                }
                else
                {
                    doQueryWithAge(query, selectedItems);
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tv_sort_age.setBackground(getResources().getDrawable(R.drawable.tv_bg));
                tv_sort_age.setTextColor(Color.BLACK);
            }
        });

        //dialogBuilder.show();
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

//        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

    }

    public List<PatientDTO> getQueryPatients(String query) {
        String search = query.trim().replaceAll("\\s", "");
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String table = "tbl_patient";
        final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC", null);
        try {
            if (searchCursor.moveToFirst()) {
                do {
                    PatientDTO model = new PatientDTO();
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                    model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                    model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                    model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                    modelList.add(model);
                } while (searchCursor.moveToNext());
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return modelList;

    }

    private void doQueryWithProviders(String querytext, List<String> providersuuids) {
        if (querytext == null) {
            List<PatientDTO> modelListwihtoutQuery = new ArrayList<PatientDTO>();
            String query =
                    "select b.openmrs_id,b.first_name,b.last_name,b.middle_name,b.uuid,b.date_of_birth from tbl_visit a, tbl_patient b, tbl_encounter c WHERE a.patientuuid = b.uuid  AND c.visituuid=a.uuid and c.provider_uuid in " +
                            "('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
                            "group by a.uuid order by b.uuid ASC";
            Logger.logD(TAG, query);
            final Cursor cursor = db.rawQuery(query, null);
            Logger.logD(TAG, "Cursour count" + cursor.getCount());

            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            PatientDTO model = new PatientDTO();
                            model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                            model.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                            model.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                            model.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                            model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                            model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))));
                            modelListwihtoutQuery.add(model);

                        } while (cursor.moveToNext());
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }

            } catch (DAOException e) {
                e.printStackTrace();
            }

            try {
                recycler = new SearchPatientAdapter(modelListwihtoutQuery, SearchPatientActivity.this);
//            Log.i("db data", "" + getQueryPatients(query));
                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(reLayoutManager);
            /*    recyclerView.addItemDecoration(new
                        DividerItemDecoration(this,
                        DividerItemDecoration.VERTICAL));*/
                recyclerView.setAdapter(recycler);

            } catch (Exception e) {
                Logger.logE("doquery", "doquery", e);
            }
        }
        else {
            String search = querytext.trim().replaceAll("\\s", "");
            List<PatientDTO> modelList = new ArrayList<PatientDTO>();
            String query =
                    "select   b.openmrs_id,b.firstname,b.last_name,b.middle_name,b.uuid,b.date_of_birth  from tbl_visit a, tbl_patient b, tbl_encounter c WHERE" +
                            "first_name LIKE " + "'%" + search +
                            "%' OR middle_name LIKE '%" + search +
                            "%' OR last_name LIKE '%" + search +
                            "%' OR openmrs_id LIKE '%" + search +
                            "%' " +
                            "AND a.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
                            "AND  a.patientuuid = b.uuid  AND c.visituuid=a.uuid " +
                            "group by a.uuid order by b.uuid ASC";
            Logger.logD(TAG, query);
            final Cursor cursor = db.rawQuery(query, null);
            Logger.logD(TAG, "Cursour count" + cursor.getCount());
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            PatientDTO model = new PatientDTO();
                            model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                            model.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                            model.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                            model.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                            model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                            model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))));
                            modelList.add(model);

                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
            } catch (DAOException sql) {
                FirebaseCrashlytics.getInstance().recordException(sql);
            }
            try {
                recycler = new SearchPatientAdapter(modelList, SearchPatientActivity.this);
                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(reLayoutManager);
           /*     recyclerView.addItemDecoration(new
                        DividerItemDecoration(this,
                        DividerItemDecoration.HORIZONTAL));*/
                recyclerView.setAdapter(recycler);

            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Logger.logE("doquery", "doquery", e);
            }
        }
    }
    private void doQueryWithAge(String querytext, List<String> patientages) {
        if (querytext == null) {
            List<PatientDTO> modelListwihtoutQuery = new ArrayList<PatientDTO>();
//            String query =
//                    "select b.openmrs_id,b.first_name,b.last_name,b.middle_name,b.uuid,b.date_of_birth from tbl_visit a, tbl_patient b, tbl_encounter c WHERE a.patientuuid = b.uuid  AND c.visituuid=a.uuid and c.provider_uuid in " +
//                            "('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
//                            "group by a.uuid order by b.uuid ASC";
//            Logger.logD(TAG, query);
//            final Cursor cursor = db.rawQuery(query, null);
//            Logger.logD(TAG, "Cursour count" + cursor.getCount());
//
//            try {
//                if (cursor != null) {
//                    if (cursor.moveToFirst()) {
//                        do {
//                            PatientDTO model = new PatientDTO();
//                            model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
//                            model.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
//                            model.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
//                            model.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
//                            model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
//                            model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
//                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))));
//                            modelListwihtoutQuery.add(model);
//
//                        } while (cursor.moveToNext());
//                    }
//                }
//                if (cursor != null) {
//                    cursor.close();
//                }
//
//            } catch (DAOException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                recycler = new SearchPatientAdapter(modelListwihtoutQuery, SearchPatientActivity.this);
////            Log.i("db data", "" + getQueryPatients(query));
//                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
//                recyclerView.setLayoutManager(reLayoutManager);
//            /*    recyclerView.addItemDecoration(new
//                        DividerItemDecoration(this,
//                        DividerItemDecoration.VERTICAL));*/
//                recyclerView.setAdapter(recycler);
//
//            } catch (Exception e) {
//                Logger.logE("doquery", "doquery", e);
//            }
//        }
//        else {
//            String search = querytext.trim().replaceAll("\\s", "");
//            List<PatientDTO> modelList = new ArrayList<PatientDTO>();
//            String query =
//                    "select   b.openmrs_id,b.firstname,b.last_name,b.middle_name,b.uuid,b.date_of_birth  from tbl_visit a, tbl_patient b, tbl_encounter c WHERE" +
//                            "first_name LIKE " + "'%" + search +
//                            "%' OR middle_name LIKE '%" + search +
//                            "%' OR last_name LIKE '%" + search +
//                            "%' OR openmrs_id LIKE '%" + search +
//                            "%' " +
//                            "AND a.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
//                            "AND  a.patientuuid = b.uuid  AND c.visituuid=a.uuid " +
//                            "group by a.uuid order by b.uuid ASC";
//            Logger.logD(TAG, query);
//            final Cursor cursor = db.rawQuery(query, null);
//            Logger.logD(TAG, "Cursour count" + cursor.getCount());
//            try {
//                if (cursor != null) {
//                    if (cursor.moveToFirst()) {
//                        do {
//                            PatientDTO model = new PatientDTO();
//                            model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
//                            model.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
//                            model.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
//                            model.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
//                            model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
//                            model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
//                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))));
//                            modelList.add(model);
//
//                        } while (cursor.moveToNext());
//                    }
//                }
//                cursor.close();
//            } catch (DAOException sql) {
//                FirebaseCrashlytics.getInstance().recordException(sql);
//            }
//            try {
//                recycler = new SearchPatientAdapter(modelList, SearchPatientActivity.this);
//                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
//                recyclerView.setLayoutManager(reLayoutManager);
//           /*     recyclerView.addItemDecoration(new
//                        DividerItemDecoration(this,
//                        DividerItemDecoration.HORIZONTAL));*/
//                recyclerView.setAdapter(recycler);
//
//            } catch (Exception e) {
//                FirebaseCrashlytics.getInstance().recordException(e);
//                Logger.logE("doquery", "doquery", e);
//            }
        }
    }
    private void doQueryWithDate(Long startDate, Long endDate) {
        List<PatientDTO> modelListwihtoutQuery = new ArrayList<PatientDTO>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        String startD = DateFormat.format("yyyy-MM-dd", new Date(startDate)).toString();
        String endD = DateFormat.format("yyyy-MM-dd", new Date(endDate)).toString();
        Toast.makeText(SearchPatientActivity.this, endD + startD, Toast.LENGTH_LONG).show();
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id,b.uuid " +
                "FROM tbl_visit a, tbl_patient b  " + 
                "WHERE a.patientuuid = b.uuid " +
                "AND a.startdate BETWEEN '" + startD + "T%'  AND '" + endD + "T%' " +
                "GROUP BY a.uuid ORDER BY a.patientuuid ASC";


                Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        PatientDTO model = new PatientDTO();
                        model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                        model.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                        model.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                        model.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                        model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                        model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                        model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))));
                        modelListwihtoutQuery.add(model);
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }


        try {
            recyclerforDate = new SearchPatientAdapter(modelListwihtoutQuery, SearchPatientActivity.this);
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(reLayoutManager);
            recyclerView.setAdapter(recyclerforDate);
        } catch (Exception e) {
            Logger.logE("doquery", "doquery", e);
        }

    }

    private String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value  FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7' ", new String[]{patientuuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {

                    phone = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));

                }
            }
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
        }
        idCursor.close();

        return phone;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}




