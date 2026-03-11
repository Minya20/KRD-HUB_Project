package kr.krd.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DateTimeException;
import java.time.LocalDate;
import kr.util.DBUtil;

public class MemberDAO {
	// ------------------------------------------------
	// UI Style Constants (UserMain과 동일하게 유지)
	// ------------------------------------------------
	public static final String RESET = "\u001B[0m";
	public static final String BOLD = "\u001B[1m";
	public static final String CLR_PRIMARY = "\u001B[36m"; // Cyan
	public static final String CLR_WHT = "\u001B[37m";    // White
	public static final String CL_GRY = "\u001B[90m";     // Gray
	public static final String CLR_ERR = "\u001B[31m";    // Red
	public static final String CLR_SUC = "\u001B[32m";    // Green
	private static final String INDENT = "      ";

	private BufferedReader br; /*= new BufferedReader(new InputStreamReader(System.in));*/
	private String cust_id;
	private String role;
	private String field;
	private LocalDate today = LocalDate.now();

	// ------------------------------------------------
	// UI Utility Methods
	// ------------------------------------------------
	public MemberDAO(BufferedReader br) {
		this.br = br;
	}
	private void printSubTitle(String title) {
		System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "◈ " + title + RESET);
		System.out.println(INDENT + CL_GRY + "────────────────────────────────────────" + RESET);
	}

	private void printInputTag(String tag) {
		System.out.print(INDENT + CLR_WHT + "  ▶ " + tag + " : " + RESET);
	}

	private void printError(String msg) {
		System.out.println("\n" + INDENT + CLR_ERR + BOLD + "✘ Error: " + RESET + msg);
	}

	private void printSuccess(String msg) {
		System.out.println("\n" + INDENT + CLR_SUC + BOLD + "✔ Success: " + RESET + msg);
	}


	//로그인 메서드
	public String userLogin(String user_id, String user_pw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String real_id = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT USER_ID, USER_PWD, USER_ACCT_STATUS_CD "
					+ "FROM USERINFO WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();

			if(!rs.next()) {
				//System.out.println("존재하지 않는 아이디입니다.");
				return "0";
			}

			String dbPw = rs.getString("USER_PWD");
			String status = rs.getString("USER_ACCT_STATUS_CD");

			if(!user_pw.equals(dbPw)) {
				System.out.println("비밀번호가 일치하지 않습니다.");
				return "0";
			}

			if("DELETED".equalsIgnoreCase(status)) {
				System.out.println("삭제된 계정은 로그인할 수 없습니다.");
				return "0";
			}

			if(!"ACTIVE".equalsIgnoreCase(status)) {
				System.out.println("현재 로그인할 수 없는 계정 상태입니다. 상태: " + status);
				return "0";
			}

			real_id = rs.getString("USER_ID");

		} catch(Exception e) {
			e.printStackTrace();
			return "0";
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		return real_id;
	}
	/*	public String userLogin(String user_id, String user_pw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_id = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM USERINFO WHERE USER_ID = ? AND USER_PWD = ? AND USER_ACCT_STATUS_CD = 'ACTIVE'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.setString(2, user_pw);
			rs = pstmt.executeQuery();

			if(rs.next()) {
				real_id = rs.getString("user_id");
			} else {
				System.out.println("아이디 혹은 비밀번호가 일치하지 않거나, 사용할 수 없는 계정입니다.");
				real_id = "0";
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_id;
	}
	 */


	//아이디 찾기 (UI 개선)
	public void findUserId() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			printSubTitle("아이디 찾기 (Find ID)");

			printInputTag("이름(Name)");
			String userName = br.readLine();

			printInputTag("이메일(E-mail)");
			String userEmail = br.readLine();

			conn = DBUtil.getConnection();
			sql = "SELECT user_id FROM userInfo WHERE user_name = ? AND user_email = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userName);
			pstmt.setString(2, userEmail);

			rs = pstmt.executeQuery();

			if(rs.next()) {
				String foundId = rs.getString("user_id");
				// 찾은 아이디를 CLR_PRIMARY(Cyan) 색상으로 강조하여 출력
				printSuccess("회원님의 아이디는 [" + CLR_PRIMARY + BOLD + foundId + RESET + CLR_WHT + "] 입니다." + RESET);
			} else {
				printError("일치하는 회원 정보가 없습니다.");
			}
		} catch(Exception e) {
			printError("아이디 찾기 처리 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	//비밀번호 재설정
	//비밀번호 재설정
	//비밀번호 재설정 (UI 개선)
	public void resetPassword() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		//비밀번호 검사 정규식 
		String passwordRegex = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

		try {
			printSubTitle("비밀번호 재설정 (Reset Password)");

			printInputTag("아이디(ID)");
			String userId = br.readLine();

			printInputTag("이름(Name)");
			String userName = br.readLine();

			printInputTag("이메일(E-mail)");
			String userEmail = br.readLine();

			conn = DBUtil.getConnection();
			sql = "SELECT user_id FROM userInfo WHERE user_id = ? AND user_name = ? AND user_email = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, userName);
			pstmt.setString(3, userEmail);

			rs = pstmt.executeQuery();

			if(!rs.next()) {
				printError("일치하는 회원 정보가 없습니다.");
				return;
			}

			rs.close();
			pstmt.close();

			String newPw;
			while(true) {
				System.out.println("\n" + INDENT + CL_GRY + "  ※ 새 비밀번호 규칙 : 특수문자 1개 포함, 8자 이상" + RESET);
				printInputTag("새 비밀번호");
				newPw = br.readLine();

				if(!newPw.matches(passwordRegex)) {
					printError("비밀번호 규칙에 맞지 않습니다.");
					continue;
				}

				printInputTag("새 비밀번호 확인");
				String confirmPw = br.readLine();

				if(!newPw.equals(confirmPw)) {
					printError("비밀번호 확인이 일치하지 않습니다. 다시 입력해주세요.");
					continue;
				}
				break;
			}

			sql = "UPDATE userInfo SET user_pwd = ? WHERE user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newPw);
			pstmt.setString(2, userId);

			int count = pstmt.executeUpdate();

			if(count == 1) {
				printSuccess("비밀번호가 성공적으로 변경되었습니다.");
			} else {
				printError("비밀번호 변경에 실패했습니다.");
			}
		} catch(Exception e) {
			printError("비밀번호 재설정 처리 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	} 

	//회원가입 메서드(권한 선택 버전)
	// 1. 회원가입 (비주얼 폼 개선)
	public void insertMember() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		String userCountry = null;
		String userAffiliation = null;
		String userField = null;

		printSubTitle("신규 회원 등록 (Sign Up)");

		try {
			printInputTag("아이디(ID)");
			String user_id = br.readLine();

			String user_pw;
			String passwordRegex = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
			while (true) {
				System.out.println(INDENT + CL_GRY + "  (특수문자 포함 8자 이상)" + RESET);
				printInputTag("비밀번호(PW)");
				user_pw = br.readLine();
				if (user_pw.matches(passwordRegex)) break;
				printError("비밀번호 규칙에 맞지 않습니다.");
			}

			printInputTag("이름(Name)");
			String user_name = br.readLine();
			printInputTag("이메일(E-mail)");
			String user_email = br.readLine();

			// 생년월일 & 전화번호
			int user_birthyear;
			int user_birthmonth;
			int user_birthday;
			String myBirth;
			printInputTag("생년월일을 입력하세요.");
			while (true) {
				try {

					System.out.println();

					printInputTag("연도(year)");
					user_birthyear = Integer.parseInt(br.readLine());

					printInputTag("월(month)");
					user_birthmonth = Integer.parseInt(br.readLine());

					printInputTag("일(day)");
					user_birthday = Integer.parseInt(br.readLine());

					// 날짜 검증
					LocalDate.of(user_birthyear, user_birthmonth, user_birthday);

					myBirth = String.format("%04d-%02d-%02d",
							user_birthyear, user_birthmonth, user_birthday);

					break;  // 정상 입력이면 탈출

				} catch(NumberFormatException e) {
					printError("숫자를 입력하세요.");

				} catch(DateTimeException e) {
					printError("잘못된 입력입니다.");
				}
			}

			String user_phoneNo;
			while (true) {
				printInputTag("연락처(숫자만 입력)");
				user_phoneNo = br.readLine();
				if (user_phoneNo.length() == 11) break;
				printError("11자리 숫자를 입력하세요.");
			}

			// 국가/성별/소속/분야 선택 로직은 가독성을 위해 별도 UI 적용
			System.out.println("\n" + INDENT + CLR_WHT + "[ 소속 및 연구 분야 설정 ]" + RESET);

			// 1. 국가 선택 (7개)
			printSubTitle("국가 선택 (Country Selection)");
			System.out.println(INDENT + CL_GRY + "  [1] 한국  [2] 미국  [3] 일본  [4] 중국");
			System.out.println(INDENT + "  [5] 대만  [6] 캐나다  [7] 영국" + RESET);

			while(true) {
				printInputTag("국가 번호 선택");
				try {
					int no = Integer.parseInt(br.readLine());
					if(no >= 1 && no <= 7) {
						userCountry = getCountry(no);
						break;
					}
					printError("1~7 사이의 숫자를 입력하세요.");
				} catch(NumberFormatException e) { printError("숫자만 입력 가능합니다."); }
			}


			printSubTitle("거주지 주소 (Address)");
			System.out.println(INDENT + CL_GRY + "  (상세 주소까지 정확히 입력해주세요)" + RESET);
			printInputTag("주소 입력");
			String user_addr = br.readLine();
			if(user_addr.trim().isEmpty()) user_addr = "미지정";

			printSubTitle("성별 선택 (Gender)");
			System.out.println(INDENT + CLR_PRIMARY + "  [1] " + RESET + "Male (남성)   " + 
					CLR_PRIMARY + "[2] " + RESET + "Female (여성)");

			int your_Gender;
			while(true) {
				printInputTag("번호 선택");
				try {
					your_Gender = Integer.parseInt(br.readLine());
					if(your_Gender == 1 || your_Gender == 2) break;
					printError("1 또는 2를 입력해주세요.");
				} catch(NumberFormatException e) {
					printError("숫자만 입력 가능합니다.");
				}
			}

			// 2. 소속 선택 (42개 - 멀티 컬럼 출력으로 깔끔하게)
			printSubTitle("기관 소속 선택 (Affiliation)");
			displayAffiliations(); // 아래 정의된 헬퍼 메서드

			while(true) {
				printInputTag("소속 기관 번호");
				try {
					int no = Integer.parseInt(br.readLine());
					if(no >= 1 && no <= 42) {
						userAffiliation = getAffiliation(no);
						break;
					}
					printError("1~42 사이의 숫자를 입력하세요.");
				} catch(NumberFormatException e) { printError("숫자만 입력 가능합니다."); }
			}

			// 3. 연구 분야 선택 (23개)
			printSubTitle("전문 연구 분야 선택 (Field)");
			displayFields(); // 아래 정의된 헬퍼 메서드

			while(true) {
				printInputTag("연구 분야 번호");
				try {
					int no = Integer.parseInt(br.readLine());
					if(no >= 1 && no <= 23) {
						userField = getField(no);
						break;
					}
					printError("1~23 사이의 숫자를 입력하세요.");
				} catch(NumberFormatException e) { printError("숫자만 입력 가능합니다."); }
			}

			System.out.println("\n" + INDENT + CLR_PRIMARY + BOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
			System.out.println(INDENT + "   입력하신 정보가 맞습니까?");
			System.out.println(INDENT + CL_GRY + "   - ID: " + RESET + user_id);
			System.out.println(INDENT + CL_GRY + "   - Name: " + RESET + user_name);
			System.out.println(INDENT + CL_GRY + "   - Gender: " + RESET + (your_Gender == 1 ? "남성" : "여성"));
			System.out.println(INDENT + CL_GRY + "   - Address: " + RESET + user_addr);
			System.out.println(INDENT + CLR_PRIMARY + BOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);

			printInputTag("가입 승인 (Y/N)");
			if(!br.readLine().equalsIgnoreCase("Y")) {
				printError("회원가입이 취소되었습니다.");
				return;
			}

			// JDBC 처리
			conn = DBUtil.getConnection();
			sql = "INSERT INTO USERINFO (user_id, user_pwd, user_name, user_email, "
					+ "user_birth_dt, user_phone_no, user_country_cd, user_addr, "
					+ "user_gender_cd, user_role_cd, user_acct_status_cd, user_affiliation, user_field) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'GST', 'ACTIVE', ?, ?)";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.setString(2, user_pw);
			pstmt.setString(3, user_name);
			pstmt.setString(4, user_email);
			pstmt.setString(5, myBirth);
			pstmt.setString(6, user_phoneNo);
			pstmt.setString(7, userCountry);
			pstmt.setString(8, user_addr);
			pstmt.setInt(9, your_Gender);
			pstmt.setString(10, userAffiliation);
			pstmt.setString(11, userField);

			int count = pstmt.executeUpdate();
			if (count == 1) printSuccess("회원가입이 완료되었습니다. 환영합니다!");



		} catch (Exception e) {
			printError("데이터베이스 처리 중 오류가 발생했습니다.");
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}
	/*public void insertMember() {

		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		//비밀번호 검사 정규식 : 특수문자1개 포함하여 8자 이상
		String passwordRegex =
				"^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

		try {
			//회원가입 내용 표시
			System.out.println("회원가입을 진행합니다.");
			System.out.println("=".repeat(50));
			System.out.print("ID:_________________");
			String user_id = br.readLine();
			String user_pw;
			while(true) {
				System.out.println("※비밀번호 규칙 : 특수문자1개포함, 8자 이상");
				System.out.print("Password:_________________");
				user_pw = br.readLine();	
				if(user_pw.matches(passwordRegex)) {
					break;
				}else {
					System.out.println("비밀번호 규칙에 위배되었습니다.");
				}
			}
			System.out.print("Name:_________________");
			String user_name = br.readLine();
			System.out.print("e-mail:_________________");
			String user_email = br.readLine();
			String user_birthDate = null;
			while(true) {
				System.out.println("BirthDate:_________________");
				System.out.println("ex)1998-03-13");
				System.out.print(">>");
				user_birthDate = br.readLine();
				if(user_birthDate.length() != 10) {
					System.out.println("생년월일을 예시에 맞춰서 입력해주세요");
					continue;
				}
				break;
			}
			String user_phoneNo=null;
			while(true) {
				System.out.println("PhoneNumber:_________________");
				System.out.println("ex)01012345678");
				System.out.print(">>");
				user_phoneNo = br.readLine();
				if(user_phoneNo.length() != 11) {
					System.out.println("전화번호를 예시에 맞게 입력해주세요");
					continue;
				}
				break;
			}
			int your_Country;
			while(true) {
				System.out.println("		Country");
				System.out.println("[1] 한국 [2] 미국 [3] 일본 [4] 중국");
				System.out.println("[5] 대만 [6] 캐나다 [7] 영국");
				System.out.print("Choose one >>");

				try {
					your_Country = Integer.parseInt(br.readLine());
					if(your_Country > 7 || your_Country < 1) {
						System.out.println("1~7 사이의 숫자를 입력해주세요!");
						System.out.println();
						continue;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력하세요");
				}

			}
			System.out.print("Address:_________________");
			String user_addr = br.readLine();
			int your_Gender;
			while(true) {
				System.out.println("		Gender");
				System.out.println("	[1] male [2] female");
				System.out.println(">>");

				try {
					your_Gender = Integer.parseInt(br.readLine());
					if(your_Gender > 2 || your_Gender < 1) {
						System.out.println("1 or 2를 입력해주세요");
						System.out.println();
						continue;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력해주세요");
				}
			}
			int your_affiliation;
			while(true) {
				System.out.println("		Choose your Affiliation");
				System.out.println("[1] DGIST [2] GIST [3] KAIST [4] POSTECH [5] UNIST");
				System.out.println("[6] 고려대학교 [7] 과학기술정보통신부 [8] 교육부 [9] 국토교통부");
				System.out.println("[10] 농림축산식품부 [11] 문화체육관광부 [12] 보건복지부 [13] 산업통상자원부");
				System.out.println("[14] 서울대학교 [15] 성균관대학교 [16] 연세대학교 [17] 중소벤처기업부");
				System.out.println("[18] 한국전자통신연구원 [19] 한양대학교 [20] 해양수산부 [21] 환경부 [22] 없음");
				System.out.print(">>");
				try {
					your_affiliation = Integer.parseInt(br.readLine());
					if(your_affiliation > 22 || your_affiliation < 1) {
						System.out.println("1 ~ 21 사이 숫자를 입력 해주세요");
						System.out.println();
						continue;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력 해주세요");
				}


			}


			int your_field;
			while(true) {
				System.out.println("		Choose your Field");
				System.out.println("[1]의료 [2] 바이오 [3] 로보틱스 [4] 데이터 [5] 기획");
				System.out.println("[6] 국방 [7] 교통 [8] 감사 [9] AI [10] 환경 [11] 협약");
				System.out.println("[12] 평가 [13] 제도 [14] 정책 [15] 정산 [16] 전기");
				System.out.println("[17] 예산 [18] 역사 [19] 에너지 [20] 소재 [21] 반도체");
				System.out.println("[22] 운영 [23] 성과");
				System.out.print(">>");

				try {
					your_field = Integer.parseInt(br.readLine());
					if(your_field > 23 || your_field < 1) {
						System.out.println("1~ 23 사이의 숫자를 입력 해주세요.");
						System.out.println();
						continue;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력 해주세요");
				}
			}

			//JDBC 수행 1,2단계
			conn = DBUtil.getConnection();
			//SQL 문 작성
			sql = "INSERT INTO USERINFO (user_id, user_pwd, user_name, user_email, "
					+ "user_birth_dt, user_phone_no, user_country_cd, user_addr, " +
					"user_gender_cd, user_role_cd, "
					+ "user_acct_status_cd, user_affiliation, user_field) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?)";


			//JDBC 수행 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id); //ID
			pstmt.setString(2, user_pw); //PW
			pstmt.setString(3, user_name); //Name
			pstmt.setString(4, user_email); //Email
			pstmt.setString(5, user_birthDate); //Birth
			pstmt.setString(6, user_phoneNo); //Phone

			//국가 대입 메서드
			pstmt.setString(7, getCountry(your_Country)); //Country
			pstmt.setString(8, user_addr); //Address

			if(your_Gender == 1) {
				pstmt.setInt(9, 1); //Gender : male
			}else if(your_Gender == 2) {
				pstmt.setInt(9, 2);//Gender : female
			}

			pstmt.setString(10, "GST"); //Role : 일반 회원
			pstmt.setString(11, "ACTIVE"); //Status : ACTIVE
			pstmt.setString(12, getAffiliation(your_affiliation)); //Affiliation

			//분야 대입 메서드
			pstmt.setString(13, getField(your_field)); //Field


			System.out.println();
			System.out.println();
			//JDBC 수행 4단계
			int count = pstmt.executeUpdate();
			System.out.println("=".repeat(30));
			System.out.println(count+"개의 회원 정보를 저장했습니다.");	

		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}*/

	private void displayAffiliations() {
		String[] affs = {
				"DGIST", "GIST", "KAIST", "POSTECH", "UNIST", "고려대학교", 
				"과기정통부", "교육부", "국토교통부", "농림부", "문체부", "보건복지부", 
				"산업부", "서울대학교", "성균관대", "연세대학교", "중기부", "ETRI", 
				"한양대학교", "해수부", "환경부", "법무부", "외교부", "통일부", "국토교통부",
				"농림축산식품부", "해양수산부", "국가보훈부", "식품의약품안전처", "인사혁신처", "방위사업청", "병무청", "조달청",
				"관세청", "산림청", "기상청", "질병관리청", "재외동포청", "우주항공청", "소방청", "새만금개발청", "없음"
		};

		for (int i = 0; i < affs.length; i++) {
			System.out.printf(INDENT + CLR_PRIMARY + "[%2d] " + CLR_WHT + "%-10s", (i + 1), affs[i]);
			if ((i + 1) % 4 == 0) System.out.println(); // 4개마다 줄바꿈
		}
		System.out.println(RESET);
	}

	private void displayFields() {
		String[] fields = {
				"의료", "바이오", "로보틱스", "데이터", "기획", "국방", "교통", "감사", "AI", "환경", 
				"협약", "평가", "제도", "정책", "정산", "전기", "예산", "역사", "에너지", "소재", 
				"반도체", "운영", "성과"
		};

		for (int i = 0; i < fields.length; i++) {
			System.out.printf(INDENT + CLR_PRIMARY + "[%2d] " + CLR_WHT + "%-8s", (i + 1), fields[i]);
			if ((i + 1) % 5 == 0) System.out.println(); // 5개마다 줄바꿈
		}
		System.out.println(RESET);
	}

	//국가 대입 메서드
	public String getCountry(int no) {
		String country = null;
		switch (no) {
		case 1:
			country = "KOREA";
			break;
		case 2:
			country = "USA";
			break;
		case 3:
			country = "JAPAN";
			break;
		case 4:
			country = "CHINA";
			break;

		case 5:
			country = "TAIWAN";
			break;
		case 6:
			country = "CANADA";
			break;
		case 7:
			country = "ENGLAND";
			break;
		}

		return country;
	}

	//필드 분야 대입 메서드
	public String getField(int no) {
		String field = null;

		switch(no) {
		case 1:
			field = "의료";
			break;
		case 2:
			field = "바이오";
			break;
		case 3:
			field = "로보틱스";
			break;
		case 4:
			field = "데이터";
			break;
		case 5:
			field = "기획";
			break;
		case 6:
			field = "국방";
			break;
		case 7:
			field = "교통";
			break;
		case 8:
			field = "감사";
			break;
		case 9:
			field = "AI";
			break;
		case 10:
			field = "환경";
			break;
		case 11:
			field = "협약";
			break;
		case 12:
			field = "평가";
			break;
		case 13:
			field = "제도";
			break;
		case 14:
			field = "정책";
			break;
		case 15:
			field = "정산";
			break;
		case 16:
			field = "전기";
			break;
		case 17:
			field = "예산";
			break;
		case 18:
			field = "역사";
			break;
		case 19:
			field = "에너지";
			break;
		case 20:
			field = "소재";
			break;
		case 21:
			field = "반도체";
			break;
		case 22:
			field = "운영";
			break;
		case 23:
			field = "성과";
			break;

		}

		return field;
	}

	public String getAffiliation(int no) {
		String affiliation = null;

		switch(no) {
		case 1:
			affiliation = "DGIST";
			break;
		case 2:
			affiliation = "GIST";
			break;
		case 3:
			affiliation = "KAIST";
			break;
		case 4:
			affiliation = "POSTECH";
			break;
		case 5:
			affiliation = "UNIST";
			break;
		case 6:
			affiliation = "고려대학교";
			break;
		case 7:
			affiliation = "과학기술정보통신부";
			break;
		case 8:
			affiliation = "교육부";
			break;
		case 9:
			affiliation = "국토교통부";
			break;
		case 10:
			affiliation = "농림축산식품부";
			break;
		case 11:
			affiliation = "문화체육관광부";
			break;
		case 12:
			affiliation = "보건복지부";
			break;
		case 13:
			affiliation = "산업통상자원부";
			break;
		case 14:
			affiliation = "서울대학교";
			break;
		case 15:
			affiliation = "성균관대학교";
			break;
		case 16:
			affiliation = "연세대학교";
			break;
		case 17:
			affiliation = "중소벤처기업부";
			break;
		case 18:
			affiliation = "한국전자통신연구원";
			break;
		case 19:
			affiliation = "한양대학교";
			break;
		case 20:
			affiliation = "해양수산부";
			break;
		case 21:
			affiliation = "환경부";
			break;
		case 22:
			affiliation = "법무부";
			break;
		case 23:
			affiliation = "외교부";
			break;
		case 24:
			affiliation = "통일부";
			break;
		case 25:
			affiliation = "국토교통부";
			break;
		case 26:
			affiliation = "농림축산식품부";
			break;
		case 27:
			affiliation = "해양수산부";
			break;
		case 28:
			affiliation = "국가보훈부";
			break;
		case 29:
			affiliation = "식품의약품안전처";
			break;
		case 30:
			affiliation = "인사혁신처";
			break;
		case 31:
			affiliation = "방위사업청";
			break;
		case 32:
			affiliation = "병무청";
			break;
		case 33:
			affiliation = "조달청";
			break;
		case 34:
			affiliation = "관세청";
			break;
		case 35:
			affiliation = "산림청";
			break;
		case 36:
			affiliation = "기상청";
			break;
		case 37:
			affiliation = "질병관리청";
			break;
		case 38:
			affiliation = "재외동포청";
			break;
		case 39:
			affiliation = "우주항공청";
			break;
		case 40:
			affiliation = "소방청";
			break;
		case 41:
			affiliation = "새만금개발청";
			break;
		case 42:
			affiliation = "무소속";
			break;
		}

		return affiliation;
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


	public int getAgencyId(String user_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		int agyId = 0;

		try {
			conn = DBUtil.getConnection();

			sql = "SELECT a.AGENCY_AGY_ID "
					+ "FROM USERINFO u JOIN AGENCY a "
					+ "ON u.USER_AFFILIATION = a.AGENCY_AGY_NAME "
					+ "WHERE u.USER_ID = ? AND u.USER_ROLE_CD = 'AGY'";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				agyId = rs.getInt("AGENCY_AGY_ID");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}

		return agyId;
	}



	//일반 회원 권한 신청
	// 2. 권한 신청 (현황 체크 및 입력 UI 개선)
	public void applyRole(String user_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			String sqlCheck = "SELECT ROLE_APP_USER_ID FROM ROLE_APPLICATION "
					+ "WHERE ROLE_APP_USER_ID = ? AND ROLE_APP_STATUS = 'PENDING'";
			//	String sqlCheck = "SELECT role_app_user_id FROM role_application WHERE ROLE_APP_USER_ID = ?";
			pstmt = conn.prepareStatement(sqlCheck);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				printError("이미 권한 승인 심사가 진행 중인 계정입니다.");
				return;
			}
			rs.close();
			pstmt.close();

			printSubTitle("시스템 권한 승인 신청");
			System.out.println(INDENT + "  [1] 연구자(개인)  [2] 연구자(단체) ");
			System.out.println(INDENT + "  [3] 기관 담당자   [4] 평가위원");

			String roleCode = "";
			while(true) {
				printInputTag("신청할 권한 번호");
				String input = br.readLine();
				if(input.equals("1")) { roleCode = "RESI"; break; }
				if(input.equals("2")) { roleCode = "RESO"; break; }
				if(input.equals("3")) { roleCode = "AGY"; break; }
				if(input.equals("4")) { roleCode = "REV"; break; }
				printError("1~4 사이의 번호를 선택하세요.");
			}

			printInputTag("신청 사유 요약");
			String reason = br.readLine();

			String sqlInsert = "INSERT INTO role_application(ROLE_APP_ID, ROLE_APP_USER_ID, ROLE_APP_ROLE_CD, ROLE_APP_APPLY_REASON) "
					+ "VALUES(role_app_seq.nextval, ?, ?, ?)";
			pstmt = conn.prepareStatement(sqlInsert);
			pstmt.setString(1, user_id);
			pstmt.setString(2, roleCode);
			pstmt.setString(3, reason);

			int cnt = pstmt.executeUpdate();
			if (cnt == 1) printSuccess("권한 신청이 정상 접수되었습니다. (승인 대기 중)");

		} catch (Exception e) {
			printError("신청 처리 중 오류가 발생했습니다.");
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	/*public void applyRole(String user_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String role = null;

		try {
			conn = DBUtil.getConnection();
			sql = "select role_app_user_id from role_application"
					+ " where ROLE_APP_USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				System.out.println("[이미 권한 신청이 진행되고 있습니다.]");
				System.out.println();
				return;	//만약에 해당 사용자의 권한 신청이 조회가 된다면 리턴함
			}
			rs.close();
			pstmt.close();

			while(true) {
				System.out.println();
				System.out.println("=".repeat(20));
				System.out.println("권한 신청을 진행합니다.");
				System.out.println("신청할 권한");
				System.out.println("[1] 연구자(개인) [2] 연구자(단체) [3] 기관 담당자 [4] 평가위원");
				try {
					int your_role = Integer.parseInt(br.readLine());
					if(your_role > 4 || your_role < 1) {
						System.out.println("제시된 권한을 보고 올바른 숫자를 입력해주세요.");
						continue;
					}
					switch(your_role) {
					case 1:
						role = "RESI";	//개인 연구원
						break;
					case 2:
						role = "RESO";	//단체 연구원
						break;
					case 3:
						role = "AGY";	//기관 담당자
						break;
					case 4:
						role = "REV";	//평가위원
						break;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력하세요.");
				}
			}
			System.out.println("	평가 사유[엔터를 누르지 않고 입력 해주세요]");
			String reason = br.readLine();

			sql = "insert into role_application("
					+ "ROLE_APP_ID,ROLE_APP_USER_ID,"
					+ "ROLE_APP_ROLE_CD, ROLE_APP_APPLY_REASON) "
					+ "values(role_app_seq.nextval,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.setString(2, role);
			pstmt.setString(3, reason);

			int cnt = pstmt.executeUpdate();
			if(cnt == 1) {
				System.out.println("권한신청이 완료 되었습니다");
			}


		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}
	}*/
	// 내 정보 조회
	public void showMyInfo(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_id, user_name, user_email, user_phone_no, "
					+ "user_country_cd, user_addr, user_gender_cd, "
					+ "user_role_cd, user_affiliation, user_field "
					+ "FROM USERINFO WHERE user_id = ?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();

			printSubTitle("내 정보 상세 조회 (Profile)");

			if(rs.next()) {
				// 카드 상단 테두리
				System.out.println(INDENT + CLR_PRIMARY + "┌──────────────────────────────────────────────────┐" + RESET);

				// 정보 출력 (간격 정렬)
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "아이디", rs.getString("user_id"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "이름", rs.getString("user_name"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "이메일", rs.getString("user_email"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "연락처", rs.getString("user_phone_no"));
				System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────┤" + RESET);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "소속", rs.getString("user_affiliation"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "분야", rs.getString("user_field"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "권한", rs.getString("user_role_cd"));
				System.out.println(INDENT + CLR_PRIMARY + "├──────────────────────────────────────────────────┤" + RESET);
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "국가", rs.getString("user_country_cd"));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "성별", convertGender(rs.getInt("user_gender_cd")));
				System.out.printf(INDENT + CLR_PRIMARY + "│  " + CL_GRY + "%-6s : " + CLR_WHT + "%s\n" + RESET, "주소", rs.getString("user_addr"));

				// 카드 하단 테두리
				System.out.println(INDENT + CLR_PRIMARY + "└──────────────────────────────────────────────────┘" + RESET);
			} else {
				printError("회원 정보를 찾을 수 없습니다.");
			}

		} catch(Exception e) {
			printError("내 정보 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 이메일 변경
	public void updateEmail(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		try {
			printSubTitle("이메일 변경");

			printInputTag("새 이메일");
			String newEmail = br.readLine();

			if(newEmail == null || newEmail.trim().isEmpty()) {
				printError("이메일을 입력해주세요.");
				return;
			}

			if(!newEmail.contains("@")) {
				printError("이메일 형식을 확인해주세요.");
				return;
			}

			conn = DBUtil.getConnection();
			sql = "UPDATE USERINFO SET user_email = ? WHERE user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newEmail.trim());
			pstmt.setString(2, userId);

			int count = pstmt.executeUpdate();

			if(count == 1) {
				printSuccess("이메일이 변경되었습니다.");
			} else {
				printError("이메일 변경에 실패했습니다.");
			}

		} catch(Exception e) {
			printError("이메일 변경 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	// 연락처 변경
	public void updatePhone(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		try {
			printSubTitle("연락처 변경");

			printInputTag("새 연락처(숫자만 입력)");
			String newPhone = br.readLine();

			if(newPhone == null || !newPhone.matches("\\d{11}")) {
				printError("연락처는 11자리 숫자로 입력해주세요.");
				return;
			}

			conn = DBUtil.getConnection();
			sql = "UPDATE USERINFO SET user_phone_no = ? WHERE user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newPhone);
			pstmt.setString(2, userId);

			int count = pstmt.executeUpdate();

			if(count == 1) {
				printSuccess("연락처가 변경되었습니다.");
			} else {
				printError("연락처 변경에 실패했습니다.");
			}

		} catch(Exception e) {
			printError("연락처 변경 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	// 비밀번호 변경
	public void changeMyPassword(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		String passwordRegex =
				"^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

		try {
			printSubTitle("비밀번호 변경");

			printInputTag("현재 비밀번호");
			String currentPw = br.readLine();

			conn = DBUtil.getConnection();
			sql = "SELECT user_id FROM USERINFO WHERE user_id = ? AND user_pwd = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, currentPw);
			rs = pstmt.executeQuery();

			if(!rs.next()) {
				printError("현재 비밀번호가 일치하지 않습니다.");
				return;
			}

			rs.close();
			pstmt.close();

			String newPw;
			while(true) {
				System.out.println(INDENT + CL_GRY + "  ※ 특수문자 1개 포함, 8자 이상" + RESET);
				printInputTag("새 비밀번호");
				newPw = br.readLine();

				if(!newPw.matches(passwordRegex)) {
					printError("비밀번호 규칙에 맞지 않습니다.");
					continue;
				}

				printInputTag("새 비밀번호 확인");
				String confirmPw = br.readLine();

				if(!newPw.equals(confirmPw)) {
					printError("새 비밀번호 확인이 일치하지 않습니다.");
					continue;
				}

				if(newPw.equals(currentPw)) {
					printError("현재 비밀번호와 다른 비밀번호를 입력해주세요.");
					continue;
				}
				break;
			}

			sql = "UPDATE USERINFO SET user_pwd = ? WHERE user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newPw);
			pstmt.setString(2, userId);

			int count = pstmt.executeUpdate();

			if(count == 1) {
				printSuccess("비밀번호가 변경되었습니다.");
			} else {
				printError("비밀번호 변경에 실패했습니다.");
			}

		} catch(Exception e) {
			printError("비밀번호 변경 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 권한 신청 처리상태 조회
	public void showRoleApplicationStatus(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT ROLE_APP_ID, ROLE_APP_ROLE_CD, ROLE_APP_APPLIED_AT, "
					+ "ROLE_APP_APPLY_REASON, ROLE_APP_STATUS, ROLE_APPROVED_AT, "
					+ "ROLE_APPROVED_BY, ROLE_REJECT_REASON "
					+ "FROM ROLE_APPLICATION "
					+ "WHERE ROLE_APP_USER_ID = ? "
					+ "ORDER BY ROLE_APP_ID DESC";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();

			printSubTitle("권한 신청 처리상태 조회");

			if(!rs.isBeforeFirst()) {
				System.out.println(INDENT + CL_GRY + "  권한 신청 이력이 없습니다." + RESET);
				return;
			}

			// 표 헤더 생성
			System.out.println(INDENT + BOLD + CLR_WHT + String.format("%-5s | %-6s | %-10s | %-12s | %-15s", 
					"신청번호", "신청권한", "신청일자", "처리상태", "비고(승인일/반려사유)") + RESET);
			System.out.println(INDENT + CL_GRY + "----------------------------------------------------------------------" + RESET);

			while(rs.next()) {
				String status = rs.getString("ROLE_APP_STATUS");
				String statusKr = convertRoleAppStatus(status);
				String statusColor = CLR_PRIMARY; // 기본 대기 색상
				String remarks = "-";

				// 상태에 따른 색상 및 비고란 데이터 변경
				if("APPROVED".equalsIgnoreCase(status)) {
					statusColor = CLR_SUC; // 승인은 초록색
					remarks = rs.getDate("ROLE_APPROVED_AT") != null ? rs.getDate("ROLE_APPROVED_AT").toString() : "-";
				} else if("REJECTED".equalsIgnoreCase(status)) {
					statusColor = CLR_ERR; // 반려는 빨간색
					String rejectReason = rs.getString("ROLE_REJECT_REASON");
					// 반려 사유가 길면 잘라주기
					if(rejectReason != null && rejectReason.length() > 8) {
						rejectReason = rejectReason.substring(0, 6) + "..";
					}
					remarks = rejectReason != null ? rejectReason : "사유 없음";
				}

				// 그리드 라인 출력
				System.out.println(INDENT + String.format("%-8d | %-8s | %-12s | " + statusColor + "%-12s" + RESET + " | %-15s", 
						rs.getInt("ROLE_APP_ID"),
						rs.getString("ROLE_APP_ROLE_CD"),
						rs.getDate("ROLE_APP_APPLIED_AT"),
						statusKr,
						remarks));
			}
			System.out.println();

		} catch(Exception e) {
			printError("권한 신청 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	private String convertGender(int genderCd) {
		if(genderCd == 1) return "남성";
		if(genderCd == 2) return "여성";
		return "미지정";
	}

	private String convertRoleAppStatus(String status) {
		if(status == null) return "알 수 없음";

		switch(status.trim().toUpperCase()) {
		case "PENDING":
			return "승인 대기";
		case "APPROVED":
			return "승인 완료";
		case "REJECTED":
			return "반려";
		default:
			return status;
		}
	}

	//로그아웃 메서드
	public boolean logout() {
		//	return false; //-> GST메뉴에서 로그아웃 시도할 때 항상 오류 문구가 먼저 찍힘.
		return true;
	}

	// 사용자의 소속 기관명을 반환하는 메서드 추가
	public String getUserAffiliation(String user_id) {
		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null; String affiliation = null;
		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement("SELECT user_affiliation FROM USERINFO WHERE USER_ID = ?");
			pstmt.setString(1, user_id); rs = pstmt.executeQuery();
			if(rs.next()) affiliation = rs.getString("user_affiliation");
		} catch(Exception e){ e.printStackTrace(); } finally { DBUtil.executeClose(rs, pstmt, conn); }
		return affiliation;
	}	

}
