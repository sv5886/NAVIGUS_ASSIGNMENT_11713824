package com.example.cpsadmin;

import android.app.Dialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AddQuestionActivity extends AppCompatActivity {

    private EditText question;
    private RadioGroup options;
    private LinearLayout answer;
    private Button uploadBtn;
    private String categoryName;
    private  int position;
    private Dialog loadingDialog;
    private QuestionModel questionModel;
    private String id,setId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("Add question");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);


        question = findViewById(R.id.question);
        options = findViewById(R.id.options);
        answer = findViewById(R.id.answer);
        uploadBtn = findViewById(R.id.button);

        categoryName = getIntent().getStringExtra("categoryName");
        setId = getIntent().getStringExtra("setId");
        position = getIntent().getIntExtra("position",-1);
        if (setId == null){
            finish();
            return;
        }

        if (position != -1){
            questionModel = QuestionsActivity.list.get(position);
            setData();
        }

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (question.getText().toString().isEmpty()){
                    question.setError("Required");
                    return;
                }
                upload();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private  void setData(){

        question.setText(questionModel.getQuestion());

        ((EditText)answer.getChildAt(0)).setText(questionModel.getA());
        ((EditText)answer.getChildAt(1)).setText(questionModel.getB());
        ((EditText)answer.getChildAt(2)).setText(questionModel.getC());
        ((EditText)answer.getChildAt(3)).setText(questionModel.getD());


        for (int i =0;i < answer.getChildCount();i++){

            if ( ((EditText)answer.getChildAt(i)).getText().toString().equals(questionModel.getAnswer())){
                RadioButton radioButton = (RadioButton) options.getChildAt(i);
                radioButton.setChecked(true);
                break;

            }

        }

    }

    private  void  upload() {
        int correct = -1;

        for (int i = 0; i < options.getChildCount(); i++) {

            EditText answers = (EditText) answer.getChildAt(i);
            if (answers.getText().toString().isEmpty()) {
                answers.setError("Required");
                return;
            }

            RadioButton radioButton = (RadioButton) options.getChildAt(i);

            if (radioButton.isChecked()) {
                correct = 1;
                break;
            }
        }

        if (correct == -1) {
            Toast.makeText(this, "please mark the correct options", Toast.LENGTH_SHORT).show();
            return;
        }

            final HashMap<String, Object> map = new HashMap<>();
            map.put("correctANS", ((EditText) answer.getChildAt(correct)).getText().toString());
            map.put("optionD", ((EditText) answer.getChildAt(3)).getText().toString());
            map.put("optionC", ((EditText) answer.getChildAt(2)).getText().toString());
            map.put("optionB", ((EditText) answer.getChildAt(1)).getText().toString());
            map.put("optionA", ((EditText) answer.getChildAt(0)).getText().toString());
            map.put("question", question.getText().toString());
            map.put("setId", setId);

            loadingDialog.show();
            if (position != -1) {
                id = questionModel.getId();
            } else {
                id = UUID.randomUUID().toString();
            }

            FirebaseDatabase.getInstance().getReference()
                    .child("SETS").child(setId).child(id)
                    .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        QuestionModel questionModel = new QuestionModel(id,
                                map.get("question").toString(),
                                map.get("optionA").toString(),
                                map.get("optionB").toString(),
                                map.get("optionC").toString(),
                                map.get("optionD").toString(),
                                map.get("correctANS").toString(),
                                map.get("setId").toString());

                        if (position != -1) {
                            QuestionsActivity.list.set(position, questionModel);
                        } else {
                            QuestionsActivity.list.add(questionModel);
                        }
                        finish();

                    } else {
                        Toast.makeText(AddQuestionActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                }
            });
        }


}
