package com.example.cpsadmin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class QuestionsActivity extends AppCompatActivity {


    private Button add,excel;
    private RecyclerView recyclerview;
    private QuestionsAdapter adapter;
    public static   List<QuestionModel> list;
    private Dialog loadingDialog;
    private DatabaseReference myRef;
    private  String setId;
    private String categoryName;
    public  static  final  int CELL_COUNT = 6;
    private TextView loadingText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        Toolbar toolbar = findViewById(R.id.toolbar);

        myRef = FirebaseDatabase.getInstance().getReference();

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        loadingText = loadingDialog.findViewById(R.id.textview);

        setSupportActionBar(toolbar);

        categoryName = getIntent().getStringExtra("category");
        setId = getIntent().getStringExtra("setId");
        getSupportActionBar().setTitle(categoryName);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        add = findViewById(R.id.add_btn);
        excel = findViewById(R.id.excel_btn);
        recyclerview = findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerview.setLayoutManager(layoutManager);
        list = new ArrayList<>();


        adapter = new QuestionsAdapter(list,categoryName, new QuestionsAdapter.DeleteListener() {
            @Override
            public void OnLongClick(final int position, final String id) {
                new AlertDialog.Builder(QuestionsActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Question")
                        .setMessage("Are you sure. you want to delete this question?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadingDialog.show();
                                myRef.child("SETS").child(setId).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                        list.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        }else {
                                            Toast.makeText(QuestionsActivity.this, "Failed to delete !", Toast.LENGTH_SHORT).show();
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
        recyclerview.setAdapter(adapter);

        getData(categoryName,setId);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           Intent addQuestion = new Intent(QuestionsActivity.this,AddQuestionActivity.class);
           addQuestion.putExtra("categoryName",categoryName);
           addQuestion.putExtra("setId",setId);
           startActivity(addQuestion);
            }
        });


        excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(QuestionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == getPackageManager().PERMISSION_GRANTED){
                    selectFile();
                }else {
                    ActivityCompat.requestPermissions(QuestionsActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},101);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectFile();
            }else {
                Toast.makeText(this, "Please Grant Permissions!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void  selectFile(){

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent,"Select File"),102);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 102){
            if (resultCode == RESULT_OK){

                String filepath = data.getData().getPath();

                if (filepath.endsWith(".xlsx")){
                    readFile(data.getData());
                }else {
                    Toast.makeText(this, "Please choose an Excel file", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private  void  getData(String categoryName, final String setId){

        loadingDialog.show();
        myRef
                .child("SETS").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot dataSnapshot1 : datasnapshot.getChildren()){
                    String id = dataSnapshot1.getKey();
                    String question = dataSnapshot1.child("question").getValue().toString();
                    String optionA = dataSnapshot1.child("optionA").getValue().toString();
                    String optionB = dataSnapshot1.child("optionB").getValue().toString();
                    String optionC = dataSnapshot1.child("optionC").getValue().toString();
                    String optionD = dataSnapshot1.child("optionD").getValue().toString();
                    String correctANS = dataSnapshot1.child("correctANS").getValue().toString();

                    list.add(new QuestionModel(id,question,optionA,optionB,optionC,optionD,correctANS,setId));

                }
                loadingDialog.dismiss();
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });

    }

    private  void readFile(final Uri fileUri){
        loadingText.setText("Scanning Questions.......");
        loadingDialog.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {


                final HashMap<String, Object> parentMap = new HashMap<>();
                final List<QuestionModel> tempList = new ArrayList<>();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(fileUri);
                    XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                    int rowsCount = sheet.getPhysicalNumberOfRows();

                    if (rowsCount > 0) {

                        for (int r = 0; r < rowsCount; r++) {
                            Row row = sheet.getRow(r);

                            if (row.getPhysicalNumberOfCells() == CELL_COUNT) {

                                String question = getcellData(row, 0, formulaEvaluator);
                                String a = getcellData(row, 1, formulaEvaluator);
                                String b = getcellData(row, 2, formulaEvaluator);
                                String c = getcellData(row, 3, formulaEvaluator);
                                String d = getcellData(row, 4, formulaEvaluator);
                                String correctAns = getcellData(row, 5, formulaEvaluator);

                                if (correctAns.equals(a) || correctAns.equals(b) || correctAns.equals(c) || correctAns.equals(d)) {

                                    HashMap<String, Object> questionMap = new HashMap<>();
                                    questionMap.put("question", question);
                                    questionMap.put("optionA", a);
                                    questionMap.put("optionB", b);
                                    questionMap.put("optionC", c);
                                    questionMap.put("optionD", d);
                                    questionMap.put("correctANS", correctAns);
                                    questionMap.put("setId", setId);

                                    String id = UUID.randomUUID().toString();

                                    parentMap.put(id, questionMap);

                                    tempList.add(new QuestionModel(id, question, a, b, c, d, correctAns, setId));

                                } else {

                                    final int finalR1 = r;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            loadingText.setText("Loading.....");
                                            loadingDialog.dismiss();
                                            Toast.makeText(QuestionsActivity.this, "Row no.  " + (finalR1 + 1) + "has no correct options", Toast.LENGTH_SHORT).show();

                                        }
                                    });

                                    return;

                                }

                            } else {

                                final int finalR = r;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingText.setText("Loading.....");
                                        loadingDialog.dismiss();
                                        Toast.makeText(QuestionsActivity.this, "Row no.  " + (finalR + 1) + "has incorrect data", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                return;
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("SETS").child(setId)
                                        .updateChildren(parentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            list.addAll(tempList);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            loadingText.setText("Loading.......");
                                            Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingDialog.dismiss();
                                    }
                                });

                            }
                        });

                        loadingText.setText("uploading....");


                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingText.setText("Loading.....");
                                Toast.makeText(QuestionsActivity.this, "File is Empty", Toast.LENGTH_SHORT).show();
                            }
                        });

                        return;
                    }

                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingText.setText("Loading.....");
                            Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (final IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingText.setText("Loading.....");
                            loadingDialog.dismiss();
                            Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    private  String  getcellData(Row row,int cellposition,FormulaEvaluator formulaEvaluator){
        String value = "";

        Cell cell = row.getCell(cellposition);

        switch (cell.getCellType()){

            case  Cell.CELL_TYPE_BOOLEAN:
            return value+cell.getBooleanCellValue();

            case Cell.CELL_TYPE_NUMERIC:
                return  value+cell.getNumericCellValue();

            case Cell.CELL_TYPE_STRING:
                return value+cell.getStringCellValue();

            default:
                return value;
        }
    }

}
