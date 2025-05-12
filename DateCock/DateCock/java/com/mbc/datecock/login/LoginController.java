package com.mbc.datecock.login;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mbc.datecock.login.LoginService;
import com.mbc.datecock.member.MemberDTO;
@Controller
public class LoginController {
	@Autowired
	SqlSession sqlSession;
	
	@RequestMapping(value="/loginprocess",method = RequestMethod.POST)
	public String log2(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String id = request.getParameter("loginId");
		String pw = request.getParameter("loginPw");
		LoginService ls = sqlSession.getMapper(LoginService.class);
		String cpw = ls.pwselect(id); //��ȣȭ�� �н����带 ������ ����
		String name = ls.nameselect(id);
		
		PasswordEncoder pe = new BCryptPasswordEncoder();
		boolean flag = pe.matches(pw,cpw); // ��ȣȭ�� �н������ �α����� �н����带 ��
		if(flag)
		{	
			
			
			Integer adminStatus = ls.getAdminStatus(id);
			Boolean isAdmin = (adminStatus != null && adminStatus ==1);
			HttpSession hs = request.getSession();
			hs.setAttribute("personalloginstate", true);
			hs.setAttribute("personalloginstate2", true);
			hs.setAttribute("id", id);
			hs.setAttribute("name", name);
			hs.setAttribute("userType", "personal"); // �ڡڡ� �Ϲ� ����� ���� ���� �ڡڡ�
			hs.setAttribute("isAdmin", isAdmin);
			
			
			return "redirect:/loginmain";
		}
		else {
			response.setContentType("text/html;charset=utf-8");
			PrintWriter pww = response.getWriter();
			pww.print("<script>alert('��й�ȣ�� ��ġ�����ʽ��ϴ�.');</script>");
			pww.print("<script>location.href='memberinput';</script>");
			pww.close();
			
			
			return "redirect:/signup";
		}
		
	}
	
	@RequestMapping(value="/logout")
	public String log33(HttpServletRequest request) {
		
			HttpSession hs = request.getSession();
			hs.removeAttribute("personalloginstate");
			hs.removeAttribute("personalloginstate2");
			hs.removeAttribute("name");
			hs.removeAttribute("id");
			hs.removeAttribute("likedPostsMap");
			hs.removeAttribute("userType"); // �ڡڡ� �α׾ƿ� �� userType ���� �Ӽ� ���� �߰� �ڡڡ�
			// �ڡڡ� �߰��� �ڵ�: isAdmin ���� �Ӽ� ���� �ڡڡ�
            hs.removeAttribute("isAdmin");
            // �ڡڡ� --- �ڡڡ�
			hs.invalidate();
			 System.out.println("���ǹ�ȿȭ ����");
			 return "redirect:/";
		}
	
}
