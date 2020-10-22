package com.example.cpsadmin;

public class QuestionModel {

    private  String id,question,optionA,optionB,optionC,optionD,answer,set;

    public QuestionModel(String id, String question, String optionA, String optionB, String optionC, String optionD, String answer, String set) {
        this.id = id;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD= optionD;
        this.answer = answer;
        this.set = set;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getA() {
        return optionA;
    }

    public void setA(String a) {
        optionB = a;
    }

    public String getB() {
        return optionB;
    }

    public void setB(String b) {
        optionB = b;
    }

    public String getC() {
        return optionC;
    }

    public void setC(String c) {
        optionC = c;
    }

    public String getD() {
        return optionD;
    }

    public void setD(String d) {
        optionD = d;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }
}
