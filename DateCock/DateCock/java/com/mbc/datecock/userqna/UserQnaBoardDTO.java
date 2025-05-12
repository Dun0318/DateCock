package com.mbc.datecock.userqna;

import java.util.Date;
import org.springframework.web.multipart.MultipartFile; // MultipartFile ����Ʈ

public class UserQnaBoardDTO {
    private int bno;
    private String title;
    private String content;
    private String writer;
    private Date regdate;
    private int viewcnt;
    // SECRET �ʵ� Ÿ���� char���� int�� ���� (0: ������, 1: ��б�)
    private int secret = 0; // �⺻�� ������(0)
    private String answerStatus = "�亯���";
    private String answerContent;
    private String answerWriter;
    private Date answerDate;

    // --- �̹��� ���� ���� �ʵ� �߰� ---
    private String imageFile; // DB�� ����� ���ϸ� �Ǵ� �� ���
    private MultipartFile qnaImageFile; // �۾���/���� �� ���� �����͸� �ޱ� ���� �ʵ� (DB ���� X)

    // �⺻ ������
    public UserQnaBoardDTO() {}

    // --- Getter and Setter ---

    public int getBno() { return bno; }
    public void setBno(int bno) { this.bno = bno; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }
    public Date getRegdate() { return regdate; }
    public void setRegdate(Date regdate) { this.regdate = regdate; }
    public int getViewcnt() { return viewcnt; }
    public void setViewcnt(int viewcnt) { this.viewcnt = viewcnt; }

    // secret �ʵ��� getter�� setter�� int Ÿ������ ����
    public int getSecret() { return secret; }
    public void setSecret(int secret) { this.secret = secret; }

    public String getAnswerStatus() { return answerStatus; }
    public void setAnswerStatus(String answerStatus) { this.answerStatus = answerStatus; }
    public String getAnswerContent() { return answerContent; }
    public void setAnswerContent(String answerContent) { this.answerContent = answerContent; }
    public String getAnswerWriter() { return answerWriter; }
    public void setAnswerWriter(String answerWriter) { this.answerWriter = answerWriter; }
    public Date getAnswerDate() { return answerDate; }
    public void setAnswerDate(Date answerDate) { this.answerDate = answerDate; }


    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public MultipartFile getQnaImageFile() {
        return qnaImageFile;
    }

    public void setQnaImageFile(MultipartFile qnaImageFile) {
        this.qnaImageFile = qnaImageFile;
    }

    // isSecretPost() �޼ҵ� ����: secret ���� 1�̸� true ��ȯ
    public boolean isSecretPost() {
        return this.secret == 1;
    }

    @Override
    public String toString() {
        return "UserQnaBoardDTO [bno=" + bno + ", title=" + title + ", writer=" + writer + ", secret=" + secret
                + ", answerStatus=" + answerStatus + ", imageFile=" + imageFile +"]";
    }
}