# 4. Azure へデプロイ

Azure Container Apps にデプロイします。

---

## リソースグループを作成
> 💡 すでに作成済みの場合はスキップします

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
```

**PowerShell の場合:**
```powershell
az group create `
  --name $env:RESOURCE_GROUP `
  --location $env:LOCATION
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) を開く
2. 検索バーで「リソース グループ」を検索
3. 「+ 作成」をクリック
4. 以下を入力:
   - **サブスクリプション**: 使用するサブスクリプション
   - **リソース グループ**: セクション 1 で設定した名前
   - **リージョン**: `Japan East`
5. 「確認および作成」→「作成」

</details>

---

## Azure Container Registry (ACR) を作成

Docker イメージを保存する場所を作ります。

### ACR の作成

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

1. ACR の名前を環境変数に設定
```bash
# ACR 名 (グローバルで一意な名前が必要)
export ACR_NAME="acr$(date +%s)"
```

**PowerShell の場合:**
```powershell
$env:ACR_NAME = "acr$((Get-Date).Ticks)"
```

> 💡 `$(date +%s)` や `$((Get-Date).Ticks)` で現在時刻を使い、世界中で一意な名前を自動生成しています。

2. ACR を作成
```bash
# ACR を作成
az acr create \
  --name $ACR_NAME \
  --resource-group $RESOURCE_GROUP \
  --sku Basic \
  --admin-enabled true
```

**PowerShell の場合:**
```powershell
# ACR を作成
az acr create `
  --name $env:ACR_NAME `
  --resource-group $env:RESOURCE_GROUP `
  --sku Basic `
  --admin-enabled true
```

3. ACR にログイン

```bash
az acr login --name $ACR_NAME
```

**PowerShell の場合:**
```powershell
az acr login --name $env:ACR_NAME
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で「リソースの作成」
2. 「コンテナー レジストリ」を検索して選択
3. 「作成」をクリック
4. 以下を入力:
   - **リソース グループ**: セクション 1 で設定した名前
   - **レジストリ名**: 一意の名前 (例: `acrworkshop12345`)
   - **場所**: `Japan East`
   - **SKU**: `Basic`
   - その他の項目や、その他のタブの項目はすべて既定値のまま利用
5. 「確認および作成」→「作成」
6. 作成後、ACR を開く
7. 左メニュー「設定」内の「アクセス キー」を選択→表示された「管理者ユーザー」にチェックを追加して有効化

**ポータルで作成した場合の環境変数設定:**

```bash
# ポータルで入力したレジストリ名を設定
export ACR_NAME="あなたが入力したACRの名前"
```

**PowerShell の場合:**
```powershell
$env:ACR_NAME = "あなたが入力したACRの名前"
```

</details>

---

## イメージを ACR にプッシュ

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

### イメージにタグ付け

```bash
# タグ付け (frontend アプリ)
docker tag frontend:v1 $ACR_NAME.azurecr.io/frontend:v1
```

**PowerShell の場合:**
```powershell
docker tag frontend:v1 "$env:ACR_NAME.azurecr.io/frontend:v1"
```

### プッシュ

```bash
docker push $ACR_NAME.azurecr.io/frontend:v1
```

**PowerShell の場合:**
```powershell
docker push "$env:ACR_NAME.azurecr.io/frontend:v1"
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal + Docker (ハイブリッド)</b></summary>

1. Azure Portal で ACR を開く
2. 「アクセス キー」から以下をコピー:
   - **ユーザー名**
   - **パスワード**

3. ローカル(VS Codeなどの)ターミナルで:

    - ACR にログイン (パスワード入力を求められます)

        ```bash
        docker login $ACR_NAME.azurecr.io -u <ユーザー名>
        ```

        **PowerShell の場合:**
        ```powershell
        docker login "$env:ACR_NAME.azurecr.io" -u <ユーザー名>
        ```


    - イメージにタグ付け

        ```bash
        # タグ付け (frontend アプリ)
        docker tag frontend:v1 $ACR_NAME.azurecr.io/frontend:v1
        ```

        **PowerShell の場合:**
        ```powershell
        docker tag frontend:v1 "$env:ACR_NAME.azurecr.io/frontend:v1"
        ```

    - プッシュ

        ```bash
        docker push $ACR_NAME.azurecr.io/frontend:v1
        ```

        **PowerShell の場合:**
        ```powershell
        docker push "$env:ACR_NAME.azurecr.io/frontend:v1"
        ```

4. Portal の ACR → 「リポジトリ」で `frontend` が表示されることを確認

</details>

---

## Container App をデプロイ（Environment も同時に作成）

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

### 環境変数の設定

Container Apps Environment の名前を環境変数に設定します。

> 💡 **重要**: 環境名は、同じリージョン内で一意である必要があります。複数人で同時にワークショップを実施する場合は、名前が重複しないように工夫してください。

```bash
# Container Apps 環境名（あなたの名前や番号を含めて一意にする）
export ACA_ENV="managedenv-<yourname>"
```

