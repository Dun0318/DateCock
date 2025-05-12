package com.mbc.datecock.bizqna;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mbc.datecock.board.PageDTO;
import com.mbc.datecock.businesslogin.BusinessLoginService; // *** BusinessLoginService ����Ʈ ***

@Service
public class BizQnaBoardServiceImpl implements BizQnaBoardService {

    private static final Logger log = LoggerFactory.getLogger(BizQnaBoardServiceImpl.class);
    private static final String NAMESPACE = "com.mbc.datecock.bizqna.mapper.BizQnaBoardMapper.";

    @Autowired
    private SqlSession sqlSession;

    // *** �� �ۼ� �� ����ڸ� ��ȸ�� ���� BusinessLoginService ���� ***
    @Autowired(required = true)
    private BusinessLoginService businessLoginService;

    // getBizQnaListPaged, getTotalBizQnaCount �� Mapper���� businessName�� �������Ƿ� Service ���� ���ʿ�
    @Override
    public List<BizQnaBoardDTO> getBizQnaListPaged(PageDTO pageDTO) throws Exception {
        log.info("BizQnaService - listPage ȣ��, pageDTO: {}", pageDTO);
        List<BizQnaBoardDTO> resultList = sqlSession.selectList(NAMESPACE + "listPage", pageDTO);
        log.info("BizQnaService - listPage ��� ��: {}", (resultList != null ? resultList.size() : "null �Ǵ� 0"));
        // ���� DTO�� businessName �ʵ尡 Mapper�� ���� ä����
        return resultList;
    }

    @Override
    public int getTotalBizQnaCount(PageDTO pageDTO) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "getTotalCount", pageDTO);
    }

    @Override
    @Transactional
    public boolean writeBizQna(BizQnaBoardDTO dto) throws Exception {
        // secret �ʵ� null ó��
        if (dto.getSecret() == null) {
            dto.setSecret(0);
        }

        // ��ȿ�� �˻�
        if (dto.getWriter() == null || dto.getWriter().trim().isEmpty() ||
            dto.getTitle() == null || dto.getTitle().trim().isEmpty() ||
            dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            log.warn("writeBizQna - �ʼ� ���� ����: {}", dto);
            return false;
        }

        // *** �߰�: �ۼ���(writer) ������� ����ڸ�(businessName) ���� ***
        String writerId = dto.getWriter();
        String businessName = null; // DB�� ����� �̸�

        // TODO: ������ ID ���� ���� �ʿ� (ServiceImpl�� populateWriterDisplayName ���� ����)
     // ���� ����: writerId�� ���ڷθ� �̷���� ������ ����ڷ� ����, �ƴϸ� �����ڷ� ����
     // (���� ������ ID�� ���ڸ� �����Ѵٴ� ���� �Ͽ�)
        boolean isBusinessUserByIdFormat = (writerId != null && writerId.matches("\\d+")); // ���ڸ����� �����Ǿ����� Ȯ��

        if (isBusinessUserByIdFormat && businessLoginService != null) { // ����� ��ȣ �����̶�� �ǴܵǸ�
            try {
                businessName = businessLoginService.nameselect(writerId);
                log.info("����� ��ȣ '{}'�� �̸� '{}' ��ȸ ����.", writerId, businessName);
            } catch (Exception e) {
                log.error("�� �ۼ� �� ����� �̸� ��ȸ ���� (����� ��ȣ: {}): {}", writerId, e.getMessage());
                // �̸� ��ȸ ���� �� DB�� null ���� (�Ǵ� �⺻�� ���� ����)
            }
        } else { // ����� ��ȣ ������ �ƴ϶�� �ǴܵǸ� (������ �Ǵ� ��Ÿ ID�� ����)
            log.info("[Service-writeBizQna] �ۼ��� '{}'�� ����ڹ�ȣ ������ �ƴϹǷ�(������ ��), Controller���� ������ �� �Ǵ� �⺻�� ó��.", writerId);
            businessName = dto.getBusinessName(); 
        }
        // *** ----------------------------------------------- ***

        log.info("BizQnaService - writeBizQna DB ���� �õ� �� DTO: {}", dto);
        int result = sqlSession.insert(NAMESPACE + "insert", dto); // Mapper ȣ��
        log.info("BizQnaService - writeBizQna DB ���� ��� (0=����, 1=����): {}", result);
        return result == 1;
    }

    // getBizQnaDetail, getBizQnaForAuth �� Mapper���� businessName�� �������Ƿ� Service ���� ���ʿ�
    @Override
    @Transactional
    public BizQnaBoardDTO getBizQnaDetail(int bno, String currentUserId, boolean isAdmin) throws Exception {
        sqlSession.update(NAMESPACE + "increaseViewCnt", bno);
        BizQnaBoardDTO dto = sqlSession.selectOne(NAMESPACE + "getDetail", bno);

        if (dto != null) {
            // ��б� ���� ó��
            if (dto.isSecretPost()) {
                if (!isAdmin && (currentUserId == null || !currentUserId.equals(dto.getWriter()))) {
                    dto.setContent("��б��Դϴ�. �ۼ��ڿ� �����ڸ� ������ Ȯ���� �� �ֽ��ϴ�.");
                }
            }
        }
        return dto;
    }

    @Override
    public BizQnaBoardDTO getBizQnaForAuth(int bno) throws Exception {
        return sqlSession.selectOne(NAMESPACE + "getDetail", bno);
    }

    // updateBizQna, deleteBizQna, saveAnswer �� businessName�� ���� �������� �����Ƿ� �״�� ��
     @Override
    @Transactional
    public boolean updateBizQna(BizQnaBoardDTO dto, String currentUserId, boolean isAdmin) throws Exception {
        BizQnaBoardDTO originalDto = getBizQnaForAuth(dto.getBno());
        if (originalDto == null) { /* ... */ return false; }
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(originalDto.getWriter()))) { /* ... */ return false; }

        dto.setWriter(originalDto.getWriter()); // �ۼ��� ID/��ȣ�� ���� ����
        // businessName�� update �������� ���������Ƿ� DTO�� �ִ��� ������� ����

        log.info("BizQnaService - updateBizQna DB ������Ʈ �õ� �� DTO: {}", dto);
        int result = sqlSession.update(NAMESPACE + "update", dto);
        log.info("BizQnaService - updateBizQna DB ������Ʈ ��� (0=����, 1=����): {}", result);
        return result == 1;
    }

     @Override
    @Transactional
    public boolean deleteBizQna(int bno, String currentUserId, boolean isAdmin) throws Exception {
         BizQnaBoardDTO originalDto = getBizQnaForAuth(bno);
        if (originalDto == null) { /* ... */ return false; }
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(originalDto.getWriter()))) { /* ... */ return false; }
        int result = sqlSession.delete(NAMESPACE + "delete", bno);
        return result == 1;
    }

     @Override
    @Transactional
    public boolean saveAnswer(BizQnaBoardDTO dto, String adminId) throws Exception {
         if (dto.getBno() <= 0 || dto.getAnswerContent() == null || dto.getAnswerContent().trim().isEmpty()) { /* ... */ return false; }
        dto.setAnswerWriter(adminId);
        dto.setAnswerStatus("�亯�Ϸ�");
        int result = sqlSession.update(NAMESPACE + "updateAnswer", dto);
        return result == 1;
    }
}