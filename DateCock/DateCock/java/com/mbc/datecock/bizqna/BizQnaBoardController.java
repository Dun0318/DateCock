package com.mbc.datecock.bizqna; // ��Ű���� ����

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mbc.datecock.board.PageDTO;
// import com.mbc.datecock.bizqna.BizQnaBoardDTO; // DTO ����Ʈ
// import com.mbc.datecock.bizqna.BizQnaBoardService; // Service ����Ʈ

@Controller
public class BizQnaBoardController { // Ŭ������ ����

    private static final Logger log = LoggerFactory.getLogger(BizQnaBoardController.class); // Ŭ������ ����

    @Autowired
    private BizQnaBoardService bizQnaService; // ���� Ÿ�� ����

    @Autowired
    private ServletContext servletContext;

    // ��� Q&A�� �̹��� ���ε� ��� (���� UserQna�� �����ϰų� �����ϰ� ��� ����)
    private static final String BIZ_QNA_IMAGE_UPLOAD_PATH = "/image"; // ����: ��� ����

    // ���� ���� Ȯ�� (��� ����� �Ǵ� ������)
    private boolean canAccessBizQna(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin != null && isAdmin) return true; // �����ڴ� �׻� ���� ����

        // *** ������ �κ�: ���� Ű "buisnessloginstate" ��� ***
        Boolean isBusinessLoggedIn = (Boolean) session.getAttribute("buisnessloginstate"); // <-- ��Ÿ ����
        String businessNumber = (String) session.getAttribute("businessnumberA");        // ���ȸ�� ��ȣ ���� Ű

