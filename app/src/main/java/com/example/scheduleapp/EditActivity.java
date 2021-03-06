package com.example.scheduleapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditActivity extends AppCompatActivity {
    TextView name, id, email, phone;
    EditText newFName, newLName, newId, newEmail, newPhone;
    String fname, lname, idNum, tempEmail, tempPhone, fullname;
    Button delete, update, cancel;
    String[] names;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        newFName = findViewById(R.id.edit_fname);
        newLName = findViewById(R.id.edit_lname);
        newId = findViewById(R.id.edit_id);
        newEmail = findViewById(R.id.edit_email);
        newPhone = findViewById(R.id.edit_phone);
        delete = findViewById(R.id.delete_button);
        update = findViewById(R.id.update_employee);
        cancel = findViewById(R.id.canceledit);
        name = findViewById(R.id.name_edit_display);
        id = findViewById(R.id.id_edit_display);
        email = findViewById(R.id.email_edit_display);
        phone = findViewById(R.id.phone_edit_display);
        name.setText(extras.getString("name"));
        fullname = extras.getString("name");
        names = fullname.split(" ");
        id.setText("ID: "+extras.getString("id"));
        idNum = extras.getString("id");
        email.setText("Email: " + extras.getString("email"));
        tempEmail = extras.getString("email");
        phone.setText("Phone #: " +extras.getString("phone"));
        tempPhone = extras.getString("phone");
        fname = names[0];
        lname = names[1];

        CollectionReference ref = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .collection("employees");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Query query = ref.whereEqualTo("name" , extras.getString("name")).whereEqualTo("id" , extras.getString("id"));
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot snapshot : task.getResult()) {
                                ref.document(snapshot.getId()).delete();
                            }
                            Toast.makeText(EditActivity.this ,
                                           "Employee Deleted.." ,
                                           Toast.LENGTH_SHORT).show();
                            CollectionReference delShift = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                                    .collection("shifts");
                            Query query = delShift.whereEqualTo("name" , extras.getString("name"));
                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot snap : task.getResult()){
                                            delShift.document(snap.getId()).delete();
                                        }
                                        Intent intent = new Intent(EditActivity.this , MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                    //.whereEqualTo("name", extras.getString("name")).whereEqualTo("id", extras.getString("id"))
                    //.whereEqualTo("email",extras.getString("email")).whereEqualTo("phoneNum",extras.getString("phone"));

                });
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(EditActivity.this, ViewEmployees.class);
                startActivity(a);
                finish();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder fieldsNotUpdated = new StringBuilder();
                if(AddNewEmployee.nameIsValid(newFName.getText().toString())) {//newFName.getText().toString().trim().length() > 0)
                    fname= newFName.getText().toString();
                }
                else if (newFName.getText().toString().length() > 0) {
                    fieldsNotUpdated.append(" first name must only contain letters\n");
                }
                if(AddNewEmployee.nameIsValid(newLName.getText().toString())) {//newLName.getText().toString().trim().length() > 0)
                    lname = newLName.getText().toString();
                }
                else if (newLName.getText().toString().length() > 0) {
                    fieldsNotUpdated.append(" last name must only contain letters\n");
                }
                // Redundant/no change id check added to condition to allow user to enter the original id in for update
                // value without receiving an error.
                if(AddNewEmployee.idIsValid(newId.getText().toString())
                   && (idNum != newId.getText().toString() && AddNewEmployee.idIsUnique(newId.getText().toString()))) { //newId.getText().toString().trim().length() > 0){
                    idNum = newId.getText().toString();
                    MainActivity.ids.remove(idNum);
                    MainActivity.ids.add(newId.getText().toString());
                }
                else if (newId.getText().toString().length() > 0) {
                    fieldsNotUpdated.append(" id" + (!AddNewEmployee.idIsValid(newId.getText().toString()) ?
                                                    " must contain only numbers\n" : " already exists\n"));
                }
                if(AddNewEmployee.isValidEmail(newEmail.getText().toString())) {//newEmail.getText().toString().trim().length() > 0){
                    tempEmail = newEmail.getText().toString();
                }
                else if (newEmail.getText().toString().length() > 0) {
                    fieldsNotUpdated.append(" email must be in the form \"username@website.top-level-domain\"\n");
                }
                if(AddNewEmployee.isValidPhoneNum(newPhone.getText().toString())) {//newPhone.getText().toString().trim().length() > 0)
                    tempPhone = newPhone.getText().toString();
                }
                else if (newPhone.getText().toString().length() > 0) {
                    fieldsNotUpdated.append(" phone number must contain at least 10 numbers with no other symbols.\n");
                }
                if (fieldsNotUpdated.toString().length() > 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(EditActivity.this).create();
                    alertDialog.setTitle("Error updating Employee");
                    alertDialog.setMessage("Employee's information was not updated:\n\n" + fieldsNotUpdated.toString());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }
                Employee e = new Employee();
                e.setName(fname + " " + lname);
                e.setId(idNum);
                e.setEmail(tempEmail);
                e.setPhoneNum(tempPhone);
                ref.whereEqualTo("name", fullname).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot snapshot : task.getResult())
                        {
                            ref.document(snapshot.getId()).set(e);
                        }
                        CollectionReference refShift = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                                .collection("shifts");
                        refShift.whereEqualTo("name" , extras.getString("name"))
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(QueryDocumentSnapshot doc : task.getResult()){
                                        refShift.document(doc.getId()).update("name", fname + " " + lname);
                                    }
                                    Intent intent1 = new Intent(EditActivity.this , MainActivity.class);
                                    startActivity(intent1);
                                    finish();
                                }
                            }
                        });
                    }
                });
                /*"name", fname + " " + lname,
                "id", idNum,
                "email", tempEmail,
                "phoneNum", tempPhone*/


            }
        });

    }
}
