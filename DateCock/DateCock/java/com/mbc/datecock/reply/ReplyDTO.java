package com.mbc.datecock.reply; // <<<--- ���� ��Ű�� ��η� ����!

import java.util.Date;

public class ReplyDTO {
    private int rno;
    private int bno;
    private String replytext;
    private String replyer;
    private Date replydate;
    private Date updatedate;

    // �⺻ ������
    public ReplyDTO() {}

    // --- ��� �ʵ� Getter & Setter ---
    // (Lombok @Data ��� ����)
    public int getRno() { return rno; }
    public void setRno(int rno) { this.rno = rno; }
    public int getBno() { return bno; }
    public void setBno(int bno) { this.bno = bno; }
    public String getReplytext() { return replytext; }
    public void setReplytext(String replytext) { this.replytext = replytext; }
    public String getReplyer() { return replyer; }
    public void setReplyer(String replyer) { this.replyer = replyer; }
    public Date getReplydate() { return replydate; }
    public void setReplydate(Date replydate) { this.replydate = replydate; }
    public Date getUpdatedate() { return updatedate; }
    public void setUpdatedate(Date updatedate) { this.updatedate = updatedate; }

    @Override
    public String toString() {
        return "ReplyDTO [rno=" + rno + ", bno=" + bno + ", replyer=" + replyer + "]";
    }
}