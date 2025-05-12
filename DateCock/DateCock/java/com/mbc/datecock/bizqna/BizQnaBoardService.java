package com.mbc.datecock.bizqna; // ��Ű���� ����

import java.util.List;

import com.mbc.datecock.board.PageDTO; // ���� PageDTO ���
// import com.mbc.datecock.bizqna.BizQnaBoardDTO; // DTO ����Ʈ

public interface BizQnaBoardService { // �������̽��� ����

    // �� ��� (����¡)
    List<BizQnaBoardDTO> getBizQnaListPaged(PageDTO pageDTO) throws Exception; // �޼ҵ��, ��ȯŸ�� ����

    // ��ü �� ����
    int getTotalBizQnaCount(PageDTO pageDTO) throws Exception; // �޼ҵ�� ����

    // �۾��� (����ڸ�)
    boolean writeBizQna(BizQnaBoardDTO dto) throws Exception; // �޼ҵ��, �Ķ���� Ÿ�� ����

    // �󼼺��� (��ȸ�� ���� ����, ���� ���� ó��)
    // currentUserId�� ���⼭�� ����ڹ�ȣ �Ǵ� ������ ID�� �� �� ����
    BizQnaBoardDTO getBizQnaDetail(int bno, String currentUserId, boolean isAdmin) throws Exception; // �޼ҵ��, ��ȯŸ�� ����

    // �� ���� (�ۼ��� ����� �Ǵ� �����ڸ�)
    boolean updateBizQna(BizQnaBoardDTO dto, String currentUserId, boolean isAdmin) throws Exception; // �޼ҵ��, �Ķ���� Ÿ�� ����

    // �� ���� (�ۼ��� ����� �Ǵ� �����ڸ�)
    boolean deleteBizQna(int bno, String currentUserId, boolean isAdmin) throws Exception; // �޼ҵ�� ����

    // ������ �亯 ���/����
    boolean saveAnswer(BizQnaBoardDTO dto, String adminId) throws Exception; // �Ķ���� Ÿ�� ����

    // (���ο�) �� ���� �������� (����/���� �� ���� Ȯ�ο� - ��ȸ�� ���� ����)
    BizQnaBoardDTO getBizQnaForAuth(int bno) throws Exception; // �޼ҵ��, ��ȯŸ�� ����
}