# 6. ストレージ追加

ファイルを保存できるようにします。

---

## Azure Storage Account を作成

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
# ストレージアカウント名 (グローバルで一意)
export STORAGE_NAME="storage$(date +%s)"

# 作成
az storage account create \
  --name $STORAGE_NAME \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --sku Standard_LRS
```

**PowerShell の場合:**
```powershell
# ストレージアカウント名 (グローバルで一意)
$env:STORAGE_NAME = "storage$((Get-Date).Ticks)"

# 作成
az storage account create `
  --name $env:STORAGE_NAME `
  --resource-group $env:RESOURCE_GROUP `
  --location $env:LOCATION `
  --sku Standard_LRS
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で「リソースの作成」
2. 「ストレージ アカウント」を検索して選択
3. 基本設定:
   - **リソース グループ**: セクション 1 で設定した名前
   - **ストレージ アカウント名**: 一意の名前 (例: `storage12345`)
   - **リージョン**: `Japan East`
   - **パフォーマンス**: `Standard`
   - **冗長性**: `ローカル冗長ストレージ (LRS)`
4. 「確認および作成」→「作成」

**ポータルで作成した場合の環境変数設定:**

```bash
# ポータルで入力したストレージアカウント名を設定
export STORAGE_NAME="storage12345"  # あなたが入力した名前に置き換え
```

**PowerShell の場合:**
```powershell
$env:STORAGE_NAME = "storage12345"  # あなたが入力した名前に置き換え
```

</details>

---

## ファイル共有を作成

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
# 接続文字列を取得
export STORAGE_KEY=$(az storage account keys list \
  --account-name $STORAGE_NAME \
  --query '[0].value' -o tsv)

# ファイル共有を作成
az storage share create \
  --name appdata \
  --account-name $STORAGE_NAME \
  --account-key $STORAGE_KEY
```

**PowerShell の場合:**
```powershell
# 接続文字列を取得
$env:STORAGE_KEY = az storage account keys list `
  --account-name $env:STORAGE_NAME `
  --query '[0].value' -o tsv

# ファイル共有を作成
az storage share create `
  --name appdata `
  --account-name $env:STORAGE_NAME `
  --account-key $env:STORAGE_KEY
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. 作成したストレージアカウントを開く
2. 左メニュー「ファイル共有」をクリック
3. 「+ ファイル共有」をクリック
4. 以下を入力:
   - **名前**: `appdata`
   - **クォータ**: デフォルトのまま (5120 GiB)
5. 「作成」

**ポータルで作成した場合の環境変数設定 (後で使用):**

アクセスキーを環境変数に設定しておきます:

1. ストレージアカウントを開く
2. 左メニュー「アクセス キー」をクリック
3. 「キーの表示」をクリック
4. **key1** の「キー」をコピー

```bash
# コピーしたキーを設定
export STORAGE_KEY="<コピーしたキー>"
```

**PowerShell の場合:**
```powershell
$env:STORAGE_KEY = "<コピーしたキー>"
```

</details>

---

## Container Apps にマウント

### ストレージを環境に追加

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
az containerapp env storage set \
  --name $ACA_ENV \
  --resource-group $RESOURCE_GROUP \
  --storage-name mystorage \
  --azure-file-account-name $STORAGE_NAME \
  --azure-file-account-key $STORAGE_KEY \
  --azure-file-share-name appdata \
  --access-mode ReadWrite
```

**PowerShell の場合:**
```powershell
az containerapp env storage set `
  --name $env:ACA_ENV `
  --resource-group $env:RESOURCE_GROUP `
  --storage-name mystorage `
  --azure-file-account-name $env:STORAGE_NAME `
  --azure-file-account-key $env:STORAGE_KEY `
  --azure-file-share-name appdata `
  --access-mode ReadWrite
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. セクション 4 で作成した Container Apps Environment を開く
2. 左メニュー「Azure Files」をクリック
3. 「+ 追加」をクリック
4. 以下を入力:
   - **名前**: `mystorage`
   - **ストレージ アカウント名**: 作成したストレージアカウントを選択
   - **ストレージ アカウント キー**: ストレージアカウントの「アクセス キー」からコピー
   - **ファイル共有名**: `appdata`
   - **アクセス モード**: `読み取り/書き込み`
