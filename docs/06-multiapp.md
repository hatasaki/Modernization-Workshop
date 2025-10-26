# 6. 複数アプリ連携

複数のアプリを連携させます。

---

## 💡 VM との比較: マイクロサービス連携の容易さ

### VM での従来の運用
- 各サービスの IP アドレスを手動管理
- ロードバランサーの設定が必要
- サービス追加時にネットワーク設定を変更
- 内部通信も外部ネットワークを経由する場合がある

### Container Apps の PaaS メリット
- ✅ **サービス名で通信**: `http://product-service` だけで接続可能
- ✅ **自動サービスディスカバリ**: IP アドレス管理不要
- ✅ **内部/外部の明確な分離**: セキュリティ設定が簡単
- ✅ **ゼロコンフィグ**: ロードバランサーやDNS設定が自動

---

## シナリオ

セクション 2 で作ったフロントエンドアプリに、バックエンド API を追加します:

- **Backend API**: 商品情報を返す API (内部のみアクセス可能)
- **Frontend**: セクション 2 で作成したアプリを改良して、Backend API を呼び出す

> 💡 **最初に作ったフロントエンドアプリを、バックエンド連携型に進化させます！**

---

## Backend API を作成

### 新しいプロジェクトを作成

```bash
cd ~/workshop
mkdir backend-api
cd backend-api
```

**PowerShell の場合:**
```powershell
cd ~/workshop
New-Item -Path backend-api -ItemType Directory
cd backend-api
```

### Spring Initializr で作成

https://start.spring.io/ で以下を設定:

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.5.7
- **Java**: 21
- **Artifact**: `backend-api`
- **Dependencies**: 
  - Spring Web
  - Spring Data JPA
  - H2 Database
  - Lombok

![Spring Initializr](../images/Spring-backendapi.png)

ダウンロードした`backend-api.zip`ファイルを`~/workshop`にコピーして展開:

```bash
unzip backend-api.zip
cd backend-api
```

**PowerShell の場合:**
```powershell
Expand-Archive -Path backend-api.zip -DestinationPath .
cd backend-api
```

> 💡 プロジェクト構造: `~/workshop/backend-api/`

### 商品エンティティを作成

VS Code で `src/main/java/com/example/backend_api/Product.java` のファイルを新規作成します（VS Code のターミナルで `code src/main/java/com/example/backend_api/Product.java`を実行）</BR>コードは下記です:

```java
package com.example.backend_api;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
}
```

### リポジトリを作成

VS Code で `src/main/java/com/example/backend_api/ProductRepository.java` のファイルを新規作成します（VS Code のターミナルで `code src/main/java/com/example/backend_api/ProductRepository.java`を実行）</BR>コードは下記です:


