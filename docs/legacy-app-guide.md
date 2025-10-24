# 既存アプリのコンテナー化ガイド

既存のアプリ (Spring Framework 5.3.x や Java 11) のアプリケーションをコンテナー化するための実践的なガイドです。

---

## 📋 対象

- Spring Framework 5.3.x ベースのアプリケーション
- Spring Boot 2.7.x 系のアプリケーション
- Java 11 で動作するアプリ
- Maven または Gradle でビルド可能なプロジェクト

---

## 🎯 コンテナー化のステップ

### ステップ 1: アプリケーションの現状確認

**なぜこのステップが必要？**

コンテナー化する前に、アプリケーションが正しく動作する環境を把握する必要があります。コンテナーは「どこでも同じように動く箱」を作る技術ですが、元のアプリケーションの要件を理解していないと、その箱を正しく作れません。

#### バージョンの確認

```bash
# Java バージョン確認
java -version
# Java 11 が必要

# ビルドツールのバージョン確認
mvn -version
# または
gradle -version
```

#### 設定ファイルの確認ポイント

**なぜ設定ファイルを確認？**

コンテナーは開発環境、テスト環境、本番環境で同じコンテナーイメージを使いますが、環境ごとに設定（データベース接続先など）は変える必要があります。そのため、設定が柔軟に変更できるようになっているか確認します。

`application.properties` または `application.yml` で以下を確認:

- **サーバーポート**: デフォルトは 8080（コンテナー内で使うポート番号）
- **データベース接続**: ハードコードされていないか（`localhost` 固定だと本番環境で使えない）
- **外部サービス URL**: 環境変数で設定可能か（環境ごとに変更できるか）
- **ファイルパス**: 絶対パスが使われていないか（`C:\temp` のような Windows 専用パスはコンテナーで動かない）

---

### ステップ 2: 環境変数への対応

**環境変数を使うメリット:**
- 1つのコンテナーイメージで全環境をカバー
- 設定変更は環境変数を変えるだけ（再ビルド不要）
- パスワードなどの機密情報を安全に管理できる

#### application.properties の例

