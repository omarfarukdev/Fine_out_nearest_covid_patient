package com.omar.find_out_nearest_covid_patient.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.omar.find_out_nearest_covid_patient.R;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateAccountActivity extends AppCompatActivity {
    TextView birthdateTv;
    DatePickerDialog.OnDateSetListener mDateListener;
    CircleImageView profileImage;
    ImageView addImage;
    EditText userNameEt, userStatusEt,userNIDEt;
    Spinner userGenderSp;
    Button createdAccount;

    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef, imageReference;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private Uri imageUri = null;
    private Bitmap imageBitmap=null;

    String currentdate,birthdate,currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        birthdateTv=findViewById(R.id.birthdate);
        profileImage=findViewById(R.id.profile_image);
        addImage=findViewById(R.id.addImage);
        userNameEt=findViewById(R.id.user_name);
        userStatusEt=findViewById(R.id.profile_status);
        userNIDEt=findViewById(R.id.nidNumberEdt);
        userGenderSp=findViewById(R.id.genderSp);
        createdAccount=findViewById(R.id.created_account);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getPhoneNumber());


        mDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker , int year , int month , int day) {
                month = month + 1;


                birthdate=(new StringBuilder().append( day ).append( "-" )
                        .append( month ).append( "-" ).append( year )).toString();

                Log.d( "onDateSet" , month + "/" + day + "/" + year +"  " +birthdate);
                birthdateTv.setText( birthdate );
            }
        };

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (ActivityCompat.checkSelfPermission(CreateAccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CreateAccountActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        imagePickerTypeBottomSheet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        createdAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                RootRef.child("Full Name").setValue(userNameEt.getText().toString());
                RootRef.child("Status").setValue(userStatusEt.getText().toString());
                RootRef.child("NID No").setValue(userNIDEt.getText().toString());
                RootRef.child("Gender").setValue(userGenderSp.getSelectedItem().toString());
                RootRef.child("BirthDate").setValue(birthdate);
                imageReference=RootRef.child("Image");


                progressDialog = new ProgressDialog(CreateAccountActivity.this);
                progressDialog.setTitle("Uploading....");
                progressDialog.setMessage("Please wait, we are sending this information...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                saveImage(imageBitmap);
            }
        });
    }
    private void imagePickerTypeBottomSheet() {
        final BottomSheetDialog dialog = new BottomSheetDialog(CreateAccountActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_selected_image);

        ImageButton cameraDialogImageBt = dialog.findViewById(R.id.cameraDialogImageBt);
        ImageButton gallaryDialogImageBt = dialog.findViewById(R.id.gallaryDialogImageBt);


        cameraDialogImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 2);
                dialog.dismiss();
            }
        });

        gallaryDialogImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 2 && resultCode == RESULT_OK ) {
            Bundle extras = data.getExtras();
            imageUri=data.getData();
            imageBitmap = (Bitmap) extras.get("data");
            profileImage.setImageBitmap(imageBitmap);

        }

        int count1 = 0;
        if (requestCode==1 && data!=null && data.getData()!=null){

            imageUri=data.getData();

            count1++;
            if (count1 == 1) {
                imageUri = data.getData();


                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex1 = cursor.getColumnIndex(filePathColumn[0]);

                String filePath1 = cursor.getString(columnIndex1);
                Log.d("FAaa",""+filePath1);
                Toast.makeText(CreateAccountActivity.this, "Call", Toast.LENGTH_SHORT).show();
                imageBitmap=BitmapFactory.decodeFile(filePath1);
                profileImage.setImageURI(imageUri);


            }

        }
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
    public String saveImage(Bitmap myBitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UserProfileImageRef= FirebaseStorage.getInstance().getReference("images/"+currentUserId);

        UploadTask uploadTask = UserProfileImageRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                UserProfileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageReference.setValue(uri.toString());
                    }
                });

                Toast.makeText(CreateAccountActivity.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                Intent intent=new Intent(CreateAccountActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(CreateAccountActivity.this, "Failure", Toast.LENGTH_SHORT).show();

            }
        });

        return "";
    }
}