```java
package com.example.backend_api;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

### REST コントローラーを作成

VS Code で `src/main/java/com/example/backend_api/ProductController.java` のファイルを新規作成します（VS Code のターミナルで `code src/main/java/com/example/backend_api/ProductController.java`を実行）</BR>コードは下記です:

```java
package com.example.backend_api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductRepository repository;
    
    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }
}
```

### サンプルデータを追加

VS Code で `src/main/java/com/example/backend_api/DataLoader.java` のファイルを新規作成します（VS Code のターミナルで `code src/main/java/com/example/backend_api/DataLoader.java`を実行）</BR>コードは下記です:

```java
package com.example.backend_api;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    
    private final ProductRepository repository;
    
    @Override
    public void run(String... args) {
        Product p1 = new Product();
        p1.setName("ノートPC");
        p1.setPrice(120000);
        p1.setStock(10);
        repository.save(p1);
        
        Product p2 = new Product();
        p2.setName("マウス");
        p2.setPrice(3000);
        p2.setStock(50);
        repository.save(p2);
    }
}
```

### `application.properties`

VS Code で `src/main/resources/application.properties` を開き、以下の設定を追加します:

```properties
server.port=8081
spring.application.name=backend-api
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
```

### Dockerfile

VS Code で `Dockerfile` を開き、バックエンド API のルートディレクトリ (`~/workshop/backend-api`) に以下のコードを作成します:

```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
```

---

## Backend API をローカルでテスト

### コンテナーイメージをビルド

```bash
docker build -t backend-api:v1 .
```

**PowerShell の場合:**
```powershell
docker build -t backend-api:v1 .
```

### ローカルで実行

```bash
docker run -d -p 8081:8081 --name backend-api backend-api:v1
```

### 動作確認

ブラウザまたは curl で以下にアクセス:

```bash
curl http://localhost:8081/api/products
```

**PowerShell の場合:**
```powershell
Invoke-WebRequest http://localhost:8081/api/products
```

商品データ（ノートPCとマウス）が JSON 形式で返ってくれば成功！

```json
[
  {"id":1,"name":"ノートPC","price":120000,"stock":10},
  {"id":2,"name":"マウス","price":3000,"stock":50}
]
```

> 💡 **まずはローカルでバックエンドAPIが正しく動作することを確認しました！**

---

## ACR にプッシュ

### イメージにタグ付け

```bash
docker tag backend-api:v1 $ACR_NAME.azurecr.io/backend-api:v1
```

**PowerShell の場合:**
```powershell
docker tag backend-api:v1 "$env:ACR_NAME.azurecr.io/backend-api:v1"
```

### プッシュ

```bash
docker push $ACR_NAME.azurecr.io/backend-api:v1
```

**PowerShell の場合:**
```powershell
docker push "$env:ACR_NAME.azurecr.io/backend-api:v1"
```

> 💡 認証エラーが出る場合はもう一度 docker login を実施してください（セクション４参照）

---

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

> 💡 **重要**: セクション 4 で設定した `ACR_USERNAME` と `ACR_PASSWORD` 環境変数が必要です。設定していない場合は以下を実行してください:

```bash
export ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
export ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)
```

**PowerShell の場合:**
```powershell
$env:ACR_USERNAME = (az acr credential show --name $env:ACR_NAME --query username -o tsv)
$env:ACR_PASSWORD = (az acr credential show --name $env:ACR_NAME --query "passwords[0].value" -o tsv)
```

**Backend API をデプロイ:**

```bash
az containerapp create \
  --name backend-api \
  --resource-group $RESOURCE_GROUP \
  --environment $ACA_ENV \
  --image $ACR_NAME.azurecr.io/backend-api:v1 \
  --target-port 8081 \
  --ingress internal \
  --registry-server $ACR_NAME.azurecr.io \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.25 \
  --memory 0.5Gi
```

**PowerShell の場合:**
```powershell
az containerapp create `
  --name backend-api `
  --resource-group $env:RESOURCE_GROUP `
  --environment $env:ACA_ENV `
  --image "$env:ACR_NAME.azurecr.io/backend-api:v1" `
  --target-port 8081 `
  --ingress internal `
  --registry-server "$env:ACR_NAME.azurecr.io" `
  --registry-username $env:ACR_USERNAME `
  --registry-password $env:ACR_PASSWORD `
  --cpu 0.25 `
  --memory 0.5Gi
```

**重要:** `--ingress internal` で内部のみアクセス可能にしています。

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で「コンテナーアプリ」を作成
2. 基本設定:
   - **リソース グループ**: セクション 1 で設定した名前
   - **コンテナーアプリ名前**: `backend-api`
   - **リージョン**: `Japan East`
   - **Container Apps 環境**: セクション 4 で作成された既存の環境を選択

3. 画面下の「次へ: コンテナー」をクリックして「コンテナー」タブへ:
   - **イメージのソース**: `Azure Container Registry`
   - **サブスクリプション**: ご利用中のサブスクリプシ名を選択
   - **レジストリ**: セクション４で作成した ACR を選択
   - **イメージ**: `backend-api`
   - **イメージ タグ**: `v1`
   - **レジストリ認証**: 「シークレット」を選択

4. **イングレス設定**:
   - ✅ 「イングレスを有効にする」
   - **イングレス トラフィック**: `Container Apps 環境に限定` ← **内部のみ**
   - **ターゲット ポート**: `8081`

5. 「確認および作成」→「作成」

