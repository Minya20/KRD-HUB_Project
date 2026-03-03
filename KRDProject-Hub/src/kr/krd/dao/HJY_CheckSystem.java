package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HJY_CheckSystem {

    public void applyAnnouncement(int annId,
                                  String userId,
                                  String attachPath,
                                  int budgetAmt) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            /* 1 공고 상태 + 정원 조회 */
            String checkSql =
                    "SELECT ANNOUNCEMENT_STATUS, ANNOUNCEMENT_RECRUIT_CAP " +
                    "FROM ANNOUNCEMENT WHERE ANNOUNCEMENT_ANN_ID = ?";
            
            //ANNOUNCEMENT_HIDDEN_YN의 값이 0인 것도 조건에 포함해야함
            //1이면 히든인경우, 0이면 히든이 아닌경우
            
            
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, annId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("공고가 존재하지 않습니다.");
                conn.rollback();
                return;
            }

            String status = rs.getString("ANNOUNCEMENT_STATUS");
            int cap = rs.getInt("ANNOUNCEMENT_RECRUIT_CAP");

            if (!"공고중".equals(status)) {
                System.out.println("현재 모집중인 공고가 아닙니다.");
                conn.rollback();
                return;
            }
            
            if(cap == 0) {
            	System.out.println("모집인원이 모두 모집되었습니다.");
            	conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();


            /* 2 현재 선정된 인원 수 체크 (SELECTION 기준) */
            /* 
            String selectedSql =
                    "SELECT COUNT(*) FROM SELECTION " +
                    "WHERE SELECTION_ANN_ID = ?";

            pstmt = conn.prepareStatement(selectedSql);
            pstmt.setInt(1, annId);
            rs = pstmt.executeQuery();
            rs.next();

            int selectedCount = rs.getInt(1);

            if (selectedCount >= cap) {
                System.out.println("이미 선정 인원이 마감되었습니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();
			*/ 
            //이미 트리거로 사용자가 신청하면(insert selection을 하면)
            //공고테이블에 cap이 자동으로 -1 되게 하였음
            //cap이 0인지만 검사하면 됨
            //신청인원수를 따로 변수에 담아 사용할 계획이면 
            //67라인에 있는 if문 부분을 수정하기
            

            /* 3 중복 신청 체크 */
            String dupSql =
                    "SELECT COUNT(*) FROM APPLICATIONS " +
                    "WHERE APPLICATION_ANN_ID = ? " +
                    "AND APPLICATION_USER_ID = ?";

            pstmt = conn.prepareStatement(dupSql);
            pstmt.setInt(1, annId);
            pstmt.setString(2, userId);
            rs = pstmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                System.out.println("이미 신청한 공고입니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();


            /* 4.진행중(SELECTED) 과제 5개 제한 */
            String countSql =
                    "SELECT COUNT(*) FROM APPLICATIONS " +
                    "WHERE APPLICATION_USER_ID = ? " +
                    "AND APPLICATION_STATUS_CD = 'SELECTED'";

            pstmt = conn.prepareStatement(countSql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            rs.next();

            if (rs.getInt(1) >= 5) {
                System.out.println("진행중 과제가 5개 이상입니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();

            
            /* 5.APPLICATION INSERT */
            String insertSql =
                    "INSERT INTO APPLICATION (" +
                    "APPLICATION_ID, " +
                    "APPLICATION_ANN_ID, " +
                    "APPLICATION_USER_ID, " +
                    "APPLICATION_APPLIED_AT, " +
                    "APPLICATION_UPDATED_AT, " +
                    "APPLICATION_ATTACH_PATH, " +
                    "APPLICATION_STATUS_CD, " +
                    "APPLICATION_BUDGET_AMT" +
                    ") VALUES (app_seq.nextval,?, ?, SYSDATE, SYSDATE, ?, 'APPLIED', ?)";
            		//삽입할 때 APPLICATION_ID 컬럼이 존재 하지 않았음
            		//추가하고 app_seq.nextval 사용
            
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, annId);
            pstmt.setString(2, userId);
            pstmt.setString(3, attachPath);
            pstmt.setInt(4, budgetAmt);

            pstmt.executeUpdate();
            pstmt.close();

            conn.commit();
            System.out.println("신청 완료!");

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {}
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }
    
    
}