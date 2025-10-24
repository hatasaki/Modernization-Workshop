# 2. Spring Boot アプリの作成

シンプルなフロントエンドアプリを作ります。

> 💡 **このアプリは後でバックエンド API と連携させます！**

## 所要時間: 15分

---

## Spring Initializr でプロジェクト作成

ブラウザで https://start.spring.io/ を開きます。

### 設定

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 2.7.18
- **Java**: 11
- **Artifact**: `frontend`
- **Dependencies**: 以下を追加
  - Spring Web
  - Thymeleaf

「GENERATE」をクリックして、`frontend.zip` をダウンロード。

### 展開

```bash
cd ~
unzip ~/Downloads/frontend.zip
cd frontend
```

---

## フロントエンドページを作成

### コントローラーを作成

`src/main/java/com/example/frontend/HomeController.java` を作成:

```java
package com.example.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "フロントエンドアプリへようこそ!");
        return "index";
    }
}
```

---

## HTML テンプレートを作成

`src/main/resources/templates/index.html` を作成:

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
    </style>
</head>
<body>
    <h1>商品管理アプリ</h1>
    <div class="message">
        <p th:text="${message}">メッセージ</p>
    </div>
    <p>※ 後でバックエンド API と連携させます！</p>
</body>
</html>
```

---

## 設定ファイル

`src/main/resources/application.properties`:

```properties
server.port=8080
```

---

## アプリを起動

```bash
# ビルドして起動
./mvnw spring-boot:run

# Windows の場合
mvnw.cmd spring-boot:run
```

---

## 動作確認

ブラウザで http://localhost:8080/ を開きます。

「フロントエンドアプリへようこそ!」と表示されれば成功!

---

## 完了!

✅ フロントエンドアプリが動きました!

> 💡 **このアプリは後で (セクション 7 で) バックエンド API を呼び出すように改良します！**

👉 次は [3. コンテナー化](./03-containerize.md) へ
