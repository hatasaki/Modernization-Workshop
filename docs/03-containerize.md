# 3. コンテナー化

アプリを Docker コンテナーにします。

## 所要時間: 15分

---

## 💡 なぜコンテナー化するのか？

### VM デプロイとの比較

**従来の VM デプロイの課題:**
- 「開発環境では動いたのに本番で動かない...」がよく発生
- Java、Maven、OS のバージョンを環境ごとに手動で合わせる必要がある
- サーバー設定のドキュメント化が必要で、属人化しやすい

**コンテナー化のメリット:**
- ✅ **環境の一貫性**: 開発 PC、テスト、本番で全く同じコンテナーが動く
- ✅ **ポータビリティ**: Azure でも、AWS でも、ローカルでも同じように動く
- ✅ **コード化**: Dockerfile に全設定が記述され、バージョン管理可能
- ✅ **高速起動**: VM は数分、コンテナーは数秒で起動

---

## Dockerfile を作成

フロントエンドアプリのルートディレクトリ (`~/frontend`) に `Dockerfile` を作成:

```dockerfile
# ビルド用のイメージ
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 実行用のイメージ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

> 💡 **このファイルで「どの環境でも同じように動く」コンテナーが作れます！**

---

## .dockerignore を作成

不要なファイルを除外:

```
target/
.mvn/
*.md
```

---

## Docker イメージをビルド

```bash
# frontend ディレクトリで実行
cd ~/frontend

# イメージをビルド
docker build -t frontend:v1 .
```

2〜3分かかります。完了するまで待ちましょう。

---

## ローカルで実行

```bash
# コンテナーを起動
docker run -d -p 8080:8080 --name frontend frontend:v1
```

### 動作確認

ブラウザで http://localhost:8080/ を開きます。

「フロントエンドアプリへようこそ!」と表示されれば成功!

---

## ログを確認

```bash
# ログを表示
docker logs frontend

# リアルタイムで表示
docker logs -f frontend
```

---

## コンテナーを停止

```bash
# 停止
docker stop frontend

# 削除
docker rm frontend
```

---

## 完了!

✅ フロントエンドアプリがコンテナーで動きました!

> 💡 **ローカルでもクラウドでも同じコンテナーが動きます！**

👉 次は [4. Azure へデプロイ](./04-deploy.md) へ
