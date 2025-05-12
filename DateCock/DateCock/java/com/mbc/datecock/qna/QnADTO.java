package com.mbc.datecock.qna;

import java.sql.Date;

public class QnADTO {
	public QnADTO(int qnanum, String title, String content, String writer, String type, String email, String phone,
			String notifyType, String answer, Date regdate) {
		super();
		this.qnanum = qnanum;
		this.title = title;
		this.content = content;
		this.writer = writer;
		this.type = type;
		this.email = email;
		this.phone = phone;
		this.notifyType = notifyType;
		this.answer = answer;
		this.regdate = regdate;
	}
	public QnADTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	private int qnanum;         // QnA ��ȣ (PK)
    private String title;       // ����
    private String content;     // ���� ����
    private String writer;      // �ۼ��� (����ڹ�ȣ or guest)
    private String type;        // user / business
    private String email;       // �̸���
    private String phone;       // �޴��� ��ȣ
    private String notifyType;  // �亯 �˸� ��� (email, sms, kakao)
    private String answer;      // ������ �亯
    private Date regdate;       // �ۼ�����
	public int getQnanum() {
		return qnanum;
	}
	public void setQnanum(int qnanum) {
		this.qnanum = qnanum;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getWriter() {
		return writer;
	}
	public void setWriter(String writer) {
		this.writer = writer;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getNotifyType() {
		return notifyType;
	}
	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public Date getRegdate() {
		return regdate;
	}
	public void setRegdate(Date regdate) {
		this.regdate = regdate;
	}
    
    
}
