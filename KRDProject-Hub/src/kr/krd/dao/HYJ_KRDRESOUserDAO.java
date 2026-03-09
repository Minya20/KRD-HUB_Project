package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import kr.util.DBUtil;

public class HYJ_KRDRESOUserDAO {

	public HJY_CheckSystem dao1 = new HJY_CheckSystem();

	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


	// ===========================
	// 공고 목록
	// ===========================
	public void selectAnn(String cust_id) {

		while (true) {

			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			try {
				conn = DBUtil.getConnection();

				String sql = "SELECT * FROM ANNOUNCEMENT WHERE ANNOUNCEMENT_HIDDEN_YN = 0 ORDER BY ANNOUNCEMENT_ANN_ID";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();

				System.out.println("\n" + "-".repeat(40));

				if (!rs.next()) {
					System.out.println("등록된 공고가 없습니다.");
					return;
				}

				System.out.println("번호\t\t공고명\t\t\t예산\t\t시작일\t\t종료일");

				do {
					System.out.print(rs.getInt("ANNOUNCEMENT_ANN_ID") + "\t" + "\t");
					System.out.print(rs.getString("ANNOUNCEMENT_TITLE") + "\t" + "\t");
					System.out.printf("%,d\t", rs.getLong("ANNOUNCEMENT_TOTAL_BUDGET"));
					System.out.print(rs.getString("ANNOUNCEMENT_START_DT") + "\t");
					System.out.println(rs.getString("ANNOUNCEMENT_END_DT"));
				} while (rs.next());

				System.out.println("-".repeat(40));
				System.out.print("1. 상세조회  2. 이전화면 : ");
				int sel = Integer.parseInt(br.readLine());
				
				if (sel == 1) {
						try {
							System.out.print("상세조회할 번호 입력 : ");
							int annId = Integer.parseInt(br.readLine());
							//System.out.println("해당 목록에 있는 번호를 입력하세요");
							detailAnn(annId, cust_id);   // 상세 메서드 분리
							
						}catch(NumberFormatException e) {
							System.out.println("숫자를 입력하세요.");
						}
				} else if (sel == 2) {
					return;   // 이전 화면으로 복귀
				} else {
					System.out.println("잘못된 입력입니다.");
				}

			} catch(NumberFormatException e) {
				System.out.println("숫자를 입력하세요");
			}
			catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}
	// ===========================
	// 공고 상세보기
	// ===========================
	private void detailAnn(int annId, String cust_id) {

		while (true) {

			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			try {
				conn = DBUtil.getConnection();

				String sql = "SELECT * FROM ANNOUNCEMENT WHERE ANNOUNCEMENT_ANN_ID = ?"
						+ " AND ANNOUNCEMENT_HIDDEN_YN = 0";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, annId);
				rs = pstmt.executeQuery();

				if (!rs.next()) {
					System.out.println("해당 공고가 없습니다.");
					return;
				}

				System.out.println("\n" + "-".repeat(60));
				System.out.println("공고번호 : " + rs.getInt("ANNOUNCEMENT_ANN_ID"));
				System.out.println("기관번호 : " + rs.getString("ANNOUNCEMENT_AGY_ID"));
				System.out.println("공고명 : " + rs.getString("ANNOUNCEMENT_TITLE"));
				System.out.println("재공고여부 : " + rs.getInt("ANNOUNCEMENT_REANN_YN"));
				System.out.println("담당자연락처 : " + rs.getString("ANNOUNCEMENT_PM_CONTACT"));
				System.out.println("모집인원 : " + rs.getInt("ANNOUNCEMENT_RECRUIT_CAP"));
				System.out.println("접수시작일 : " + rs.getString("ANNOUNCEMENT_START_DT"));
				System.out.println("접수종료일 : " + rs.getString("ANNOUNCEMENT_END_DT"));
				System.out.println("공고상태 : " + rs.getString("ANNOUNCEMENT_STATUS"));
				System.out.println("모집분야 : " + rs.getString("ANNOUNCEMENT_FIELD"));
				System.out.println("공고담당자 : " + rs.getString("ANNOUNCEMENT_CREATED_BY"));
				System.out.printf("총예산 : %,d\n", rs.getLong("ANNOUNCEMENT_TOTAL_BUDGET"));
				System.out.println("-".repeat(60));

				System.out.print("1. 신청하기  2. 목록으로 : ");
				int sel = Integer.parseInt(br.readLine());

				if (sel == 1) {
					
					//이미 로그인했는데 굳이 다시 신청자 아이디를 입력할 이유가 없어 보여서 로그인 시 유저 아이디 값을 받아서 사용하도록
					// 수정 하였음
					//System.out.print("신청자 아이디 입력 : ");
					//String userId = br.readLine();

					System.out.print("첨부파일 경로 입력 : ");
					String attachPath = br.readLine();
					int budgetAmt;
					while(true) {
						System.out.print("신청 예산 입력[10억미만] : ");
						budgetAmt = Integer.parseInt(br.readLine());
						if(budgetAmt > 999999999) {
							System.out.println("정해진 범위 내에서 입력 해주 세요.");
							continue;
						}
						break;
					}
					System.out.println("체크용");
					System.out.println(cust_id);

					// 신청 처리 (DB 처리 전부 여기서 수행)
					dao1.applyAnnouncement(annId, cust_id, attachPath, budgetAmt);
					return;
				} 
				else if (sel == 2) {
					return;  // 목록으로
				} 
				else {
					System.out.println("잘못된 입력입니다.");
				}

			}catch (NumberFormatException e) {
				System.out.println("숫자를 입력하세요");
			}catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}
}