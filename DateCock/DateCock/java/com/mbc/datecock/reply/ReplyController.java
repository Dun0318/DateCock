package com.mbc.datecock.reply; // <<<--- ���� ��� ���� ��Ű�� ��η� ����!

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // <<<--- @Controller ���
import org.springframework.web.bind.annotation.PathVariable; // <<<--- @PathVariable import
import org.springframework.web.bind.annotation.RequestBody; // <<<--- @RequestBody import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody; // <<<--- @ResponseBody import

@Controller // <<<--- @RestController ��� @Controller ���
@RequestMapping("/replies") // ��� ���� ��û �⺻ ���
public class ReplyController {

    private static final Logger log = LoggerFactory.getLogger(ReplyController.class);

    @Autowired
    private ReplyService replyService; // <<<--- ReplyService ����ü ���� �ʿ�

    /**
     * ��� ��� ��ȸ (GET /replies/{bno})
     * @param bno �Խñ� ��ȣ
     * @return ��� ��� �Ǵ� ���� ����
     */
    @RequestMapping(value = "/{bno}", method = RequestMethod.GET) // <<<--- @GetMapping ��� ���
    @ResponseBody // <<<--- JSON/������ ���� ���� ���� �߰�
    public ResponseEntity<List<ReplyDTO>> list(@PathVariable("bno") int bno) {
        log.info("��� ��� ��ȸ ��û: /replies/{}", bno);
        ResponseEntity<List<ReplyDTO>> entity = null;
        try {
            List<ReplyDTO> list = replyService.getCommentList(bno); // ���� ȣ��
            entity = new ResponseEntity<>(list, HttpStatus.OK); // ���� (200)
        } catch (Exception e) {
            log.error("��� ��� ��ȸ ����: bno={}", bno, e);
            entity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // ���� (500)
        }
        return entity;
    }

