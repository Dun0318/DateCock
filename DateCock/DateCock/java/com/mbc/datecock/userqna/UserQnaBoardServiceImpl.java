package com.mbc.datecock.userqna;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mbc.datecock.board.PageDTO;

@Service
public class UserQnaBoardServiceImpl implements UserQnaBoardService {

    private static final Logger log = LoggerFactory.getLogger(UserQnaBoardServiceImpl.class);
    private static final String NAMESPACE = "com.mbc.datecock.userqna.mapper.UserQnaBoardMapper.";

    @Autowired
    private SqlSession sqlSession;

    @Override
    public List<UserQnaBoardDTO> getUserQnaListPaged(PageDTO pageDTO) throws Exception {
        log.info("Service - listPage ȣ��, pageDTO: {}", pageDTO);
        List<UserQnaBoardDTO> resultList = sqlSession.selectList(NAMESPACE + "listPage", pageDTO);
        log.info("Service - listPage ��� ��: {}", (resultList != null ? resultList.size() : "null �Ǵ� 0"));
        return resultList;
    }

    @Override
    public int getTotalUserQnaCount(PageDTO pageDTO) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "getTotalCount", pageDTO);
    }

    @Override
    @Transactional
    public boolean writeUserQna(UserQnaBoardDTO dto) throws Exception {
        // ... (���� ��ȿ�� �˻�) ...
        if (dto.getWriter() == null || /* ... */ dto.getContent().trim().isEmpty()) {
            log.warn("writeUserQna - �ʼ� ���� ����: {}", dto);
            return false;
        }

        // !!! �߿�: DB ���� ���� DTO ���� �α� !!!
        log.info("Service - writeUserQna DB ���� �õ� �� DTO: {}", dto);
        log.info("Service - writeUserQna DB ���� �õ� �� imageFile ��: [{}]", dto.getImageFile()); // ���ȣ�� ���μ� �� �� Ȯ�� ����

        int result = sqlSession.insert(NAMESPACE + "insert", dto);
        log.info("Service - writeUserQna DB ���� ��� (0=����, 1=����): {}", result);
        if(result != 1) {
             log.warn("Service - writeUserQna DB ���� ����! DTO: {}", dto); // ���� �� �� ��Ȯ��
        }
        return result == 1;
    }


    @Override
    @Transactional
    public UserQnaBoardDTO getUserQnaDetail(int bno, String currentUserId, boolean isAdmin) throws Exception {
        sqlSession.update(NAMESPACE + "increaseViewCnt", bno); // ��ȸ�� ����
        UserQnaBoardDTO dto = sqlSession.selectOne(NAMESPACE + "getDetail", bno);

        if (dto != null && dto.isSecretPost()) { // isSecretPost()�� secret == 1 ���� Ȯ��
            if (!isAdmin && (currentUserId == null || !currentUserId.equals(dto.getWriter()))) {
                dto.setContent("��б��Դϴ�. �ۼ��ڿ� �����ڸ� ������ Ȯ���� �� �ֽ��ϴ�.");
            }
        }
        return dto;
    }
    
    @Override
    public UserQnaBoardDTO getUserQnaForAuth(int bno) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "getDetail", bno);
    }


    @Override
    @Transactional
    public boolean updateUserQna(UserQnaBoardDTO dto, String currentUserId, boolean isAdmin) throws Exception {
        UserQnaBoardDTO originalDto = getUserQnaForAuth(dto.getBno());
        // ... (���� ���� �˻�) ...
        if (originalDto == null || /* ... */ !currentUserId.equals(originalDto.getWriter()))  {
             // ���� ���� �Ǵ� �� ���� ó��
            return false;
        }

        dto.setWriter(originalDto.getWriter()); // �ۼ��ڴ� ���� ����

        dto.setWriter(originalDto.getWriter());

        // !!! (���� ����) ���� �������� DTO�� �亯 ���� �ʵ� �ʱ�ȭ !!!
        dto.setAnswerStatus("�亯���");
        dto.setAnswerContent(null);
        dto.setAnswerWriter(null);
        dto.setAnswerDate(null);
        log.info("Service - updateUserQna: �亯 ���� �ʱ�ȭ��.");

        log.info("Service - updateUserQna DB ������Ʈ �õ� �� DTO: {}", dto);
        log.info("Service - updateUserQna DB ������Ʈ �õ� �� imageFile ��: [{}]", dto.getImageFile());

        int result = sqlSession.update(NAMESPACE + "update", dto); // XML���� �̹� �ʱ�ȭ�ϹǷ� DTO ���� ���� ��
        log.info("Service - updateUserQna DB ������Ʈ ��� (0=����, 1=����): {}", result);
         if(result != 1) {
             log.warn("Service - updateUserQna DB ������Ʈ ����! DTO: {}", dto); // ���� �� �� ��Ȯ��
        }
        return result == 1;
    }

    @Override
    @Transactional
    public boolean deleteUserQna(int bno, String currentUserId, boolean isAdmin) throws Exception {
        UserQnaBoardDTO originalDto = getUserQnaForAuth(bno);
        if (originalDto == null) {
            log.warn("deleteUserQna - ������ �Խñ� ����: bno={}", bno);
            return false;
        }
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(originalDto.getWriter()))) {
            log.warn("deleteUserQna - ���� ���� ����: bno={}, ��û��={}", bno, currentUserId);
            return false;
        }
        int result = sqlSession.delete(NAMESPACE + "delete", bno);
        return result == 1;
    }

    @Override
    @Transactional
    public boolean saveAnswer(UserQnaBoardDTO dto, String adminId) throws Exception {
        if (dto.getBno() <= 0 || dto.getAnswerContent() == null || dto.getAnswerContent().trim().isEmpty()) {
            log.warn("saveAnswer - �ʼ� ���� ���� (bno �Ǵ� �亯 ����)");
            return false;
        }
        dto.setAnswerWriter(adminId); 
        dto.setAnswerStatus("�亯�Ϸ�"); 
        int result = sqlSession.update(NAMESPACE + "updateAnswer", dto);
        return result == 1;
    }
}