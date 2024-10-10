<div align="center"> 
  <h1>SPINLOG(스핀로그) - Backend</h1>
  <img src="https://github.com/SpinLog/.github/assets/63975200/10a367a8-d5e8-4163-9ea2-d72c1f28ad7d.png"/>
  <p> <a href = "https://spinlog.swygbro.com/">스핀로그 바로가기</a> </p>
</div>

<br>

## 프로젝트 소개 📝
- 10명 중 6명은 감정소비를 후회하고, 4명 만족합니다. 후회하는 감정소비를 줄이고 현명한 감정소비를 위해 제작한 서비스입니다.
- 감정발현으로 충동소비를 했을경우 소비내역과 감정일기를 적는 감정 가계부
- 음성타이핑, AI피드백으로 편리하고 똑똑하게 사용 가능
<br>

## 팀원 👨‍👨‍👧‍👧👩‍👦‍👦
|메인 페이지, 대시보드, 게시글, OpenAI 통신|통계, 배포, 모니터링|OAuth2 로그인, 회원 정보 관리|
| :----: | :----: | :----: |
|<img src="https://github.com/user-attachments/assets/654b84f2-35eb-400e-9e7c-0fc60b15d655" width="200" height="150"/>|<img src="https://github.com/user-attachments/assets/79ae3f10-7ef0-427f-a719-eaa23b49b354" width="200" height="150"/>|<img src="https://github.com/user-attachments/assets/c138d420-97b5-497b-b4c9-f43eec492c7a" width="200" height="150"/>|
|[정영동](https://github.com/Yeongdong)|[한상현](https://github.com/Hansanghyun-github)|[황중섭](https://github.com/seop-h)|
<br>

## 프로젝트 기술 스택
<img width="400" alt="image" src="https://github.com/SpinLog/backend/assets/63975200/a380629f-e9ac-4166-a155-5cc63ed37830">
<br>

## ERD
<img width="500" alt="image" src="https://github.com/SpinLog/backend/assets/63975200/a36cc4a6-34c4-4179-83f6-f4f14d8d3d33">
<br>

## 프로젝트 아키텍처
<img width="635" alt="image" src="https://github.com/SpinLog/backend/assets/63975200/e9e3c3a8-42ad-42c9-84c5-cdb0064fdaf3">
<br>

## Rules
협의를 통한 결정된 사항은 팀 Notion에 기록한다. 기록된 내용은 개발시 적용을 원칙으로 한다.

1. **Git Convention**
- 협의를 통해 Workflow, Branch 전략, Commit convention을 정의

2. **Coding Convention**
- 네이밍 규칙, 빌더, PR 규칙을 적용해 협업시 불편함이 없도록 개발

3. **프로젝트 아키텍처 구조**
- 3 tier architecture를 기본으로, 각 도메인별 담당자 할당
    - 영동: 메인 페이지, 대시보드, 게시글, 외부 API 연동
    - 상현: 통계, 배포, 모니터링
    - 중섭: 로그인, 회원정보 관리

4. **스터디**
- 공통 적용 기술에 대해 스터디가 필요할시, 1주마다 주제를 정해 스터디 후 내용을 팀원간 공유

5. **Core Time**
- 빠른 소통과 개발을 위해 13 - 16시 코어 타임 적용(금요일 제외)

<br>

## Git Convention
**1. Git Workflow** <br>
  : main → develop → feature/이슈번호-기능, fix/이슈번호-기능, refactor/이슈번호-기능
  - local - feature/123-로그인 에서 각자 작업
  - 작업 완료 후 remote - develop 에 PR
  - 코드 리뷰 후 Approve 받고 Merge
  - remote - develop 에 Merge 될 때 마다 모든 팀원 remote - develop pull 받아 최신 상태 유지
  - 풀 리퀘스트 후 메인 브랜치와 합쳐진 feature 하위 브랜치는 원격에서 삭제(즉, 현재 구현 진행중인 브랜치들만 원격에 존재하는 것)
  - 커밋은 한글로 작성

<br>

**2. Branch 전략**
  - master 브랜치는 프로덕션 코드
  - develop 브랜치는 개발용 코드
  - ~~ 브랜치는 로컬 개발용 코드

<br>

**3. Commit Convention**

|태그 이름|설명|
|-------|---|
|feat|새로운 기능에 대한 커밋|
|fix|버그 수정에 대한 커밋|
|hotfix|issue나 QA에서 급한 버그 수정|
|build|빌드 관련 파일 수정에 대한 커밋|
|chore|그 외 자잘한 수정에 대한 커밋|
|style|코드 스타일 혹은 포맷 등에 관한 커밋|
|docs|문서 수정에 대한 커밋|
|test|테스트 코드 수정에 대한 커밋|
|refactor|코드 리팩토링에 대한 커밋|

<br>

## 코딩 컨벤션
1.  **네이밍 규칙**
    1. 변수나 함수, 클래스명은 camelCase를 사용한다.
    2. 함수의 경우 동사+명사 사용한다.
        - ex) getInfo()
    3. DB에 저장되는 컬럼명은 snakeCase를 사용한다.
        - ex) member_id
    4. Url명은 kebabCase를 사용하며, 명사와 소문자로 구성한다.
    5. 구분자로 하이픈(-)을 사용하며, 되도록이면 구분자 없이 구성한다.
        - ex) www.example.com/user
2. **빌더**
    1. 가독성 향상을 위해 생성자 대신 빌더를 필수적으로 사용한다.
3. **PR 규칙**
    1. 충돌이 났을 경우: 로컬로 가져와서 해결하는데, 코드리뷰와 같이 실시간으로 개발자들끼리 협의 후 해결
    2. PR 올린 사람이 PR 회의 주최: 주 3일 - 화목토 PR 리뷰를 요청한다.
    3. 기능 구현시 다른 코드를 고칠 것이라는 판단이 들면 공유하고 협의 후 진행한다.
    4. feature에서는 새로운 코드를 추가를 지향, 기존 코드 수정은 지양한다.