    /**
     * �� ��� ��� (POST /replies/new)
     * @param dto ��� ���� (JSON)
     * @param session �ۼ��� Ȯ�ο�
     * @return ó�� ��� (JSON)
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST) // <<<--- @PostMapping ��� ���
    @ResponseBody // <<<--- JSON/������ ���� ���� ���� �߰�
    public ResponseEntity<Map<String, Object>> create(@RequestBody ReplyDTO dto, HttpSession session) {
        log.info("�� ��� ��� ��û: {}", dto);
        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> entity = null;
        try {
            String replyerId = (String) session.getAttribute("id");
            Boolean loginState = (Boolean) session.getAttribute("personalloginstate"); // ���� Ű Ȯ��!
            if (loginState == null || !loginState || replyerId == null) {
                response.put("success", false); response.put("message", "�α��� �ʿ�");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401
            }
            dto.setReplyer(replyerId);

            if (dto.getReplytext() == null || dto.getReplytext().trim().isEmpty()) {
                 response.put("success", false); response.put("message", "��� ���� ����");
                 return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
             }
             if (dto.getBno() <= 0) {
                  response.put("success", false); response.put("message", "�Խñ� ��ȣ ����");
                  return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
             }

            boolean success = replyService.addComment(dto); // ���� ȣ��
            if (success) {
                response.put("success", true); response.put("message", "��� ��� ����");
                entity = new ResponseEntity<>(response, HttpStatus.OK); // 200
            } else {
                response.put("success", false); response.put("message", "��� ��� ����");
                entity = new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
            }
        } catch (Exception e) {
            log.error("��� ��� �� ���� �߻�: {}", dto, e);
            response.put("success", false); response.put("message", "��� ��� �� ���� ����");
            entity = new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
        return entity;
    }

    /**
     * ��� ���� (PUT /replies/{rno})
     * @param rno ��� ��ȣ
     * @param dto ���� ���� (replytext)
     * @param session ����� Ȯ�ο�
     * @return ó�� ��� (JSON)
     */
    @RequestMapping(value = "/{rno}", method = RequestMethod.PUT) // <<<--- @PutMapping ��� ���
    @ResponseBody // <<<--- JSON/������ ���� ���� ���� �߰�
    public ResponseEntity<Map<String, Object>> modify(
            @PathVariable("rno") int rno,
            @RequestBody ReplyDTO dto, // JSON���� ���� ���� ����
            HttpSession session) {
    	
        log.info("{}�� ��� ���� ��û: {}", rno, dto.getReplytext());
        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> entity = null;
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin"); // ������ ���� Ȯ��
        try {
             String currentUserId = (String) session.getAttribute("id");
             Boolean loginState = (Boolean) session.getAttribute("personalloginstate");
             if (loginState == null || !loginState || currentUserId == null) {
                 response.put("success", false); response.put("message", "�α��� �ʿ�");
                 return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401
             }
             
          // --- �ڡڡ� ���� Ȯ�� ���� �߰� �ڡڡ� ---
             boolean hasPermission = false;
             try {
                 ReplyDTO originalReply = replyService.getReply(rno); // ��� ���� ��������
                 if (originalReply != null) {
                     if (currentUserId.equals(originalReply.getReplyer())) { // �ۼ��� Ȯ��
                         hasPermission = true;
                     } else if (isAdmin != null && isAdmin == true) { // ������ Ȯ��
                         hasPermission = true;
                         
                     }
                 } else {
                      response.put("success", false); response.put("message", "������ ����� �������� �ʽ��ϴ�.");
                      return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
                 }
             } catch (Exception e) {
                  log.error("��� ���� ���� Ȯ�� �� ���� �߻� (rno={})", rno, e);
                  response.put("success", false); response.put("message", "��� ���� Ȯ�� �� ���� �߻�");
                  return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
             }

             if (!hasPermission) {
                
                 response.put("success", false); response.put("message", "��� ���� ������ �����ϴ�.");
                 return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // 403 Forbidden
             }
             // --- �ڡڡ� ���� Ȯ�� ���� �� �ڡڡ� ---
             
             

             dto.setRno(rno); // ����� rno�� DTO�� ����
             // TODO: ���� �������� ���� ����ڰ� ��� �ۼ������� Ȯ���ϴ� ���� �ʿ�!
             boolean success = replyService.modifyComment(dto); // ����: ���� �޼ҵ� (���� �ʿ�)

             if (success) {
                 response.put("success", true); response.put("message", "��� ���� ����");
                 entity = new ResponseEntity<>(response, HttpStatus.OK); // 200
             } else {
                 response.put("success", false); response.put("message", "��� ���� ���� �Ǵ� ���� ����");
                 entity = new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 �Ǵ� 403
             }
        } catch (Exception e) {
            log.error("{}�� ��� ���� �� ����", rno, e);
            response.put("success", false); response.put("message", "��� ���� �� ���� ����");
            entity = new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
        return entity;
    }

    /**
     * ��� ���� (DELETE /replies/{rno})
     * @param rno ��� ��ȣ
     * @param session ����� Ȯ�ο�
     * @return ó�� ��� (JSON)
     */
    @RequestMapping(value = "/{rno}", method = RequestMethod.DELETE) // <<<--- @DeleteMapping ��� ���
    @ResponseBody // <<<--- JSON/������ ���� ���� ���� �߰�
    public ResponseEntity<Map<String, Object>> remove(
            @PathVariable("rno") int rno,
            HttpSession session) {

        log.info("{}�� ��� ���� ��û", rno);
        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> entity = null;
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
         try {
            String currentUserId = (String) session.getAttribute("id");
            Boolean loginState = (Boolean) session.getAttribute("personalloginstate");
             if (loginState == null || !loginState || currentUserId == null) {
                 response.put("success", false); response.put("message", "�α��� �ʿ�");
                 return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401
             }
             
          // --- �ڡڡ� ���� Ȯ�� ���� �߰� �ڡڡ� ---
         boolean hasPermission = false;
         try {
             ReplyDTO originalReply = replyService.getReply(rno); // ��� ���� ��������
             if (originalReply != null) {
                 if (currentUserId.equals(originalReply.getReplyer())) { // �ۼ��� Ȯ��
                     hasPermission = true;
                 } else if (isAdmin != null && isAdmin == true) { // ������ Ȯ��
                     hasPermission = true;
                     
                 }
             } else {
                  response.put("success", false); response.put("message", "������ ����� �������� �ʽ��ϴ�.");
                  return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
             }
         } catch (Exception e) {
              log.error("��� ���� ���� Ȯ�� �� ���� �߻� (rno={})", rno, e);
              response.put("success", false); response.put("message", "��� ���� Ȯ�� �� ���� �߻�");
              return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
         }

         if (!hasPermission) {
            
             response.put("success", false); response.put("message", "��� ���� ������ �����ϴ�.");
             return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // 403 Forbidden
         }
        // --- �ڡڡ� ���� Ȯ�� ���� �� �ڡڡ� ---
             
             
            // TODO: ���� �������� ���� ����ڰ� ��� �ۼ������� Ȯ�� �� ���� ó��
            boolean success = replyService.removeComment(rno, currentUserId); // ����: ���� �޼ҵ� (���� �ʿ�)

            if (success) {
                response.put("success", true); response.put("message", "��� ���� ����");
                return new ResponseEntity<>(response, HttpStatus.OK); // 200
            } else {
                 response.put("success", false); response.put("message", "��� ���� ���� �Ǵ� ���� ����");
                 return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 �Ǵ� 403
            }
        } catch (Exception e) {
             log.error("{}�� ��� ���� �� ����", rno, e);
             response.put("success", false); response.put("message", "��� ���� �� ���� �߻�");
             return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
    }

} // End of ReplyController class