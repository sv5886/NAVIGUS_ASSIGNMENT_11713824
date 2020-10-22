package com.example.cpsadmin;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.*;

public class CategoryActivity<dialog, uri> extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private CircleImageView addImage;
    private EditText categoryname;
    private Button addBtn;
    private Dialog loadingDialog, categoryDialog;
    private  RecyclerView recyclerView;
    private Uri  image;
    private String downloadUrl;
    public static List<CategoryModel> list;
    private   CategoryAdapter adapter;
    private CategoryAdapter.DeleteListener deleteListener;

    public CategoryActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        setCategoryDialog();

        recyclerView = findViewById(R.id.rv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);

        list = new ArrayList<>();
        adapter = new CategoryAdapter(list,new CategoryAdapter.DeleteListener() {
            @Override
            public void onDelete(final String key, final int position) {

                new AlertDialog.Builder(CategoryActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete category")
                        .setMessage("Are you sure. you want to delete this category?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadingDialog.show();
                                myRef.child("Categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){

                                            for (String setIds : list.get(position).getSets()){
                                                myRef.child("SETS").child(setIds).removeValue();
                                            }
                                            list.remove(position);
                                            adapter.notifyDataSetChanged();
                                           loadingDialog.dismiss();
                                        }else{
                                            Toast.makeText(CategoryActivity.this, "Failed to delete !", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        recyclerView.setAdapter(adapter);

        loadingDialog.show();

        myRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    List<String>  sets =  new ArrayList<>();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("sets").getChildren()){
                        sets.add(dataSnapshot2.getKey());
                    }

                    list.add(new CategoryModel(dataSnapshot1.child("name").getValue().toString(),
                            sets,
                            dataSnapshot1.child("url").getValue().toString(),
                            dataSnapshot1.getKey()
                            ));
                }
                adapter.notifyDataSetChanged();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CategoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            categoryDialog.show();
        }

        if (item.getItemId() == R.id.logout){

            new AlertDialog.Builder(CategoryActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                    .setTitle("Logout")
                    .setMessage("Are you sure. you want to logout?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loadingDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(CategoryActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel",null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }


    private void setCategoryDialog() {

        categoryDialog = new Dialog(this);
        categoryDialog.setContentView(R.layout.add_category_dialog);
        categoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_box));
        categoryDialog.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        categoryDialog.setCancelable(true);

        addImage = categoryDialog.findViewById(R.id.image);
        categoryname = categoryDialog.findViewById(R.id.categoryName);
        addBtn = categoryDialog.findViewById(R.id.add);


        addImage.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                      MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
              startActivityForResult(galleryIntent,101);
          }
      });

       addBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               if (categoryname.getText() == null || categoryname.getText().toString().isEmpty()) {
                   categoryname.setError("Required");
                   return;
               }

               for (CategoryModel model : list){
                  if  (categoryname.getText().toString().equals(model.getName())){
                      categoryname.setError("Category name Already present");
                      return;
                  }
               }

               if (image == null) {
                   Toast.makeText(CategoryActivity.this, "please select your image", Toast.LENGTH_SHORT).show();
                   return;
               }
               categoryDialog.dismiss();
               uploadData();
           }
       });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101){
            if (resultCode == RESULT_OK){
                image = data.getData();
                addImage.setImageURI(image);
            }
        }
    }

    private  void uploadData(){
        loadingDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        final StorageReference imageReference = storageReference.child("Categories").child(image.getLastPathSegment());


        UploadTask uploadTask = imageReference.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            downloadUrl = task.getResult().toString();
                            uploadCategoryName();
                        }else{
                            loadingDialog.dismiss();
                            Toast.makeText(CategoryActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    Toast.makeText(CategoryActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            }
        });

    }

    private  void  uploadCategoryName(){

        Map<String,Object> map = new HashMap<>();
        map.put("name",categoryname.getText().toString());
        map.put("sets",0);
        map.put("url",downloadUrl);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final String id = UUID.randomUUID().toString();

        database.getReference().child("Categories").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    list.add(new CategoryModel(categoryname.getText().toString(),new ArrayList<String>(),downloadUrl,id));
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(CategoryActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });

    }
}
