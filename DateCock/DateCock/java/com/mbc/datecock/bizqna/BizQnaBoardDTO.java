package com.mbc.datecock.bizqna; // ��Ű���� ����

import java.util.Date;
import org.springframework.web.multipart.MultipartFile;

public class BizQnaBoardDTO { // Ŭ������ ����
    private int bno;
    private String title;
    private String content;
    private String writer; // ����ڵ�Ϲ�ȣ ����
    private Date regdate;
    private int viewcnt;
    // private int secret = 0; // ����: int Ÿ��, �⺻�� 0 (������)
    private Integer secret; // *** ����: Integer Ÿ������ ���� (null ���) ***
    private String answerStatus = "�亯���";
    private String answerContent;
    private String answerWriter; // ������ ID ����
    private Date answerDate;
    private String imageFile;
    private MultipartFile qnaImageFile; // ������ ���� ���� �� ���
    private String businessName;
    public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	// �⺻ ������
    public BizQnaBoardDTO() {}

    // --- Getter and Setter ---
    public int getBno() { return bno; }
    public void setBno(int bno) { this.bno = bno; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getWriter() { return writer; } // ����� ��ȣ�� ����� ����
    public void setWriter(String writer) { this.writer = writer; }
    public Date getRegdate() { return regdate; }
    public void setRegdate(Date regdate) { this.regdate = regdate; }
    public int getViewcnt() { return viewcnt; }
    public void setViewcnt(int viewcnt) { this.viewcnt = viewcnt; }

    // *** ����: secret �ʵ��� getter�� setter�� Integer Ÿ������ ***
    public Integer getSecret() { return secret; }
    public void setSecret(Integer secret) { this.secret = secret; }

    public String getAnswerStatus() { return answerStatus; }
    public void setAnswerStatus(String answerStatus) { this.answerStatus = answerStatus; }
    public String getAnswerContent() { return answerContent; }
    public void setAnswerContent(String answerContent) { this.answerContent = answerContent; }
    public String getAnswerWriter() { return answerWriter; }
    public void setAnswerWriter(String answerWriter) { this.answerWriter = answerWriter; }
    public Date getAnswerDate() { return answerDate; }
    public void setAnswerDate(Date answerDate) { this.answerDate = answerDate; }
    public String getImageFile() { return imageFile; }
    public void setImageFile(String imageFile) { this.imageFile = imageFile; }
    public MultipartFile getQnaImageFile() { return qnaImageFile; }
    public void setQnaImageFile(MultipartFile qnaImageFile) { this.qnaImageFile = qnaImageFile; }

    // *** ����: isSecretPost() �޼ҵ忡�� null ���� �� ��� ***
    public boolean isSecretPost() {
        // secret �ʵ尡 null�� �ƴϰ�, �� ���� 1�� ������ true ��ȯ
        return Integer.valueOf(1).equals(this.secret);
    }

    @Override
    public String toString() {
        return "BizQnaBoardDTO [bno=" + bno + ", title=" + title + ", writer=" + writer
                + ", businessName=" + businessName // �߰��� �ʵ� ����
                + ", secret=" + secret + ", answerStatus=" + answerStatus + ", imageFile=" + imageFile +"]";
    }
}