**PowerShell の場合:**
```powershell
# Container Apps 環境名（あなたの名前や番号を含めて一意にする）
$env:ACA_ENV = "managedenv-<yourname>"
```

> 💡 `<yourname>` を、あなたの名前やニックネーム、番号などに置き換えてください。例: `managedenv-tanaka`

### Container Apps Environment を作成

```bash
az containerapp env create \
  --name $ACA_ENV \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION
```

**PowerShell の場合:**
```powershell
az containerapp env create `
  --name $env:ACA_ENV `
  --resource-group $env:RESOURCE_GROUP `
  --location $env:LOCATION
```

3〜5分かかります。

### ACR の認証情報を取得

```bash
export ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
export ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)
```

**PowerShell の場合:**
```powershell
$env:ACR_USERNAME = (az acr credential show --name $env:ACR_NAME --query username -o tsv)
$env:ACR_PASSWORD = (az acr credential show --name $env:ACR_NAME --query "passwords[0].value" -o tsv)
```

### フロントエンドアプリをデプロイ

```bash
az containerapp create \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --environment $ACA_ENV \
  --image $ACR_NAME.azurecr.io/frontend:v1 \
  --target-port 8080 \
  --ingress external \
  --registry-server $ACR_NAME.azurecr.io \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 3
```

**PowerShell の場合:**
```powershell
az containerapp create `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --environment $env:ACA_ENV `
  --image "$env:ACR_NAME.azurecr.io/frontend:v1" `
  --target-port 8080 `
  --ingress external `
  --registry-server "$env:ACR_NAME.azurecr.io" `
  --registry-username $env:ACR_USERNAME `
  --registry-password $env:ACR_PASSWORD `
  --cpu 0.5 `
  --memory 1.0Gi `
  --min-replicas 1 `
  --max-replicas 3
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

### Container App を作成（Environment も自動作成される）

1. [Azure Portal](https://portal.azure.com/) で「リソースの作成」
2. 「コンテナーアプリ」を検索して選択
3. 「作成」をクリック
4. 基本設定:
   - **リソース グループ**: セクション 1 で設定した名前
   - **コンテナー アプリ名**: `frontend`
   - **リージョン**: `Japan East`
5. **Container Apps 環境**:
   - `(新規)`と表示されていることを確認（自動で新規作成されます）
   - Azure が自動生成する名前をそのまま使用（例: `managedEnvironment-xxxxx`）

6. 画面下の「次へ: コンテナー」をクリックして「コンテナー」タブへ:
   - **イメージのソース**: `Azure Container Registry`
   - **サブスクリプション**: ご利用中のサブスクリプシ名を選択
   - **レジストリ**: 作成した ACR を選択
   - **イメージ**: `frontend`
   - **イメージ タグ**: `v1`
   - **レジストリ認証**: 「シークレット」を選択

7. 「イングレス」タブ:
   - ✅ 「イングレス」のチェックボックスにチェック
   - **イングレス トラフィック**: `どこからでもトラフィックを受け入れます`
   - **ターゲット ポート**: `8080`

8. 画面下部の「確認および作成」をクリック→「作成」

### [option] 作成された Environment 名を環境変数に設定

Container App の作成が完了したら、自動生成された Environment 名を確認して環境変数に設定します。

1. Azure Portal でセクション 1 で作成したリソースグループを開く
2. 種類が「Container Apps Environment」のリソースを探す
3. その名前（例: `managedEnvironment-xxxxx`）をメモ

**環境変数に設定:**

```bash
# 確認した環境名を設定
export ACA_ENV="managedEnvironment-xxxxx"  # あなたの環境名に置き換え
```

**PowerShell の場合:**
```powershell
$env:ACA_ENV = "managedEnvironment-xxxxx"  # あなたの環境名に置き換え
```

</details>

---

## アプリにアクセス
Azure ポータルの Azure Container Apps の概要ページ右にアプリの URL リンクが表示されますので、クリックして接続できます。

### コマンドで URL を取得

```bash
export APP_URL=$(az containerapp show \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "アプリの URL: https://$APP_URL"
```

**PowerShell の場合:**
```powershell
$env:APP_URL = (az containerapp show `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --query properties.configuration.ingress.fqdn -o tsv)

Write-Host "アプリの URL: https://$env:APP_URL"
```

ブラウザで確認

ブラウザで `https://<表示された URL>` を開いてみましょう。

「フロントエンドアプリへようこそ!」と表示されれば成功!

---

## ログを確認
ポータルの左メニュー`ログ ストリーム`からコンテナーのログを確認できます。</BR>
コマンドで確認する方法は下記のとおりです。

```bash
az containerapp logs show \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --follow
```

**PowerShell の場合:**
```powershell
az containerapp logs show `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --follow
```

---

## 完了!

✅ Azure でフロントエンドアプリが動いています!

> 💡 **セクション 7 でバックエンド API を追加して、本格的なマイクロサービスにします！**

👉 次は [5. スケーリング](./05-scaling.md) へ
