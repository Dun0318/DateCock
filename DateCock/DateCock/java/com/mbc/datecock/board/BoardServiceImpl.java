package com.mbc.datecock.board; // <<<--- ���� ��Ű�� ��η� ����!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 // <<<--- �� import �� Ȯ�� �� �߰�!
import com.mbc.datecock.board.BoardDTO;    // <<<--- BoardDTO import �� Ȯ��!

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import java.io.File;
import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.HashMap;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // <<<--- @Service ������̼� Ȯ��!
public class BoardServiceImpl implements BoardService {

	private static final Logger log = LoggerFactory.getLogger(BoardServiceImpl.class);
	
	
	
	// ���� ���ε� ��� ���� ���� ServletContext ���� (Controller���� �̹� ���Թް� ����)
    // Service���� ServletContext�� ���� ����ϴ� ���� �׻� ���� ������ �ƴ�����,
    // ���� ��θ� ��� ���� Controller���� ��θ� �޾ƿ��ų� �ٸ� ����� ����� ���� �ֽ��ϴ�.
    // ���⼭�� ������ Service������ ���Թ޴� ������ �����մϴ�.
    @Autowired
    private ServletContext servletContext;
	
	
	
    // SqlSession ���� ���� ���
    @Autowired // <<<--- @Autowired Ȯ��!
    private SqlSession sqlSession;

    // ����� namespace (Board.xml�� namespace�� ��ġ���Ѿ� ��)
    // �� ��δ� BoardMapper �������̽��� ��� XML �� SQL�� ã�� ���� �ʿ��մϴ�.
    private static final String NAMESPACE = "com.mbc.datecock.board.mapper.BoardMapper."; // <<<--- ���� Board.xml�� namespace Ȯ��!
    
    
    @Override
    public List<BoardDTO> getBoardListPaged(PageDTO pageDTO) throws Exception {
        log.info("getBoardListPaged({}) - ���� ȣ���", pageDTO);
        List<BoardDTO> list = null;
        try {
            // Mapper�� listPage ���� ȣ�� (�Ķ���ͷ� PageDTO ����)
            list = sqlSession.selectList(NAMESPACE + "listPage", pageDTO);

            // --- ��� ������ ���� ���� ---
            if (list != null && !list.isEmpty()) {
                for (BoardDTO dto : list) {
                    if (dto != null) {
                        dto.setRelativeTime(formatRelativeTime(dto.getRegdate()));
                        dto.setSnippet(generateSnippet(dto.getContent()));
                        // ����� ��� �� �߰� ����...
                         if(dto.getThumbnail() != null && !dto.getThumbnail().isEmpty() && !dto.getThumbnail().startsWith("/")) {
                              dto.setThumbnail("/upload/" + dto.getThumbnail()); // ���� ��� �������� ���� (�� �κ��� ���� �ڵ忡 �־���)
                         }
                    }
                }
                
            } else {
                log.info("�ش� ������({})�� ��ȸ�� �Խñ� ����.", pageDTO.getNowPage());
            }
            // --- ���� ���� �� ---
        } catch (Exception e) {
            log.error("getBoardListPaged() - �Խñ� ��� ��ȸ �� ���� �߻�", e);
            throw e;
        }
        return list;
    }

    /**
     * ��ü �Խñ� �� ��ȸ
     */
    @Override
    public int getTotalBoardCount(PageDTO pageDTO) throws Exception { // �ڡڡ� PageDTO �Ķ���� ���� �ڡڡ�
        log.info("getTotalBoardCount({}) - ���� ȣ���", pageDTO);
        int totalCount = 0;
        try {
            // Mapper ȣ�� �� PageDTO ���� (�˻� ���� ����)
            totalCount = sqlSession.selectOne(NAMESPACE + "getTotalBoardCount", pageDTO);
            log.info("�˻� ���� ����� ��ü �Խñ� ��: {}", totalCount);
        } catch (Exception e) {
             log.error("getTotalBoardCount() - ��ü �Խñ� �� ��ȸ �� ���� �߻�", e);
             throw e;
        }
        return totalCount;
    }

    // --- Helper �޼ҵ� (getBoardList���� ����ϹǷ� ����) ---

