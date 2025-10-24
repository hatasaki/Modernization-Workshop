# 7. 複数アプリ連携

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
cd ~
mkdir backend-api
cd backend-api
```

### Spring Initializr で作成

https://start.spring.io/ で以下を設定:

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.3.5
- **Java**: 21
- **Artifact**: `backend-api`
- **Dependencies**: 
  - Spring Web
  - Spring Data JPA
  - H2 Database
  - Lombok

ダウンロードして展開:

```bash
cd ~
unzip ~/Downloads/backend-api.zip
cd backend-api
```

### 商品エンティティを作成

`src/main/java/com/example/backendapi/Product.java`:

```java
package com.example.backendapi;

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

`src/main/java/com/example/backendapi/ProductRepository.java`:

```java
package com.example.backendapi;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

### REST コントローラーを作成

`src/main/java/com/example/backendapi/ProductController.java`:

```java
package com.example.backendapi;

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

`src/main/java/com/example/backendapi/DataLoader.java`:

```java
package com.example.backendapi;

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

```properties
server.port=8081
spring.application.name=backend-api
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
```

### Dockerfile

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

## Backend API をデプロイ

### ビルドしてプッシュ

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
docker build -t $ACR_NAME.azurecr.io/backend-api:v1 .
docker push $ACR_NAME.azurecr.io/backend-api:v1
```

**PowerShell の場合:**
```powershell
docker build -t "$env:ACR_NAME.azurecr.io/backend-api:v1" .
docker push "$env:ACR_NAME.azurecr.io/backend-api:v1"
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal + Docker</b></summary>

```bash
# ローカルでビルド & プッシュ (ACR 情報は Portal からコピー)
docker build -t <your-acr-name>.azurecr.io/backend-api:v1 .
docker push <your-acr-name>.azurecr.io/backend-api:v1
```

**注意:** `<your-acr-name>` を実際の ACR 名に置き換えてください（例: `acrworkshop12345`）。

</details>

### デプロイ (内部アクセスのみ)

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

1. [Azure Portal](https://portal.azure.com/) で「Container Apps」を作成
2. 基本設定:
   - **名前**: `backend-api`
   - **リソース グループ**: セクション 1 で設定した名前
   - **Container Apps Environment**: セクション 4 で作成した環境を選択

3. コンテナー設定:
   - **イメージ**: ACR から `backend-api:v1` を選択

4. **イングレス設定 (重要)**:
   - ✅ 「イングレスを有効にする」
   - **イングレス トラフィック**: `Container Apps Environment 内に限定` ← **内部のみ**
   - **ターゲット ポート**: `8081`

5. 「確認および作成」→「作成」

</details>

---

## Frontend を改良する

セクション 2 で作ったフロントエンドアプリを、Backend API を呼び出すように改良します。

### フロントエンドのディレクトリに移動

```bash
cd ~/frontend
```

### コントローラーを更新

`src/main/java/com/example/frontend/HomeController.java` を以下に変更:

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
            // Backend API を呼び出し
            String apiUrl = "http://backend-api/api/products";
            Object products = restTemplate.getForObject(apiUrl, Object.class);
            model.addAttribute("products", products);
            model.addAttribute("message", "Backend API から商品データを取得しました!");
        } catch (Exception e) {
            model.addAttribute("message", "Backend API に接続できません (まだデプロイされていない可能性があります)");
            model.addAttribute("products", null);
        }
        return "index";
    }
}
```

**ポイント:** `http://backend-api` で内部のバックエンド API を呼び出せます!

### HTML テンプレートを更新

`src/main/resources/templates/index.html` を以下に変更:

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

## Frontend を再デプロイ

### ビルドしてプッシュ

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
# frontend ディレクトリで実行
cd ~/frontend
docker build -t $ACR_NAME.azurecr.io/frontend:v3 .
docker push $ACR_NAME.azurecr.io/frontend:v3
```

**PowerShell の場合:**
```powershell
# frontend ディレクトリで実行
cd ~/frontend
docker build -t "$env:ACR_NAME.azurecr.io/frontend:v3" .
docker push "$env:ACR_NAME.azurecr.io/frontend:v3"
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal + Docker</b></summary>

```bash
cd ~/frontend
# ローカルでビルド & プッシュ
docker build -t <your-acr-name>.azurecr.io/frontend:v3 .
docker push <your-acr-name>.azurecr.io/frontend:v3
```

**注意:** `<your-acr-name>` を実際の ACR 名に置き換えてください（例: `acrworkshop12345`）。

</details>

### 既存のアプリを更新

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

セクション 4 で作成した `frontend` アプリを更新します:

```bash
az containerapp update \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --image $ACR_NAME.azurecr.io/frontend:v3
```

**PowerShell の場合:**
```powershell
az containerapp update `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --image "$env:ACR_NAME.azurecr.io/frontend:v3"
```

> 💡 **新規作成ではなく、既存のアプリを更新 (update) します！**

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で既存の Container App `frontend` を開く
2. 「リビジョン管理」→「新しいリビジョンの作成」
3. 「コンテナー」セクションで既存のコンテナーを選択して編集
4. **イメージ タグ** を `v3` に変更
5. 「作成」をクリック

> 💡 **自動的に新しいバージョンにデプロイされます！ゼロダウンタイム！**

</details>

---

## 動作確認

### Frontend の URL を取得 (既に取得済みの場合はスキップ)

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
Backend API (内部のみ) ← セクション 7 で新規作成
```

> 💡 **Frontend は外部からアクセス可能、Backend API は内部のみ！セキュアな構成です！**

---

## 完了!

✅ 複数のアプリが連携しました!

🎉 **ワークショップ完了です!**

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

さらに学びたい方は:
- [Azure Container Apps ドキュメント](https://learn.microsoft.com/azure/container-apps/)
- [Dapr でマイクロサービス](https://docs.dapr.io/)
- [KEDA でイベント駆動](https://keda.sh/)

お疲れ様でした! 🚀
