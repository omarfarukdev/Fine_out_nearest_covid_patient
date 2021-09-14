package com.omar.find_out_nearest_covid_patient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreateAccountActivity extends AppCompatActivity {

    TextView birthdateTv;
    DatePickerDialog.OnDateSetListener mDateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        birthdateTv=findViewById(R.id.birthdate);
        mDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker , int year , int month , int day) {
                month = month + 1;

                Log.d( "onDateSet" , month + "/" + day + "/" + year );
                birthdateTv.setText( new StringBuilder().append( day ).append( "-" )
                        .append( month ).append( "-" ).append( year ) );
            }
        };

    }

    public void birthdate(View view) {

        final Calendar calendar=Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
        String currentdate= format.format(calendar.getTime());
        //birthdateTv.setText(currentdate);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog =  new DatePickerDialog(
                CreateAccountActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateListener,
                year,month,day
        );
        dialog.setTitle("Set Date");
        dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        dialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );

        dialog.show();
    }
}