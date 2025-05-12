package com.mbc.datecock.board;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface BoardService {

	boolean deleteBoard(int bno, String userId) throws Exception;

	boolean writeBoard(BoardDTO dto);

	BoardDTO getBoardDetail(int bno);

	void updateReplyCnt(int bno, int amount) throws Exception; 
	
	/**
     * Ư�� �Խñ��� ���ƿ� ���� 1 ������ŵ�ϴ�.
     * @param bno �Խñ� ��ȣ
     * @return ������Ʈ�� ���� �� (���� 1)
     */
    boolean increaseLikeCount(int bno) throws Exception ;
    
    /**
     * Ư�� �Խñ��� ���� ���ƿ� ���� ��ȸ�մϴ�.
     * @param bno �Խñ� ��ȣ
     * @return ���� ���ƿ� �� (Integer Ÿ�� ���� - ��� ���� �� null ��ȯ ����)
     */
    int getLikeCount(int bno) throws Exception;

	int getTotalBoardCount(PageDTO countCriteria) throws Exception;

	List<BoardDTO> getBoardListPaged(PageDTO pageDTO) throws Exception;
	
	/**
     * �Խñ� ����
     * @param dto ������ ������ ��� DTO (bno ����)
     * @param userId ���� ��û ����� ID (�ۼ��� Ȯ�ο�)
     * @return ���� ���� ����
     * @throws Exception
     */
	boolean updateBoard(BoardDTO dto, String userId) throws Exception;
}
