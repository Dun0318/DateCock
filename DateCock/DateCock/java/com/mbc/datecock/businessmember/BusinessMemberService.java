package com.mbc.datecock.businessmember;

public interface BusinessMemberService {
	//ȸ������
	void businessinsert(String businessnumber, String password, String email, String businessname, String phone,
			String birthyear);
	
	//����� ��Ϲ�ȣ �ߺ��˻�
	int businessnumbercheck(String businessnumber);
	
	//����ڸ�� �̸����� db�� �ִ��� �˻� - ����� ��Ϲ�ȣ ã��
	BusinessMemberDTO findMemberId(String businessname, String email);
	
	//����� pw db�� �ݿ� - ��й�ȣ ã��
	int updateTempPassword(BusinessMemberDTO dto);
	
	//����� ��Ϲ�ȣ db�� �ִ��� �˻� - ��й�ȣ ã��
	int samebusinessnumber(String businessnumber);
	
	//�̸��� db�� �ִ��� �˻� - ��й�ȣ ã��
	int checkEmailMatch(BusinessMemberDTO dto);
	
	
}
