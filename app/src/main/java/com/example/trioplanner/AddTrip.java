package com.example.trioplanner;


import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trioplanner.FirebaseDBOperation.FirebaseModelImpl;
import com.example.trioplanner.FirebaseDBOperation.HomeContract;
import com.example.trioplanner.FirebaseDBOperation.HomePresenterImpl;
import com.example.trioplanner.data.Trip;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.Calendar;

import java.util.UUID;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.trioplanner.Uitiles.KEY_PASS_TRIP;
import static com.example.trioplanner.Uitiles.SAVED_OFFLINE;
import static com.example.trioplanner.Uitiles.SAVED_ONLINE;
import static com.example.trioplanner.Uitiles.TAG;
import static com.example.trioplanner.Uitiles.checkInternetState;


public class AddTrip extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener,
        HomeContract.AddTripView {

    Calendar c = Calendar.getInstance();
    HomeContract.HomePresenter addTripPresenter;

    @BindView(R.id.consAddTrip)
    View consViewGroup;
    @BindView(R.id.name)
    TextInputEditText name;

    @BindView(R.id.date)
    TextInputEditText date;
    @BindView(R.id.time)
    TextInputEditText time;
    @BindView(R.id.notes)
    TextInputEditText notes;
    @BindView(R.id.roundTrip)
    CheckBox round;

    View view;


    //start  those value came form methods below
    String tripNameStartPoint = "";
    String tripNameEndPoint = "";

    private LatLng latLngStartPointloc;
    private LatLng latLngEndPointloc;
    //end those value came form methods below


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnClick(R.id.add)

    void addTrip(View view) {
        String tripName = name.getText().toString();
        String tripDate = date.getText().toString();
        String tripTime = time.getText().toString();
        String tripNotes = notes.getText().toString();
        String tripType = String.valueOf(isRoundTrip());
        String tripStatus = Uitiles.STATUS_UPCOMING;

        //  name - startLoc -  endLoc -  date -  time -  type -  notes

        // if one of 7 filed is empty show dialog else save it im db
        if (tripName.isEmpty() || tripNameStartPoint.isEmpty() || tripNameEndPoint.isEmpty()
                || tripDate.isEmpty() || tripTime.isEmpty() || tripNotes.isEmpty()
               ) {
            Uitiles.showCustomDialog(consViewGroup, "Please fill all fields", this, "Warning!");
        } else {

            // came from coresponding methods below
            String latLagLoc1 = latLngStartPointloc.latitude
                    + "_"
                    + latLngStartPointloc.longitude;
            String latLagLoc2 = latLngEndPointloc.latitude
                    + "_"
                    + latLngEndPointloc.longitude;

            //  name - startLoc -  endLoc -  date -  time -  type -  notes
            Trip trip = new Trip(tripName,
                    tripNameStartPoint, tripNameEndPoint,
                    tripDate, tripTime,
                    tripType, tripNotes, tripStatus, SAVED_ONLINE);
            trip.setLatLngString1(latLagLoc1);
            trip.setLatLngString2(latLagLoc2);
            trip.setId(UUID.randomUUID().toString());
        //alarm manager
            Intent intent = new Intent(AddTrip.this, AlertReceiver.class);
//            Bundle bundle = new Bundle();
//            bundle.putSerializable(KEY_PASS_TRIP, trip);
            intent.putExtra(KEY_PASS_TRIP, trip);
//            intent.putExtras(bundle);
            // id - name - startLoc - endLoc - date - time - type - notes -status -  isSavedOnline
            // LatLngString1 -LatLngString2
            intent.putExtra(Uitiles.ID, trip.getId());
            intent.putExtra(Uitiles.NAME, trip.getName());
            intent.putExtra(Uitiles.START_LOC, trip.getStartLoc());
            intent.putExtra(Uitiles.END_LOC, trip.getEndLoc());
            intent.putExtra(Uitiles.DATA, trip.getDate());
            intent.putExtra(Uitiles.TIME, trip.getTime());
            intent.putExtra(Uitiles.TYPE, trip.getType());
            intent.putExtra(Uitiles.NOTES, trip.getNotes());
            intent.putExtra(Uitiles.STATUS, trip.getStatus());
            intent.putExtra(Uitiles.IS_SAVED_ONLINE, trip.getIsSavedOnline());
            intent.putExtra(Uitiles.LAT_LNG_STRING_1, trip.getLatLngString1());
            intent.putExtra(Uitiles.LAT_LNG_STRING_2, trip.getLatLngString2());

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(AddTrip.this,
                    1, intent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            //alarmManager.cancel(pi);

            if (checkInternetState(this)) {
                trip.setIsSavedOnline(SAVED_ONLINE);
                addTripPresenter.onSaveTrip(trip);
            } else {
                trip.setIsSavedOnline(SAVED_OFFLINE);
                // TODO 1- Salah add to room DB
            }

            finish();

        }

    }


    private void getTime() {
        TimePickerFragment timePickerDialogFragment = new TimePickerFragment();
        timePickerDialogFragment.show(getSupportFragmentManager(), "Time Picker");
    }

    private void getDate() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "Date Picker");
    }

    private Boolean isRoundTrip() {
        return round.isChecked();
    }

    @OnClick(R.id.addDate)
    void addDate(View view) {
        getDate();
    }

    @OnClick(R.id.addTime)
    void addTTime(View view) {
        getTime();
        //Toast.makeText(this,"Butter knife working !",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);
        ButterKnife.bind(this);

        Log.i(TAG, "{isEmpty} addTrip: if isEmpty " + tripNameStartPoint.isEmpty());


        addTripPresenter = new HomePresenterImpl(new FirebaseModelImpl(), this);
        // Initialize the SDK
        Places.initialize(getApplicationContext(), "AIzaSyDuifm35ZNF7OOG7exwhOrda3mb1H8qFnA");
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment1 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.spoint1);

        AutocompleteSupportFragment autocompleteFragment2 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.epoint1);
        // Set up a PlaceSelectionListener to handle the response.
        // Specify the types of place data to return.
        autocompleteFragment1.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteFragment2.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteFragment1.setCountry("EG");
        autocompleteFragment2.setCountry("EG");
        // first et
        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                tripNameStartPoint = place.getName();
                latLngStartPointloc = place.getLatLng();
                // TODO: Hossam Get info about the selected place.
//                Toast.makeText(getApplicationContext(), " " +
//                        place.getLatLng(), Toast.LENGTH_SHORT).show();
                Log.i("TAG", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("TAG", "An error occurred: " + status);
            }
        });

        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                tripNameEndPoint = place.getName();
                latLngEndPointloc = place.getLatLng();

                Log.i(TAG, "##onPlaceSelected:## LatLng >> " + place.getLatLng());
                Log.i(TAG, "##onPlaceSelected:## LatLng >> " + place.getLatLng().latitude);

                // TODO: Hossam Get info about the selected place.
              //  Toast.makeText(getApplicationContext(), " " + place.getName(), Toast.LENGTH_SHORT).show();
                Log.i("TAG", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("TAG", "An error occurred: " + status);
            }
        });

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.i("TAG", "onTimeSet: ");
        time.setText(hourOfDay + ":" + minute);
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        date.setText(dayOfMonth + "/" + month + "/" + year);

    }


    @Override
    public void onTripSaveSuccess(String state) {
        // Toast.makeText(this, "Trip Added Successfully", Toast.LENGTH_SHORT).show();
    }


}