        return Boolean.TRUE.equals(isBusinessLoggedIn) && businessNumber != null && !businessNumber.isEmpty();
    }

    // ���
    @RequestMapping(value = "/bqlist", method = RequestMethod.GET) // ��� ����
    public String list(PageDTO pageDTO, Model model, HttpSession session, HttpServletResponse response) throws Exception {
        // �����ڰ� �ƴϰ�, ��� ����� ���� ���ǵ� �������� ���ϸ� ���� �Ұ� (canAccessBizQna ȣ��)
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin")) && !canAccessBizQna(session)) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('��� ���ǰԽ��� ���� ������ �����ϴ�.'); location.href='../main';</script>"); // �α��� �������� ��������
            out.flush();
            return null;
        }

        // PageDTO�� ���� �α����� ����� ���� ����
        String currentLoginId = null; // ������ �Ǵ� ����� ��ȣ
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        if (isAdmin) {
            currentLoginId = (String) session.getAttribute("id"); // ������ ID (Ȥ�� �ٸ� �ĺ���)
        } else {
            currentLoginId = (String) session.getAttribute("businessnumberA"); // ����� ��ȣ
        }
        pageDTO.setLoginUserId(currentLoginId); // MyBatis���� writer �� �� ��� (����ڹ�ȣ �Ǵ� null)
        pageDTO.setAdmin(isAdmin);

        int totalCount = bizQnaService.getTotalBizQnaCount(pageDTO); // ���� �޼ҵ�� ����
        pageDTO.setTotal(totalCount);
        if (pageDTO.getTotal() > 0) {
             pageDTO.calculatePaging();
        } else {
             log.info("�� ��� ���� �Խñ� ���� 0�̹Ƿ� ����¡ ����� �ǳʶݴϴ�.");
        }

        List<BizQnaBoardDTO> list = bizQnaService.getBizQnaListPaged(pageDTO); // ���� �޼ҵ��, ��ȯŸ�� ����
        model.addAttribute("list", list);
        model.addAttribute("paging", pageDTO);
        model.addAttribute("currentLoginUserId", currentLoginId); // JSP���� �ۼ��� �� � ���
        model.addAttribute("isUserAdmin", isAdmin);
        return "bqlist"; // JSP ��� ����
    }

    // �۾��� ��
    @RequestMapping(value = "/bqwrite", method = RequestMethod.GET)
    public String writeForm(HttpSession session, HttpServletResponse response, Model model) throws IOException {
        // --- ���� Ȯ�� ���� ---
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        // *** ������ �κ�: ���� Ű "buisnessloginstate" ��� ***
        Boolean isBusinessLoggedIn = (Boolean) session.getAttribute("buisnessloginstate"); // <-- ��Ÿ ����
        String businessNumber = (String) session.getAttribute("businessnumberA");
        String adminId = (String) session.getAttribute("id"); // ������ ID �������� (���� Ű Ȯ�� �ʿ�)

        boolean isAuthorized = false;
        if (Boolean.TRUE.equals(isAdmin)) { // ������ �켱 Ȯ��
            isAuthorized = true;
            log.info("������({})�� ��� ���� �ۼ� ������ ����", adminId);
        } else if (Boolean.TRUE.equals(isBusinessLoggedIn) && businessNumber != null && !businessNumber.isEmpty()) { // ��� ȸ�� Ȯ��
            isAuthorized = true;
            log.info("��� �����({})�� ��� ���� �ۼ� ������ ����", businessNumber);
        }

        if (!isAuthorized) { // ������ ���� ���
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            // ���� �Ұ� �޽���
            out.println("<script>alert('��� ���Ǳ� �ۼ� ������ �����ϴ�. ���ȸ�� �Ǵ� �����ڷ� �α������ּ���.'); location.href='../DateCocklog';</script>"); // �α��� ������ ��� Ȯ��
            out.flush();
            return null; // View ��ȯ �� ��
        }
        // --- ���� Ȯ�� ���� �� ---

        // ������ ������ �� DTO �𵨿� �߰��ϰ� �� �������� �̵�
        model.addAttribute("bizQnaBoardDTO", new BizQnaBoardDTO());
        return "bqwrite"; // Tiles definition �̸�
    }

    @RequestMapping(value = "/bqwriteAction", method = RequestMethod.POST)
    public String writeAction(BizQnaBoardDTO dto, HttpSession session, RedirectAttributes rttr, HttpServletResponse response) throws Exception {
        // --- ���� Ȯ�� �� �ۼ���, ����ڸ� ���� ���� ---
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        Boolean isBusinessLoggedIn = (Boolean) session.getAttribute("buisnessloginstate"); // ���� Ű ��Ÿ ����
        String businessNumberFromSession = (String) session.getAttribute("businessnumberA"); // ���ǿ��� ����� ��ȣ ��������
        String adminId = (String) session.getAttribute("id"); // ������ ID

        boolean isAuthorized = false;
        String writerIdentity = null;       // ���� �ۼ��ڷ� ��ϵ� �� (����ڹ�ȣ �Ǵ� ������ID)
        String sessionBusinessName = null;  // ���ǿ��� ������ ����ڸ�

        if (Boolean.TRUE.equals(isAdmin) && adminId != null) { // ������ ���̽�
            isAuthorized = true;
            writerIdentity = adminId;
            // �����ڰ� ���� �� ��� ����ڸ� ó�� ��å �ʿ� (��: "������" �Ǵ� null)
            sessionBusinessName = "������"; // ����: �����ڰ� �ۼ� �� "������"�� ǥ��
            log.info("������({})�� ��� ���� �ۼ� ó�� �õ�", writerIdentity);
        } else if (Boolean.TRUE.equals(isBusinessLoggedIn) && businessNumberFromSession != null && !businessNumberFromSession.isEmpty()) { // ��� ȸ�� ���̽�
            isAuthorized = true;
            writerIdentity = businessNumberFromSession;
            // *** �߿�: ���ǿ��� ����ڸ� �������� ***
            sessionBusinessName = (String) session.getAttribute("businessname");
            log.info("��� �����({})�� ��� ���� �ۼ� ó�� �õ�, ����ڸ�: {}", writerIdentity, sessionBusinessName);
        }

        if (!isAuthorized) { // ���� ���� ��� ó��
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('�α����� �ʿ��ϰų� �� �ۼ� ������ �����ϴ�.'); location.href='../DateCocklog';</script>");
            out.flush();
            return null;
        }

        dto.setWriter(writerIdentity); // DTO�� Ȯ���� �ۼ���(����ڹ�ȣ �Ǵ� ������ID) ����
        // *** �߿�: DTO�� ���ǿ��� ������ ����ڸ� ���� ***
        dto.setBusinessName(sessionBusinessName);
        // --- ���� Ȯ�� �� �ۼ���, ����ڸ� ���� �� ---

        // --- ���� ���ε� ���� (������ ���� ����) ---
        MultipartFile uploadFile = dto.getQnaImageFile();
        String uploadedFileName = null;
        if (uploadFile != null && !uploadFile.isEmpty()) {
            String originalFileName = uploadFile.getOriginalFilename();
            // �̹��� ���ε� ��� (BIZ_QNA_IMAGE_UPLOAD_PATH Ȯ�� �ʿ�)
            String uploadDir = servletContext.getRealPath(BIZ_QNA_IMAGE_UPLOAD_PATH); 
            File saveDir = new File(uploadDir);
            if (!saveDir.exists()) {
                if(!saveDir.mkdirs()) {
                     log.error("BizQnA ���ε� ���� ���� ����: {}", uploadDir);
                     rttr.addFlashAttribute("errorMessage", "���� ���� ���� ������ �����߽��ϴ�.");
                     rttr.addFlashAttribute("bizQnaBoardDTO", dto); // �Է°� ������ ���� DTO ����
                     return "redirect:/bqwrite"; // �۾��� ������ �ٽ� �̵�
                }
            }
            String fileExtension = (originalFileName.contains(".")) ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            File saveFile = new File(uploadDir, uniqueFileName);
            try {
                uploadFile.transferTo(saveFile);
                uploadedFileName = uniqueFileName;
                log.info("BizQnA ���� ���ε� ����: {}", saveFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("BizQnA ���� ���ε� ����: ", e);
                rttr.addFlashAttribute("errorMessage", "���� ���ε� �� ������ �߻��߽��ϴ�.");
                rttr.addFlashAttribute("bizQnaBoardDTO", dto);
                return "redirect:/bqwrite";
            }
        }
        dto.setImageFile(uploadedFileName); // DB�� ����� ���ϸ� (������ null)
        // --- ���� ���ε� ���� �� ---

        // --- ���� ȣ�� �� ��� ó�� ---
        boolean writeSuccess = false;
        try {
             // ���� DTO���� writer�� businessName�� ��� �����Ǿ� ����
            writeSuccess = bizQnaService.writeBizQna(dto);
        } catch (Exception e) {
            log.error("BizQnA DB ���� �� ���� �߻�", e);
            // writeSuccess�� �̹� false
        }

        if (writeSuccess) {
            rttr.addFlashAttribute("message", "��� ���Ǳ��� ��ϵǾ����ϴ�.");
        } else {
            rttr.addFlashAttribute("errorMessage", "��� ���Ǳ� ��Ͽ� �����߽��ϴ�. �Է� ������ Ȯ�����ּ���.");
            rttr.addFlashAttribute("bizQnaBoardDTO", dto); // ���� �� �Է°� ������ ���� DTO ����
            if (uploadedFileName != null) { // ���� ���ε�� ���������� DB ������ ������ ���, ���ε�� ���� ����
                 try {
                    new File(servletContext.getRealPath(BIZ_QNA_IMAGE_UPLOAD_PATH + File.separator + uploadedFileName)).delete();
                    log.info("DB ���� ���з� ���� BizQnA ���ε� ���� ����: {}", uploadedFileName);
                 } catch(Exception e) {
                    log.error("DB ���� ���� �� ���ε� ���� ���� �� ����", e);
                 }
            }
            return "redirect:/bqwrite"; // �۾��� ������ �ٽ� �̵�
        }
        return "redirect:/bqlist"; // ���� �� �������
        // --- ���� ȣ�� �� ��� ó�� �� ---
    }

    // �󼼺���
    @RequestMapping(value = "/bqview", method = RequestMethod.GET) // ��� ����
    public String view(@RequestParam("bno") int bno, PageDTO pageDTO, Model model, HttpSession session, RedirectAttributes rttr, HttpServletResponse response) throws Exception {
        String currentLoginId = null; // ���� �α����� ����� ID (������ ID �Ǵ� ����ڹ�ȣ)
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        // *** ����� �̹� ��Ÿ("buisnessloginstate")�� �Ǿ� ���� ***
        Boolean isBusinessLoggedIn = (Boolean) session.getAttribute("buisnessloginstate"); // <-- ��Ÿ ������
        String businessNumber = (String) session.getAttribute("businessnumberA");

        if (isAdmin) {
            currentLoginId = (String) session.getAttribute("id"); // �������� ��� �Ϲ� ID ��� ����
        } else if (Boolean.TRUE.equals(isBusinessLoggedIn) && businessNumber != null) {
            currentLoginId = businessNumber;
        }

        BizQnaBoardDTO dtoForAuth = bizQnaService.getBizQnaForAuth(bno); // ���� �޼ҵ��, ��ȯŸ�� ����
        if (dtoForAuth == null) {
            rttr.addFlashAttribute("errorMessage", "�ش� ��� ���Ǳ��� ã�� �� �����ϴ�.");
            // *** �߿�: �󼼺��⿡���� pageDTO.getQueryString() ��� �ʿ� ***
            // rttr.addAttribute(...) �� ���� �Ķ���� �߰��ϰų�, PageDTO�� getQueryString() ���
            return "redirect:/bqlist" + pageDTO.getQueryString(); // ������ bqlist.jsp�� ��ũ ���� ��İ� ��ġ��Ű�� ���� �ʿ�
        }

        boolean canView = isAdmin; // �����ڴ� �׻� ���� ����
        if (!canView && dtoForAuth.getWriter().equals(currentLoginId)) { // �ۼ��� ����(�����)�� ���
            canView = true;
        }
        if (!canView && dtoForAuth.getSecret() == 0) { // �������� ���
            canView = true;
        }


        if (!canView) { // ���� ���� üũ (���⼭�� isBusinessLoggedIn ���)
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('���� �� ������ �����ϴ�.'); location.href='../bqlist" + pageDTO.getQueryString() +"';</script>"); // ��� ���� �� �Ķ���� ����
            out.flush();
            return null;
        }

        BizQnaBoardDTO dto = bizQnaService.getBizQnaDetail(bno, currentLoginId, isAdmin); // ���� �޼ҵ��, ��ȯŸ�� ����

        model.addAttribute("dto", dto); // DTO Ÿ�� ����
        model.addAttribute("paging", pageDTO);
        model.addAttribute("currentLoginUserId", currentLoginId); // JSP���� ���
        model.addAttribute("isUserAdmin", isAdmin);
        return "bqview"; // JSP ��� ����
    }

    // ���� ��
    @RequestMapping(value = "/bqedit", method = RequestMethod.GET) // ��� ����
    public String editForm(@RequestParam("bno") int bno, PageDTO pageDTO, Model model, HttpSession session, RedirectAttributes rttr, HttpServletResponse response) throws Exception {
        String currentLoginId = null;
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        String businessNumberFromSession = (String) session.getAttribute("businessnumberA");

        if (isAdmin) {
            currentLoginId = (String) session.getAttribute("id");
        } else if (businessNumberFromSession != null) {
            currentLoginId = businessNumberFromSession;
        }


        BizQnaBoardDTO dto = bizQnaService.getBizQnaForAuth(bno); // ���� �޼ҵ��, ��ȯŸ�� ����

        if (dto == null) {
            rttr.addFlashAttribute("errorMessage", "������ ���� �������� �ʽ��ϴ�.");
            // *** �����̷�Ʈ �� ����¡ ���� ���� ***
            rttr.addAttribute("nowPage", pageDTO.getNowPage());
            rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
            if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
            if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
            return "redirect:/bqlist"; // ��� ����
        }
        // �����ڰ� �ƴϰ�, ���� �α����� ����� ��ȣ�� �� �ۼ���(����ڹ�ȣ)�� �ٸ��� ���� ����
        if (!isAdmin && (currentLoginId == null || !currentLoginId.equals(dto.getWriter()))) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('���� ������ �����ϴ�.'); history.back();</script>");
            out.flush();
            return null;
        }
        model.addAttribute("dto", dto); // DTO Ÿ�� ����
        model.addAttribute("paging", pageDTO);
        return "bqedit"; // JSP ��� ����
    }

    // ���� ó��
    @RequestMapping(value = "/bqeditAction", method = RequestMethod.POST) // ��� ����
    public String editAction(
            BizQnaBoardDTO dto, // DTO Ÿ�� ����
            @RequestParam(value="deleteExistingImage", required=false) String deleteExistingImage,
            PageDTO pageDTO,
            HttpSession session,
            RedirectAttributes rttr,
            HttpServletResponse response) throws Exception {

        log.info("--- ��� ���Ǳ� ���� ó�� ���� (bno: {}) ---", dto.getBno());

        String currentLoginId = null; // ���� �α����� ����� ID (������ ID �Ǵ� ����ڹ�ȣ)
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        String businessNumberFromSession = (String) session.getAttribute("businessnumberA");

        if (isAdmin) {
            currentLoginId = (String) session.getAttribute("id");
        } else if (businessNumberFromSession != null) {
            currentLoginId = businessNumberFromSession;
        }

        BizQnaBoardDTO originalDto = bizQnaService.getBizQnaForAuth(dto.getBno()); // ����, DTO ����
        if (originalDto == null) {
            // *** ���� �� ���� ó�� (�����̷�Ʈ �� ����¡ ���� ����) ***
            rttr.addFlashAttribute("errorMessage", "������ ���� ���� �����ϴ�.");
            rttr.addAttribute("nowPage", pageDTO.getNowPage());
            rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
            if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
            if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
            return "redirect:/bqlist"; // ��� ����
        }
        if (!isAdmin && (currentLoginId == null || !currentLoginId.equals(originalDto.getWriter()))) {
            // (���� ���� ���� ó�� ������ UserQna�� ����)
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('���� ������ �����ϴ�.'); history.back();</script>");
            out.flush();
            return null;
        }

        // --- ���� ó�� ---
        String oldFileName = originalDto.getImageFile();
        String dbUpdateFileName = oldFileName;
        String newUploadedFileName = null;
        String realUploadPath = servletContext.getRealPath(BIZ_QNA_IMAGE_UPLOAD_PATH);

        MultipartFile uploadFile = dto.getQnaImageFile();
        if (uploadFile != null && !uploadFile.isEmpty()) {
             File saveDir = new File(realUploadPath);
            if (!saveDir.exists()) { saveDir.mkdirs(); }
            String originalFilename = uploadFile.getOriginalFilename();
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFileName = UUID.randomUUID().toString() + extension;
            File saveFile = new File(realUploadPath, savedFileName);
            try {
                uploadFile.transferTo(saveFile);
                newUploadedFileName = savedFileName;
                dbUpdateFileName = newUploadedFileName;
                log.info("��� QnA �� ���� ���� ����: {}", savedFileName);
            } catch (IOException e) {
                 rttr.addFlashAttribute("errorMessage", "���� ���� �� ���� �߻�");
                 // *** �����̷�Ʈ �� ����¡ ���� ���� ***
                 rttr.addAttribute("bno", dto.getBno());
                 rttr.addAttribute("nowPage", pageDTO.getNowPage());
                 rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
                 if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
                 if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
                 return "redirect:/bqedit"; // ��� ����
            }
        } else {
            if ("yes".equals(deleteExistingImage) && oldFileName != null) {
                dbUpdateFileName = null;
            }
        }
        dto.setImageFile(dbUpdateFileName);
        // --- ���� ó�� �� ---

        boolean updateSuccess = false;
        try {
             dto.setWriter(originalDto.getWriter()); // �ۼ��ڴ� ���� �Ұ�
             updateSuccess = bizQnaService.updateBizQna(dto, currentLoginId, isAdmin); // ���� �޼ҵ��, �Ķ���� Ÿ�� ����
        } catch (Exception e) {
            log.error("BizQnA DB ������Ʈ �� ���� �߻�!", e);
        }

        if (updateSuccess) {
            // ���� ���� ����
            if (newUploadedFileName != null && oldFileName != null && !oldFileName.isEmpty()) {
                new File(realUploadPath, oldFileName).delete();
            } else if ("yes".equals(deleteExistingImage) && dbUpdateFileName == null && oldFileName != null && !oldFileName.isEmpty()) {
                new File(realUploadPath, oldFileName).delete();
            }
            rttr.addFlashAttribute("message"," ��� ���Ǳ��� �����Ǿ����ϴ�.");
            // *** �󼼺��� �����̷�Ʈ �� ����¡ ���� ���� ***
            rttr.addAttribute("bno", dto.getBno());
            rttr.addAttribute("nowPage", pageDTO.getNowPage());
            rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
            if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
            if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
            return "redirect:/bqview"; // ��� ����
        } else {
            // DB ������Ʈ ���� �� �ѹ� ó��
            if (newUploadedFileName != null) {
                new File(realUploadPath, newUploadedFileName).delete();
            }
            rttr.addFlashAttribute("errorMessage", "��� ���Ǳ� ������ �����߽��ϴ�.");
            // *** ������ �����̷�Ʈ �� ����¡ ���� ���� ***
            rttr.addAttribute("bno", dto.getBno());
            rttr.addAttribute("nowPage", pageDTO.getNowPage());
            rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
            if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
            if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
            return "redirect:/bqedit"; // ��� ����
        }
    }

    // ���� ó��
    @RequestMapping(value = "/bqdelete", method = RequestMethod.GET) // ��� ����
    public String deleteAction(@RequestParam("bno") int bno, PageDTO pageDTO, HttpSession session, RedirectAttributes rttr, HttpServletResponse response) throws Exception {
        String currentLoginId = null;
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        String businessNumberFromSession = (String) session.getAttribute("businessnumberA");

        if (isAdmin) {
            currentLoginId = (String) session.getAttribute("id");
        } else if (businessNumberFromSession != null) {
            currentLoginId = businessNumberFromSession;
        }

        BizQnaBoardDTO dtoToDelete = bizQnaService.getBizQnaForAuth(bno); // ����, DTO ����
        if (dtoToDelete == null) {
            // *** �� ���� ó�� (�����̷�Ʈ �� ����¡ ���� ����) ***
            rttr.addFlashAttribute("errorMessage", "������ ���� �������� �ʽ��ϴ�.");
            // pageDTO.getQueryString() ��� ���� �Ķ���� �߰�
            rttr.addAttribute("nowPage", pageDTO.getNowPage());
            rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
            if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
            if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
            return "redirect:/bqlist"; // ��� ����
        }
        if (!isAdmin && (currentLoginId == null || !currentLoginId.equals(dtoToDelete.getWriter()))) {
            // (���� ���� ���� ó�� ������ UserQna�� ����)
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('���� ������ �����ϴ�.'); history.back();</script>");
            out.flush();
            return null;
        }

        boolean deleteDbSuccess = false;
        try {
            deleteDbSuccess = bizQnaService.deleteBizQna(bno, currentLoginId, isAdmin); // ���� �޼ҵ�� ����
        } catch (Exception e) {
            log.error("BizQnA DB ���� �� ����", e);
        }

        if (deleteDbSuccess) {
            // ÷������ ����
            if (dtoToDelete.getImageFile() != null && !dtoToDelete.getImageFile().isEmpty()) {
                String filePath = servletContext.getRealPath(BIZ_QNA_IMAGE_UPLOAD_PATH + File.separator + dtoToDelete.getImageFile());
                new File(filePath).delete();
            }
            rttr.addFlashAttribute("message", "��� ���Ǳ��� �����Ǿ����ϴ�.");
        } else {
            rttr.addFlashAttribute("errorMessage", "��� ���Ǳ� ������ �����߰ų� ������ �����ϴ�.");
            // *** ���� �� �󼼺��� �����̷�Ʈ - ����¡ ���� ���� ***
            rttr.addAttribute("bno", bno);
            rttr.addAttribute("nowPage", pageDTO.getNowPage());
            rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
            if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
            if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
            return "redirect:/bqview"; // ��� ����
        }
        // *** ���� �� ��� �����̷�Ʈ - ����¡ ���� ���� ***
        rttr.addAttribute("nowPage", pageDTO.getNowPage());
        rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
        if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
        if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
        return "redirect:/bqlist"; // ��� ����
    }

    // ������ �亯 ����/����
    @RequestMapping(value = "/bqSaveAnswer", method = RequestMethod.POST) // ��� ����
    public String saveAnswer(
            BizQnaBoardDTO dto, // DTO Ÿ�� ����
            PageDTO pageDTO,
            HttpSession session,
            RedirectAttributes rttr,
            HttpServletResponse response) throws Exception {

        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            // (������ ���� ���� ó�� ������ UserQna�� ����)
             response.setContentType("text/html;charset=UTF-8");
             PrintWriter out = response.getWriter();
             out.println("<script>alert('�亯 �ۼ� ������ �����ϴ�.'); history.back();</script>");
             out.flush();
            return null;
        }
        String adminId = (String) session.getAttribute("id"); // ������ ID

        boolean saveSuccess = false;
        String qnaTitle = ""; // ������ ���� ����
        try {
            // ���� �Խñ� ���� ��ȸ (�亯 ���� ����)
            BizQnaBoardDTO originalDto = bizQnaService.getBizQnaForAuth(dto.getBno()); // ����: ������ �������� ���� ��ȸ
            if (originalDto != null) {
                qnaTitle = originalDto.getTitle();
            }
            saveSuccess = bizQnaService.saveAnswer(dto, adminId);
        } catch (Exception e) {
            log.error("BizQnA �亯 ���� �� ���� �߻� (bno: {})", dto.getBno(), e);
        }

        if (saveSuccess) {
            // ��ȸ�� title ���
            rttr.addFlashAttribute("message", "'" + qnaTitle + "' ������ ��� ���Ǳۿ� �亯�� ���/�����Ǿ����ϴ�.");
        } else {
            rttr.addFlashAttribute("errorMessage", "��� ���Ǳ� �亯 ���/������ �����߽��ϴ�.");
        }
        // *** �󼼺��� �����̷�Ʈ �� ����¡ ���� ���� ***
        rttr.addAttribute("bno", dto.getBno());
        rttr.addAttribute("nowPage", pageDTO.getNowPage());
        rttr.addAttribute("cntPerPage", pageDTO.getCntPerPage());
        if (pageDTO.getSearchType() != null) rttr.addAttribute("searchType", pageDTO.getSearchType());
        if (pageDTO.getKeyword() != null) rttr.addAttribute("keyword", pageDTO.getKeyword());
        return "redirect:/bqview"; // ��� ����
    }
}