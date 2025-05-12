package com.mbc.datecock.board;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

;

@Controller
public class BoardController {
	
	private static final Logger log = LoggerFactory.getLogger(BoardController.class);
	//�� ������̼��� ������ boardService ��ü�� null ���°� �Ǿ�, boardList �� boardView �޼ҵ� �ȿ���
	//boardService.getBoardList() ���� ȣ���� �� NullPointerException�� �߻��մϴ�. 
	//(�Ƹ� ��� ������ ���� �� �� ������ ������ ���� �ֽ��ϴ�.)
    @Autowired // <<<--- @Autowired Ȯ��!
    private BoardService boardService;
    @Autowired
    private ServletContext servletContext;
    
    
 // LoginController �Ǵ� �ٸ� ��Ʈ�ѷ��� �߰�:
    @RequestMapping(value = "/loginmain", method = RequestMethod.GET)
    public String showMainPageAfterLogin() {
        return "main"; // ��: main.jsp �� ������
    }
    /**
     * �Խñ� ��� ������ ��û ó��
     */
    @RequestMapping(value = "/listup", method = RequestMethod.GET)
    public String boardList(
            // 1. @RequestParam���� �Ķ���� ����� ���� + �⺻�� ����
            @RequestParam(value = "nowPage", required = false, defaultValue = "1") int nowPage,
            @RequestParam(value = "cntPerPage", required = false, defaultValue = "10") int cntPerPage,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

       

        // 2. (������������ ����) �߰� ��ȿ�� �˻� �� �⺻�� ����
        if (nowPage <= 0) { nowPage = 1; }
        if (cntPerPage <= 0 || cntPerPage > 100) { cntPerPage = 10; } // ��: �ִ� 100��

        // 3. �˻� ���Ǹ� ���� �ӽ� DTO ���� (Count��)
        PageDTO countCriteria = new PageDTO();
        countCriteria.setSearchType(searchType);
        countCriteria.setKeyword(keyword);

        try {
            // 4. �˻� ���� ����� ��ü �Խñ� �� ��ȸ
            int totalCount = boardService.getTotalBoardCount(countCriteria);
            log.info("Total count with search criteria: {}", totalCount);

            // 5. �ڡڡ� ��ȿ�� ����� ���� PageDTO ���� �� ����¡ ��� �ڡڡ�
            PageDTO paging = new PageDTO(); // View�� listPage ������ ������ ���� ��ü
            paging.setSearchType(searchType); // �˻� ���� ����
            paging.setKeyword(keyword);    // �˻� ���� ����
            paging.setNowPage(nowPage);       // ������ ���� ������ ����
            paging.setCntPerPage(cntPerPage); // ������ �������� �� �� ����
            paging.setTotal(totalCount);     // ��ü �� �� ����
            paging.calculatePaging();        // �� ��� �� ���� �� ����¡ ��� ����� ȣ�� ��
            log.info("Final PageDTO for query and view: {}", paging);

            // 6. ����¡ �� �˻� ���� �����Ͽ� �Խñ� ��� ��ȸ
            List<BoardDTO> boardList = boardService.getBoardListPaged(paging);
            log.info("Fetched board list size: {}", (boardList != null ? boardList.size() : "null"));

            // 7. Model�� ������ �߰�
            model.addAttribute("boardList", boardList);
            model.addAttribute("paging", paging); // ��� �Ϸ�� PageDTO ����
          // �ڡڡ� ������ ID�� Model�� �߰� �ڡڡ�
            model.addAttribute("adminUserId", "admin"); // ���� ������ ID�� ����!

        } catch (Exception e) {
            log.error("�Խñ� ��� ��ȸ(����¡+�˻�) �� ���� �߻�", e);
            model.addAttribute("errorMessage", "��� ��ȸ �� ���� �߻�");
        }
        return "listup";
    }

    
 // --- ���� �۾��� �� �����ֱ� �޼ҵ� �߰� ���� ---
    /**
     * �Խñ� �ۼ� �� ������ ��û�� ó���մϴ�. (�α��� Ȯ��)
     * @param session �α��� ���� Ȯ�� ���� ���� HttpSession ���
     * @return ������ View�� �̸� ("write") �Ǵ� �α��� �������� �����̷�Ʈ
     */
    @RequestMapping(value = "/write", method = RequestMethod.GET) // URL ��� Ȯ�� (/board/write ��)
    public String boardWriteForm(HttpSession session) {
        log.info("'/write' (GET) ��û ���� - �۾��� �� ������");

        // �α��� ���� Ȯ�� ("personalloginstate" Ű ���)
        Boolean loginState = (Boolean) session.getAttribute("personalloginstate");
        if (loginState == null || !loginState) {
            log.warn("��α��� ������� �۾��� ������ ���� �õ�. �α��� �������� �����̷�Ʈ.");
            return "redirect:/memberinput"; // �α��� ������ ���
        }

        log.info("�۾��� �� ������(write.jsp)�� �̵��մϴ�.");
        return "write"; // -> /WEB-INF/views/write.jsp (ViewResolver ���� Ȯ��)
    }
    // --- ���� �۾��� �� �����ֱ� �޼ҵ� �� ���� ---


