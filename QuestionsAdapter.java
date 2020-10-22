package com.example.cpsadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.viewHolder> {

    private List<QuestionModel> list;
   private  String category;
   private  DeleteListener listener;

    public QuestionsAdapter(List<QuestionModel> list,String category,DeleteListener listener) {
        this.list = list;
        this.category = category;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
     String question = list.get(position).getQuestion();
     String answer = list.get(position).getAnswer();

     holder.setData(question,answer,position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        private TextView question,answer;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            question = (TextView) itemView.findViewById(R.id.question);
            answer = (TextView) itemView.findViewById(R.id.answer);
        }


        private  void  setData(String question,String answer, final int position){
            this.question.setText(position+1+". " +question);
            this.answer.setText("Ans. "+answer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent editIntent = new Intent(itemView.getContext(),AddQuestionActivity.class);
                    editIntent.putExtra("categoryName",category);
                    editIntent.putExtra("setId",list.get(position).getSet());
                    editIntent.putExtra("position",position);
                    itemView.getContext().startActivity(editIntent);
                }
            });



            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.OnLongClick(position,list.get(position).getId());
                    return false;
                }
            });
        }
    }

    public   interface  DeleteListener{
        void OnLongClick(int position,String id);
    }

}
