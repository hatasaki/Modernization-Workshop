# 4. Azure へデプロイ

Azure Container Apps にデプロイします。

---

## リソースグループを作成

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
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

### 環境変数の設定

まず、ACR の名前を環境変数に設定します:

```bash
# ACR 名 (グローバルで一意な名前が必要)
export ACR_NAME="acr$(date +%s)"
```

**PowerShell の場合:**
```powershell
$env:ACR_NAME = "acr$((Get-Date).Ticks)"
```

> 💡 `$(date +%s)` や `$((Get-Date).Ticks)` で現在時刻を使い、世界中で一意な名前を自動生成しています。

### ACR の作成

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
# ACR を作成
az acr create \
  --name $ACR_NAME \
  --resource-group $RESOURCE_GROUP \
  --sku Basic \
  --admin-enabled true
```

### ACR にログイン

```bash
az acr login --name $ACR_NAME
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
5. 「確認および作成」→「作成」
6. 作成後、ACR を開く
7. 左メニュー「アクセス キー」→「管理者ユーザー」を有効化

**ポータルで作成した場合の環境変数設定:**

```bash
# ポータルで入力したレジストリ名を設定
export ACR_NAME="acrworkshop12345"  # あなたが入力した名前に置き換え
```

**PowerShell の場合:**
```powershell
$env:ACR_NAME = "acrworkshop12345"  # あなたが入力した名前に置き換え
```

</details>

---

## イメージを ACR にプッシュ

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

### イメージにタグ付け

```bash
# ACR のログインサーバー名を取得
ACR_SERVER=$(az acr show --name $ACR_NAME --query loginServer -o tsv)

# タグ付け (frontend アプリ)
docker tag frontend:v1 $ACR_SERVER/frontend:v1
```

### プッシュ

```bash
docker push $ACR_SERVER/frontend:v1
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal + Docker (ハイブリッド)</b></summary>

1. Azure Portal で ACR を開く
2. 「アクセス キー」から以下をコピー:
   - **ログイン サーバー** (例: `acrworkshop12345.azurecr.io`)
   - **ユーザー名**
   - **パスワード**

3. ローカルターミナルで:

```bash
# ACR にログイン (パスワード入力を求められます)
docker login <ログインサーバー> -u <ユーザー名>

# タグ付け (frontend アプリ)
docker tag frontend:v1 <ログインサーバー>/frontend:v1

# プッシュ
docker push <ログインサーバー>/frontend:v1
```

4. Portal の ACR → 「リポジトリ」で `frontend` が表示されることを確認

</details>

---

## Container App をデプロイ（Environment も同時に作成）

> 💡 **Container App を初めて作成する際、Container Apps Environment も自動的に作成されます。**

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

### ACR の認証情報を取得

```bash
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)
```

### Container Apps Environment と Frontend アプリを作成

まず Environment を作成します:

```bash
az containerapp env create \
  --name managedenv-workshop \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION
```

3〜5分かかります。

次にフロントエンドアプリをデプロイします:

```bash
az containerapp create \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --environment managedenv-workshop \
  --image $ACR_SERVER/frontend:v1 \
  --target-port 8080 \
  --ingress external \
  --registry-server $ACR_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 3
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で「リソースの作成」
2. 「Container Apps」を検索して選択
3. 「作成」をクリック
4. 基本設定:
   - **リソース グループ**: セクション 1 で設定した名前
   - **コンテナー アプリ名**: `frontend`
   - **リージョン**: `Japan East`
5. **Container Apps Environment**:
   - 「新規作成」を選択
   - **環境名**: デフォルトの名前をそのまま使用（例: `managedEnvironment-xxxxx`）
   - または任意の名前（例: `managedenv-workshop`）を入力
   - **作成** をクリック

6. 「コンテナー」タブ:
   - **イメージのソース**: `Azure Container Registry`
   - **レジストリ**: 作成した ACR を選択
   - **イメージ**: `frontend`
   - **イメージ タグ**: `v1`
   - ✅ 「管理者の資格情報を使用する」にチェック

5. 「イングレス」タブ:
   - ✅ 「イングレスを有効にする」にチェック
   - **イングレス トラフィック**: `任意の場所からのトラフィックを受け入れる`
   - **ターゲット ポート**: `8080`

6. 「スケール」タブ:
   - **最小レプリカ数**: `1`
   - **最大レプリカ数**: `3`

7. 「確認および作成」→「作成」

</details>

---

## 作成された Environment 名を確認

後のセクションで使用するため、Environment 名を確認して環境変数に設定します。

### 方法 A: Azure CLI で確認

```bash
# Environment 名を取得
ACA_ENV=$(az containerapp env list \
  --resource-group $RESOURCE_GROUP \
  --query "[0].name" -o tsv)

echo "Container Apps Environment: $ACA_ENV"

# 環境変数に設定（後のセクションで使用）
export ACA_ENV
```

**PowerShell の場合:**
```powershell
# Environment 名を取得
$env:ACA_ENV = (az containerapp env list `
  --resource-group $env:RESOURCE_GROUP `
  --query "[0].name" -o tsv)

Write-Host "Container Apps Environment: $env:ACA_ENV"
```

### 方法 B: Azure Portal で確認

1. [Azure Portal](https://portal.azure.com/) を開く
2. セクション 1 で作成したリソースグループを開く
3. 種類が「Container Apps Environment」のリソースを探す
4. その名前（例: `managedEnvironment-xxxxx` または `managedenv-workshop`）をメモ

**確認した名前を環境変数に設定:**

```bash
# 確認した環境名を設定
export ACA_ENV="managedEnvironment-xxxxx"  # あなたの環境名に置き換え
```

**PowerShell の場合:**
```powershell
$env:ACA_ENV = "managedEnvironment-xxxxx"  # あなたの環境名に置き換え
```

---

## アプリにアクセス

### URL を取得

```bash
APP_URL=$(az containerapp show \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "アプリの URL: https://$APP_URL"
```

### ブラウザで確認

ブラウザで `https://<表示された URL>` を開いてみましょう。

「フロントエンドアプリへようこそ!」と表示されれば成功!

---

## ログを確認

```bash
az containerapp logs show \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --follow
```

---

## 完了!

✅ Azure でフロントエンドアプリが動いています!

> 💡 **セクション 7 でバックエンド API を追加して、本格的なマイクロサービスにします！**

👉 次は [5. スケーリング](./05-scaling.md) へ
