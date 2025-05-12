package com.mbc.datecock.businesslogin;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // ������ ���� ������ ���!
public class BusinessLoginServiceImpl implements BusinessLoginService {

    @Autowired
    private SqlSession sqlSession; // MyBatis ����� ���� SqlSession ����

    private static final String NAMESPACE = "com.mbc.datecock.businesslogin.BusinessLoginService."; // ���� ���ӽ����̽�

    @Override
    public String pwselect(String businessnumber) {
        return sqlSession.selectOne(NAMESPACE + "pwselect", businessnumber);
    }

    @Override
    public String nameselect(String businessnumber) {
        // ���� XML�� nameselect ���� ID�� �Ķ���� Ȯ��
        return sqlSession.selectOne(NAMESPACE + "nameselect", businessnumber);
    }
}