    // --- ���� �۾��� ó�� �޼ҵ� (AJAX ���) �߰� ���� ---
    /**
     * �Խñ� �ۼ� ó�� (AJAX ���)
     * @param dto ������ ���۵� �����͸� ���� BoardDTO ��ü
     * @param session �ۼ��� ���� Ȯ�� ���� ���� HttpSession ���
     * @return ó�� ���(���� ����, �޽���)�� ���� Map ��ü (JSON���� ��ȯ��)
     */
    @RequestMapping(value = "/writeAction", method = RequestMethod.POST) // URL ��� Ȯ�� (/board/writeAction ��)
    @ResponseBody // AJAX ����� ������̼�
    public Map<String, Object> boardWriteAction(BoardDTO dto, @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
    		HttpSession session){
        log.info("'/writeAction' (POST AJAX) ��û ���� - �Խñ� ��� ó��");
        log.debug("���޹��� BoardDTO (�ۼ��� ���� ��): {}", dto);

        Map<String,Object> response = new HashMap<>(); // ����� Map

        // �α��� Ȯ�� �� �ۼ��� ���� ���� ("personalloginstate", "id" Ű ���)
        Boolean loginState = (Boolean) session.getAttribute("personalloginstate");
        String writerId = (String) session.getAttribute("id");

        if (loginState == null || !loginState || writerId == null || writerId.trim().isEmpty()) {
            log.warn("��α��� �Ǵ� ���� ID ���� ����. AJAX ���� ���� ��ȯ.");
            response.put("success", false);
            response.put("message", "�α����� �ʿ��ϰų� ���� ������ �ùٸ��� �ʽ��ϴ�.");
            return response;
        }

        // DTO�� �ۼ��� ID ����
        dto.setWriter(writerId);
        log.debug("�ۼ��� ���� ���� �Ϸ�: {}", dto.getWriter());
        
        // --- ���� ���� ���ε� ó�� ���� �߰� ���� ---
        String dbThumbnailPath = null; // DB ���� ��� ���� �ʱ�ȭ
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            log.debug("÷�ε� ����� ���� ó�� ����: {}", thumbnailFile.getOriginalFilename());
            try {
                // 1. ���� ���� ��� ���� -> webapp ���� �ٷ� �Ʒ� 'image' ������ ���� ���� ���
                String uploadDir = servletContext.getRealPath("/image/"); // <<<--- ���� ��� ���� (/image/)
                log.info("���� ���� ���� ���: {}", uploadDir);

                // 2. ���ε� ���� ���� (������)
                // webapp �Ʒ� ������ ���� �̸� �����δ� ���� �����ϴ�.
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    if(dir.mkdirs()) {
                        log.info("���ε� ���� ����: {}", uploadDir);
                    } else {
                        log.error("���ε� ���� ���� ����: {}", uploadDir);
                        // ���� ���� ���� �� ���� ó�� �ʿ�
                        response.put("success", false);
                        response.put("message", "���� ���� ���� ������ �����߽��ϴ�.");
                        return response;
                    }
                }

                // 3. ���� ���ϸ� ���� (UUID + Ȯ����)
                String originalFilename = thumbnailFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String savedFileName = UUID.randomUUID().toString() + extension;
                log.info("����� ���ϸ�: {}", savedFileName);

                // 4. ���� ���� ó�� (���� ���� ��� ���)
                File saveFile = new File(uploadDir, savedFileName);
                thumbnailFile.transferTo(saveFile); // ���� ��ũ�� ���� ����

                // 5. DB�� ������ ��� ���� (�� ���� ���� ��� - /image/ �� ����)
                dbThumbnailPath = "/image/" + savedFileName; // <<<--- DB ���� ��� ���� (/image/...)
                log.info("����� ���� ���� ����. DB ���� ���: {}", dbThumbnailPath);

            } catch (Exception e) {
                log.error("����� ���� ����/ó�� �� ���� �߻�", e);
                response.put("success", false);
                response.put("message", "���� ���ε� �� ������ �߻��߽��ϴ�.");
                return response; // ���� �� �ߴ�
            }
        } else {
             log.info("÷�ε� ����� ���� ����.");
        }

        // 6. DTO�� ���� ����� ��� ���� (���� ���ų� ���� �� null)
        dto.setThumbnail(dbThumbnailPath);
        log.debug("DTO�� thumbnail ��� ����: {}", dto.getThumbnail());
        // --- ���� ���� ���ε� ó�� ���� �� ���� ---
        try {
             if (boardService == null) {
                  log.error("boardService is null! Dependency Injection Check Needed.");
                  throw new IllegalStateException("���� ���� �߻�");
             }
            boolean success = boardService.writeBoard(dto); // BoardService�� writeBoard �޼ҵ� �ʿ�

            if (success) {
                response.put("success", true);
                response.put("message", "�Խñ��� ���������� ��ϵǾ����ϴ�.");
                log.info("�Խñ� ��� ����.");
            } else {
                response.put("success", false);
                response.put("message", "�Խñ� ��Ͽ� �����߽��ϴ�. �ٽ� �õ����ּ���.");
                log.warn("�Խñ� ��� ���� (Service ��� false).");
            }
        } catch (Exception e) {
            log.error("�Խñ� ��� �� ���� �߻�", e);
            response.put("success", false);
            response.put("message", "�Խñ� ��� �� ������ �߻��߽��ϴ�. ��� �� �ٽ� �õ����ּ���.");
        }
        return response; // ��� Map ��ȯ (JSON���� ��ȯ��)
    }

    // --- �ٸ� �޼ҵ�(�۾��� ��/ó��, �󼼺��� ��)�� ���߿� �߰� ---
 // --- ���� �� ���� ó�� �޼ҵ� ���� ---
    /**
     * Ư�� ��ȣ�� �Խñ� �� ���� ������ ��û�� ó���մϴ�.
     * @param bno URL�� ���� �Ķ����(?bno=��)�� ���޵� �Խñ� ��ȣ
     * @param model View(JSP)�� �����͸� �����ϱ� ���� ��ü
     * @return ������ View�� �̸� ("board/view")
     */
    
    @RequestMapping(value="/view",method= RequestMethod.GET) // GET������� board/view ��û
    public String boardView(@RequestParam("bno") int bno,Model mo,HttpSession session ) { //@RequestParam���� bno �Ķ���� �ޱ�
    
    	log.info("'/borad/view'(GET)��û ���� - �Խñ� ��ȣ:{}",bno);
    	
    	try {
    		//���� ��ü null üũ(Ȥ�� �� ���� ���)
    		if(boardService == null) {
    			log.error("boardService is null! Dependency Injection Check Needed.");
    			mo.addAttribute("errorMessage", "�Խ��� ���� �ε� ����");
    			//������ ���� �������� ������� �����̷�Ʈ �ʿ�
    			return "redirect:/listup";
    		}
    		
    		// 1. ���� ���� ȣ�� -> �� ���� ��������
            //    BoardServiceImpl�� getBoardDetail �޼ҵ忡�� ��ȸ�� ���� �� ���� ��ȸ�� ��� ó���Ѵٰ� ����
    		// �ڡڡ� �߿�: getBoardDetail�� ���� userId�� ���� �ʰų�, �޾Ƶ� ���ƿ� ���� Ȯ�� ���� ���� �ڡڡ�
    		BoardDTO boardDTO = boardService.getBoardDetail(bno);
    		// 2. ��ȸ ���(BoardDTO ��ü)�� Model�� �߰� -> JSP���� ��� ����
    		mo.addAttribute("boardDTO", boardDTO);
    		 if (boardDTO != null) {
    			 @SuppressWarnings("unchecked")
                 Set<Integer> likedPostsInSession = (Set<Integer>) session.getAttribute("likedPostsMap");
                 boolean userLikedInSession = (likedPostsInSession != null && likedPostsInSession.contains(bno));
                 mo.addAttribute("userLikedInSession", userLikedInSession); // �𵨿� �߰�
                 // �ڡڡ� ������ ID�� Model�� �߰� �ڡڡ�
                 mo.addAttribute("adminUserId", "admin"); // ���� ������ ID�� ����!
                 log.info("{}�� �Խñ� ��ȸ ����. ���� ���ƿ� ����: {}", bno, userLikedInSession);
            } else {
                 // ��ȸ�� �Խñ��� ���� ��� (�߸��� bno ��)
                 log.warn("{}�� �Խñ� ������ �������� �ʽ��ϴ�.", bno);
                 // JSP���� ${empty boardDTO} �� üũ�Ͽ� �޽��� ǥ��
            }

        } catch (Exception e) {
            // ������ ��ȸ �� ���� �߻� ��
            log.error("�Խñ� �� ��ȸ �� ���� �߻� (bno: {})", bno, e);
            mo.addAttribute("errorMessage", "�Խñ��� �ҷ����� �� ������ �߻��߽��ϴ�.");
            // �ʿ��ϴٸ� ������ ���� �������� �����ְų� ������� �����̷�Ʈ
            // return "error";
        	}

	        // 3. ������ JSP ������ ���� �̸� ��ȯ
	        // ViewResolver ������ ���� /WEB-INF/views/board/view.jsp ������ ã�� ��
	        return "view";
    	}
    // --- ���� �� ���� ó�� �޼ҵ� �� ���� ---
    	
    /**
     * �Խñ� ���� ó��
     * @param bno ������ �Խñ� ��ȣ
     * @param session �α��� ����� Ȯ��
     * @param rttr �����̷�Ʈ �� �޽��� ���޿�
     * @return
     */
    // JavaScript���� GET ������� ȣ���ϹǷ� �ϴ� GET���� ���� (POST/DELETE ����)
    @RequestMapping(value="/board/delete", method = RequestMethod.GET)
    public String deleteBoard(@RequestParam("bno") int bno, HttpSession session, RedirectAttributes rttr) {
        log.info("'/board/delete'(GET) ��û ���� - ������ �Խñ� ��ȣ: {}", bno);

        // 1. �α��� ���� Ȯ��
        Boolean loginState = (Boolean) session.getAttribute("personalloginstate");
        String userId = (String) session.getAttribute("id");

        if (loginState == null || !loginState || userId == null) {
            log.warn("�Խñ� ���� ����: �α����� �ʿ��մϴ�. (bno={})", bno);
            // �α��� �������� �����ų� ���� �޽����� �Բ� ������� �����̷�Ʈ
            rttr.addFlashAttribute("errorMessage", "�α����� �ʿ��մϴ�.");
            return "redirect:/memberinput"; // �α��� ������ ���
        }
        
     // �ڡڡ� �߰� �ڵ� ���� (������ Ȯ�� �� ���� üũ) �ڡڡ�
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        boolean hasPermission = false;
        BoardDTO boardToDelete = null; // �ۼ��� Ȯ�� ���� �ʿ�
        try {
             boardToDelete = boardService.getBoardDetail(bno); // ��ȸ�� ���� ���� �޼ҵ� ��� ����
             if (boardToDelete != null) {
                 if (userId.equals(boardToDelete.getWriter()) || (isAdmin != null && isAdmin == true)) {
                     hasPermission = true;
                 }
             }
        } catch (Exception e) {
             log.error("���� ���� Ȯ�� �� �Խñ� ��ȸ ����(bno={})", bno, e);
             rttr.addFlashAttribute("errorMessage", "�Խñ� ���� Ȯ�� �� ���� �߻�");
             return "redirect:/listup"; // ���� �� �������
        }

        if (!hasPermission) {
            
             rttr.addFlashAttribute("errorMessage", "���� ������ �����ϴ�.");
             // ���� ������ �� ���� �������� �����̷�Ʈ
             return "redirect:/view?bno=" + bno;
        }
        // �ڡڡ� �߰� �ڵ� �� �ڡڡ�
        

        // 2. ���� ȣ���Ͽ� ���� ó��
        boolean deleteSuccess = false;
        try {
            // �ڡڡ� Service�� deleteBoard ȣ�� �ڡڡ�
            deleteSuccess = boardService.deleteBoard(bno, userId);

            if (deleteSuccess) {
                log.info("�Խñ� {} ���� ���� (by {})", bno, userId);
                // ���� �޽����� �����̷�Ʈ �� �������� ����
                rttr.addFlashAttribute("message", "�Խñ��� ���������� �����Ǿ����ϴ�.");
            } else {
                // ���񽺿��� false�� ��ȯ�� ��� (���� ���ų� ��� ���� ��)
                log.warn("�Խñ� {} ���� ���� (���� ó�� ��� false)", bno);
                rttr.addFlashAttribute("errorMessage", "�Խñ��� ������ �� ���ų� ������ �����ϴ�.");
                // ���� �� �� �������� �ٽ� �����ų� ������� ���� �� ����
                // return "redirect:/view?bno=" + bno;
            }
        } catch (Exception e) {
            log.error("�Խñ� {} ���� ó�� �� ���� �߻�", bno, e);
            rttr.addFlashAttribute("errorMessage", "�Խñ� ���� �� ������ �߻��߽��ϴ�.");
            // ���� �߻� �ÿ��� ������� �̵�
        }

        // 3. ó�� �� ��� �������� �����̷�Ʈ
        return "redirect:/listup"; // ���� �Ŀ��� ��� �������� �̵�
    }
    
    /**
     * �Խñ� ���� �� ������ ��û ó��
     * @param bno ������ �Խñ� ��ȣ
     * @param session �α��� �� �ۼ��� Ȯ�ο�
     * @param model View(JSP)�� �Խñ� ������ ���޿�
     * @param rttr ���� ���� �� �޽��� ���޿�
     * @return ���� �� View �Ǵ� �����̷�Ʈ ���
     */
    @RequestMapping(value ="/edit",method = RequestMethod.GET) // GET ��� /board/edit ��û ó��
    public String showEditForm(@RequestParam("bno") int bno, HttpSession session, Model model, RedirectAttributes rttr) {
        log.info("'/board/edit'(GET) ��û ���� - ������ �Խñ� ��ȣ: {}", bno);

        // 1. �α��� ���� Ȯ��
        Boolean loginState = (Boolean) session.getAttribute("personalloginstate");
        String userId = (String) session.getAttribute("id");

        if (loginState == null || !loginState || userId == null) {
            rttr.addFlashAttribute("errorMessage", "�α����� �ʿ��մϴ�.");
            return "redirect:/memberinput"; // �α��� ��������
        }
        
     // �ڡڡ� �߰� �ڵ� ���� (������ Ȯ��) �ڡڡ�
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        // �ڡڡ� �߰� �ڵ� �� �ڡڡ�
        
        
        // 2. �Խñ� ���� ��������
        BoardDTO boardDTO = null;
        try {
            // �ڡڡ� getBoardDetail�� ��ȸ�� ���� ������ ���Ե� �� ������,
            // �ڡڡ� ���� �� �ε������� ��ȸ�� ���� ���� ���� �޼ҵ�(��: getBoardForEdit)�� Service�� ����� ���� �� �����ϴ�.
            // �ڡڡ� ���⼭�� �ϴ� getBoardDetail�� ����Ѵٰ� �����մϴ�.
            boardDTO = boardService.getBoardDetail(bno); // userId ���ʿ�

            if (boardDTO == null) {
                rttr.addFlashAttribute("errorMessage", "�������� �ʴ� �Խñ��Դϴ�.");
                return "redirect:/listup"; // �������
            }
         // �ڡڡ� �߰� �ڵ� ���� (���� Ȯ��) �ڡڡ� �ۼ��� ���ΰ� ������ ������ �Ѳ����� Ȯ�� userId.equals�� �ۼ��� Ȯ����
            boolean hasPermission = userId.equals(boardDTO.getWriter()) || (isAdmin != null && isAdmin == true);
            if (!hasPermission) {
                
                rttr.addFlashAttribute("errorMessage", "���� ������ �����ϴ�.");
                return "redirect:/view?bno=" + bno;
            }
            
            
            
            

            // 4. Model�� �Խñ� ���� �߰� -> JSP ���� �� ä��� ����
            model.addAttribute("boardDTO", boardDTO);
            log.info("�Խñ� {} ���� ������ �̵�", bno);

            return "edit"; // -> /WEB-INF/views/board/edit.jsp ���� �ʿ�

        } catch (Exception e) {
            log.error("�Խñ� ���� �� �ε� �� ���� �߻� (bno={})", bno, e);
            rttr.addFlashAttribute("errorMessage", "�Խñ� ������ �ҷ����� �� ���� �߻�");
            return "redirect:/listup"; // ���� �� �������
        }
    }
    
    
    /**
     * �Խñ� ���� ó�� (���� ó�� ����)
     * @param dto ������ ������ ��� DTO (bno, title, content ��)
     * @param thumbnailFile ���� ÷�ε� ����� ���� (������)
     * @param existingThumbnail ���� ����� ���� �� ��� (���� ���� �� �����, edit.jsp�� hidden input)
     * @param session �α��� ����� Ȯ��
     * @param rttr �����̷�Ʈ �� �޽��� ���޿�
     * @return �����̷�Ʈ ��� (����: �󼼺���, ����: ������)
     */
    @RequestMapping(value="/board/editAction", method=RequestMethod.POST) // edit.jsp�� form action�� ��ġ
    public String processEditForm(
            BoardDTO dto, // �� �ʵ� �ڵ� ���ε� (bno, title, content ��)
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile, // name="thumbnailFile"
            @RequestParam(value = "existingThumbnail", required = false) String existingThumbnail, // name="existingThumbnail"
            HttpSession session,
            RedirectAttributes rttr) {

        int bno = dto.getBno(); // dto�� bno�� ���ε��Ǿ����� Ȯ�� �ʿ�
        log.info("'/board/editAction'(POST) ��û ���� - ���� ��� bno: {}", bno);

        // 1. �α��� Ȯ��
        String userId = (String) session.getAttribute("id");
        if (userId == null || !(Boolean)session.getAttribute("personalloginstate")) {
            rttr.addFlashAttribute("errorMessage", "�α����� �ʿ��մϴ�.");
            return "redirect:/memberinput"; // �α��� ������ ���
        }

        // 2. �ʼ� �� Ȯ��
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty() ||
            dto.getContent() == null || dto.getContent().trim().isEmpty()) {
             rttr.addFlashAttribute("errorMessage", "����� ������ �ʼ� �Է� �׸��Դϴ�.");
             return "redirect:/edit?bno=" + bno; // �ٽ� ���� ������
        }
        // �ڡڡ� �߰� �ڵ� ���� (������ Ȯ�� �� ���� üũ) �ڡڡ�
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        boolean hasPermission = false;
        BoardDTO originalBoard = null; // ���� �Խñ� ���� �����
        try {
            originalBoard = boardService.getBoardDetail(bno); // ���� ��ȸ
            if (originalBoard == null) { /* �Խñ� ���� */ return "redirect:/listup"; }
            if (userId.equals(originalBoard.getWriter()) || (isAdmin != null && isAdmin == true)) {
                hasPermission = true;
            }
        } catch (Exception e) {
            log.error("���� ���� Ȯ�� �� �Խñ� ��ȸ ���� (bno={})", bno, e);
            rttr.addFlashAttribute("errorMessage", "�Խñ� ���� Ȯ�� �� ���� �߻�");
            return "redirect:/edit?bno=" + bno; // �ٽ� ����������
        }

        if (!hasPermission) {
            
            rttr.addFlashAttribute("errorMessage", "���� ������ �����ϴ�.");
            return "redirect:/view?bno=" + bno;
        }
        // �ڡڡ� �߰� �ڵ� �� �ڡڡ�
        
        
        
        String webPathForDb = null;    // DB�� ���� ����� ����� �� ���
        String oldFileWebPath = null;  // ������ ���� ���� �� ���

        try {
            // 3. ���� ���� Ȯ�� (DB���� ���� �Խñ� ���� �����ͼ� �ۼ��� ��)
            //    ��ȸ�� ���� ���� �޼ҵ� ��� ���� (getBoardForEdit ��)
            originalBoard = boardService.getBoardDetail(bno); // �ϴ� getBoardDetail ���

            if (originalBoard == null) {
                rttr.addFlashAttribute("errorMessage", "������ �Խñ��� �������� �ʽ��ϴ� (bno:" + bno + ").");
                return "redirect:/listup"; // �������
            }
           
            oldFileWebPath = originalBoard.getThumbnail(); // ������ ���� �ִ� ���� ���� ��� ����

            // 4. �� ����� ���� ó��
            boolean newFileUploaded = (thumbnailFile != null && !thumbnailFile.isEmpty());
            if (newFileUploaded) {
                log.info("�� ����� ���� ������. ó�� ���� (bno={}).", bno);
                // ���� ���� ���丮 (webapp/image/)
                String uploadDir = servletContext.getRealPath("/image/");
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) uploadDirFile.mkdirs();

                // ���� ���ϸ� ����
                String originalFilename = thumbnailFile.getOriginalFilename();
                String extension = "";
                if (originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String savedFileName = UUID.randomUUID().toString() + extension;
                File saveFile = new File(uploadDir, savedFileName);

                // �� ���� ����
                thumbnailFile.transferTo(saveFile);
                webPathForDb = "/image/" + savedFileName; // DB�� ����� �� ���
                log.info("�� ����� ���� �Ϸ�: {}", webPathForDb);

                // dto�� �� ��� ���� (DB ������Ʈ ����)
                dto.setThumbnail(webPathForDb);

            } else {
                // �� ������ ������ ���� ����� ����
                // dto�� thumbnail �ʵ带 �������� ������ Mapper�� <if>���� ���� DB ������Ʈ �ȵ�
                log.info("�� ����� ���� ����. ���� ����� ���� (bno={}).", bno);
                webPathForDb = oldFileWebPath; // DB ������Ʈ �� ���� ���� ���� ���� ���� ��� ���� (�ʼ��� �ƴ�)
                // dto.setThumbnail(oldFileWebPath); // �̷��� ��������� �����ص� Mapper�� <if>���� ���� null�̸� ������Ʈ �ȵ� �� ���� (���� Ȯ�� �ʿ�)
                                                    // Mapper�� <if test="thumbnail != null"> �����̶��, DTO�� null�̳� �������� �־ ������Ʈ �ȵ�.
                                                    // ���⼭�� DTO�� thumbnail�� �״�� �ΰų�, �Ʒ����� null�� ó���Ͽ� Mapper <if> Ȱ��
                 dto.setThumbnail(null); // Mapper�� <if test="thumbnail != null"> �� Ȱ���ϱ� ���� null�� ����
            }

            // 5. ���� ȣ���Ͽ� DB ������Ʈ
            boolean updateSuccess = boardService.updateBoard(dto, userId);

            if (updateSuccess) {
                // 6. DB ������Ʈ ���� ��, (���� �� ������ ���ε� �Ǿ��ٸ�) ���� ���� ����
                if (newFileUploaded && oldFileWebPath != null && !oldFileWebPath.isEmpty()) {
                     try {
                         String oldRealPath = servletContext.getRealPath(oldFileWebPath);
                         File oldFile = new File(oldRealPath);
                         if (oldFile.exists() && oldFile.isFile()) {
                             if (oldFile.delete()) {
                                 log.info("DB ������Ʈ ���� �� ���� ����� ���� ���� ����: {}", oldRealPath);
                             } else {
                                 log.warn("DB ������Ʈ ���������� ���� ����� ���� ���� ����: {}", oldRealPath);
                             }
                         }
                     } catch (Exception fileDeleteError) {
                         log.error("DB ������Ʈ ���� �� ���� ����� ���� ���� �� ���� (path={})", oldFileWebPath, fileDeleteError);
                     }
                 }

                rttr.addFlashAttribute("message", "�Խñ��� ���������� �����Ǿ����ϴ�.");
                return "redirect:/view?bno=" + bno; // ���� �� �� �����
            } else {
                 // ���񽺿��� false ��ȯ �� (���� ���� �� Service ���ο��� ó���� ���)
                 rttr.addFlashAttribute("errorMessage", "�Խñ� ������ �����߽��ϴ�.");
                 return "redirect:/edit?bno=" + bno; // ���� �� �ٽ� ���� ������
            }

        } catch (IOException e) { // ���� ����(transferTo) ����
            log.error("���� ���� �� IOException �߻� (bno={})", bno, e);
            rttr.addFlashAttribute("errorMessage", "���� ���� �� ������ �߻��߽��ϴ�.");
            return "redirect:/edit?bno=" + bno;
        } catch (Exception e) { // ��Ÿ ��� ���� (DB ����, NullPointer ��)
            log.error("�Խñ� ���� ó�� �� ���� �߻� (bno={})", bno, e);
            rttr.addFlashAttribute("errorMessage", "�Խñ� ���� �� ������ �߻��߽��ϴ�.");
            return "redirect:/edit?bno=" + bno; // ���� �� �ٽ� ���� ������
        }
    }

    
    
    
    /**
     * �Խñ� ���ƿ� ó�� (AJAX)
     * @param bno ���ƿ��� �Խñ� ��ȣ
     * @param session �α��� ����� Ȯ��
     * @return ó�� ��� (���� ����, �޽���, ���� ���ƿ� ��)
     */
    @RequestMapping(value = "/board/like", method = RequestMethod.POST) 
    @ResponseBody // ����� JSON���� ��ȯ
    public Map<String, Object> boardLike(@RequestParam("bno") int bno, HttpSession session) {
        log.info("'/board/like' (POST AJAX) ��û ���� - �Խñ� ��ȣ: {}", bno);
        Map<String, Object> response = new HashMap<>();

        // 1. �α��� ���� Ȯ��
        Boolean loginState = (Boolean) session.getAttribute("personalloginstate");
        String userId = (String) session.getAttribute("id"); // ���ƿ� ���� ����� ID (�ʿ�� �α�/�̷� ������)

        if (loginState == null || !loginState || userId == null) {
            log.warn("���ƿ� ����: �α����� �ʿ��մϴ�. (bno={})", bno);
            response.put("success", false);
            response.put("message", "�α����� �ʿ��մϴ�.");
            response.put("likeCount", -1); // ���� �� ���ƿ� �� -1 �Ǵ� ���� ��
            return response;
        }

        log.info("���ƿ� ��û �����: {}", userId); // ���� ���ƿ� �������� �α� ���

        try {
            // 2. ���ǿ��� '���ƿ� ���� �� ���' ��������
            @SuppressWarnings("unchecked")
            Set<Integer> likedPostsInSession = (Set<Integer>) session.getAttribute("likedPostsMap");
            if (likedPostsInSession == null) {
                likedPostsInSession = new HashSet<>();
            }

            // 3. �ڡڡ� ���� ���ǿ��� �̹� ���ƿ並 �������� Ȯ�� �ڡڡ�
            if (likedPostsInSession.contains(bno)) {
                // �̹� ���� ���ǿ��� ���ƿ並 �������Ƿ�, DB ������Ʈ ���� ���� ���¸� ��ȯ
                log.info("����� {}�� �Խñ� {}��(��) �̹� ���� ���ǿ��� ���ƿ� �� (DB ������Ʈ ����).", userId, bno);
                response.put("success", true); // �۾��� ���� (���� Ȯ��)
                response.put("message", "�̹� '���ƿ�' �����Դϴ�. (����)");
                response.put("action", "already_liked_in_session");
                // ���� DB�� ���ƿ� ���� �ٽ� ��ȸ�ؼ� ������ (�ٸ� ����� ������ ���� �����Ƿ�)
                response.put("likeCount", boardService.getLikeCount(bno));
            } else {
                // �ڡڡ� ���� ���ǿ��� ó�� ���ƿ� ���� -> DB ������Ʈ + ���� ��� �ڡڡ�
                log.info("����� {}�� �Խñ� {}�� ���� ���ǿ��� ó�� ���ƿ� ����. DB ������Ʈ �õ�.", userId, bno);

                // 4. DB ������Ʈ �õ� (BoardService ȣ��)
                boolean dbUpdateSuccess = boardService.increaseLikeCount(bno);

                if (dbUpdateSuccess) {
                    // 5. DB ������Ʈ ���� ��, ���ǿ� ���
                    likedPostsInSession.add(bno);
                    session.setAttribute("likedPostsMap", likedPostsInSession);

                    // 6. ����� DB ���ƿ� �� �ٽ� ��ȸ
                    int currentLikeCount = boardService.getLikeCount(bno);

                    log.info("�Խñ� {} DB ���ƿ� ����. ���� ��� �Ϸ�. ���� ���ƿ� ��: {}", bno, currentLikeCount);
                    response.put("success", true);
                    response.put("message", "'���ƿ�' ó�� �Ϸ�!");
                    response.put("action", "liked_and_counted"); // DB ������Ʈ �� ���� ��� �Ϸ� ����
                    response.put("likeCount", currentLikeCount); // ����� ī��Ʈ ��ȯ
                } else {
                    // DB ������Ʈ ���� �� (���� �Խñ� ��)
                    log.warn("�Խñ� {} DB ���ƿ� ������Ʈ ���� (service ��� false).", bno);
                    response.put("success", false);
                    response.put("message", "'���ƿ�' ó���� �����߽��ϴ�.");
                    response.put("action", "db_update_failed");
                    // ���� �ÿ��� ���� DB ī��Ʈ ��ȸ �õ�
                    response.put("likeCount", boardService.getLikeCount(bno));
                }
            }
        } catch (Exception e) {
            
            response.put("success", false);
            response.put("message", "������ �߻��߽��ϴ�. �ٽ� �õ����ּ���.");
            response.put("action", "error");
            // ���� �� ���ƿ� �� -1 �Ǵ� ��ȸ �õ�
             try { response.put("likeCount", boardService.getLikeCount(bno)); } catch (Exception ignored) { response.put("likeCount", -1); }
        }

        return response;
    }
    
    
    
    
    	
}
    
    

	 