    /**
     * Date ��ü�� ��� �ð� ���ڿ�("X�� ��" ��)�� ��ȯ�մϴ�.
     */
    private String formatRelativeTime(Date past) {
        if (past == null) return "";
        long now = System.currentTimeMillis();
        long diff = now - past.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutes < 1) return "��� ��";
        else if (minutes < 60) return minutes + "�� ��";
        else if (hours < 24) return hours + "�ð� ��";
        else if (days < 7) return days + "�� ��";
        else {
             java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy.MM.dd");
             return sdf.format(past);
        }
    }

    /**
     * HTML �±׸� �����ϰ� �ִ� ���̷� �߶� ���� ����� �����մϴ�.
     */
    private String generateSnippet(String content) {
         if (content == null) return "";
         String textOnly = content.replaceAll("<[^>]*>", "");
         int maxLength = 100; // ��� ���� ��
         if (textOnly.length() > maxLength) {
             return textOnly.substring(0, maxLength) + "...";
         } else {
             return textOnly;
         }
    }
    

    // --- ���� �۾��� �޼ҵ� ���� �߰� ���� ---
    /**
     * ���ο� �Խñ��� ����մϴ�.
     * @param dto ����� �Խñ� ������ ���� DTO
     * @return ��� ���� �� true, ���� �� false
     */
    @Override
    public boolean writeBoard(BoardDTO dto) {
        log.info("writeBoard() - ���� ȣ��� (SqlSession ���). �ۼ���: {}", dto.getWriter());
         // �⺻ ��ȿ�� �˻� (Controller������ ������ Service������ �ϴ� ���� ����)
         if (dto == null || dto.getTitle() == null || dto.getTitle().trim().isEmpty() ||
            dto.getContent() == null || dto.getContent().trim().isEmpty() ||
            dto.getWriter() == null || dto.getWriter().trim().isEmpty()) {
            log.warn("writeBoard() - �Խñ� ��� ����: �ʼ� ���� ����");
            return false;
         }

         int result = 0;
         try {
             // MyBatis insert ȣ�� (BoardMapper.xml �� id="insertBoard" �ʿ�)
             result = sqlSession.insert(NAMESPACE + "insertBoard", dto);
             log.debug("�Խñ� DB ���� ��� (���� ���� �� ��): {}", result);
         } catch (Exception e) {
             log.error("writeBoard() - �Խñ� DB ���� �� ���� �߻�", e);
             return false; // ���� �߻� �� ����
         }
         return result == 1; // ���Ե� ���� 1���� ����
    }
    
    // --- ���� �� ���� �޼ҵ� ���� �߰� ���� ---
    /**
     * Ư�� �Խñ��� �� ������ ��ȸ�ϰ� ��ȸ���� 1 ������ŵ�ϴ�.
     * @param bno ��ȸ�� �Խñ� ��ȣ
     * @return ��ȸ�� �Խñ� DTO (������ null)
     */
    @Transactional // <<<--- ��ȸ�� ������ SELECT�� �� Ʈ��������� ����
    @Override
    public BoardDTO getBoardDetail(int bno) {
    	log.info("getBoardDetail({}) - ���� ȣ���",bno);
    	BoardDTO dto = null;
    	
    	try {
    		//1.��ȸ�� ���� update ����
    		int updatecnt = sqlSession.update(NAMESPACE+"increaseViewCnt",bno);
    		if(updatecnt >0) {
    			log.debug("{}�� �Խñ� ��ȸ�� ���� ����.",bno);
    		}
    		else {
    			log.debug("{}�� �Խñ� ��ȸ�� ���� ��� ���� (updatecnt=0).",bno);
    		}
    		
    		//2.�Խñ� �� ���� select ����
    		dto = sqlSession.selectOne(NAMESPACE+"getDetail",bno);
    		
    		//3.��ȸ ��� ����(�ʿ�� �߰�)
    	}
    	 catch (Exception e) {
             log.error("getBoardDetail({}) - ó�� �� ���� �߻�", bno, e);
             // �ʿ�� ���ܸ� �ٽ� �����ų� null ��ȯ
             // throw new RuntimeException("�Խñ� �� ��ȸ ����", e);
             return null;
         }
    	
    	 return dto; // ��ȸ�� DTO �Ǵ� null ��ȯ
    	
    }
    // --- ���� ��� �� ������Ʈ �޼ҵ� ���� �߰� ���� ---
    /**
     * �Խñ��� ��� ���� �����Ѵ�. (ReplyService���� ȣ���)
     * @param bno �Խñ� ��ȣ
     * @param amount ���淮 (��� �߰�: 1, ��� ����: -1)
     * @throws Exception
     */
    @Override 
    public void updateReplyCnt(int bno, int amount) throws Exception {
        log.info("�Խñ�({}) ��� �� ���� ��û: {}", bno, amount);
        try {
            // sqlSession���� update ���� �� �Ķ���ʹ� �ϳ��� �ѱ� �� �����Ƿ� Map ���
            Map<String, Object> params = new HashMap<>();
            params.put("bno", bno);
            params.put("amount", amount);

            // NAMESPACE + id �� XML�� ���� ����
            int updateResult = sqlSession.update(NAMESPACE + "updateReplyCnt", params); 

            if (updateResult > 0) {
                
            } else {
                 log.warn("�Խñ�({}) ��� �� ���� ����� ���ų� �����߽��ϴ�.", bno);
            }
        } catch (Exception e) {
            log.error("�Խñ�({}) ��� �� ���� �� DB ���� �߻�", bno, e);
            throw e; // ���� �߻� �� ���� �ٽ� ������
        }
    }
    // --- ���� ��� �� ������Ʈ �޼ҵ� ���� �߰� �� ���� ---
    
    @Override
    public boolean increaseLikeCount(int bno) throws Exception {
    	 log.info("increaseLikeCount(bno={}) ȣ�� - SqlSession ���� ���", bno);
    	if(sqlSession == null) {
    		 log.info("increaseLikeCount(bno={}) ȣ�� - SqlSession ���� ���", bno);
    		throw new IllegalStateException("��ü�� ���Ե����ʾҽ��ϴ�.");
    	}
    	
    	// sqlSession.update(���ӽ����̽� + SQL_ID, �Ķ����)
    	int updatedRows = sqlSession.update(NAMESPACE+"increaseLikeCnt",bno);
    	log.info("�Խñ� {} ���ƿ� �� ������Ʈ ���: {} �� ������Ʈ��", bno, updatedRows);
    	return updatedRows == 1; //1�� ���� ������Ʈ �Ǿ����� ���� boolean ������ ��ȯ ����
    }
    
    @Override
    public int getLikeCount(int bno) throws Exception { // throws Exception �߰� ��� (selectOne ���� ���ɼ�)
        log.info("getLikeCount(bno={}) ȣ�� - SqlSession ���� ���", bno);
        if (sqlSession == null) {
            throw new IllegalStateException("��ü�� ���Ե��� �ʾҽ��ϴ�.");
        }

        Integer likeCount = null; // ����� Integer�� �޴� ���� �����ϴ� (��� ���� �� null)
        try {
            // --- !!! ������ �κ� !!! ---
            // sqlSession.selectOne()�� ����Ͽ� ���ƿ� ���� ��ȸ�մϴ�.
            // XML ���ۿ��� id="getLikeCnt" �� <select> ������ �־�� �մϴ�.
            likeCount = sqlSession.selectOne(NAMESPACE + "getLikeCnt", bno); // <--- selectOne() ���!
            // --- !!! ���� �� ---

            log.info("�Խñ� {} ���� ���ƿ� �� ��ȸ ���: {}", bno, likeCount);

        } catch (Exception e) {
            log.error("getLikeCount({}) - ���ƿ� �� ��ȸ �� ���� �߻�", bno, e);
            throw e; // �Ǵ� -1 ��ȯ �� ���� ó�� ��å�� �°�
        }

        // ��ȸ ����� null�̸� 0 �Ǵ� -1 �� �⺻�� ó��
        return (likeCount != null) ? likeCount : 0; // null�̸� 0���� ��ȯ (�Ǵ� -1)
    }
    
    @Transactional // DB ������ ���� ������ ��� ó�� (�ϳ��� �����ϸ� �ѹ�ǵ���)
    @Override
    public boolean deleteBoard(int bno, String userId) throws Exception {
        log.info("deleteBoard(bno={}, userId={}) - ���� ȣ���", bno, userId);

        // 1. (������������ ����) ���񽺿����� �ۼ��� Ȯ�� - Controller���� �̹� �ߴ��� ���� üũ
        BoardDTO boardToDelete = sqlSession.selectOne(NAMESPACE + "getDetail", bno);

        if (boardToDelete == null) {
            log.warn("������ �Խñ� ���� (bno={})", bno);
            return false; // ������ �Խñ� ����
        }
       

        // 2. DB���� �Խñ� ���� �õ�
        int deleteDbResult = 0;
        String thumbnailPathToDelete = boardToDelete.getThumbnail(); // ���� ���� ���� ��� ����

        try {
            deleteDbResult = sqlSession.delete(NAMESPACE + "deleteBoard", bno);
            log.info("�Խñ� {} DB ���� ���: {} �� ������", bno, deleteDbResult);
        } catch (Exception e) {
            log.error("�Խñ� {} DB ���� �� ���� �߻�", bno, e);
            throw e; // DB ���� �� �ѹ�ǵ��� ���� �ٽ� ������
        }

        // 3. DB ���� ���� ��, ÷������(�����) ���� �õ�
        if (deleteDbResult == 1) {
            if (thumbnailPathToDelete != null && !thumbnailPathToDelete.isEmpty()) {
                try {
                    // �� ���(/image/...) �� ���� ���� ��η� ��ȯ
                    String realPath = servletContext.getRealPath(thumbnailPathToDelete);
                    File fileToDelete = new File(realPath);

                    if (fileToDelete.exists()) {
                        if (fileToDelete.delete()) {
                            log.info("����� ���� ���� ����: {}", realPath);
                        } else {
                            log.warn("����� ���� ���� ���� (���� ���� �Ұ� ��): {}", realPath);
                            // ���� ���� ���� �� ��å ���� �ʿ� (DB �ѹ�? ���? - ���⼱ ���)
                        }
                    } else {
                        log.warn("������ ����� ������ ���� ��ο� ����: {}", realPath);
                    }
                } catch (Exception e) {
                    log.error("����� ���� ���� �� ���� �߻� (path={})", thumbnailPathToDelete, e);
                    // ���� ó�� ���� �� ��å ���� �ʿ�
                }
            }
            return true; // DB ���� ����
        } else {
            log.warn("�Խñ� {} DB ���� ���� (���� ���� �� ����)", bno);
            return false; // DB ���� ����
        }
    }
    
    /**
     * �Խñ� ���� ó�� (DB ������Ʈ + �ʿ�� ����� ���� ��ü)
     */
    @Transactional // DB ������Ʈ, ���� ����/���� ���� ��� ó��
    @Override
    public boolean updateBoard(BoardDTO dto, String userId) throws Exception {
       

        // 2. �ڡڡ� ����� ���� ó�� ������ writeBoard �� �����ϰ� �ʿ� �ڡڡ�
        //    (�� ���� ����, ���� ���� ���� �� - writeBoard �޼ҵ� �����Ͽ� ����)
        //    ���� �� ������ ���ε� �Ǿ��ٸ� dto.setThumbnail(�� �� ���) ����
        //    �� ���� ���� ���� ���� ���� �� dto.setThumbnail(null) �Ǵ� ���� �� ���� (update ������ if�� Ȱ��)
        //    --> ���� ó�� ������ �����ϹǷ� writeBoard�� ������ �����Ͽ� ���� ���� �ʿ�!
        //    --> ���⼭�� DB ������Ʈ�� �켱 �����մϴ�. (���� ������ ���߿� �߰�)

        // 3. DB ������Ʈ �õ�
        int updateResult = 0;
        try {
             // DTO�� writer �ʵ尡 �ִٸ�, ���⼭ ������ ���� �ۼ��ڷ� ����� ���� ����
             // dto.setWriter(originalBoard.getWriter());

             // ����, ����, (ó����)����� ��� ������Ʈ
             updateResult = sqlSession.update(NAMESPACE + "updateBoard", dto); // XML ID Ȯ��!
             log.info("�Խñ� {} DB ������Ʈ ���: {} �� ������Ʈ��", dto.getBno(), updateResult);
        } catch (Exception e) {
            log.error("�Խñ� {} DB ������Ʈ �� ���� �߻�", dto.getBno(), e);
            throw e; // �ѹ�ǵ��� ���� ������
        }

        return updateResult == 1;
    }
    
    
    

}