</details>

---

## Frontend を改良する

セクション 2 で作ったフロントエンドアプリを、Backend API を呼び出すように改良します。

### フロントエンドのディレクトリに移動

```bash
cd ~/workshop/frontend
```

**PowerShell の場合:**
```powershell
cd ~/workshop/frontend
```

### コントローラーを更新

VS Code で `src/main/java/com/example/frontend/HomeController.java` を開き (`code src/main/java/com/example/frontend/HomeController.java`)、以下のコードに上書き（リプレース - 既存のコードはすべて削除して入れ替え）します:

```java
package com.example.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class HomeController {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @GetMapping("/")
    public String home(Model model) {
        try {
            // Backend API を呼び出し (環境変数で切り替え可能)
            String backendHost = System.getenv().getOrDefault("BACKEND_HOST", "backend-api");
            String apiUrl = "http://" + backendHost + "/api/products";
            Object products = restTemplate.getForObject(apiUrl, Object.class);
            model.addAttribute("products", products);
            model.addAttribute("message", "Backend API から商品データを取得しました!");
        } catch (Exception e) {
            model.addAttribute("message", "Backend API に接続できません: " + e.getMessage());
            model.addAttribute("products", null);
        }
        return "index";
    }
}
```

**ポイント:** 
- 環境変数 `BACKEND_HOST` でバックエンドのホスト名を指定できます
- ローカル環境では `localhost` を、ACA環境では `backend-api` を使用します

### HTML テンプレートを更新

VS Code で `src/main/resources/templates/index.html` を開き (`code src/main/resources/templates/index.html`)、以下のコードに上書き（リプレース - 既存のコードはすべて削除して入れ替え）します:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>商品管理アプリ</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        h1 {
            color: #0078d4;
        }
        .message {
            padding: 20px;
            background-color: #f0f0f0;
            border-radius: 5px;
            margin: 20px 0;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #0078d4;
            color: white;
        }
    </style>
</head>
<body>
    <h1>商品管理アプリ</h1>
    <div class="message">
        <p th:text="${message}">メッセージ</p>
    </div>
    
    <div th:if="${products != null}">
        <h2>商品一覧</h2>
        <p>※ Backend API (内部サービス) から取得</p>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>商品名</th>
                    <th>価格</th>
                    <th>在庫</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="product : ${products}">
                    <td th:text="${product.id}">1</td>
                    <td th:text="${product.name}">商品名</td>
                    <td th:text="${product.price} + '円'">価格</td>
                    <td th:text="${product.stock}">在庫</td>
                </tr>
            </tbody>
        </table>
    </div>
</body>
</html>
```

---

## ローカル環境で連携テスト

ローカルで両方のコンテナーを実行して、システム連携を確認します。

### Frontend のコンテナーイメージをビルド

```bash
docker build -t frontend:v2 .
```

### Dockerネットワークを作成

両方のコンテナーが通信できるように、専用のネットワークを作成します:

```bash
docker network create workshop-network
```

### Backend API コンテナーをネットワークに再接続

既存の Backend API コンテナーを停止・削除して、ネットワークに接続した状態で再起動します:

```bash
docker stop backend-api
docker rm backend-api
docker run -d --name backend-api --network workshop-network -p 8081:8081 backend-api:v1
```

### Frontend コンテナーを起動

環境変数 `BACKEND_HOST` を設定して、Backend API のコンテナー名を指定します:

```bash
docker run -d --name frontend --network workshop-network -p 8080:8080 -e BACKEND_HOST=backend-api frontend:v2
```

**ポイント:**
- `--network workshop-network`: 両方のコンテナーが同じネットワークに接続
- `-e BACKEND_HOST=backend-api`: 環境変数でバックエンドのホスト名を指定
- Frontend から `http://backend-api:8081/api/products` でアクセス可能

### 動作確認

ブラウザで `http://localhost:8080` を開きます。

**確認ポイント:**
- ✅ 「Backend API から商品データを取得しました!」のメッセージ
- ✅ ノートPC (120,000円) とマウス (3,000円) が表示される
- ✅ Frontend が Backend API をDockerネットワーク経由で呼び出している

