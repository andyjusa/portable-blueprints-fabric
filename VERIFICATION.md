# 검증 결과 — Portable Blueprints Fabric 1.21.1

검증일: 2026-11-12 (Asia/Seoul)
포트: `2.0.10-fabric.1+1.21.1`

## 통과한 검사

- `./gradlew runSmokeClient build`: BUILD SUCCESSFUL.
- 실제 Fabric GameTest 서버: 필수 테스트 4/4 통과.
  - 실제 서바이벌 플레이어의 스캔 → .bp 저장 → 건설 → 재료 소모.
  - 연결된 상자에 같은 재료가 여러 슬롯에 있어도 정확히 한 개만 차감, simulate 추출은 롤백.
  - 정상 아이템 요청 허용, 소유권 위조·타인 명의 공유 요청 거부.
  - 원본 .bp 좌표 x/y/z JSON fixture의 저장·읽기 호환 및 null 처리.
- 실제 Minecraft 클라이언트: 타이틀, 새 flat 월드, 스캐너 선택 영역, 스캐너 UI, 태블릿 UI, 블록 홀로그램을 자동 실행하고 6개 화면 캡처.
- 홀로그램은 stone / oak stairs / light blue stained glass를 사용. 불투명·투명 VBO 레이어가 준비된 뒤 캡처했고, 유리의 파란 색상·텍스처·투명도를 직접 확인.
- 배포 JAR에 테스트 모드 클래스가 없고, NeoForge 메타데이터가 없으며, Fabric 메타데이터와 Mixin refmap이 포함됨을 확인.

## 범위와 제한

macOS arm64 / Java 21에서 개발 환경 실행과 배포 JAR 생성을 확인했습니다. 운영 모드팩·Windows·Linux에서 배포 JAR를 직접 실행한 검증은 아직 하지 않았습니다. 모든 모드 조합과 모든 원본 부가 기능을 검증한 것은 아닙니다.

ProjectE·Sophisticated Backpacks의 NeoForge 전용 연동, NeoForge ModelData 기반 커스텀 모델 호환은 포함하지 않습니다. 설정은 JSON이며 기존 TOML 자동 이관은 제공하지 않습니다.
