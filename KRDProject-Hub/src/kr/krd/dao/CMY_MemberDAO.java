package kr.krd.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;



public class CMY_MemberDAO {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String cust_id;
	String role;
	String field;

	//로그인 메서드
	public String userLogin(String user_id, String user_pw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_id = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM USERINFO WHERE USER_ID = ? AND USER_PWD = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.setString(2, user_pw);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				String user_name = rs.getString("USER_NAME");
				System.out.println(user_name + "님 환영합니다.");
				real_id = rs.getString("user_id");	//일치하면 real_id에 유저 아이디를 넣는다

			}else {
				System.out.println("아이디 혹은 비밀번호가 일치 하지 않습니다.");
				real_id = "0";	//틀리면 "0"을 넣는다.

			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_id;
	}

	//사용자의 권한을 반환하는 메서드
	public String getUserRole(String user_id) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_role = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_role_cd FROM USERINFO WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				real_role = rs.getString("user_role_cd");	//일치하면 real_id에 유저 아이디를 넣는다
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_role;
	}

	//사용자의 분야를 반환하는 메서드
	public String getUserField(String user_id) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_field = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_field FROM USERINFO WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				real_field = rs.getString("user_field");	//일치하면 real_id에 유저 아이디를 넣는다
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_field;
	}


	//평가 배정 목록 조회
	//평가 테이블에 행이 있어야 조회 함
	public void readEval() {
		while(true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			String sql = null;
			ResultSet rs = null;
			//DB 행값을 담을 변수를 선언
			int eval_id=0;				//평가번호
			int eval_app_id=0	;	//신청번호
			String eval_rev_id="";	//평가위원ID
			String eval_field="";			//평가분야
			Date eval_assigned_dt =null;	//평가배정일
			int eval_score=0;	
			String eval_comment="";			//평가 코멘트//평가 점수
			String eval_status="";		//평가 상태

			Date ann_start_dt =null;		//공고(평가)시작일
			Date ann_end_dt =null;			//공고(평가)마감일			
			String ann_title="";			//공고명
			String ann_desc="";			//공고 설명
			String ann_budget="";	//총 예산

			int app_budget_amt=0;		//신청자의 요구 예산
			String app_user_id="";		//신청자 아이디
			String app_attach="";	//신청자 첨부파일

			String ag_name="";		//기관명

			try {
				conn = DBUtil.getConnection();
				sql = "SELECT  e.EVALUATION_ID,\r\n"
						+ "		e.EVALUATION_APPLICATION_ID,\r\n"
						+ "		e.EVALUATION_REVIEWER_ID,\r\n"
						+ "		e.EVALUATION_FIELD,\r\n"
						+ "		e.EVALUATION_ASSIGNED_at,\r\n"
						+ "		e.EVALUATION_SCORE,\r\n"
						+ "		e.EVALUATION_STATUS_CD,\r\n"
						+ "		e.EVALUATION_IS,\r\n"
						+ "		a.APPLICATION_ID,\r\n"
						+ "		a.APPLICATION_BUDGET_AMT,\r\n"
						+ "		a.APPLICATION_USER_ID,\r\n"
						+ "        a.APPLICATION_ATTACH_PATH,\r\n"
						+ "        ag.AGENCY_AGY_NAME,\r\n"
						+ "		an.ANNOUNCEMENT_START_DT,\r\n"
						+ "        an.ANNOUNCEMENT_END_DT,\r\n"
						+ "		an.ANNOUNCEMENT_TITLE,\r\n"
						+ "		an.ANNOUNCEMENT_DESC,\r\n"
						+ "        an.ANNOUNCEMENT_TOTAL_BUDGET\r\n"
						+ "FROM EVALUATIONS e\r\n"
						+ "JOIN APPLICATIONS a \r\n"
						+ "ON e.EVALUATION_APPLICATION_ID = a.APPLICATION_ID\r\n"
						+ "JOIN ANNOUNCEMENT an\r\n"
						+ "ON a.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ann_ID\r\n"
						+ "JOIN AGENCY ag\r\n"
						+ "ON an.ANNOUNCEMENT_AGY_ID = ag.AGENCY_AGY_ID\r\n"
						+ "WHERE e.EVALUATION_REVIEWER_ID = ?";
				pstmt = conn.prepareStatement(sql);

				pstmt.setString(1, cust_id);
				//pstmt.setString(2, field);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					//내용들 대충
					do {
						eval_id = rs.getInt("EVALUATION_ID");						//평가번호
						eval_app_id = rs.getInt("EVALUATION_APPLICATION_ID");		//신청번호
						eval_rev_id = rs.getString("EVALUATION_REVIEWER_ID");	//평가위원ID
						eval_field = rs.getString("EVALUATION_FIELD");			//평가분야
						eval_assigned_dt = rs.getDate("EVALUATION_ASSIGNED_AT");	//평가배정일
						eval_score = rs.getInt("EVALUATION_SCORE");	
						eval_comment = rs.getString("EVALUATION_IS");			//평가 코멘트//평가 점수
						eval_status = rs.getString("EVALUATION_STATUS_CD");		//평가 상태

						ann_start_dt = rs.getDate("ANNOUNCEMENT_START_DT");		//공고(평가)시작일
						ann_end_dt = rs.getDate("ANNOUNCEMENT_END_DT");			//공고(평가)마감일			
						ann_title = rs.getString("ANNOUNCEMENT_TITLE");			//공고명
						ann_desc = rs.getString("ANNOUNCEMENT_DESC");			//공고 설명
						ann_budget = rs.getString("ANNOUNCEMENT_TOTAL_BUDGET");	//총 예산

						app_budget_amt = rs.getInt("APPLICATION_BUDGET_AMT");		//신청자의 요구 예산
						app_user_id = rs.getString("APPLICATION_USER_ID");		//신청자 아이디
						app_attach = rs.getString("APPLICATION_ATTACH_PATH");	//신청자 첨부파일

						ag_name = rs.getString("AGENCY_AGY_NAME");				//기관명

						if(eval_comment == null) {
							eval_comment = "";
						}
						System.out.println("");
						System.out.print("평가번호\t");
						System.out.print("과제명\t\t\t");
						System.out.print("신청자\t");
						System.out.print("기관명\t\t");
						System.out.print("분야\t");
						System.out.print("평가 상태\t\t");
						System.out.print("평가 마감일\t");
						System.out.print("연구 수행 기간\n");
						System.out.println("=".repeat(100));
						System.out.print(eval_id+"\t");
						System.out.print(ann_title+"\t");
						System.out.print(app_user_id+"\t");
						System.out.print(ag_name+"\t\t");
						System.out.print(eval_field+"\t");
						System.out.print(eval_status+"\t");
						System.out.print(ann_end_dt+"\t");
						System.out.print(ann_start_dt+"-"+ann_end_dt+"\n");
					}
					while(rs.next());
					System.out.println();
					System.out.println();
					System.out.println("[1]상세보기");
					System.out.println("[2]나가기");
					int eval_no = Integer.parseInt(br.readLine());
					if(eval_no == 1) {
						System.out.println();
						System.out.println("상세 보기할 대상의 번호를 입력 하세요.");
						String do_eval_no = br.readLine();
						//상세 보기 메서드
						viewAppDetail(do_eval_no);
					}else if(eval_no == 2) {
						//
						System.out.println("목록에서 메뉴로");
						break;
					}
				}else {
					System.out.println("┌───────────────────────────────┐");
					System.out.println("│평가할 대상 목록이 없습니다.		│");
					System.out.println("└───────────────────────────────┘");
				}
				System.out.println();
				System.out.println();

			}
			catch(Exception e){e.printStackTrace();}
			finally {DBUtil.executeClose(rs, pstmt, conn);}
		}//end of while

	}

	//상세 조회 메서드 (평가 테이블에서 보고 싶은 값을 인자로 받아온다.)
	public void viewAppDetail(String eval_no) 
	{
		mother:while(true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			String sql = null;
			ResultSet rs = null;
			//DB 행값을 담을 변수를 선언
			int eval_id=0;				//평가번호
			int eval_app_id=0	;	//신청번호
			String eval_rev_id="";	//평가위원ID
			String eval_field="";			//평가분야
			Date eval_assigned_dt =null;	//평가배정일
			int eval_score=0;	
			String eval_comment="";			//평가 코멘트//평가 점수
			String eval_status="";		//평가 상태

			Date ann_start_dt =null;		//공고(평가)시작일
			Date ann_end_dt =null;			//공고(평가)마감일			
			String ann_title="";			//공고명
			String ann_desc="";			//공고 설명
			String ann_budget="";	//총 예산

			int app_budget_amt=0;		//신청자의 요구 예산
			String app_user_id="";		//신청자 아이디
			String app_attach="";	//신청자 첨부파일

			String ag_name="";		//기관명

			try {
				conn = DBUtil.getConnection();
				sql = "SELECT  e.EVALUATION_ID,\r\n"
						+ "		e.EVALUATION_APPLICATION_ID,\r\n"
						+ "		e.EVALUATION_REVIEWER_ID,\r\n"
						+ "		e.EVALUATION_FIELD,\r\n"
						+ "		e.EVALUATION_ASSIGNED_at,\r\n"
						+ "		e.EVALUATION_SCORE,\r\n"
						+ "		e.EVALUATION_STATUS_CD,\r\n"
						+ "		e.EVALUATION_IS,\r\n"
						+ "		a.APPLICATION_ID,\r\n"
						+ "		a.APPLICATION_BUDGET_AMT,\r\n"
						+ "		a.APPLICATION_USER_ID,\r\n"
						+ "        a.APPLICATION_ATTACH_PATH,\r\n"
						+ "        ag.AGENCY_AGY_NAME,\r\n"
						+ "		an.ANNOUNCEMENT_START_DT,\r\n"
						+ "        an.ANNOUNCEMENT_END_DT,\r\n"
						+ "		an.ANNOUNCEMENT_TITLE,\r\n"
						+ "		an.ANNOUNCEMENT_DESC,\r\n"
						+ "        an.ANNOUNCEMENT_TOTAL_BUDGET\r\n"
						+ "FROM EVALUATIONS e\r\n"
						+ "JOIN APPLICATIONS a \r\n"
						+ "ON e.EVALUATION_APPLICATION_ID = a.APPLICATION_ID\r\n"
						+ "JOIN ANNOUNCEMENT an\r\n"
						+ "ON a.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ann_ID\r\n"
						+ "JOIN AGENCY ag\r\n"
						+ "ON an.ANNOUNCEMENT_AGY_ID = ag.AGENCY_AGY_ID\r\n"
						+ "WHERE e.EVALUATION_ID = ? AND e.EVALUATION_REVIEWER_ID = ?"
						+ "AND an.ANNOUNCEMENT_HIDDEN_YN = 0";	//히든 여부를 나타내는 where절 부분
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, eval_no);
				pstmt.setString(2, cust_id);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					//내용들 대충

					eval_id = rs.getInt("EVALUATION_ID");						//평가번호
					eval_app_id = rs.getInt("EVALUATION_APPLICATION_ID");		//신청번호
					eval_rev_id = rs.getString("EVALUATION_REVIEWER_ID");	//평가위원ID
					eval_field = rs.getString("EVALUATION_FIELD");			//평가분야
					eval_assigned_dt = rs.getDate("EVALUATION_ASSIGNED_AT");	//평가배정일
					eval_score = rs.getInt("EVALUATION_SCORE");	
					eval_comment = rs.getString("EVALUATION_IS");			//평가 코멘트//평가 점수
					eval_status = rs.getString("EVALUATION_STATUS_CD");		//평가 상태

					ann_start_dt = rs.getDate("ANNOUNCEMENT_START_DT");		//공고(평가)시작일
					ann_end_dt = rs.getDate("ANNOUNCEMENT_END_DT");			//공고(평가)마감일			
					ann_title = rs.getString("ANNOUNCEMENT_TITLE");			//공고명
					ann_desc = rs.getString("ANNOUNCEMENT_DESC");			//공고 설명
					ann_budget = rs.getString("ANNOUNCEMENT_TOTAL_BUDGET");	//총 예산

					app_budget_amt = rs.getInt("APPLICATION_BUDGET_AMT");		//신청자의 요구 예산
					app_user_id = rs.getString("APPLICATION_USER_ID");		//신청자 아이디
					app_attach = rs.getString("APPLICATION_ATTACH_PATH");	//신청자 첨부파일

					ag_name = rs.getString("AGENCY_AGY_NAME");				//기관명

					if(eval_comment == null) {
						eval_comment = "";
					}
					System.out.println("");
					System.out.print("평가번호\t");
					System.out.print("과제명\t\t\t");
					System.out.print("신청자\t");
					System.out.print("기관명\t\t");
					System.out.print("분야\t");
					System.out.print("평가 상태\t\t");
					System.out.print("평가 마감일\t");
					System.out.print("연구 수행 기간\n");
					System.out.println("=".repeat(100));
					System.out.print(eval_id+"\t");
					System.out.print(ann_title+"\t");
					System.out.print(app_user_id+"\t");
					System.out.print(ag_name+"\t\t");
					System.out.print(eval_field+"\t");
					System.out.print(eval_status+"\t");
					System.out.print(ann_end_dt+"\t");
					System.out.print(ann_start_dt+"-"+ann_end_dt+"\n");
					System.out.println(eval_rev_id);
				}else {
					System.out.println("┌────────────────────────────────────┐");
					System.out.println("│본인의 평가목록에 존재하는 번호를 입력하세요! │");
					System.out.println("└────────────────────────────────────┘");
					System.out.println();
					return;
				}
				System.out.println();
				System.out.println();
				System.out.println("[1]평가하기");
				System.out.println("[2]나가기");
				int do_eval_no = Integer.parseInt(br.readLine());
				if(do_eval_no == 1) {


					//재평가 여부를 검사하는 메서드
					//true 값이면 재평가여부를 묻고
					//재평가하면 평가하기 메서드를 실행
					//아니면 브레이크
					//false 값이면 그대로 아래 임시평가 체크 메서드를 진행함
					if(reEval(eval_id)) {
						System.out.println("이미 해당 과제를 평가하였습니다. 재평가를 진행하시겠습니까?");
						System.out.println("[1]재평가하기 [2]돌아가기");
						int choose_no = Integer.parseInt(br.readLine());
						if(choose_no == 1) {
							//재평가대상을 위한 전용 평가 메서드
							//해당 메서드에서는 평가작성후 나갈 시 임시저장 여부를 묻지 않는다.
							reSubmiteval(eval_id);
							
							return;
						}else if(choose_no == 2) {
							break;
						}
					}
					
					//임시 평가 체크 메서드
					boolean checkYN = checkTempEval(eval_id);
					//checkTempEval메서드에서 임시평가를 사용하면 checkYN에 True값 리턴
					//임시평가 값이 존재하지 않거나 임시평가를 사용하지 않으면 false로 리턴함
					System.out.println(checkYN + ":값");
					if(!checkYN) {
						//평가하기
						submiteval(eval_id);
						return;
					}else {
						//임시평가의 값을 사용하여 평가테이블에 그대로 저장하는 메서드
						useSaveEval(eval_id);
						return;
					}
					//평가 메서드
				}else if(do_eval_no == 2) {
					//
					System.out.println();
					return;

				}
			}
			catch(Exception e){e.printStackTrace();}
			finally {DBUtil.executeClose(rs, pstmt, conn);}	
		}//end of while

	}
	//기본 평가하기 메서드
	//해당 평가를 작성하기 위해 필요한 평가 번호를 가져온다.
	public void submiteval(int eval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		int score = 0;
		String comment = null;

		try {
			//점수, 평가 부분
			score = checkScore("점수 :"); //점수가 0~100 으로만 입력하게 해주는 메서드
			System.out.println("코멘트 작성");
			comment = br.readLine();

			//DB연결 부분
			conn = DBUtil.getConnection();
			sql = "update evaluations set EVALUATION_SCORE = ?, EVALUATION_IS = ?, EVALUATION_STATUS_CD = ? where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, score);			//점수 저장
			pstmt.setString(2, comment);	//평가코멘트 저장
			pstmt.setInt(4, eval_id);		//업데이트에 사용할 평가 번호 삽입
			System.out.println();
			System.out.println();

			mother:while(true) {
				System.out.println("[1]평가제출");
				System.out.println("[2]나가기");
				int save_eval_no = Integer.parseInt(br.readLine());
				if(save_eval_no == 1) {
					String submit = "SUBMITTED";
					pstmt.setString(3, submit);
					int cnt = pstmt.executeUpdate();
					System.out.println(cnt + "건의 평가를 작성 완료 하였습니다");
					break;
				}else if(save_eval_no == 2) {
					//나가기를 했을 때 작성한 평가에 대해 임시저장 여부를 묻는다.
					while(true) {
						System.out.println("작성한 평가를 임시저장 하시겠습니까?");
						System.out.print("[1] 저장한다.  [2] 나가기 >>");
						int saveTemp = Integer.parseInt(br.readLine());
						if(saveTemp == 1) {
							//임시저장하는 메서드, 평가번호,점수,코멘트를 인자로 받아들인다.
							saveTempEval(eval_id,score,comment);
							break mother;
						}
						if(saveTemp == 2) {
							//그냥 나감(평가상세로 나가기)
							System.out.println("이전으로 돌아갑니다.");
							return;
						}
						else {
							System.out.println("1 혹은 2를 입력 해주세요.");
						}
					}

				}else {
					System.out.println("1 혹은 2를 입력 해주세요");
				}
			}

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}	
	}
	
	//해당 메서드 역시 동일하게 평가 제출 메서드지만
	//재평가 대상을 위한 전용 메서드로 만약 점수와 코멘트를 입력하고
	//제출하지 않고 나갈시 임시저장 여부를 묻지 않고 바로 종료 한다.
	public void reSubmiteval(int eval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		int score = 0;
		String comment = null;

		try {
			//점수, 평가 부분
			score = checkScore("점수 :"); //점수가 0~100 으로만 입력하게 해주는 메서드
			System.out.println("코멘트 작성");
			comment = br.readLine();

			//DB연결 부분
			conn = DBUtil.getConnection();
			sql = "update evaluations set EVALUATION_SCORE = ?, EVALUATION_IS = ?, EVALUATION_STATUS_CD = ? where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, score);			//점수 저장
			pstmt.setString(2, comment);	//평가코멘트 저장
			pstmt.setInt(4, eval_id);		//업데이트에 사용할 평가 번호 삽입
			System.out.println();
			System.out.println();

			while(true) {
				System.out.println("[1]평가제출");
				System.out.println("[2]나가기");
				int save_eval_no = Integer.parseInt(br.readLine());
				if(save_eval_no == 1) {
					String submit = "SUBMITTED";
					pstmt.setString(3, submit);
					int cnt = pstmt.executeUpdate();
					System.out.println(cnt + "건의 평가를 작성 완료 하였습니다");
					break;
				}else if(save_eval_no == 2) {
					//그냥 나감(평가상세로 나가기)
					System.out.println("이전으로 돌아갑니다.");
					return;
				}else {
					System.out.println("1 혹은 2를 입력 해주세요");
				}
			}

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}	
	}
	

	//임시평가 내용을 그대로 사용하는 메서드
	public void useSaveEval(int eval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		int temp_eval_no = 0;
		int temp_eval_eval_no = 0;
		int temp_eval_score = 0;
		String temp_eval_comment = null;

		try {

			//DB연결 부분
			conn = DBUtil.getConnection();
			//임시저장 테이블에서
			sql = "select * from TEMP_EVAL where TEMP_EVAL_EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eval_id);		
			rs = pstmt.executeQuery();
			
			//임시평가 값을 가져옴
			if(rs.next()) {
				temp_eval_no = rs.getInt("TEMP_EVAL_ID");
				temp_eval_eval_no = rs.getInt("TEMP_EVAL_EVALUATION_ID");
				temp_eval_score = rs.getInt("TEMP_EVAL_SCORE");
				temp_eval_comment = rs.getString("TEMP_EVAL_IS");
			}else {
				System.out.println("sql구문 오류일듯?");
				return;
			}
			pstmt.close();
			sql = "update EVALUATIONS set EVALUATION_SCORE=?,EVALUATION_IS=?,EVALUATION_STATUS_CD=?"
					+ " where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, temp_eval_score);
			pstmt.setString(2, temp_eval_comment);
			pstmt.setString(3,"SUBMITTED");
			pstmt.setInt(4, eval_id);
			
			int cnt = pstmt.executeUpdate();
			System.out.println(cnt+"개 임시저장한 값을 사용하여 평가에 반영하였습니다.");

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}	
	}

	//평가하기에서 점수를 올바르게 작성했는지 검사하는 메서드
	public int checkScore(String scoreInfo) {
		int score;
		while(true) {
			System.out.print(scoreInfo);	
			try {
				score = Integer.parseInt(br.readLine());
				if(score > 100 || score < 0) {// 점수가 100 초과 혹은 0 미만인 경우
					System.out.println("점수를 0~100점 사이로 입력해주세요!");
					continue;
				}else {
					break;
				}
			}
			catch(IOException e) {e.printStackTrace();}
			catch(NumberFormatException e) {System.out.println("숫자를 입력해주세요.");}
		}
		return score;
	}


	//재평가 체크 메서드
	public boolean reEval(int reEval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		Boolean resultbn = false;


		try {
			conn = DBUtil.getConnection();
			sql = "select * from EVALUATIONS WHERE EVALUATION_ID = ? and EVALUATION_STATUS_CD = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, reEval_id);
			pstmt.setString(2, "SUBMITTED"); //제출이 완료 되었는지
			rs = pstmt.executeQuery();

			if(rs.next()) {
				resultbn = true; //값이 존재하면 (이미 평가를 하였으니까 true 대입)
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return resultbn;
	}



	//임시평가 체크 메서드
	//아직 임시평가값을 사용하는 부분 분기를 완성해야함
	public boolean checkTempEval(int eval_id) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		boolean checkFlag = false;

		try {
			conn = DBUtil.getConnection();
			//sql은 해당 평가번호를 임시평가 테이블에 검색한다
			sql = "select * from TEMP_EVAL WHERE TEMP_EVAL_EVALUATION_ID"
					+ "= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eval_id);
			rs = pstmt.executeQuery();

			if(rs.next()) {
				checkFlag = true;

			}else {
				System.out.println("임시평가한 데이터가 없습니다.");
			}
			
			while(checkFlag) {//true면 임시평가한 값이 있음 false면 없음
				//검색 결과가 있다는것 = 임시저장을 한 경우
				System.out.println("해당 과제에 대해 임시평가 기록이 존재합니다 해당 평가를 그대로 평가에 사용하시겠습니까?");
				System.out.println("[1] 사용한다.  [2]사용하지 않는다.(임시평가 내용이 지워집니다!)");
				System.out.print(">");
				int temp_choose = Integer.parseInt(br.readLine());
				if(temp_choose == 1) {
					break;
				}
				else if(temp_choose == 2) {
					//임시평가에 해당 행을 삭제하고 평가하기 메서드를 실행하는 메서드 작성
					rs.close();
					pstmt.close();
					
					sql = "delete from TEMP_EVAL where TEMP_EVAL_EVALUATION_ID = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setInt(1, eval_id);
					int delcnt = pstmt.executeUpdate();
					System.out.println(delcnt + "건의 임시평가를 제거 하였습니다.");
					System.out.println();
					checkFlag = false;
				}
				else {
					System.out.println("1 또는 2를 입력하여 주세요.");
					System.out.println();
				}

			}

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}	
		return checkFlag;
	}

	//임시저장 메서드
	//평가 제출중 제출하지 않고 나가기를 눌렀을 때
	//값을 임시저장하려는 경우 작동하는 메서드
	public void saveTempEval(int eval_id, int score, String comment) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			
			sql = "insert into temp_eval(TEMP_EVAL_ID,TEMP_EVAL_EVALUATION_ID,"
					+ "TEMP_EVAL_SCORE,TEMP_EVAL_IS) "
					+ "values(temp_eval_seq.nextval,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eval_id);
			pstmt.setInt(2, score);
			pstmt.setString(3, comment);
			
			int cnt = pstmt.executeUpdate();
			System.out.println(cnt+"개의 평가데이터를 임시저장 하였습니다.");
			pstmt.close();
			sql = "update evaluations set EVALUATION_STATUS_CD = ? where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "SAVED");
			pstmt.setInt(2, eval_id);
			cnt = pstmt.executeUpdate();
			System.out.println(cnt+"개의 평가데이터의 상태를 SAVED로 변경하였습니다.");

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}	
	}



	//평가목록 화면 메서드
	public void callReviewerMenu(String myCust_id, String myRole, String myField) {
		cust_id = myCust_id; //UserMain에서 가져온 사용자 ID를 MemberDAO에 있는 cust_id로 삽입
		role = myRole;	//UserMain에서 가져온 사용자의 권한을 role에 삽입
		field = myField;
		System.out.println("전달된 cust_id = [" + cust_id + "]");
		while(true) {
			System.out.println("┌────────────────────────────────────────────────────────┐");
			System.out.println("│							 │");
			System.out.println("│	국가 연구과제 관리 프로그램	「KRD Hubs」		 │");
			System.out.println("│							 │");
			System.out.println("│	1. 평가배정목록조회					 │");
			System.out.println("│	2. 평가기록조회					 │");
			System.out.println("│	3. 내정보						 │");
			System.out.println("│	4. 로그아웃					 │");
			System.out.println("│	5. 종료						 │");
			System.out.println("│							 │");
			System.out.println("│등급 : 평가위원					ver.1.0	 │");
			System.out.println("└────────────────────────────────────────────────────────┘");
			System.out.println("［원하시는 메뉴를 선택하세요 ]");
			System.out.print(">>");
			try {
				int rev_choose = Integer.parseInt(br.readLine());
				if(rev_choose == 1) {
					//평가배정목록조회
					readEval();
				}else if(rev_choose == 2) {
					//평가기록조회
				}else if(rev_choose == 3) {
					//내정보
				}else if(rev_choose == 4) {
					//로그아웃
					return;
				}else if(rev_choose == 5) {
					System.out.println("프로그램 종료");
					System.exit(0);
				}
			}
			catch(Exception e) {e.printStackTrace();}
			//finally {if(br != null)try{br.close();}catch(IOException e) {}}
		}
	}

	//내 정보 보기 메서드

	//로그아웃 메서드
	public boolean logout() {
		return false;
	}
}
