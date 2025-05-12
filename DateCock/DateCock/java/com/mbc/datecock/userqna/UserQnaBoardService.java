package com.mbc.datecock.userqna;

import java.util.List;
import com.mbc.datecock.board.PageDTO; // ���� PageDTO ���

public interface UserQnaBoardService {

    // �� ��� (����¡)
    List<UserQnaBoardDTO> getUserQnaListPaged(PageDTO pageDTO) throws Exception;

    // ��ü �� ����
    int getTotalUserQnaCount(PageDTO pageDTO) throws Exception;

    // �۾���
    boolean writeUserQna(UserQnaBoardDTO dto) throws Exception;

    // �󼼺��� (��ȸ�� ���� ����, ���� ���� ó��)
    UserQnaBoardDTO getUserQnaDetail(int bno, String currentUserId, boolean isAdmin) throws Exception;

    // �� ���� (�ۼ��� �Ǵ� �����ڸ�)
    boolean updateUserQna(UserQnaBoardDTO dto, String currentUserId, boolean isAdmin) throws Exception;

    // �� ���� (�ۼ��� �Ǵ� �����ڸ�)
    boolean deleteUserQna(int bno, String currentUserId, boolean isAdmin) throws Exception;

    // ������ �亯 ���/����
    boolean saveAnswer(UserQnaBoardDTO dto, String adminId) throws Exception;

    // (���ο�) �� ���� �������� (����/���� �� ���� Ȯ�ο� - ��ȸ�� ���� ����)
    UserQnaBoardDTO getUserQnaForAuth(int bno) throws Exception;
}