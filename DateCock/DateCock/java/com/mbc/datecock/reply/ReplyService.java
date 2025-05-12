package com.mbc.datecock.reply; // <<<--- ���� ��Ű�� ��η� ����!

import java.util.List;

public interface ReplyService {

    /** Ư�� �Խñ��� ��� ��� ��ȸ */
    public List<ReplyDTO> getCommentList(int bno) throws Exception;

    /** �� ��� ��� */
    public boolean addComment(ReplyDTO dto) throws Exception;

    /** ��� ���� (�ۼ��� Ȯ�� ������ Impl����) */
    public boolean modifyComment(ReplyDTO dto) throws Exception;

    /** ��� ���� (�ۼ��� Ȯ�� ������ Impl����) */
    public boolean removeComment(int rno, String userId) throws Exception;

    /** (����) ��� 1�� ���� ��ȸ */
    public ReplyDTO getComment(int rno) throws Exception;
    // �ڡڡ� ��� �ܰ� ��ȸ �޼ҵ� �߰� �ڡڡ�
    ReplyDTO getReply(int rno) throws Exception;
}