> 💡 **ローカル環境でマイクロサービス連携が動作しました！これと同じ構成をACAにデプロイします！**

### コンテナーの停止とクリーンアップ

テストが完了したら、コンテナーを停止して削除します:

```bash
docker stop frontend backend-api
docker rm frontend backend-api
docker network rm workshop-network
```

---

## 更新したフロントエンドを Azure Container Apps にデプロイ

ローカルで動作確認ができたので、Azure Container Apps でマルチコンテナー通信を実施します。

---

### Frontend をデプロイ

#### 更新したコンテナーをACRにプッシュ

```bash
docker push $ACR_NAME.azurecr.io/frontend:v2
```

**PowerShell の場合:**
```powershell
docker push "$env:ACR_NAME.azurecr.io/frontend:v2"
```

#### ACAにデプロイ

セクション 4 で作成した `frontend` アプリを更新します。

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
az containerapp update \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --image $ACR_NAME.azurecr.io/frontend:v2
```

**PowerShell の場合:**
```powershell
az containerapp update `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --image "$env:ACR_NAME.azurecr.io/frontend:v2"
```

> 💡 **新規作成ではなく、既存のアプリを更新 (update) します！**

> 💡 **ACA環境では、`BACKEND_HOST` 環境変数を設定しなくても、デフォルト値の `backend-api` でサービス名解決されます！**

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で既存の Container App `frontend` を開く
2. 「リビジョン管理」→「新しいリビジョンの作成」
3. 「コンテナー」セクションで既存のコンテナーを選択して編集
4. **イメージ タグ** を `v2` に変更
5. 「作成」をクリック

> 💡 **自動的に新しいバージョンにデプロイされます！ゼロダウンタイム！**

</details>

---

## 動作確認

### Frontend の URL を取得 (既に取得済みの場合はスキップ)
Azure ポータルの Azure Container Apps の概要ページ右にアプリの URL リンクが表示されますので、クリックして接続できます。

```bash
export FRONTEND_URL=$(az containerapp show \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "Frontend URL: https://$FRONTEND_URL"
```

**PowerShell の場合:**
```powershell
$env:FRONTEND_URL = az containerapp show `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --query properties.configuration.ingress.fqdn -o tsv

Write-Host "Frontend URL: https://$env:FRONTEND_URL"
```

### ブラウザでアクセス

ブラウザで `https://<Frontend URL>` を開きます。

商品一覧の表が表示されれば成功！

**確認ポイント:**
- ✅ 「Backend API から商品データを取得しました!」のメッセージ
- ✅ ノートPC (120,000円) とマウス (3,000円) が表示される
- ✅ Frontend が Backend API (内部サービス) を呼び出している

---

## アーキテクチャ

```
インターネット
    ↓
Frontend (外部公開) ← セクション 2 で作成したアプリを改良
    ↓
Backend API (内部のみ) ← セクション 6 で新規作成
```

> 💡 **Frontend は外部からアクセス可能、Backend API は内部のみ！セキュアな構成です！**

---

## 完了!

✅ 複数のアプリが連携しました!

🎉 **基本的なワークショップが完了です!**

> 💡 **さらに学びたい方へ:** [セクション 7 (ストレージ連携)](./07-storage.md) でデータ永続化について学べます（オプション）。

---

## 後片付け

```bash
# リソースグループごと削除
az group delete --name $RESOURCE_GROUP --yes
```

**PowerShell の場合:**
```powershell
# リソースグループごと削除
az group delete --name $env:RESOURCE_GROUP --yes
```

---

## 次のステップ

### オプション: さらに学びたい方へ

👉 [セクション 7: ストレージ連携（オプション）](./07-storage.md) でデータ永続化について学べます

### Azure のさらなる学習

さらに学びたい方は:
- [Azure Container Apps ドキュメント](https://learn.microsoft.com/azure/container-apps/)
- [Dapr でマイクロサービス](https://docs.dapr.io/)
- [KEDA でイベント駆動](https://keda.sh/)

お疲れ様でした! 🚀
