package com.mbc.datecock.reply; // <<<--- ���� ��Ű�� ��η� ����!

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Ʈ����� ���

import com.mbc.datecock.board.BoardService;

import org.apache.ibatis.session.SqlSession; // SqlSession ���

// import com.mbc.datecock.board.BoardMapper; // �Խ��� ��� �� ������Ʈ �� �ʿ�

@Service // <<<--- ���� �� ���!
public class ReplyServiceImpl implements ReplyService {
	@Autowired
	private BoardService boardService; 
    private static final Logger log = LoggerFactory.getLogger(ReplyServiceImpl.class);

    @Autowired
    private SqlSession sqlSession; // <<<--- SqlSession ����!

    // ReplyMapper.xml�� namespace�� ��ġ�ϴ� ������ �� (���� '.' ����)
    private static final String NAMESPACE = "com.mbc.datecock.reply.mapper.ReplyMapper."; // <<<--- ���� namespace Ȯ��!

    // @Autowired
    // private BoardMapper boardMapper; // �Խ��� ��� �� ������Ʈ �� �ʿ�

    @Override
    public List<ReplyDTO> getCommentList(int bno) throws Exception {
        log.info("getCommentList({}) - ��� ��� ��ȸ", bno);
        // NAMESPACE + "listByBno" �� XML�� select ȣ��
        return sqlSession.selectList(NAMESPACE + "listByBno", bno);
    }
    
    
    @Transactional // ��� ��� + (������) �Խñ� ��� �� ������Ʈ�� ����
    @Override
    public boolean addComment(ReplyDTO dto) throws Exception {
        log.info("addComment({}) - �� ��� ���", dto);
        // TODO: �ʿ�� XSS ���� ó�� ���� �߰� (��: Lucy XSS Filter �� ���)

        // NAMESPACE + "insert" �� XML�� insert ȣ��
        

        // TODO: �Խ��� ���̺� ��� �� ������Ʈ ���� (BoardMapper �ʿ�)
        // if (insertCount == 1) {
        //     boardMapper.updateReplyCnt(dto.getBno(), 1); // ��� �� 1 ����
        // }
        int insertCount = sqlSession.insert(NAMESPACE + "insert", dto);
        if (insertCount == 1) {
            try {
                // �ڡڡ� boardService ȣ�� �ڡڡ�
                boardService.updateReplyCnt(dto.getBno(), 1); 
                log.info("{}�� �Խñ� ��� �� 1 ���� (via BoardService)", dto.getBno());
            } catch (Exception e) { /* ���� ó�� */ throw e; }
        }
        return insertCount == 1; 
        
        
        
        // 1�� �� ���� �� ����
    }

    @Transactional // ��� ���� ��ȸ(�ۼ��� Ȯ��) + ���� �ʿ�� Ʈ�����
    @Override
    public boolean modifyComment(ReplyDTO dto) throws Exception {
        log.info("modifyComment({}) - ��� ����", dto);
        // TODO: XSS ���� ó��

        // TODO: �ۼ��� ���� Ȯ�� ���� ���� (�ʼ�!)
        // ReplyDTO originalReply = getComment(dto.getRno()); // �Ʒ� getComment �޼ҵ� ���
        // if (originalReply == null || !originalReply.getReplyer().equals(dto.getReplyer())) {
        //     log.warn("��� ���� ���� ����: rno={}, ��û��={}", dto.getRno(), dto.getReplyer());
        //     return false; // ���� ���ų� ���� �� ����
        // }

        // NAMESPACE + "update" �� XML�� update ȣ��
        int updateCount = sqlSession.update(NAMESPACE + "update", dto);
        return updateCount == 1;
    }

    @Transactional // ��� ���� ��ȸ(�ۼ��� Ȯ��) + ���� �ʿ�� Ʈ�����
    @Override
    public boolean removeComment(int rno, String userId) throws Exception {
        log.info("removeComment({}) - ��� ����, ��û��: {}", rno, userId);

        // TODO: �ۼ��� ���� Ȯ�� ���� ���� (�ʼ�!)
        // ReplyDTO originalReply = getComment(rno);
        // if (originalReply == null || !originalReply.getReplyer().equals(userId)) {
        //     log.warn("��� ���� ���� ����: rno={}, ��û��={}", rno, userId);
        //     return false; // ���� ���ų� ���� �� ����
        // }

        // NAMESPACE + "delete" �� XML�� delete ȣ��
        
        // TODO: �Խ��� ���̺� ��� �� ������Ʈ ���� (BoardMapper �ʿ�)
        // if (deleteCount == 1) {
        //     boardMapper.updateReplyCnt(originalReply.getBno(), -1); // ��� �� 1 ����
        // }
        ReplyDTO originalReply = getComment(rno); 
        if (originalReply == null) { return false; }
        // ... (�ۼ��� Ȯ��) ...
        int deleteCount = sqlSession.delete(NAMESPACE + "delete", rno);
        if (deleteCount == 1) {
            try {
                // �ڡڡ� boardService ȣ�� �ڡڡ�
                boardService.updateReplyCnt(originalReply.getBno(), -1);
                log.info("{}�� �Խñ� ��� �� 1 ���� (via BoardService, ��� {} ����)", originalReply.getBno(), rno);
            } catch (Exception e) { /* ���� ó�� */ throw e; }
        }
        
     
        return deleteCount == 1;
    }

    // (����) ��� 1�� ��ȸ (����/���� �� �ۼ��� Ȯ�ο�)
    @Override
    public ReplyDTO getComment(int rno) throws Exception {
        log.info("getComment({}) - ��� �ܰ� ��ȸ", rno);
        return sqlSession.selectOne(NAMESPACE + "read", rno); // XML�� id="read" �߰� �ʿ�
    }
    
    @Override
    public ReplyDTO getReply(int rno) throws Exception {
        log.info("getReply(rno={}) ȣ��", rno);
        return sqlSession.selectOne(NAMESPACE + "getReply", rno); // Mapper ID ����
    }
    
    
}