**変更前（ハードコード）:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=password
server.port=8080
```

**変更後（環境変数対応）:**
```properties
# デフォルト値を持たせることで、ローカル開発環境でも動作
spring.datasource.url=${DATABASE_URL:jdbc:mysql://localhost:3306/mydb}
spring.datasource.username=${DATABASE_USERNAME:root}
spring.datasource.password=${DATABASE_PASSWORD:password}
server.port=${PORT:8080}
```

#### よく使う環境変数パターン

```properties
# データベース
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.hikari.maximum-pool-size=${DB_POOL_SIZE:10}

# Redis
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.password=${REDIS_PASSWORD:}

# 外部 API
app.external.api.url=${EXTERNAL_API_URL}
app.external.api.key=${EXTERNAL_API_KEY}
app.external.api.timeout=${API_TIMEOUT:30000}

# ログレベル
logging.level.root=${LOG_LEVEL:INFO}
logging.level.com.example=${APP_LOG_LEVEL:DEBUG}
```

---

### ステップ 3: Dockerfile の作成

**なぜ Dockerfile が必要？**

Dockerfile は「コンテナーの設計図」です。この設計図に従って、Docker がアプリケーションを含むコンテナーイメージを自動的に作成してくれます。

**Dockerfile に書くこと:**
1. **どの OS・環境を使うか**（Java 11 が入った Linux など）
2. **アプリをどうビルドするか**（Maven または Gradle でコンパイル）
3. **アプリをどう起動するか**（java -jar コマンド実行）
4. **セキュリティ設定**（管理者権限で動かさない）

**マルチステージビルドとは？**

Dockerfile を「ビルドステージ」と「実行ステージ」の 2 段階に分けています。

- **ビルドステージ**: Maven/Gradle でアプリをコンパイル（大きなツールが必要）
- **実行ステージ**: コンパイル済みの JAR ファイルだけを持ってくる（軽量な Java 実行環境のみ）

**メリット**: 最終的なコンテナーイメージが小さくなる（ビルドツールは含まれない）ため、デプロイが速く、セキュリティリスクも減ります。

プロジェクトのルートディレクトリに `Dockerfile` を作成します。

#### Maven プロジェクトの場合

```dockerfile
# ===============================================
# ビルドステージ
# ===============================================
FROM maven:3.8-openjdk-11-slim AS build
WORKDIR /app

# 依存関係を先にダウンロード（Docker キャッシュ活用）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ソースコードをコピーしてビルド
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===============================================
# 実行ステージ
# ===============================================
FROM eclipse-temurin:11-jre-jammy
WORKDIR /app

# 非 root ユーザーで実行（セキュリティ）
RUN groupadd -r appuser && useradd -r -g appuser appuser

# ビルド成果物をコピー
COPY --from=build /app/target/*.jar app.jar

# 所有権を変更
RUN chown -R appuser:appuser /app

# ユーザーを切り替え
USER appuser

# ポート公開
EXPOSE 8080

# JVM オプション（コンテナー環境向け最適化）
ENV JAVA_OPTS="\
  -Xmx512m \
  -Xms256m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -Djava.security.egd=file:/dev/./urandom"

# アプリケーション起動
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### Gradle プロジェクトの場合

```dockerfile
# ===============================================
# ビルドステージ
# ===============================================
FROM gradle:7.6-jdk11 AS build
WORKDIR /app

# 依存関係を先にダウンロード
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon

# ソースコードをコピーしてビルド
COPY src ./src
RUN gradle bootJar --no-daemon

# ===============================================
# 実行ステージ
# ===============================================
FROM eclipse-temurin:11-jre-jammy
WORKDIR /app

# 非 root ユーザーで実行
RUN groupadd -r appuser && useradd -r -g appuser appuser

# ビルド成果物をコピー
COPY --from=build /app/build/libs/*.jar app.jar

# 所有権を変更
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="\
  -Xmx512m \
  -Xms256m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

### ステップ 4: .dockerignore の作成

**なぜ .dockerignore が必要？**

Docker がイメージをビルドする際、プロジェクトフォルダ内の全ファイルをまず Docker エンジンに送信します。不要なファイル（ビルド済みファイル、IDE 設定、ログなど）も含まれると:

- **ビルドが遅くなる**（大量のファイル転送に時間がかかる）
- **イメージサイズが無駄に大きくなる**（不要なファイルが含まれる）
- **機密情報が漏れる可能性**（ローカルの .env ファイルなど）

**.dockerignore の役割:**

`.gitignore` と同じ仕組みで、「Docker に送らないファイル」を指定します。これにより、ビルドが高速化され、イメージが軽量になります。

**除外するファイルの例:**
- **ビルド成果物**: `target/`, `build/`（Dockerfile 内で新しくビルドするので不要）
- **IDE 設定**: `.idea/`, `.vscode/`（開発環境固有の設定）
- **機密情報**: `.env`, `application-local.properties`（パスワードなどが含まれる可能性）

```
# ビルド成果物
target/
build/
*.jar
*.war
*.ear

# IDE 設定
.idea/
.vscode/
.settings/
*.iml
*.ipr
*.iws
.classpath
.project

# Git
.git/
.gitignore
.gitattributes

# ドキュメント
*.md
docs/
README*

# ログとキャッシュ
*.log
logs/
.cache/

# ビルドツール
.mvn/
.gradle/
gradle-wrapper.jar

# OS 固有
.DS_Store
Thumbs.db

# 設定ファイル（機密情報を含む可能性）
*.env
.env.*
application-local.properties
application-local.yml
```

---

### ステップ 5: ローカルでテスト

**ローカルテストで確認すること:**
1. **コンテナーイメージが正しくビルドできるか**
2. **アプリケーションが起動するか**
3. **環境変数が正しく読み込まれるか**
4. **API エンドポイントが応答するか**
5. **メモリやログが想定通りか**

#### イメージのビルド

**このコマンドで何が起こる？**

`docker build` は Dockerfile の設計図を読み込んで、実際のコンテナーイメージを作成します。`-t my-legacy-app:v1` は「このイメージに名前とバージョンを付ける」という意味です。

```bash
# イメージをビルド
docker build -t my-legacy-app:v1 .

# ビルド時間の確認（パフォーマンス測定）
time docker build -t my-legacy-app:v1 .
```

#### コンテナーの起動と確認

**docker run コマンドの各オプションの意味:**
- `-d`: バックグラウンドで実行（ターミナルを占有しない）
- `-p 8080:8080`: PC のポート 8080 をコンテナーのポート 8080 に接続
- `-e DATABASE_URL="..."`: 環境変数を設定（コンテナー内で使われる）
- `--name my-app`: コンテナーに名前を付ける（管理しやすくする）

```bash
# コンテナーを起動（環境変数を指定）
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL="jdbc:h2:mem:testdb" \
  -e DATABASE_USERNAME="sa" \
  -e DATABASE_PASSWORD="" \
  -e LOG_LEVEL="INFO" \
  --name my-app \
  my-legacy-app:v1

# ログをリアルタイムで確認
docker logs -f my-app

# コンテナーの状態確認
docker ps

# ヘルスチェック（Spring Boot Actuator を使用している場合）
curl http://localhost:8080/actuator/health

# アプリケーションのエンドポイント確認
curl http://localhost:8080/api/your-endpoint

# コンテナー内のリソース使用状況
docker stats my-app
```

#### 停止と削除

```bash
# コンテナーを停止
docker stop my-app

# コンテナーを削除
docker rm my-app

# イメージを削除（必要に応じて）
docker rmi my-legacy-app:v1
```

---

## 🔧 コンテナー化でよくある問題と対処法

### 問題 1: ビルドが遅い

**なぜこの問題が起きる？**

Docker は Dockerfile の各行（コマンド）ごとに「レイヤー」という層を作ります。ファイルが変更されると、その行以降のレイヤーが全て作り直されます。

**よくある失敗例:**
```dockerfile
# ソースコードを先にコピー
COPY . .
# 依存関係をダウンロード
RUN mvn dependency:go-offline
```

この順序だと、ソースコード（頻繁に変更される）を変えるたびに、依存関係（ほとんど変わらない）も毎回ダウンロードされてしまいます。

**対処法（Docker キャッシュの活用）:**
```dockerfile
# 依存関係定義ファイルを先にコピー
COPY pom.xml .
RUN mvn dependency:go-offline -B

# その後ソースコードをコピー
COPY src ./src
```

### 問題 2: メモリ不足エラー

**なぜこの問題が起きる？**

Java アプリケーションは「ヒープメモリ」という領域にデータを保存します。デフォルトでは、Java はシステムの物理メモリに応じて自動的にヒープサイズを決めますが、コンテナー環境では設定が必要な場合があります。

**症状:**
```
java.lang.OutOfMemoryError: Java heap space
```

**いつ発生するか:**
- 大量のデータを処理する
- メモリリークがある（プログラムのバグ）
- コンテナーに割り当てられたメモリが少なすぎる

**対処法:**
```dockerfile
# JVM ヒープサイズを調整
ENV JAVA_OPTS="-Xmx1024m -Xms512m"

# またはコンテナー起動時に指定
docker run -e JAVA_OPTS="-Xmx2048m" ...
```

### 問題 3: タイムゾーンの問題

**なぜこの問題が起きる？**

Docker の Linux コンテナーは、デフォルトで **UTC（協定世界時）** に設定されています。日本時間（JST）と 9 時間ずれるため:
- ログのタイムスタンプが 9 時間ずれる
- 日時計算が正しく動かない（予約システムなど）
- レポートの日付が間違う

**症状:**
- ログのタイムスタンプが UTC になる（日本時間より 9 時間遅い）
- 日時処理がずれる（深夜 0 時のバッチが午前 9 時に実行される）

**対処法:**
```dockerfile
# Dockerfile に追加
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
```

### 問題 4: 文字化け

**なぜこの問題が起きる？**

コンテナーの OS（Linux）がデフォルトで日本語に対応していない場合があります。日本語のログやデータが正しく表示されません。

**症状:**
- ログに `???` や `文字化け` が表示される
- 日本語のファイル名が読めない
- データベースに保存した日本語が壊れる

**対処法:**
```dockerfile
# Dockerfile に追加
ENV LANG=ja_JP.UTF-8
ENV LANGUAGE=ja_JP:ja
ENV LC_ALL=ja_JP.UTF-8
```

### 問題 5: ファイルアップロードが保存されない

**なぜこの問題が起きる？**

コンテナー内に保存したファイルは、コンテナーを削除すると **完全に消えてしまいます**。

**具体例:**
1. ユーザーが画像をアップロード → コンテナー内の `/app/uploads` に保存
2. アプリを更新してコンテナーを再起動
3. `/app/uploads` のファイルが全て消える！

**これは VM との大きな違いです:**
- **VM**: ディスクに保存したファイルは永続的に残る
- **コンテナー**: コンテナーを削除すると全て消える（揮発性）

**対処法:**
- **ボリュームマウント**: ホスト PC のフォルダとコンテナーを接続
- **クラウドストレージ**: Azure Blob Storage や AWS S3 を使用（推奨）

```bash
# ボリュームを使用する場合（ローカルテスト用）
docker run -v /path/on/host:/app/uploads my-legacy-app:v1

# Azure の場合は、このワークショップのセクション 6 で学べます
```

---

## 📊 パフォーマンスチューニング

### JVM オプションの最適化

**なぜチューニングが必要？**

コンテナー環境では、メモリやCPUが制限されることが多いです（コスト削減のため）。Java のデフォルト設定は物理サーバー向けなので、コンテナーに最適化することでパフォーマンスが向上します。

**各オプションの意味:**
- `-Xmx1024m`: ヒープメモリの最大値（このアプリは最大 1GB まで使える）
- `-Xms512m`: ヒープメモリの初期値（起動時に 512MB 確保）
- `-XX:+UseContainerSupport`: コンテナーのメモリ制限を認識する
- `-XX:MaxRAMPercentage=75.0`: コンテナーのメモリの 75% まで使う
- `-XX:+UseG1GC`: G1 ガベージコレクター使用（低遅延）
- `-Djava.security.egd=file:/dev/./urandom`: 起動を高速化

```dockerfile
# Java 11 向け推奨設定
ENV JAVA_OPTS="\
  -Xmx1024m \
  -Xms512m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:ParallelGCThreads=4 \
  -XX:ConcGCThreads=2 \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -Djava.security.egd=file:/dev/./urandom \
  -Djava.awt.headless=true"
```

### イメージサイズの削減

**なぜイメージサイズが重要？**

コンテナーイメージが大きいと:
- **デプロイが遅い**（Azure へのアップロードに時間がかかる）
- **ストレージコストが高い**（Azure Container Registry の料金）
- **セキュリティリスクが増える**（不要なツールが攻撃の入り口になる）

**削減テクニック:**

```dockerfile
# 1. Alpine ベースイメージを使用（最小限の Linux）
FROM eclipse-temurin:11-jre-alpine
# 通常: 200-300MB → Alpine: 100-150MB

# 2. 不要なパッケージを削除
RUN apk del wget curl || true

# 3. マルチステージビルドで中間ファイルを除外
# （上記の Dockerfile 例で既に実装済み）
# ビルドツール（Maven/Gradle）は最終イメージに含まれない
```

---

## 🔒 セキュリティのベストプラクティス

### 1. 非 root ユーザーで実行

**なぜ root ユーザーで動かしてはいけない？**

デフォルトでは、コンテナー内のアプリは「root ユーザー（管理者権限）」で動きます。もし攻撃者がアプリの脆弱性を突いてコンテナーに侵入した場合、管理者権限を持っているため:
- コンテナー内の全ファイルを読み書きできる
- 他のコンテナーや、場合によってはホストOS にも攻撃できる

**対策: 専用ユーザーを作成**

```dockerfile
# 権限の低いユーザーを作成
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser
```

これにより、攻撃者がコンテナーに侵入しても、できることが限定されます。

### 2. 機密情報の管理

**なぜ Dockerfile にパスワードを書いてはいけない？**

Dockerfile に直接書いた情報は、コンテナーイメージに **永久に残ります**。イメージを配布すると、パスワードが漏洩します。

**悪い例:**
```dockerfile
ENV DATABASE_PASSWORD=mypassword123  # NG!
```

**良い例: 環境変数で外部から渡す**

```properties
# application.properties
spring.datasource.password=${DATABASE_PASSWORD}

# .dockerignore に追加（ローカルの機密情報を除外）
*.env
application-local.properties
```

### 3. 最新のベースイメージを使用

**なぜ古いイメージは危険？**

古いベースイメージには、既知のセキュリティ脆弱性が含まれている可能性があります。攻撃者はこれらの脆弱性を狙います。

**対策: 公式の最新イメージを使う**

```dockerfile
# セキュリティパッチが適用された公式イメージ
FROM eclipse-temurin:11-jre-jammy
# 定期的に `docker pull` で最新版に更新
```

---

## 🚀 次のステップ

コンテナー化が完了したら:

1. **Azure へのデプロイ**
   - [メインワークショップ](../README.md) のセクション 4 以降を参照
   - Azure Container Apps へのデプロイ方法を学ぶ

2. **CI/CD パイプライン構築**
   - GitHub Actions で自動ビルド・デプロイ
   - Azure DevOps で統合

3. **モニタリングとロギング**
   - Azure Application Insights の統合
   - ログ集約の設定

---

## 📚 参考資料

- [Spring Framework 5.3 ドキュメント](https://docs.spring.io/spring-framework/docs/5.3.x/reference/html/)
- [Spring Boot 2.7 ドキュメント](https://docs.spring.io/spring-boot/docs/2.7.x/reference/html/)
- [Docker ベストプラクティス](https://docs.docker.com/develop/dev-best-practices/)
- [Java コンテナー最適化ガイド](https://developers.redhat.com/articles/2021/04/28/top-10-must-know-kubernetes-design-patterns)

---

## 💬 サポート

質問や問題が発生した場合:
- [Issues](https://github.com/hatasaki/Modernization-Workshop/issues) でフィードバックをお寄せください
- [メインワークショップ](../README.md) で Azure へのデプロイ方法を学べます

お疲れ様でした! 🎉