5. 「追加」

</details>

---

## ストレージマウント機能を追加してデプロイ

### ステップ 1: コードにストレージ機能を追加

ストレージが正しくマウントされているか確認するため、ファイルの読み書き機能を追加します。

**編集するファイル:** `src/main/java/com/example/frontend/HomeController.java`

VS Code で `HomeController.java` を開き、既存のコードに以下のメソッドを**追加**します:

```java
package com.example.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class HomeController {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @GetMapping("/")
    public String home(Model model) {
        // ... 既存のコード ...
        return "index";
    }
    
    // ↓↓↓ ここから下を追加 ↓↓↓
    
    @GetMapping("/write")
    @ResponseBody
    public String write() throws IOException {
        Files.write(
            Paths.get("/data/test.txt"), 
            "Hello from Container Apps!".getBytes()
        );
        return "File written to /data/test.txt";
    }

    @GetMapping("/read")
    @ResponseBody
    public String read() throws IOException {
        return Files.readString(Paths.get("/data/test.txt"));
    }
}
```

### ステップ 2: イメージをビルドしてプッシュ

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
cd ~/workshop/frontend

# イメージをビルド (v2 としてタグ付け)
docker build -t $ACR_NAME.azurecr.io/frontend:v2 .

# ACR にプッシュ
docker push $ACR_NAME.azurecr.io/frontend:v2
```

**PowerShell の場合:**
```powershell
cd ~/workshop/frontend

# イメージをビルド (v2 としてタグ付け)
docker build -t "$env:ACR_NAME.azurecr.io/frontend:v2" .

# ACR にプッシュ
docker push "$env:ACR_NAME.azurecr.io/frontend:v2"
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal + Docker (ハイブリッド)</b></summary>

```bash
cd ~/workshop/frontend
# ローカルでビルド & プッシュ
docker build -t <your-acr-name>.azurecr.io/frontend:v2 .
docker push <your-acr-name>.azurecr.io/frontend:v2
```

**注意:** `<your-acr-name>` を実際の ACR 名に置き換えてください（例: `acrworkshop12345`）。

</details>

### ステップ 3: ストレージをマウントして Container App を更新

<details>
<summary>📘 <b>方法 A: Azure CLI (YAML)</b></summary>

VS Code で `frontend-storage.yaml` を開き、以下のコードを作成します:

```yaml
properties:
  template:
    containers:
      - name: frontend
        image: <your-acr-name>.azurecr.io/frontend:v2
        volumeMounts:
          - volumeName: storage
            mountPath: /data
    volumes:
      - name: storage
        storageType: AzureFile
        storageName: mystorage
```

**重要:** `<your-acr-name>` を実際の ACR 名に置き換えてください。

**YAMLを適用してデプロイ:**

```bash
az containerapp update \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --yaml frontend-storage.yaml
```

**PowerShell の場合:**
```powershell
az containerapp update `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --yaml frontend-storage.yaml
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で Container App `frontend` を開く
2. 「リビジョン管理」→「新しいリビジョンの作成」
3. 「コンテナー」セクションで既存のコンテナーを選択して編集
4. **イメージとタグ**:
   - **イメージ タグ** を `v2` に変更
5. 「ボリューム マウント」タブ:
   - 「+ 追加」をクリック
   - **ボリュームの種類**: `Azure Files`
   - **ストレージ名**: `mystorage` (先ほど作成したもの)
   - **マウント パス**: `/data`
6. 「保存」→「作成」

</details>

---

## 動作確認

```bash
# ファイルを書き込み
curl https://$APP_URL/write

# ファイルを読み込み
curl https://$APP_URL/read
```

**PowerShell の場合:**
```powershell
# ファイルを書き込み
curl "https://$APP_URL/write"

# ファイルを読み込み
curl "https://$APP_URL/read"
```

"Hello from Container Apps!" と表示されれば成功!

---

## 完了!

✅ ストレージがマウントできました!

👉 次は [7. 複数アプリ連携](./07-multiapp.md) へ
